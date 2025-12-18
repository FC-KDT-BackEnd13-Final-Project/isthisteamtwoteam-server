package org.etmetmy.bn_server.domain.file.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.comment.entity.Comment;
import org.etmetmy.bn_server.domain.file.dto.request.FileCreateRequest;
import org.etmetmy.bn_server.domain.file.dto.request.FilePermanentDeleteRequest;
import org.etmetmy.bn_server.domain.file.dto.request.FileRestoreRequest;
import org.etmetmy.bn_server.domain.file.dto.response.FilePermanentDeleteResponse;
import org.etmetmy.bn_server.domain.file.dto.response.FileRestoreResponse;
import org.etmetmy.bn_server.domain.file.dto.response.S3UploadResult;
import org.etmetmy.bn_server.domain.file.dto.response.TempFileListDTO;
import org.etmetmy.bn_server.domain.file.entity.File;
import org.etmetmy.bn_server.domain.file.repository.FileRepository;
import org.etmetmy.bn_server.domain.post.dto.response.PostPermanentDeleteResponse;
import org.etmetmy.bn_server.domain.post.entity.Post;
import org.etmetmy.bn_server.domain.post.repository.PostRepository;
import org.etmetmy.bn_server.domain.post.service.PostServiceImpl;
import org.etmetmy.bn_server.domain.project.entity.Project;
import org.etmetmy.bn_server.domain.project.entity.ProjectCheckList;
import org.etmetmy.bn_server.domain.project.repository.ProjectMemberRepository;
import org.etmetmy.bn_server.domain.project.repository.ProjectRepository;
import org.etmetmy.bn_server.domain.user.entity.Role;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.etmetmy.bn_server.domain.user.repository.UserRepository;
import org.etmetmy.bn_server.domain.history.entity.ChangeType;
import org.etmetmy.bn_server.domain.history.event.HistoryFileEvent;
import org.etmetmy.bn_server.exception.code.ErrorCode;
import org.etmetmy.bn_server.exception.custom.*;
import org.etmetmy.bn_server.global.util.IpAddressUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final ProjectRepository projectRepository;
    private final PostRepository postRepository;
    private final S3Client s3Client;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    // 2. 임시 파일 업로드
    @Override
    @Transactional
    public List<TempFileListDTO> postFiles(Long projectId, List<MultipartFile> files, Long uploadedBy) {

        Project project = projectRepository.findById(projectId).orElseThrow(ProjectNotFoundException::new);

        List<File> savedFiles = new ArrayList<>();

        for (MultipartFile file : files) {

            // 1. S3 업로드
            S3UploadResult uploadResult = uploadToS3(file);

            // 2. 임시 파일 엔티티 생성 (post = null, isTemp = true)
            File fileEntity = FileCreateRequest.Converter.toEntity(project, uploadResult, uploadedBy);

            // 3. DB에 저장
            File saved = fileRepository.save(fileEntity);
            savedFiles.add(saved);
        }
        return TempFileListDTO.Converter.from(savedFiles);
    }

    // 3. 임시 파일 삭제 (hard delete)
    @Override
    @Transactional
    public void deleteTempFile(Long projectId, List<Long> fileIds) {

        List<File> files = fileRepository.findAllById(fileIds);
        for (File file : files) {
            if (file.getIsTemp() == false)
                throw new BusinessException(ErrorCode.FILE_NOT_TEMP);
        }
        deleteFilesFromS3AndDb(files);
    }

    // 4. 업로드 된 파일 삭제 (soft delete)
    @Override
    @Transactional
    public void deletePostFiles(Long projectId, Long postId, Long fileId, Long loginUserId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(ProjectNotFoundException::new);

        Post post = postRepository.findById(postId)
                .orElseThrow(BoardNotFoundException::new);

        // 게시글이 해당 프로젝트 소속인지 체크
        PostServiceImpl.validatePostBelongsToProject(post, project);

        // 권한 체크 (작성자만 가능)
        PostServiceImpl.validateWriter(post, loginUserId);

        // 파일 검증
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FILE_NOT_FOUND));

        // 파일이 해당 게시글에 속하는지 확인
        if (file.getPost() == null || !file.getPost().getPostId().equals(postId)) {
            throw new BusinessException(ErrorCode.FILE_NOT_IN_POST);
        }

        // Soft Delete 적용
        file.softDelete(loginUserId);

        fileRepository.save(file);

        // 파일 히스토리 이벤트 발행 (DELETE)
        String clientIp = null;
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                clientIp = IpAddressUtil.getClientIp(request);
            }
        } catch (Exception e) {
            // RequestContext가 없는 경우 null로 저장
        }

        eventPublisher.publishEvent(
                new HistoryFileEvent(file, ChangeType.DELETE, loginUserId, clientIp)
        );
    }

    // 5. 삭제된 파일 복원
    @Override
    @Transactional
    public FileRestoreResponse restoreDeletedFiles(Long loginUserId, @Valid FileRestoreRequest request){

        // 1. 권한 검증 (관리자만)
        User user = userRepository.findById(loginUserId).orElseThrow(UserNotFoundException::new);
        if (user.getRole() != Role.ADMIN) {
            throw new BusinessException(ErrorCode.DELETED_FILE_ACCESS_DENIED);
        }

        // 2. 요청한 파일 ID 조회
        List<Long> fileIds = request.getFileIds();
        if (fileIds == null || fileIds.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        List<File> files = fileRepository.findAllById(fileIds);

        // 3. 존재 개수 비교
        if (files.size() != fileIds.size()) {
            throw new BusinessException(ErrorCode.FILE_NOT_IN_POST);
        }

        // 4. 삭제 여부 체크 후 restore
        files.forEach(file -> {
            if (!file.getIsDeleted()) {
                throw new BusinessException(ErrorCode.FILE_NOT_DELETED);
            }
            file.restore();
        });

        fileRepository.saveAll(files);
        return FileRestoreResponse.Converter.from(files, loginUserId);
    }

    // 6. 삭제된 파일 영구 삭제 (hard delete)
    @Override
    @Transactional
    public FilePermanentDeleteResponse hardDeleteFiles(Long loginUserId, @Valid FilePermanentDeleteRequest request) {

        // 1. 권한 검증 (관리자만)
        User user = userRepository.findById(loginUserId).orElseThrow(UserNotFoundException::new);
        if (user.getRole() != Role.ADMIN) {
            throw new BusinessException(ErrorCode.DELETED_FILE_ACCESS_DENIED);
        }

        // 2. 요청한 파일 ID 조회
        List<Long> fileIds = request.getFileIds();
        if (fileIds == null || fileIds.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        List<File> files = fileRepository.findAllById(fileIds);

        // 3. 존재 개수 비교
        if (files.size() != fileIds.size()) {
            throw new BusinessException(ErrorCode.FILE_NOT_IN_POST);
        }

        for (File file : files) {
            // 삭제된 파일인지 검증 (영구 삭제는 isDeleted == true인 파일만 허용)
            if (!file.getIsDeleted()) {
                throw new BusinessException(ErrorCode.FILE_NOT_DELETED);
            }
        }
        deleteFilesFromS3AndDb(files);
        return FilePermanentDeleteResponse.Converter.from(files);
    }

    // 7. S3 업로드 메서드
    @Override
    public S3UploadResult uploadToS3(MultipartFile file) {

        String originalFilename = file.getOriginalFilename();
        String storedFileName = UUID.randomUUID() + "_" + originalFilename;

        try (InputStream is = file.getInputStream()) {

            PutObjectRequest req = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(storedFileName)
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(req, RequestBody.fromInputStream(is, file.getSize()));

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }

        String fileUrl = s3Client.utilities()
                .getUrl(url -> url.bucket(bucketName).key(storedFileName))
                .toString();

        String fileType = extractFileType(originalFilename);
        String fileSize = formatSize(file.getSize());

        return new S3UploadResult(originalFilename, storedFileName, fileUrl, fileSize, fileType);
    }

    // 파일 확장자 추출 헬퍼 메서드
    private String extractFileType(String filename) {
        if (filename == null || filename.isEmpty()) return "unknown";
        int lastDot = filename.lastIndexOf('.');
        return lastDot >= 0 ? filename.substring(lastDot + 1).toLowerCase() : "unknown";
    }

    // 파일 사이즈를 포맷팅하는 헬퍼 메서드
    private String formatSize(long bytes) {
        double mb = bytes / (1024.0 * 1024.0);
        return String.format("%.1fMB", mb);
    }

    // 8. 삭제에 필요한 key 반환
    @Override
    public String getKeyFromFileUrls(String fileUrl) {
        try {
            URL url = new URI(fileUrl).toURL();
            String decodedKey = URLDecoder.decode(url.getPath(), StandardCharsets.UTF_8);

            //경로 앞에 '/' 제거 후 반환
            return decodedKey.substring(1);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_URL_FORMAT);
        }
    }

    // 9. S3에서 파일 삭제 후 DB 레코드도 삭제
    @Override
    public void deleteFilesFromS3AndDb(List<File> files) {
        for (File file : files) {
            try {
                String fileUrl = file.getFilePath();
                Long fileId = file.getFileId();

                // S3 key 추출
                String key = getKeyFromFileUrls(fileUrl);

                // S3 삭제 요청
                DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build();

                s3Client.deleteObject(deleteRequest);

                // DB 레코드 삭제
                fileRepository.deleteById(fileId);

            } catch (Exception e) {
                throw new BusinessException(ErrorCode.FILE_DELETE_FAILED);
            }
        }
    }

    // 10. S3에서 파일 삭제
    @Override
    public void deleteFilesFromS3(List<File> files) {
        for (File file : files) {
            try {
                String fileUrl = file.getFilePath();

                // S3 key 추출
                String key = getKeyFromFileUrls(fileUrl);

                // S3 삭제 요청
                DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build();

                s3Client.deleteObject(deleteRequest);

            } catch (Exception e) {
                throw new BusinessException(ErrorCode.FILE_DELETE_FAILED);
            }
        }
    }

    // 11. S3 업로드 결과로 받은 파일 정보를 기반으로 File 엔티티를 생성하여 Post에 저장
    @Override
    @Transactional
    public void saveFiles(Post post, List<Long> fileIds, Long loginUserId) {
        saveFilesInternal(post, null, null, fileIds, loginUserId);
    }

    // 12. S3 업로드 결과로 받은 파일 정보를 기반으로 File 엔티티를 생성하여 Post에 저장
    @Override
    @Transactional
    public void saveFiles(Comment comment, List<Long> fileIds, Long loginUserId) {
        saveFilesInternal(null, comment, null, fileIds, loginUserId);
    }

    // 13. S3 업로드 결과로 받은 파일 정보를 기반으로 File 엔티티를 생성하여 Post에 저장
    @Override
    @Transactional
    public void saveFiles(ProjectCheckList projectCheckList, List<Long> fileIds, Long loginUserId) {
        saveFilesInternal(null, null, projectCheckList, fileIds, loginUserId);
    }

    // 14. S3 업로드 결과로 받은 파일 정보를 기반으로 File 엔티티를 생성하여 Post/Comment에 저장
    private void saveFilesInternal(Post post, Comment comment, ProjectCheckList projectCheckList, List<Long> fileIds, Long loginUserId) {
        if (fileIds == null || fileIds.isEmpty()) {
            return;
        }

        List<File> tempFiles = fileRepository.findAllById(fileIds)
                .stream()
                .filter(File::getIsTemp)
                .toList();

        if (tempFiles.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }

        // IP 주소 가져오기
        String clientIp = null;
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                clientIp = IpAddressUtil.getClientIp(request);
            }
        } catch (Exception e) {
            // RequestContext가 없는 경우 (비동기 등) null로 저장
        }

        for (File file : tempFiles) {
            if (comment == null && projectCheckList == null) {
                file.attachToPost(post, loginUserId);
            } else if (post == null && projectCheckList == null) {
                file.attachToComment(comment, loginUserId);
            } else {
                file.attachToProjectCheckList(projectCheckList, loginUserId);
            }

            // 파일 히스토리 이벤트 발행 (CREATE)
            eventPublisher.publishEvent(
                    new HistoryFileEvent(file, ChangeType.CREATE, loginUserId, clientIp)
            );
        }

        fileRepository.saveAll(tempFiles);
    }
}
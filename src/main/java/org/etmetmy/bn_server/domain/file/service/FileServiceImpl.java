package org.etmetmy.bn_server.domain.file.service;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.file.dto.request.FileDeleteRequest;
import org.etmetmy.bn_server.domain.file.dto.response.ActiveFileListDTO;
import org.etmetmy.bn_server.domain.file.entity.File;
import org.etmetmy.bn_server.domain.file.repository.FileRepository;
import org.etmetmy.bn_server.domain.post.entity.Post;
import org.etmetmy.bn_server.domain.post.repository.PostRepository;
import org.etmetmy.bn_server.domain.post.service.PostServiceImpl;
import org.etmetmy.bn_server.domain.project.entity.Project;
import org.etmetmy.bn_server.domain.project.repository.ProjectMemberRepository;
import org.etmetmy.bn_server.domain.project.repository.ProjectRepository;
import org.etmetmy.bn_server.exception.code.ErrorCode;
import org.etmetmy.bn_server.exception.custom.BoardNotFoundException;
import org.etmetmy.bn_server.exception.custom.BusinessException;
import org.etmetmy.bn_server.exception.custom.ProjectNotFoundException;
import org.etmetmy.bn_server.exception.custom.ProjectPermissionDeniedException;
import org.etmetmy.bn_server.global.util.SessionUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
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

    private final FileRepository fileRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final PostRepository postRepository;
    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    // 1. 프로젝트별 파일 목록 조회
    @Override
    public List<ActiveFileListDTO> findAllByProjectId(Long projectId, HttpSession session){

        // 1. 로그인 사용자 확인
        Long loginUserId = SessionUtil.getLoginUserId(session);

        // 2. 프로젝트 존재 여부 검증
        projectRepository.findById(projectId)
                .orElseThrow(ProjectNotFoundException::new);

        // 3. 사용자 권한 검증
        boolean isMember = projectMemberRepository.existsByProjectIdAndUserId(projectId, loginUserId);
        if (!isMember) {
            throw new ProjectPermissionDeniedException();
        }

        // 4. 파일 목록 조회
        List<File> files = fileRepository.findByProjectId(projectId);

        return ActiveFileListDTO.Converter.from(files);

    }

    // 2. 임시 파일 업로드
    @Override
    @Transactional
    public List<ActiveFileListDTO> postFiles(Long projectId, Long postId, List<MultipartFile> files){

        Post post = postRepository.findById(postId)
                .orElseThrow(BoardNotFoundException::new);

        List<File> savedFiles = new ArrayList<>();

        for (MultipartFile file : files) {

            // S3 업로드
            String fileUrl = uploadToS3(file);

            // 파일명, 확장자 추출
            String savedFileName = extractFileName(fileUrl);
            String fileType = extractFileType(fileUrl);

            // DB에 저장
            File saved = fileRepository.save(File.builder()
                    .fileTitle(savedFileName) // 원본 파일명
                    .filePath(fileUrl)
                    .fileSize(file.getSize())
                    .fileType(fileType)
                    .post(post)
                    .uploadedBy(post.getUser().getId())
                    .isDeleted(false)
                    .build());

            savedFiles.add(saved);
        }
        return ActiveFileListDTO.Converter.from(savedFiles);
    }

    // 3. 파일 삭제 (hard delete)
    @Override
    @Transactional
    public void deleteFile(Long projectId, List<Long> fileIds){

        List<File> files = fileRepository.findAllById(fileIds);

        // 파일들이 해당 프로젝트에 속하는지 검증 (악의적 요청 가정)
        for (File file : files) {
            if (!file.getPost().getProject().getId().equals(projectId)) {
                throw new BusinessException(ErrorCode.FILE_NOT_IN_POST);
            }
        }
        // 일부만 있어도 에러 발생
        if (files.size() != fileIds.size()) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }

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

    // 5. 업로드 된 파일 삭제 (soft delete)
    @Override
    @Transactional
    public void deletePostFiles(Long projectId, Long postId, Long fileId, Long loginUserId){

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
    }

    // URL 에서 파일명 추출
    public static String extractFileName(String url) {
        int lastSlash = url.lastIndexOf('/');
        return lastSlash >= 0 ? url.substring(lastSlash + 1) : "unknown";
    }

    // URL 에서 파일 확장자 추출
    public static String extractFileType(String url) {
        int lastDot = url.lastIndexOf('.');
        return lastDot >= 0 ? url.substring(lastDot + 1).toLowerCase() : "unknown";
    }

    // S3 업로드 메서드
    private String uploadToS3(MultipartFile file) {

        String originalFilename = file.getOriginalFilename();
        String s3FileName = UUID.randomUUID() + "_" + originalFilename;

        try(InputStream is = file.getInputStream()){

            PutObjectRequest req = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3FileName)
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(req, RequestBody.fromInputStream(is,file.getSize()));
        }
        catch (Exception e){
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }
        return s3Client.utilities().getUrl(url -> url.bucket(bucketName).key(s3FileName))
                .toString();
    }

    // 삭제에 필요한 key 반환
    private String getKeyFromFileUrls(String fileUrl) {
        try{
            URL url = new URI(fileUrl).toURL();
            String decodedKey = URLDecoder.decode(url.getPath(), StandardCharsets.UTF_8);

            //경로 앞에 '/' 제거 후 반환
            return decodedKey.substring(1);
        }
        catch (Exception e){
            throw new BusinessException(ErrorCode.INVALID_URL_FORMAT);
        }
    }
}
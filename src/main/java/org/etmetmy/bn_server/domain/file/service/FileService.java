package org.etmetmy.bn_server.domain.file.service;

import jakarta.validation.Valid;
import org.etmetmy.bn_server.domain.comment.entity.Comment;
import org.etmetmy.bn_server.domain.file.dto.request.FilePermanentDeleteRequest;
import org.etmetmy.bn_server.domain.file.dto.request.FileRestoreRequest;
import org.etmetmy.bn_server.domain.file.dto.response.FilePermanentDeleteResponse;
import org.etmetmy.bn_server.domain.file.dto.response.FileRestoreResponse;
import org.etmetmy.bn_server.domain.file.dto.response.S3UploadResult;
import org.etmetmy.bn_server.domain.file.dto.response.TempFileListDTO;
import org.etmetmy.bn_server.domain.file.entity.File;
import org.etmetmy.bn_server.domain.post.entity.Post;
import org.etmetmy.bn_server.domain.project.entity.ProjectCheckList;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {

    // 임시 파일 업로드
    List<TempFileListDTO> postFiles(Long projectId, List<MultipartFile> files, Long uploadedBy);

    // 임시 파일 삭제 (hard delete)
    void deleteTempFile(Long projectId, List<Long> fileIds);

    // 업로드 된 파일 삭제 (soft delete)
    void deletePostFiles(Long projectId, Long postId, Long fileId, Long loginUserId);

    // S3 업로드 메서드
    public S3UploadResult uploadToS3(MultipartFile file);

    // 삭제에 필요한 key 반환
    public String getKeyFromFileUrls(String fileUrl);

    // S3와 DB 에서 파일 삭제
    void deleteFilesFromS3AndDb(List<File> files);

    // S3에서 파일 삭제
    void deleteFilesFromS3(List<File> files);

    // S3 업로드 결과로 받은 파일 정보를 기반으로 File 엔티티를 생성하여 Post에 저장
    void saveFiles(Post post, List<Long> fileIds, Long loginUserId);

    // S3 업로드 결과로 받은 파일 정보를 기반으로 File 엔티티를 생성하여 Comment에 저장
    void saveFiles(Comment comment, List<Long> fileIds, Long loginUserId);

    // S3 업로드 결과로 받은 파일 정보를 기반으로 File 엔티티를 생성하여 ProjectCheckList에 저장
    void saveFiles(ProjectCheckList projectCheckList, List<Long> fileIds, Long loginUserId);

    // 삭제된 파일 복원
    public FileRestoreResponse restoreDeletedFiles(Long loginUserId, @Valid FileRestoreRequest request);

    // 삭제된 파일 영구 삭제
    FilePermanentDeleteResponse hardDeleteFiles(Long loginUserId, @Valid FilePermanentDeleteRequest request);

    // 프로필 이미지 수정 - 기존 이미지 S3에서 삭제
    void removeOldProfileImage(String oldImageUrl);

    // 프로필 이미지 수정 - 새 이미지 업로드 후 S3 이미지 URL 저장
    String uploadProfileImage(MultipartFile image, Long userId);

}


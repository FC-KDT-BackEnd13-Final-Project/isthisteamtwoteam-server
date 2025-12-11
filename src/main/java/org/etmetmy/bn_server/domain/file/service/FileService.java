package org.etmetmy.bn_server.domain.file.service;

import jakarta.servlet.http.HttpSession;
import org.etmetmy.bn_server.domain.file.dto.response.ActiveFileListDTO;
import org.etmetmy.bn_server.domain.file.entity.File;
import org.etmetmy.bn_server.domain.post.dto.request.PostCreateRequest;
import org.etmetmy.bn_server.domain.post.entity.Post;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {

    //프로젝트별 파일 목록 조회
    List<ActiveFileListDTO> findAllByProjectId(Long projectId, HttpSession session);

    // 임시 파일 업로드
    List<ActiveFileListDTO> postFiles(Long projectId, Long postId, List<MultipartFile> files);

    // 임시 업로드 파일 삭제 (hard delete)
    void deleteFile(Long projectId, List<Long> fileIds);

    // 업로드 된 파일 삭제 (soft delete)
    void deletePostFiles(Long projectId, Long postId, Long fileId, Long loginUserId);

    // S3 업로드 메서드
    public String uploadToS3(MultipartFile file);

    // 삭제에 필요한 key 반환
    public String getKeyFromFileUrls(String fileUrl);

    // S3와 DB 에서 파일 삭제
    void deleteFilesFromS3AndDb(List<File> files);

    // S3에서 파일 삭제
    void deleteFilesFromS3(List<File> files);

    // S3 업로드 결과로 받은 파일 정보를 기반으로 File 엔티티를 생성하여 Post에 저장
    void saveFiles(Post post, List<PostCreateRequest.FileInfo> fileInfos, Long uploadedBy);
}

package org.etmetmy.bn_server.domain.file.service;

import jakarta.servlet.http.HttpSession;
import org.etmetmy.bn_server.domain.file.dto.ActiveFileListDTO;

import java.util.List;

public interface FileService {

    //프로젝트별 파일 목록 조회
    List<ActiveFileListDTO> findAllByProjectId(Long projectId, HttpSession session);

    // 업로드 된 파일 삭제
    void deletePostFiles(Long projectId, Long postId, Long fileId, Long loginUserId);

}

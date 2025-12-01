package org.etmetmy.bn_server.domain.file.service;

import org.etmetmy.bn_server.domain.file.dto.FileListDTO;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FileService {

    //프로젝트별 파일 목록 조회
    List<FileListDTO> findAllByProjectId(Long projectId);
}

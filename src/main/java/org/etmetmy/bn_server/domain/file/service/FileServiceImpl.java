package org.etmetmy.bn_server.domain.file.service;

import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.file.dto.FileListDTO;
import org.etmetmy.bn_server.domain.file.repository.FileRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileRepository fileRepository;

    //프로젝트별 파일 목록 조회
    @Override
    public List<FileListDTO> findAllByProjectId(Long projectId){

        List<FileListDTO> files = fileRepository.findByProjectId(projectId);
    }
}
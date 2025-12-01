package org.etmetmy.bn_server.domain.file.service;

import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.file.dto.FileListDTO;
import org.etmetmy.bn_server.domain.file.entity.File;
import org.etmetmy.bn_server.domain.file.repository.FileRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileRepository fileRepository;

    //프로젝트별 파일 목록 조회
    @Override
    public List<FileListDTO> findAllByProjectId(Long projectId){

        List<File> files = fileRepository.findByProjectId(projectId);

        return files.stream()
                .map(file -> FileListDTO.builder()
                        .fileId(file.getFileId())
                        .fileTitle(file.getFileTitle())
                        .filePath(file.getFilePath())
                        .fileType(file.getFileType())
                        .fileSize(file.getFileSize())
                        .entityTypeName(file.getEntityType().getEntityType())
                        .postId(file.getPost().getPostId())
                        .uploadUserId(file.getUploadedBy())
                        .uploadedAt(file.getCreatedAt())
                        .Deleted(file.getIsDeleted())
                        .deletedAt(file.getDeletedAt())
                        .deleteUserId(file.getDeletedBy())
                        .build())
                .collect(Collectors.toList());
    }
}
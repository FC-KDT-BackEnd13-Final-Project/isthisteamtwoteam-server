package org.etmetmy.bn_server.domain.file.service;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.file.dto.ActiveFileListDTO;
import org.etmetmy.bn_server.domain.file.entity.File;
import org.etmetmy.bn_server.domain.file.repository.FileRepository;
import org.etmetmy.bn_server.domain.project.repository.ProjectMemberRepository;
import org.etmetmy.bn_server.domain.project.repository.ProjectRepository;
import org.etmetmy.bn_server.exception.custom.ProjectNotFoundException;
import org.etmetmy.bn_server.exception.custom.ProjectPermissionDeniedException;
import org.etmetmy.bn_server.global.util.SessionUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileRepository fileRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    //프로젝트별 파일 목록 조회
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

        return files.stream()
                .map(file -> ActiveFileListDTO.builder()
                        .fileId(file.getFileId())
                        .fileTitle(file.getFileTitle())
                        .filePath(file.getFilePath())
                        .fileType(file.getFileType())
                        .fileSize(file.getFileSize())
                        .entityTypeName(file.getEntityType().getEntityType())
                        .postId(file.getPost().getPostId())
                        .uploadUserId(file.getUploadedBy())
                        .uploadedAt(file.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
package org.etmetmy.bn_server.domain.file.service;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.file.dto.ActiveFileListDTO;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileRepository fileRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final PostRepository postRepository;

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

        return ActiveFileListDTO.Converter.from(files);

    }

    // 업로드 된 파일 삭제 API
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

    //URL 에서 파일명 추출
    public static String extractFileName(String url) {
        int lastSlash = url.lastIndexOf('/');
        return lastSlash >= 0 ? url.substring(lastSlash + 1) : "unknown";
    }

    //URL 에서 파일 확장자 추출
    public static String extractFileType(String url) {
        int lastDot = url.lastIndexOf('.');
        return lastDot >= 0 ? url.substring(lastDot + 1).toLowerCase() : "unknown";
    }
}
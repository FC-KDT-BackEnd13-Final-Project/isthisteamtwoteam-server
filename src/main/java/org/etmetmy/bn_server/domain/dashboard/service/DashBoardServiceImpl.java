package org.etmetmy.bn_server.domain.dashboard.service;

import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.dashboard.dto.response.DashBoardResponse;
import org.etmetmy.bn_server.domain.dashboard.dto.response.DashBoardStatusResponseDTO;
import org.etmetmy.bn_server.domain.post.entity.Post;
import org.etmetmy.bn_server.domain.post.entity.RequestStatus;
import org.etmetmy.bn_server.domain.post.repository.PostRepository;
import org.etmetmy.bn_server.domain.project.entity.Project;
import org.etmetmy.bn_server.domain.project.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashBoardServiceImpl implements DashBoardService {

    private final PostRepository postRepository;
    private final ProjectRepository projectRepository;

    @Override
    @Transactional(readOnly = true)
    public DashBoardResponse getStatusDashboard(Long loginUserId) {
        // STATUS_PENDING(요청대기) 상태인 Post 목록
        List<Post> pendingPosts = postRepository.findPostsWithRequestStatus(RequestStatus.STATUS_PENDING);
        List<DashBoardStatusResponseDTO> pendingList = DashBoardStatusResponseDTO.Converter.toPostDTOList(pendingPosts, RequestStatus.STATUS_PENDING);


        // STATUS_REJECTED(반려) 상태인 Post 목록
        List<Post> rejectedPosts = postRepository.findPostsWithRequestStatus(RequestStatus.STATUS_REJECTED);
        List<DashBoardStatusResponseDTO> rejectedList = DashBoardStatusResponseDTO.Converter.toPostDTOList(rejectedPosts, RequestStatus.STATUS_REJECTED);

        // 진행 중인 Project
        List<Project> inProgressProjects = projectRepository.findProjectsInProgress();
        List<DashBoardStatusResponseDTO> inProgress = DashBoardStatusResponseDTO.Converter.toProejctDTOList(inProgressProjects);

        // 유지 보수 Project
        List<Project> maintenanceProjects = projectRepository.findProjectsMaintenance();
        List<DashBoardStatusResponseDTO> maintenances = DashBoardStatusResponseDTO.Converter.toProejctDTOList(maintenanceProjects);

        // stats와 List를 포함한 wrapper 반환
        return DashBoardResponse.of(pendingList, rejectedList, inProgress, maintenances);
    }
}

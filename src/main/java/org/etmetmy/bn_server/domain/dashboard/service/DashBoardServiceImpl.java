package org.etmetmy.bn_server.domain.dashboard.service;

import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.dashboard.dto.response.*;
import org.etmetmy.bn_server.domain.post.entity.Post;
import org.etmetmy.bn_server.domain.post.entity.RequestStatus;
import org.etmetmy.bn_server.domain.post.entity.Stage;
import org.etmetmy.bn_server.domain.post.repository.PostRepository;
import org.etmetmy.bn_server.domain.project.entity.Project;
import org.etmetmy.bn_server.domain.project.repository.ProjectMemberRepository;
import org.etmetmy.bn_server.domain.project.repository.ProjectRepository;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.etmetmy.bn_server.domain.user.repository.UserRepository;
import org.etmetmy.bn_server.exception.custom.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashBoardServiceImpl implements DashBoardService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final PostRepository postRepository;

    // 1. 프로젝트 목록 조회 (접근 권한 정보 제공)
    public List<ProjectListResponse> getProjectList(Long loginUserId) {

        // 유저 검증
        User user = userRepository.findById(loginUserId).orElseThrow(UserNotFoundException::new);

        // 삭제되지 않은 모든 프로젝트 목록 조회
        List<Project> allProjects = projectRepository.findActiveProjects();

        // 권한이 있는 모든 프로젝트 조회
        List<Long> myProjectIds = projectMemberRepository.findProjectIdsByUserId(loginUserId);

        return ProjectListResponse.Converter.from(allProjects, myProjectIds);
    }

    @Override
    @Transactional(readOnly = true)
    public DashBoardResponse getStatusDashboard(Long loginUserId) {
        // 유저 검증
        userRepository.findById(loginUserId)
                .orElseThrow(UserNotFoundException::new);

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
        return DashBoardResponse.Converter.of(pendingList, rejectedList, inProgress, maintenances);
    }

    @Override
    @Transactional(readOnly = true)
    public ApprovalRequestListResponse getApprovalRequest() {

        // Request가 있는 모든 Post 조회 (상태별, 단계별 카운팅 및 리스트 생성용)
        List<Post> allPostsWithRequest = postRepository.findAllPostsWithRequest();
        List<ApprovalRequestResponse> allPosts = ApprovalRequestResponse.Converter.from(allPostsWithRequest);

        // Converter에서 단계별 필터링 및 응답 생성
        return ApprovalRequestListResponse.Converter.of(allPosts);
    }
}

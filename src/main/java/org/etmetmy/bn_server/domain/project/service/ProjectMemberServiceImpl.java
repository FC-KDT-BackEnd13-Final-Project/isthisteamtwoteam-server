package org.etmetmy.bn_server.domain.project.service;

import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.company.entity.CompanyType;
import org.etmetmy.bn_server.domain.project.dto.response.ProjectMemberSearchResponse;
import org.etmetmy.bn_server.domain.project.entity.ProjectMember;
import org.etmetmy.bn_server.domain.project.repository.ProjectMemberRepository;
import org.etmetmy.bn_server.domain.user.entity.Role;
import org.etmetmy.bn_server.domain.user.repository.UserRepository;
import org.etmetmy.bn_server.exception.custom.InvalidInputException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ProjectMemberServiceImpl implements ProjectMemberService {
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    @Override
    public boolean hasRoleToProject(Long userId, Long projectId) {
        boolean hasRole;

        ProjectMember projectMember = projectMemberRepository.findByUserIdAndProjectId(userId, projectId);
        if(projectMember == null ){
            hasRole = false;
        }else{
            hasRole = true;
        }

        return hasRole;
    }

    // 프로젝트 생성 - 개발사 담당자, 사원 조회
    @Transactional(readOnly = true)
    @Override
    public List<ProjectMemberSearchResponse> searchDeveloperMembers() {
        return userRepository.findDeveloperCandidates();
    }
    // 프로젝트 생성- 고객사 담당자, 사원 조회
    @Transactional(readOnly = true)
    @Override
    public List<ProjectMemberSearchResponse> searchClientMembers() {
        return userRepository.findClientCandidates();
    }

    // 프로젝트 설정 - 개발사 사원 조회 (프로젝트 멤버 + Admin 제외)
    @Transactional(readOnly = true)
    @Override
    public List<ProjectMemberSearchResponse> searchDeveloperMembersForProject(Long projectId) {
        Set<Long> existingUserIds = getProjectMemberUserIds(projectId);

        return userRepository.findDeveloperCandidates().stream()
                .filter(dto -> dto.getUserId() != null && !existingUserIds.contains(dto.getUserId()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<ProjectMemberSearchResponse> searchClientMembersForProject(Long projectId) {
        Set<Long> existingUserIds = getProjectMemberUserIds(projectId);

        return userRepository.findClientCandidates().stream()
                .filter(dto -> dto.getUserId() != null && !existingUserIds.contains(dto.getUserId()))
                .collect(Collectors.toList());
    }

    //
    @Transactional(readOnly = true)
    @Override
    public List<ProjectMemberSearchResponse> searchUsersForCreate(Role role){
        validateRole(role);

        if (role == Role.DEVELOPER) {
            // 개발사: 회사명 NULL (레포에서 NULL로 만들어줌)
            return searchDeveloperMembers();
        } else if (role == Role.CUSTOMER) {
            // 고객사: 회사명 포함
            return searchClientMembers();
        } else {
            throw new InvalidInputException("유효하지 않은 role 값입니다. (DEVELOPER, CUSTOMER만 사용 가능)");
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<ProjectMemberSearchResponse> searchUsersForProject(Long projectId, Role role) {

        validateRole(role);

        if (role == Role.DEVELOPER) {
            // 개발사: 회사명 NULL, 프로젝트 멤버 제외
            return searchDeveloperMembersForProject(projectId);
        } else if (role == Role.CUSTOMER) {
            // 고객사: 회사명 포함, 프로젝트 멤버 제외
            return searchClientMembersForProject(projectId);
        } else {
            throw new InvalidInputException("유효하지 않은 role 값입니다. (DEVELOPER, CUSTOMER만 사용 가능)");
        }
    }

    // 공통 검증 로직
    private void validateRole(Role role) {
        if (role == null) {
            throw new InvalidInputException("role 파라미터는 필수입니다. (DEVELOPER 또는 CUSTOMER)");
        }
        if (role == Role.ADMIN) {
            throw new InvalidInputException("관리자(ADMIN)는 조회할 수 없습니다.");
        }
    }

    //프로젝트에 속한 유저
    private Set<Long> getProjectMemberUserIds(Long projectId) {
        return projectMemberRepository.findByProjectId(projectId).stream()
                .map(ProjectMember::getUser)
                .filter(Objects::nonNull)
                .map(user -> user.getId())
                .collect(Collectors.toSet());
    }

}

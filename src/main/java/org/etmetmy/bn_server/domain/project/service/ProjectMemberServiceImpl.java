package org.etmetmy.bn_server.domain.project.service;

import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.company.entity.CompanyType;
import org.etmetmy.bn_server.domain.project.dto.response.ProjectMemberSearchResponse;
import org.etmetmy.bn_server.domain.project.entity.ProjectMember;
import org.etmetmy.bn_server.domain.project.repository.ProjectMemberRepository;
import org.etmetmy.bn_server.domain.user.entity.Role;
import org.etmetmy.bn_server.domain.user.entity.User;
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

    //
    @Transactional(readOnly = true)
    @Override
    public List<ProjectMemberSearchResponse> searchUsersForCreate(Role role){
        validateRole(role);

        List<User> users = switch (role){
            case DEVELOPER -> userRepository.findDeveloperCandidates();
            case CUSTOMER -> userRepository.findClientCandidates();
            default -> throw new InvalidInputException("role은 DEVELOPER 또는 CUSTOMER만 가능합니다");
        };

        return users.stream()
                .map(user -> convertToDto(user, role))
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<ProjectMemberSearchResponse> searchUsersForProject(Long projectId, Role role) {
        validateRole(role);
        Set<Long> existingUserIds = getProjectMemberUserIds(projectId);

        List<User> users = switch (role) {
            case DEVELOPER -> userRepository.findDeveloperCandidates();
            case CUSTOMER -> userRepository.findClientCandidates();
            default -> throw new InvalidInputException("role은 DEVELOPER 또는 CUSTOMER만 가능합니다.");
        };
        return users.stream()
                .filter(user -> !existingUserIds.contains(user.getId()))
                .map(user -> convertToDto(user, role))
                .toList();
    }

    private ProjectMemberSearchResponse convertToDto(User user, Role role) {
        return ProjectMemberSearchResponse.builder()
                .userId(user.getId())
                .userName(user.getName())
                .email(user.getEmail())
                .companyId(user.getCompany() != null ? user.getCompany().getCompanyId() : null)
                // 개발사는 회사명 NULL, 고객사는 회사명 포함
                .companyName(role == Role.DEVELOPER ? null :
                        (user.getCompany() != null ? user.getCompany().getCompanyName() : null))
                .build();
    }

    // 공통 검증 로직
    private void validateRole(Role role) {
        if (role == null){ throw new InvalidInputException("role 파라미터는 필수입니다. (DEVELOPER 또는 CUSTOMER)");}
        if (role == Role.ADMIN){ throw new InvalidInputException("관리자(ADMIN)는 조회할 수 없습니다.");}
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

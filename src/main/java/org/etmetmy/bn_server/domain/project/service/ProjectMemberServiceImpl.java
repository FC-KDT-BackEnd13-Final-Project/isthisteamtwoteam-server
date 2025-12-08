package org.etmetmy.bn_server.domain.project.service;

import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.company.entity.CompanyType;
import org.etmetmy.bn_server.domain.project.dto.response.ProjectMemberSearchResponse;
import org.etmetmy.bn_server.domain.project.entity.ProjectMember;
import org.etmetmy.bn_server.domain.project.repository.ProjectMemberRepository;
import org.etmetmy.bn_server.domain.user.repository.UserRepository;
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
    public List<ProjectMemberSearchResponse> searchDeveloperMembers(String keyword) {
        CompanyType companyType = CompanyType.DEVELOPER;

        if (keyword == null || keyword.trim().isEmpty()) {
            return userRepository.findByCompanyType(companyType);
        } else {
            String searchKeyword = "%" + keyword.trim() + "%";
            return userRepository.searchByCompanyTypeAndKeyword(companyType, searchKeyword);
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

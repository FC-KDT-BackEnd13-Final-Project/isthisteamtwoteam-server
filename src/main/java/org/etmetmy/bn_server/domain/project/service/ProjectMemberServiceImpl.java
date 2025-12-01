package org.etmetmy.bn_server.domain.project.service;

import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.project.repository.ProjectMemberRepository;
import org.etmetmy.bn_server.domain.user.entity.ProjectMember;
import org.etmetmy.bn_server.global.CustomException;
import org.etmetmy.bn_server.global.StatusCode;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class ProjectMemberServiceImpl implements ProjectMemberService {
    private final ProjectMemberRepository projectMemberRepository;

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


}

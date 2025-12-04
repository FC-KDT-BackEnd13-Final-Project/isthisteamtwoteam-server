package org.etmetmy.bn_server.domain.project.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.etmetmy.bn_server.domain.project.entity.ProjectMember;
import org.etmetmy.bn_server.domain.project.repository.ProjectMemberRepository;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectMemberServiceImpl implements ProjectMemberService {
    private final ProjectMemberRepository projectMemberRepository;

    @Override
    public boolean hasRoleToProject(Long userId, Long projectId) {
        boolean hasRole;
        log.info("hasRoleToProject 호출 ");

        ProjectMember projectMember = projectMemberRepository.findByUserIdAndProjectId(userId, projectId);
        log.info("projectMember ");
        if(projectMember == null ){
            hasRole = false;
        }else{
            hasRole = true;
        }

        return hasRole;
    }


}

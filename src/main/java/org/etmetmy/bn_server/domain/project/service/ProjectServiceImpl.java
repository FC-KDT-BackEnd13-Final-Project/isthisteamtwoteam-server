package org.etmetmy.bn_server.domain.project.service;

import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.company.entity.Company;
import org.etmetmy.bn_server.domain.company.repository.CompanyRepository;
import org.etmetmy.bn_server.domain.project.dto.entityDto.ProjectDTO;
import org.etmetmy.bn_server.domain.project.dto.request.ProjectCreateRequest;
import org.etmetmy.bn_server.domain.project.dto.response.ProjectResponse;
import org.etmetmy.bn_server.domain.project.entity.Project;
import org.etmetmy.bn_server.domain.project.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;

    @Override
    @Transactional
    public Long createProject(ProjectCreateRequest request, Long createdById){

        Project project = ProjectCreateRequest.Converter.toEntity(request, createdById);
        Project saveProject = projectRepository.save(project);

        // TODO: members, selectedChecklistIds 를 사용해
        // ProjectMember / ProjectChecklist 엔티티도 함께 저장
        return saveProject.getProjectId();
    }
    private String normalizeRole(String role) {
        if (role == null) return null;
        // "Developer", "developer" 등 들어와도 일관되게 저장
        return role.trim().toUpperCase(); // 예: "DEVELOPER", "CLIENT"
    }

    @Transactional(readOnly = true)
    @Override
    public List<ProjectResponse> getAllProjects() {
        List<Project> projects = projectRepository.findAll();
        return ProjectDTO.Converter.toResponseList(projects);
    }


}

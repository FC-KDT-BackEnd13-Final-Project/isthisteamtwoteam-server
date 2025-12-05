package org.etmetmy.bn_server.domain.project.repository;

import org.etmetmy.bn_server.domain.project.entity.Project;
import org.etmetmy.bn_server.domain.project.entity.ProjectCheckList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectCheckListRepository extends JpaRepository<ProjectCheckList, Long> {
    List<ProjectCheckList> findByProject(Project project);
}

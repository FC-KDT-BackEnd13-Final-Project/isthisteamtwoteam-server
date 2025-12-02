package org.etmetmy.bn_server.domain.project.repository;

import org.etmetmy.bn_server.domain.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Project p " +
            "SET p.projectName = :projectName " +
            "WHERE p.id = :projectId")
    int updateProjectTitle(@Param("projectId") Long projectId,
                           @Param("projectName") String projectName);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Project p " +
            "SET p.startDate = :startDate, " +
            "    p.endDate = :endDate " +
            "WHERE p.id = :projectId")
    int updateProjectDates(@Param("projectId") Long projectId,
                           @Param("startDate") LocalDate startDate,
                           @Param("endDate") LocalDate endDate);

}

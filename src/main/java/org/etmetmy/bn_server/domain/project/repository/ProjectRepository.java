package org.etmetmy.bn_server.domain.project.repository;

import org.etmetmy.bn_server.domain.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Project p " +
            "SET p.projectName = :projectName " +
            "WHERE p.id = :projectId")
    int updateProjectName(@Param("projectId") Long projectId,
                           @Param("projectName") String projectName);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Project p " +
            "SET p.startDate = :startDate, " +
            "    p.endDate = :endDate " +
            "WHERE p.id = :projectId")
    int updateProjectDates(@Param("projectId") Long projectId,
                           @Param("startDate") LocalDate startDate,
                           @Param("endDate") LocalDate endDate);

    //프로젝트 삭제(휴지통이동)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Project p SET p.isDeleted = TRUE, p.deletedAt = :deletedAt WHERE p.id = :projectId")
    int moveToTrash(@Param("projectId") Long projectId,
                    @Param("deletedAt") LocalDateTime deletedAt);


    // 프로젝트 진행단계 수정
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Project p " +
            "SET p.stage.id = :stageId " +
            "WHERE p.id = :projectId")
    int updateProjectStage(@Param("projectId") Long projectId,
                           @Param("stageId") Long stageId);

    // 진행중인 프로젝트 조회
    @Query("SELECT p FROM Project p " +
            "JOIN FETCH p.stage s " +
            "JOIN FETCH p.company c " +
            "WHERE p.stage.id BETWEEN 3 AND 7 " +
            "AND p.isDeleted = false " +
            "ORDER BY p.createdAt DESC")
    List<Project> findProjectsInProgress();

    // 유지보수 프로젝트 조회
    @Query("SELECT p FROM Project p " +
            "JOIN FETCH p.stage s " +
            "JOIN FETCH p.company c " +
            "WHERE p.stage.id = 8" +
            "AND p.isDeleted = false " +
            "ORDER BY p.createdAt DESC")
    List<Project> findProjectsMaintenance();


}

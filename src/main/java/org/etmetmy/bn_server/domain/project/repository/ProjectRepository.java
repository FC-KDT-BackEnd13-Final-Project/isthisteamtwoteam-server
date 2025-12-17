package org.etmetmy.bn_server.domain.project.repository;

import org.etmetmy.bn_server.domain.project.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
            "WHERE p.stage.stageName in ('요구사항 정의', '화면 설계', '디자인, 퍼블리싱', '개발', '검수')" +
            "AND p.isDeleted = false " +
            "ORDER BY p.createdAt DESC")
    List<Project> findProjectsInProgress();

    // 유지보수 프로젝트 조회
    @Query("SELECT p FROM Project p " +
            "JOIN FETCH p.stage s " +
            "JOIN FETCH p.company c " +
            "WHERE p.stage.stageName = '유지보수'" +
            "AND p.isDeleted = false " +
            "ORDER BY p.createdAt DESC")
    List<Project> findProjectsMaintenance();

    // 삭제되지 않은 모든 프로젝트 조회
    @Query("select p from Project p where (p.isDeleted IS NULL OR p.isDeleted = false)")
    List<Project> findActiveProjects();

    // 특정 프로젝트 ID 리스트에서 진행 중인 프로젝트만 조회 (고객용 대시보드)
    @Query("SELECT p FROM Project p " +
            "JOIN FETCH p.stage s " +
            "JOIN FETCH p.company c " +
            "WHERE p.id IN :projectIds " +
            "AND p.stage.stageName in ('요구사항 정의', '화면 설계', '디자인, 퍼블리싱', '개발', '검수') " +
            "AND p.isDeleted = false " +
            "ORDER BY p.createdAt DESC")
    List<Project> findProjectsInProgressByProjectIds(@Param("projectIds") List<Long> projectIds);

    // 특정 프로젝트 ID 리스트에서 유지보수 프로젝트만 조회 (고객용 대시보드)
    @Query("SELECT p FROM Project p " +
            "JOIN FETCH p.stage s " +
            "JOIN FETCH p.company c " +
            "WHERE p.id IN :projectIds " +
            "AND p.stage.stageName = '유지보수' " +
            "AND p.isDeleted = false " +
            "ORDER BY p.createdAt DESC")
    List<Project> findProjectsMaintenanceByProjectIds(@Param("projectIds") List<Long> projectIds);

    // 특정 프로젝트 ID 리스트의 활성 프로젝트만 조회 (고객용 프로젝트 목록)
    @Query("SELECT p FROM Project p " +
            "JOIN FETCH p.stage s " +
            "JOIN FETCH p.company c " +
            "WHERE p.id IN :projectIds " +
            "AND (p.isDeleted IS NULL OR p.isDeleted = false) " +
            "ORDER BY p.updatedAt DESC")
    List<Project> findActiveProjectsByProjectIds(@Param("projectIds") List<Long> projectIds);

    Page<Project> findByProjectNameContainingIgnoreCaseAndIsDeleted(String searchKeyword, Boolean isDeleted, Pageable pageable);

    Page<Project> findByIsDeleted(Boolean isDeleted, Pageable pageable);
}

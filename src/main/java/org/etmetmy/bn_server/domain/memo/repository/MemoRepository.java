package org.etmetmy.bn_server.domain.memo.repository;

import org.etmetmy.bn_server.domain.memo.entity.Memo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MemoRepository extends JpaRepository<Memo,Long> {
    @Query("SELECT m FROM Memo m " +
            "WHERE m.project.projectId = :projectId " +
            "ORDER BY m.createdAt DESC")
    List<Memo> findByProjectId(@Param("projectId") Long projectId);
}

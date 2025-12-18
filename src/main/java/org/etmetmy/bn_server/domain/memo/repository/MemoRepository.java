package org.etmetmy.bn_server.domain.memo.repository;

import org.etmetmy.bn_server.domain.memo.entity.Memo;
import org.etmetmy.bn_server.domain.memo.entity.MemoType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemoRepository extends JpaRepository<Memo,Long> {
    @Query("select m from Memo m " +
            "where m.project.id = :projectId and m.memoType = :memoType " +
            "order by m.createdAt")
    Optional<Memo> findByProjectId(@Param("projectId") Long projectId, @Param("memoType") MemoType memoType);

    @Query("select  m from Memo m where m.project.id = :projectId and m.user.id = :userId and m.memoType = :memoType ")
    Optional<Memo> findByUserIdAndProjectId(@Param("userId") Long userId, @Param ("projectId") Long projectId , @Param("memoType") MemoType memoType);

    @Query("select m from Memo m " +
            "where m.project.id = :projectId and m.memoType = :memoType ")
    Optional<Memo> findProjectMemoByProjectId (@Param ("projectId") Long projectId , @Param("memoType") MemoType memoType);
}

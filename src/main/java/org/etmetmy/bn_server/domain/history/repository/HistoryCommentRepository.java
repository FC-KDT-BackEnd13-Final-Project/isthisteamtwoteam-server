package org.etmetmy.bn_server.domain.history.repository;

import org.etmetmy.bn_server.domain.history.entity.HistoryComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryCommentRepository extends JpaRepository<HistoryComment, Long> {

    /**
     * 원본 댓글 ID로 히스토리 목록 조회 (최신순)
     */
    List<HistoryComment> findByOriginalCommentIdOrderByCreatedAtDesc(Long originalCommentId);

    /**
     * 프로젝트 ID로 히스토리 목록 조회 (최신순)
     */
    List<HistoryComment> findByProjectIdOrderByCreatedAtDesc(Long projectId);

    /**
     * 게시글 ID로 히스토리 목록 조회 (최신순)
     */
    List<HistoryComment> findByPostIdOrderByCreatedAtDesc(Long postId);
}

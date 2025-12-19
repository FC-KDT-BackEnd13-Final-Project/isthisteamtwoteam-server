package org.etmetmy.bn_server.domain.history.repository;

import org.etmetmy.bn_server.domain.history.entity.HistoryLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryLinkRepository extends JpaRepository<HistoryLink, Long> {

    /**
     * 원본 링크 ID로 히스토리 목록 조회 (최신순)
     */
    List<HistoryLink> findByOriginalLinkIdOrderByCreatedAtDesc(Long originalLinkId);

    /**
     * 프로젝트 ID로 히스토리 목록 조회 (최신순)
     */
    List<HistoryLink> findByProjectIdOrderByCreatedAtDesc(Long projectId);

    /**
     * 게시글 ID로 히스토리 목록 조회 (최신순)
     */
    List<HistoryLink> findByPostIdOrderByCreatedAtDesc(Long postId);

    /**
     * 댓글 ID로 히스토리 목록 조회 (최신순)
     */
    List<HistoryLink> findByCommentIdOrderByCreatedAtDesc(Long commentId);
}

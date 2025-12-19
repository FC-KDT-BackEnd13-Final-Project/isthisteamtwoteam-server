package org.etmetmy.bn_server.domain.history.repository;

import org.etmetmy.bn_server.domain.history.entity.HistoryPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryPostRepository extends JpaRepository<HistoryPost, Long> {

    /**
     * 원본 게시글 ID로 히스토리 목록 조회 (최신순)
     */
    List<HistoryPost> findByOriginalPostIdOrderByCreatedAtDesc(Long originalPostId);

    /**
     * 프로젝트 ID로 히스토리 목록 조회 (최신순)
     */
    List<HistoryPost> findByProjectIdOrderByCreatedAtDesc(Long projectId);
}

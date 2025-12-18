package org.etmetmy.bn_server.domain.history.repository;

import org.etmetmy.bn_server.domain.history.entity.HistoryFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryFileRepository extends JpaRepository<HistoryFile, Long> {

    /**
     * 원본 파일 ID로 히스토리 목록 조회 (최신순)
     */
    List<HistoryFile> findByOriginalFileIdOrderByCreatedAtDesc(Long originalFileId);

    /**
     * 프로젝트 ID로 히스토리 목록 조회 (최신순)
     */
    List<HistoryFile> findByProjectIdOrderByCreatedAtDesc(Long projectId);

    /**
     * 게시글 ID로 히스토리 목록 조회 (최신순)
     */
    List<HistoryFile> findByPostIdOrderByCreatedAtDesc(Long postId);

    /**
     * 댓글 ID로 히스토리 목록 조회 (최신순)
     */
    List<HistoryFile> findByCommentIdOrderByCreatedAtDesc(Long commentId);
}

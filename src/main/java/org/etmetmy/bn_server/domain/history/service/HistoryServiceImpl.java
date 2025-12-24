package org.etmetmy.bn_server.domain.history.service;

import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.history.dto.*;
import org.etmetmy.bn_server.domain.history.entity.*;
import org.etmetmy.bn_server.domain.history.repository.*;
import org.etmetmy.bn_server.domain.post.entity.Post;
import org.etmetmy.bn_server.domain.post.repository.PostRepository;
import org.etmetmy.bn_server.exception.custom.BoardNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HistoryServiceImpl implements HistoryService {

    private final PostRepository postRepository;
    private final HistoryPostRepository historyPostRepository;
    private final HistoryCommentRepository historyCommentRepository;
    private final HistoryFileRepository historyFileRepository;
    private final HistoryLinkRepository historyLinkRepository;

    @Override
    public HistoryListResponse getAllHistory(Long postId) {
        // 1. 게시글 존재 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(BoardNotFoundException::new);

        List<HistoryItemResponse> allHistories = new ArrayList<>();

        // 2. 각 히스토리 테이블에서 데이터 조회
        List<HistoryPost> postHistories = historyPostRepository.findByOriginalPostIdOrderByCreatedAtDesc(postId);
        List<HistoryComment> commentHistories = historyCommentRepository.findByPostIdOrderByCreatedAtDesc(postId);
        List<HistoryFile> fileHistories = historyFileRepository.findByPostIdOrderByCreatedAtDesc(postId);
        List<HistoryLink> linkHistories = historyLinkRepository.findByPostIdOrderByCreatedAtDesc(postId);

        // 3. HistoryPost → HistoryItemResponse 변환
        for (HistoryPost historyPost : postHistories) {
            HistoryPostDetail detail = HistoryPostDetail.builder()
                    .beTitle(historyPost.getBeTitle())
                    .beContent(historyPost.getBeContent())
                    .beStageName(historyPost.getBeStageName())
                    .beIsCompleted(historyPost.getBeIsCompleted())
                    .afTitle(historyPost.getAfTitle())
                    .afContent(historyPost.getAfContent())
                    .afStageName(historyPost.getAfStageName())
                    .afIsCompleted(historyPost.getAfIsCompleted())
                    .build();

            String title = HistoryItemResponse.createTitle(
                    historyPost.getChangedByUser().getName(),
                    historyPost.getCreatedAt(),
                    "POST",
                    historyPost.getChangeType()
            );

            // 변경된 내용 추출
            List<ChangeContent> changeContents = extractPostChanges(historyPost);

            allHistories.add(HistoryItemResponse.builder()
                    .title(title)
                    .targetType("POST")
                    .changeType(historyPost.getChangeType())
                    .changedAt(historyPost.getCreatedAt())
                    .changedByUserName(historyPost.getChangedByUser().getName())
                    .changeIp(historyPost.getChangeIp())
                    .details(detail)
                    .changeContents(changeContents)
                    .build());
        }

        // 4. HistoryComment → HistoryItemResponse 변환
        for (HistoryComment historyComment : commentHistories) {
            HistoryCommentDetail detail = HistoryCommentDetail.builder()
                    .beContent(historyComment.getBeContent())
                    .afContent(historyComment.getAfContent())
                    .build();

            String title = HistoryItemResponse.createTitle(
                    historyComment.getChangedByUser().getName(),
                    historyComment.getCreatedAt(),
                    "COMMENT",
                    historyComment.getChangeType()
            );

            // 변경된 내용 추출
            List<ChangeContent> changeContents = extractCommentChanges(historyComment);

            allHistories.add(HistoryItemResponse.builder()
                    .title(title)
                    .targetType("COMMENT")
                    .changeType(historyComment.getChangeType())
                    .changedAt(historyComment.getCreatedAt())
                    .changedByUserName(historyComment.getChangedByUser().getName())
                    .changeIp(historyComment.getChangeIp())
                    .details(detail)
                    .changeContents(changeContents)
                    .build());
        }

        // 5. HistoryFile → HistoryItemResponse 변환
        for (HistoryFile historyFile : fileHistories) {
            HistoryFileDetail detail = HistoryFileDetail.builder()
                    .fileName(historyFile.getFileName())
                    .filePath(historyFile.getFilePath())
                    .fileSize(historyFile.getFileSize())
                    .fileType(historyFile.getFileType())
                    .build();

            String title = HistoryItemResponse.createTitle(
                    historyFile.getChangedByUser().getName(),
                    historyFile.getCreatedAt(),
                    "FILE",
                    historyFile.getChangeType()
            );

            // 변경된 내용 추출
            List<ChangeContent> changeContents = extractFileChanges(historyFile);

            allHistories.add(HistoryItemResponse.builder()
                    .title(title)
                    .targetType("FILE")
                    .changeType(historyFile.getChangeType())
                    .changedAt(historyFile.getCreatedAt())
                    .changedByUserName(historyFile.getChangedByUser().getName())
                    .changeIp(historyFile.getChangeIp())
                    .details(detail)
                    .changeContents(changeContents)
                    .build());
        }

        // 6. HistoryLink → HistoryItemResponse 변환
        for (HistoryLink historyLink : linkHistories) {
            HistoryLinkDetail detail = HistoryLinkDetail.builder()
                    .linkUrl(historyLink.getLinkUrl())
                    .build();

            String title = HistoryItemResponse.createTitle(
                    historyLink.getChangedByUser().getName(),
                    historyLink.getCreatedAt(),
                    "LINK",
                    historyLink.getChangeType()
            );

            // 변경된 내용 추출
            List<ChangeContent> changeContents = extractLinkChanges(historyLink);

            allHistories.add(HistoryItemResponse.builder()
                    .title(title)
                    .targetType("LINK")
                    .changeType(historyLink.getChangeType())
                    .changedAt(historyLink.getCreatedAt())
                    .changedByUserName(historyLink.getChangedByUser().getName())
                    .changeIp(historyLink.getChangeIp())
                    .details(detail)
                    .changeContents(changeContents)
                    .build());
        }

        // 7. 시간순 정렬 (최신순)
        allHistories.sort(Comparator.comparing(HistoryItemResponse::getChangedAt).reversed());

        // 8. 응답 생성
        return HistoryListResponse.builder()
                .postId(postId)
                .totalCount(allHistories.size())
                .histories(allHistories)
                .build();
    }

    /**
     * 게시글 변경 내용 추출
     */
    private List<ChangeContent> extractPostChanges(HistoryPost historyPost) {
        List<ChangeContent> changes = new ArrayList<>();

        if (historyPost.getChangeType() == ChangeType.DELETE) {
            // 삭제인 경우
            if (historyPost.getBeTitle() != null) {
                changes.add(ChangeContent.ofDelete("title", historyPost.getBeTitle()));
            }
            if (historyPost.getBeContent() != null) {
                changes.add(ChangeContent.ofDelete("content", historyPost.getBeContent()));
            }
        } else if (historyPost.getChangeType() == ChangeType.UPDATE) {
            // 수정인 경우 - 변경된 필드만 추출
            if (!equals(historyPost.getBeTitle(), historyPost.getAfTitle())) {
                changes.add(ChangeContent.of("title", historyPost.getBeTitle(), historyPost.getAfTitle()));
            }
            if (!equals(historyPost.getBeContent(), historyPost.getAfContent())) {
                changes.add(ChangeContent.of("content", historyPost.getBeContent(), historyPost.getAfContent()));
            }
            if (!equals(historyPost.getBeStageName(), historyPost.getAfStageName())) {
                changes.add(ChangeContent.of("stage", historyPost.getBeStageName(), historyPost.getAfStageName()));
            }
            if (!equals(historyPost.getBeIsCompleted(), historyPost.getAfIsCompleted())) {
                changes.add(ChangeContent.of("completed",
                        historyPost.getBeIsCompleted() != null && historyPost.getBeIsCompleted() ? "완료" : "미완료",
                        historyPost.getAfIsCompleted() != null && historyPost.getAfIsCompleted() ? "완료" : "미완료"));
            }
        }

        return changes;
    }

    /**
     * 댓글 변경 내용 추출
     */
    private List<ChangeContent> extractCommentChanges(HistoryComment historyComment) {
        List<ChangeContent> changes = new ArrayList<>();

        if (historyComment.getChangeType() == ChangeType.DELETE) {
            // 삭제인 경우
            if (historyComment.getBeContent() != null) {
                changes.add(ChangeContent.ofDelete("content", historyComment.getBeContent()));
            }
        } else if (historyComment.getChangeType() == ChangeType.UPDATE) {
            // 수정인 경우
            if (!equals(historyComment.getBeContent(), historyComment.getAfContent())) {
                changes.add(ChangeContent.of("content", historyComment.getBeContent(), historyComment.getAfContent()));
            }
        }

        return changes;
    }

    /**
     * 파일 변경 내용 추출 (파일은 CREATE/DELETE만 있음)
     */
    private List<ChangeContent> extractFileChanges(HistoryFile historyFile) {
        List<ChangeContent> changes = new ArrayList<>();

        if (historyFile.getChangeType() == ChangeType.DELETE) {
            changes.add(ChangeContent.ofDelete("file", historyFile.getFileName()));
        } else if (historyFile.getChangeType() == ChangeType.CREATE) {
            changes.add(ChangeContent.ofCreate("file", historyFile.getFileName()));
        }

        return changes;
    }

    /**
     * 링크 변경 내용 추출 (링크는 CREATE/DELETE만 있음)
     */
    private List<ChangeContent> extractLinkChanges(HistoryLink historyLink) {
        List<ChangeContent> changes = new ArrayList<>();

        if (historyLink.getChangeType() == ChangeType.DELETE) {
            changes.add(ChangeContent.ofDelete("link", historyLink.getLinkUrl()));
        } else if (historyLink.getChangeType() == ChangeType.CREATE) {
            changes.add(ChangeContent.ofCreate("link", historyLink.getLinkUrl()));
        }

        return changes;
    }

    /**
     * null-safe 비교 헬퍼 메서드
     */
    private boolean equals(Object obj1, Object obj2) {
        if (obj1 == null && obj2 == null) {
            return true;
        }
        if (obj1 == null || obj2 == null) {
            return false;
        }
        return obj1.equals(obj2);
    }
}

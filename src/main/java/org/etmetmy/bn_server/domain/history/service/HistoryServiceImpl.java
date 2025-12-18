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

            allHistories.add(HistoryItemResponse.builder()
                    .title(title)
                    .targetType("POST")
                    .changeType(historyPost.getChangeType())
                    .changedAt(historyPost.getCreatedAt())
                    .changedByUserName(historyPost.getChangedByUser().getName())
                    .changeIp(historyPost.getChangeIp())
                    .details(detail)
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

            allHistories.add(HistoryItemResponse.builder()
                    .title(title)
                    .targetType("COMMENT")
                    .changeType(historyComment.getChangeType())
                    .changedAt(historyComment.getCreatedAt())
                    .changedByUserName(historyComment.getChangedByUser().getName())
                    .changeIp(historyComment.getChangeIp())
                    .details(detail)
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

            allHistories.add(HistoryItemResponse.builder()
                    .title(title)
                    .targetType("FILE")
                    .changeType(historyFile.getChangeType())
                    .changedAt(historyFile.getCreatedAt())
                    .changedByUserName(historyFile.getChangedByUser().getName())
                    .changeIp(historyFile.getChangeIp())
                    .details(detail)
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

            allHistories.add(HistoryItemResponse.builder()
                    .title(title)
                    .targetType("LINK")
                    .changeType(historyLink.getChangeType())
                    .changedAt(historyLink.getCreatedAt())
                    .changedByUserName(historyLink.getChangedByUser().getName())
                    .changeIp(historyLink.getChangeIp())
                    .details(detail)
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
}

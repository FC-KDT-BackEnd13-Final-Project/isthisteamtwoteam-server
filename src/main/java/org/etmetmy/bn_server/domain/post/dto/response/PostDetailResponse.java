package org.etmetmy.bn_server.domain.post.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.etmetmy.bn_server.domain.comment.dto.response.CommentResponse;
import org.etmetmy.bn_server.domain.comment.entity.Comment;
import org.etmetmy.bn_server.domain.file.dto.response.FileInfoDTO;
import org.etmetmy.bn_server.domain.link.dto.LinkInfoDTO;
import org.etmetmy.bn_server.domain.post.entity.Post;
import org.etmetmy.bn_server.domain.post.entity.Request;
import org.etmetmy.bn_server.domain.user.entity.User; // User Entity import

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Builder
public class PostDetailResponse {

    private final Long projectId;
    private final Long postId;
    private final Long parentPostId; // null or Id 번호
    private final Long userId;

    private final String title;
    private final String authorName;
    private final String authorIp;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final Boolean isCompleted;
    private final String stageName;

    private final String content;

    private final String approveStatus;
    private final String approverName;
    private final LocalDateTime approvedAt;
    private final String rejectionReason;

    private final List<FileInfoDTO> rejectionFiles;
    private final List<LinkInfoDTO> rejectionLinks;

    private final List<FileInfoDTO> files;
    private final List<LinkInfoDTO> links;
    private final List<CommentResponse> comments;

    private final Boolean canEdit;

    public static class Converter {
        public static PostDetailResponse fromEntity(Post post, User author, List<Comment> comments, Request request, Long loginUserId) {
            // 파일 변환 (삭제되지 않은 파일만)
            List<FileInfoDTO> fileInfos = FileInfoDTO.Converter.from(post.getFiles());

            // 링크 변환
            List<LinkInfoDTO> linkInfos = LinkInfoDTO.Converter.from(post.getLinks());

            // 거절(Request) 시 첨부된 파일/링크 추출
            List<FileInfoDTO> rejFiles = (request != null && request.getFiles() != null)
                    ? FileInfoDTO.Converter.from(request.getFiles()) : List.of();

            List<LinkInfoDTO> rejLinks = (request != null && request.getLinks() != null)
                    ? LinkInfoDTO.Converter.from(request.getLinks()) : List.of();

            // 댓글 변환 (계층 구조 포함)
            List<CommentResponse> commentCreateResponse = convertCommentsToHierarchy(comments, loginUserId);

            return PostDetailResponse.builder()
                    .projectId(post.getProject().getId())
                    .postId(post.getPostId())
                    .parentPostId(post.getParentPostId())
                    .userId(author.getId())
                    .authorName(author.getName())
                    .authorIp(post.getUserIp())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .createdAt(post.getCreatedAt())
                    .updatedAt(post.getUpdatedAt())
                    .isCompleted(post.getIsCompleted())
                    .stageName(post.getStage().getStageName())
                    .files(fileInfos)
                    .links(linkInfos)
                    .rejectionFiles(rejFiles)
                    .rejectionLinks(rejLinks)
                    .approveStatus(request != null && request.getApproveStatus() != null
                            ? request.getApproveStatus().getDescription()
                            : null)
                    .approverName(request != null && request.getApprover() != null ? request.getApprover().getName() : null)
                    .approvedAt(request != null ? request.getUpdatedAt() : null)
                    .rejectionReason(request != null ? request.getRejectReason() : null)
                    .comments(commentCreateResponse)
                    .canEdit(author.getId().equals(loginUserId))
                    .build();
        }

        /**
         * 댓글 리스트를 계층 구조로 변환 (수정된 로직)
         */
        private static List<CommentResponse> convertCommentsToHierarchy(List<Comment> comments, Long loginUserId) {
            // 1. 모든 댓글을 부모 ID 기준으로 그룹핑
            Map<Long, List<Comment>> groupedByParentId = comments.stream()
                    .filter(comment -> comment.getParent() != null)
                    .collect(Collectors.groupingBy(comment -> comment.getParent().getCommentId()));

            // 2. 최상위 댓글에 대해서만 재귀적으로 변환 시작
            return comments.stream()
                    .filter(comment -> comment.getParent() == null)
                    .map(rootComment -> toDtoRecursive(rootComment, groupedByParentId, loginUserId)) // ID 추가
                    .toList();
        }

        /**
         * 재귀적으로 댓글을 DTO로 변환하는 헬퍼 메소드
         */
        private static CommentResponse toDtoRecursive(Comment comment, Map<Long, List<Comment>> groupedByParentId, Long loginUserId) {
            // 현재 댓글의 파일 및 링크 변환
            List<FileInfoDTO> fileInfos = FileInfoDTO.Converter.from(comment.getFiles());
            List<LinkInfoDTO> linkInfos = LinkInfoDTO.Converter.from(comment.getLinks());

            // 현재 댓글의 자식 댓글들을 찾아서 재귀적으로 DTO 변환
            List<CommentResponse> replies = groupedByParentId
                    .getOrDefault(comment.getCommentId(), List.of()) // 현재 댓글을 부모로 가지는 자식들을 가져옴
                    .stream()
                    .map(child -> toDtoRecursive(child, groupedByParentId, loginUserId)) // 자식들에 대해 재귀 호출
                    .toList();

            // 현재 댓글 엔티티와 변환된 파일, 링크, 자식 DTO 리스트를 사용해 최종 DTO 생성
            return CommentResponse.Converter.from(comment, fileInfos, linkInfos, replies, loginUserId);
        }
    }
}
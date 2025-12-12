package org.etmetmy.bn_server.domain.post.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.etmetmy.bn_server.domain.comment.dto.response.CommentResponse;
import org.etmetmy.bn_server.domain.comment.entity.Comment;
import org.etmetmy.bn_server.domain.file.dto.response.FileInfoDTO;
import org.etmetmy.bn_server.domain.file.entity.File;
import org.etmetmy.bn_server.domain.link.dto.LinkInfoDTO;
import org.etmetmy.bn_server.domain.link.entity.Link;
import org.etmetmy.bn_server.domain.post.entity.Post;
import org.etmetmy.bn_server.domain.post.entity.Request;
import org.etmetmy.bn_server.domain.user.entity.User; // User Entity import

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PostDetailResponse {
  
    private final Long postId;
    private final Long parentPostId; // null or Id 번호
    private final Long userId;

    private final String title;
    private final String authorName;
    private final LocalDateTime createdAt;
    private final Boolean isCompleted;
    private final String stageName;

    private final String content;

    private final String approveStatus;
    private final String rejectionReason;

    private final List<FileInfoDTO> files;
    private final List<LinkInfoDTO> links;
    private final List<CommentResponse> comments;


    public static class Converter {
        public static PostDetailResponse fromEntity(Post post, User author, List<Comment> comments,Request request) {
            // 파일 변환 (삭제되지 않은 파일만)
            List<FileInfoDTO> fileInfos = FileInfoDTO.Converter.from(post.getFiles());

            // 링크 변환 (삭제되지 않은 링크만)
            List<LinkInfoDTO> linkInfos = LinkInfoDTO.Converter.from(post.getLinks());

            // 댓글 변환 (계층 구조 포함)
            List<CommentResponse> commentResponses = convertCommentsToHierarchy(comments);

            return PostDetailResponse.builder()
                    .parentPostId(post.getParentPostId())
                    .authorName(author.getName())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .createdAt(post.getCreatedAt())
                    .isCompleted(post.getIsCompleted())
                    .stageName(post.getStage().getStageName())
                    .files(fileInfos)
                    .links(linkInfos)
                    .approveStatus(request.getApproveStatus().getDescription())
                    .rejectionReason(request.getRejectReason())
                    .comments(commentResponses)
                    .build();
        }

        /**
         * 댓글 리스트를 계층 구조로 변환
         * 최상위 댓글만 반환하고, 대댓글은 replies에 포함
         */
        private static List<CommentResponse> convertCommentsToHierarchy(List<Comment> comments) {
            // commentId2가 null인 최상위 댓글만 필터링
            return comments.stream()
                    .filter(comment -> comment.getCommentId2() == null)
                    .map(rootComment -> {
                        // 해당 댓글의 대댓글 찾기
                        List<CommentResponse> replies = comments.stream()
                                .filter(comment -> rootComment.getCommentId().equals(comment.getCommentId2()))
                                .map(CommentResponse.Converter::from)
                                .toList();

                        // 최상위 댓글과 대댓글을 함께 반환
                        return CommentResponse.fromEntity(rootComment, replies);
                    })
                    .toList();
        }
    }
}
package org.etmetmy.bn_server.domain.request.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.etmetmy.bn_server.domain.file.dto.response.FileInfoDTO;
import org.etmetmy.bn_server.domain.post.entity.Post;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class PostApprovalRequestDto {

    @JsonProperty("post_id")
    private Long postId;

    @JsonProperty("post_title")
    private String postTitle;

    @JsonProperty("approval_status")
    private String approvalStatus;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("responded_at")
    private LocalDateTime respondedAt;

    @JsonProperty("responder")
    private String responder;

    @JsonProperty("rejection_reason")
    private String rejectionReason;

    @JsonProperty("stage_id")
    private Long stageId;

    @JsonProperty("stage_name")
    private String stageName;

    private List<String> links;
    private final List<FileInfoDTO> files;


    public static class Converter {
        public static List<PostApprovalRequestDto> from(List<Post> posts) {
            return posts.stream()
                    .map(post-> PostApprovalRequestDto.builder()
                            .postId(post.getPostId())
                            .postTitle(post.getTitle())
                            .approvalStatus(post.getRequest().getApproveStatus().getDescription())
                            .createdAt(post.getCreatedAt())
                            .respondedAt(post.getRequest() != null ? post.getRequest().getReplyTime() : null)
                            .responder(post.getRequest() != null && post.getRequest().getResponder() != null
                                    ? post.getRequest().getResponder().getName()
                                    : null)
                            .rejectionReason(post.getRequest() != null ? post.getRequest().getRejectReason() : null)
                            .stageId(post.getStage() != null ? post.getStage().getId() : null)
                            .stageName(post.getStage() != null ? post.getStage().getStageName() : null)
                            .build()
                    )
                    .toList();
        }
    }
}
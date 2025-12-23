package org.etmetmy.bn_server.domain.dashboard.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.etmetmy.bn_server.domain.post.entity.Post;
import org.etmetmy.bn_server.domain.post.entity.Request;
import org.etmetmy.bn_server.domain.post.entity.RequestStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApprovalRequestResponse {

    @JsonProperty("post_id")
    private Long postId;

    @JsonProperty("post_title")
    private String postTitle;

    @JsonProperty("")
    private LocalDateTime createdAt;
    private RequestStatus requestStatus;
    private String postStageName;

    private String projectName;

    private String companyName;

    private String approveStatus;

    public static class Converter {
        public static ApprovalRequestResponse from(Post post, Request request) {
            return ApprovalRequestResponse.builder()
                    .postId(post.getPostId())
                    .postTitle(post.getTitle())
                    .projectName(post.getProject().getProjectName())
                    .companyName(post.getProject().getCompany().getCompanyName())
                    .postStageName(post.getStage().getStageName())
                    .approveStatus(request != null && request.getApproveStatus() != null ? request.getApproveStatus().getDescription() : null)
                    .createdAt(post.getCreatedAt())
                    .requestStatus(post.getRequest().getApproveStatus())
                    .build();
        }

        public static List<ApprovalRequestResponse> from(List<Post> posts) {
            return posts.stream()
                    .map(post -> from(post, post.getRequest()))
                    .collect(Collectors.toList());
        }
    }
}

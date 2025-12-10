package org.etmetmy.bn_server.domain.dashboard.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.etmetmy.bn_server.domain.post.entity.Post;
import org.etmetmy.bn_server.domain.post.entity.RequestStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApprovalRequestResponse {

    // 게시글 관련
    private Long postId;                    // 게시글 id
    private String postTitle;               // 게시글 제목
    private LocalDateTime createdAt;        // 게시글 생성일자
    private RequestStatus requestStatus;    // 게시글 요청상태
    private String postStageName;           // 게시글 진행단계

    // 프로젝트 관련
    private String projectName;             // 프로젝트 이름

    // 회사 관련
    private String companyName;             // 회사 이름

    public static class Converter {
        public static ApprovalRequestResponse from(Post post) {
            return ApprovalRequestResponse.builder()
                    .postId(post.getPostId())
                    .postTitle(post.getTitle())
                    .projectName(post.getProject().getProjectName())
                    .companyName(post.getProject().getCompany().getCompanyName())
                    .postStageName(post.getStage().getStageType().getDescription())
                    .createdAt(post.getCreatedAt())
                    .requestStatus(post.getRequest().getApproveStatus())
                    .build();
        }

        public static List<ApprovalRequestResponse> from(List<Post> posts) {
            return posts.stream()
                    .map(Converter::from)
                    .collect(Collectors.toList());
        }
    }
}

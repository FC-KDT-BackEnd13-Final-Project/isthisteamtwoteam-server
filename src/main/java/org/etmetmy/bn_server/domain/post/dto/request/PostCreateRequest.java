package org.etmetmy.bn_server.domain.post.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.post.entity.Post;
import org.etmetmy.bn_server.domain.post.entity.Request;
import org.etmetmy.bn_server.domain.post.entity.RequestStatus;
import org.etmetmy.bn_server.domain.post.entity.Stage;
import org.etmetmy.bn_server.domain.project.entity.Project;
import org.etmetmy.bn_server.domain.user.entity.User;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostCreateRequest {

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 200, message = "제목은 200자를 초과할 수 없습니다.")
    private String title;

    @NotBlank(message = "본문은 필수입니다.")
    private String content;

    @NotNull(message = "단계는 필수입니다.")
    private String stageName;

    private Long parentId;

    // 승인요청 여부 (기본값: false)
    @Builder.Default
    private Boolean requestApproval = false;

    // S3에 업로드된 파일 정보 목록 (선택 사항)
    private List<Long> fileIds;

    // 링크 URL 목록 (선택 사항)
    private List<String> linkUrls;

    public static class Converter {
        public static Post toEntity(
                Project project, User user, Stage stage, Long postNumber, Post parent, PostCreateRequest requestDto) {

            return Post.builder()
                    .project(project)
                    .user(user)
                    .parentPostId(parent != null ? parent.getPostId() : null)
                    .title(requestDto.getTitle())
                    .content(requestDto.getContent())
                    .stage(stage)
                    .postNumber(postNumber)
                    .isCompleted(false)
                    .build();
        }

        /**
         * 승인요청이 있는 경우 Request 엔티티 생성 (초기 상태: PENDING)
         * @return Request 엔티티 또는 null (승인요청이 없는 경우)
         */
        public static Request toRequestEntity(
                PostCreateRequest requestDto, Post post, User user) {

            if (!Boolean.TRUE.equals(requestDto.getRequestApproval())) {
                return null;
            }

            return Request.builder()
                    .post(post)
                    .responder(user)
                    .approveStatus(RequestStatus.STATUS_PENDING)
                    .build();
        }
    }
}
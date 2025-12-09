package org.etmetmy.bn_server.domain.post.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.post.entity.Post;
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

    @NotNull(message = "단계 ID는 필수입니다.")
    private Long stageId;

    // S3에 업로드된 임시 파일 URL 목록 (선택 사항)
    private List<String> fileUrls;

    // 링크 URL 목록 (선택 사항)
    private List<String> linkUrls;

    public static class Converter {
        public static Post toEntity(
                Project project,
                User user,
                String title,
                String content,
                Stage stage,
                Long postNumber) {

            return Post.builder()
                    .project(project)
                    .user(user)
                    .parentPostId(null) //일반 게시글은 부모 없음
                    .title(title)
                    .content(content)
                    .stage(stage)
                    .postNumber(postNumber)
                    .isCompleted(false)
                    .build();
        }
        public static Post toReplyEntity(
                Project project,
                User user,
                Post post,
                String title,
                String content,
                Stage stage,
                Long postNumber) {

            return Post.builder()
                    .project(project)
                    .user(user)
                    .parentPostId(post.getPostId()) //일반 게시글은 부모 없음
                    .title(title)
                    .content(content)
                    .stage(stage)
                    .postNumber(postNumber)
                    .isCompleted(false)
                    .build();
        }
    }
}
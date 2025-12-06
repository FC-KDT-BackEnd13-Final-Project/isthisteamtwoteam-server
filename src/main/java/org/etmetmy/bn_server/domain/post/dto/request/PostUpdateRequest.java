package org.etmetmy.bn_server.domain.post.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.post.entity.Post;
import org.etmetmy.bn_server.domain.post.entity.Stage;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostUpdateRequest {

    @Size(max = 200, message = "제목은 200자를 초과할 수 없습니다.")
    private String title;

    private String content;

    private Long stageId;

    // S3에 업로드된 임시 파일 URL 목록 (선택 사항)
    private List<String> fileUrls;

    // 링크 URL 목록 (선택 사항)
    private List<String> linkUrls;


    public static class Converter {

        /**
         * Post 엔티티에 변경사항 적용
         * 파일과 링크 업데이트는 Service 레이어에서 처리
         */
        public static void applyTo(PostUpdateRequest dto, Post post, Stage stage) {
            if (dto.getTitle() != null) {
                post.updateTitle(dto.getTitle());
            }
            if (dto.getContent() != null) {
                post.updateContent(dto.getContent());
            }
            if (stage != null) {
                post.updateStage(stage);
            }
            // 파일과 링크 업데이트는 Service 레이어에서 별도 처리
        }
    }
}

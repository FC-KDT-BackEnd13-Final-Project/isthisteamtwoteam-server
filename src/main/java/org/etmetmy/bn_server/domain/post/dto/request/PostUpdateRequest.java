package org.etmetmy.bn_server.domain.post.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.post.entity.Post;
import org.etmetmy.bn_server.domain.post.entity.Request;
import org.etmetmy.bn_server.domain.post.entity.RequestStatus;
import org.etmetmy.bn_server.domain.post.entity.Stage;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostUpdateRequest {

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 200, message = "제목은 200자를 초과할 수 없습니다.")
    private String title;

    @NotBlank(message = "본문은 필수입니다.")
    private String content;

    @NotBlank(message = "단계는 필수입니다.")
    private String stage;

    private Long parentId;

    // 승인요청 여부
    private Boolean requestApproval;

    // 추가할 파일 ID 목록 (임시 파일 ID, 선택 사항)
    private List<Long> addFileIds;

    // 삭제할 파일 ID 목록 (기존 파일 ID, 선택 사항)
    private List<Long> removeFileIds;

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

        public static Request toRequestEntity(
                PostUpdateRequest requestDto, Post post, Long requestUserId) {

            if (!Boolean.TRUE.equals(requestDto.getRequestApproval())) {
                return null;
            }

            return Request.builder()
                    .post(post)
                    .requestUserId(requestUserId)
                    .approveStatus(RequestStatus.STATUS_PENDING)
                    .build();
        }
    }
}

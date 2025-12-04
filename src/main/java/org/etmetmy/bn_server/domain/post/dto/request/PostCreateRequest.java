package org.etmetmy.bn_server.domain.post.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

}

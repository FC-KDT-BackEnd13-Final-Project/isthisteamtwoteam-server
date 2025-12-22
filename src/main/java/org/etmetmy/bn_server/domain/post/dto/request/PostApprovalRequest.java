package org.etmetmy.bn_server.domain.post.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor

public class PostApprovalRequest {

    @JsonProperty("reject_reason")
    private String rejectReason;

    // S3에 업로드된 파일 정보 목록 (선택 사항)
    private List<Long> fileIds;

    // 링크 URL 목록 (선택 사항)
    private List<String> linkUrls;
}
package org.etmetmy.bn_server.domain.post.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor

public class PostApprovalRequest {

    @NotNull(message = "승인/거절 요청 사용자 ID는 필수입니다.")
    private Long approverId;

    private String rejectReason;
}
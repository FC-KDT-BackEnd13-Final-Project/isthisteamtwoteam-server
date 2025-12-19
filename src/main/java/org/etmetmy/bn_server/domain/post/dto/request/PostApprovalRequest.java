package org.etmetmy.bn_server.domain.post.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor

public class PostApprovalRequest {

    private String rejectReason;
}
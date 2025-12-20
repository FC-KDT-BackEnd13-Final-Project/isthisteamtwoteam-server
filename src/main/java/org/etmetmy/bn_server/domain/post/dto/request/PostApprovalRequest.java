package org.etmetmy.bn_server.domain.post.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor

public class PostApprovalRequest {

    @JsonProperty("reject_reason")
    private String rejectReason;
}
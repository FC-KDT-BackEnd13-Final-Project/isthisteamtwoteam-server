package org.etmetmy.bn_server.domain.project.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateStageRequest {
    private String stageName;
}
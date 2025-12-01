package org.etmetmy.bn_server.domain.memo.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemoUpdateRequestDto {
    private String content;
}

package org.etmetmy.bn_server.domain.checkList.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.checkList.entity.CheckList;

@Getter
@NoArgsConstructor
public class CheckListCreateRequest {
    // 내부 Converter
    public static class Converter{

        public static CheckList toEntity() {
            return CheckList.builder()
                    .content(null)
                    .build();
        }
    }
}

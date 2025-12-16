package org.etmetmy.bn_server.domain.checkList.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.checkList.entity.CheckList;

import java.util.List;

@Getter
@NoArgsConstructor
public class CheckListCreateRequest {
    private String content;

    // 내부 Converter
    public static class Converter{

        public static CheckList toEntity(CheckListCreateRequest request) {
            return CheckList.builder()
                    .content(request.getContent())
                    .build();
        }
    }
}

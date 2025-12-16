package org.etmetmy.bn_server.domain.checkList.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.checkList.entity.CheckList;

import java.util.List;

@Getter
@NoArgsConstructor
public class CheckListCreateRequest {
    private String content;

    // S3에 업로드된 파일 정보 목록
    private List<Long> fileIds;

    // 링크 URL 목록
    private List<String> linkUrls;

    // 내부 Converter
    public static class Converter{

        public static CheckList toEntity(CheckListCreateRequest request) {
            return CheckList.builder()
                    .content(request.getContent())
                    .build();
        }
    }
}

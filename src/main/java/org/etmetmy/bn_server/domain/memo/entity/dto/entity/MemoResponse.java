package org.etmetmy.bn_server.domain.memo.entity.dto.entity;

import lombok.Builder;
import lombok.Getter;
import org.etmetmy.bn_server.domain.memo.entity.Memo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class MemoResponse {

    private Long memoId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static class Converter {

        public static MemoResponse from(Memo memo) {
            return MemoResponse.builder()
                    .memoId(memo.getMemoId())
                    .content(memo.getContent())
                    .createdAt(memo.getCreatedAt())
                    .updatedAt(memo.getUpdatedAt())
                    .build();
        }

        public static List<MemoResponse> from(List<Memo> memos) {
            return memos.stream()
                    .map(Converter::from)
                    .collect(Collectors.toList());
        }
    }
}

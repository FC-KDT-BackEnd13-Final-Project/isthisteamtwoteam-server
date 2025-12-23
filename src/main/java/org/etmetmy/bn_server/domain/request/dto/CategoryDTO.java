package org.etmetmy.bn_server.domain.request.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
public class CategoryDTO {

    @JsonProperty("stage_id")
    private Long stageId;

    @JsonProperty("category_name")
    private String categoryName;

    @JsonProperty("total_cnt")
    private Long totalCnt;

    private List<PostApprovalRequestDto> requests;

    public static class Converter {
        public static CategoryDTO of(Long stageId, String categoryName, List<PostApprovalRequestDto> posts) {
            return CategoryDTO.builder()
                    .stageId(stageId)
                    .categoryName(categoryName)
                    .totalCnt((long)posts.size())
                    .requests(posts)
                    .build();
        }

        public static List<CategoryDTO> fromAll(List<PostApprovalRequestDto> posts) {
            // Stage 별로 그룹화
            Map<Long, List<PostApprovalRequestDto>> groupedByStageId = posts.stream()
                    .collect(Collectors.groupingBy(PostApprovalRequestDto::getStageId));

            // StageId 순서로 정렬하여 CategoryDTO 생성
            return groupedByStageId.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(entry -> {
                        Long stageId = entry.getKey();
                        List<PostApprovalRequestDto> stagePosts = entry.getValue();
                        String stageName = stagePosts.isEmpty() ? null : stagePosts.get(0).getStageName();
                        return of(stageId, stageName, stagePosts);
                    })
                    .toList();
        }
    }
}

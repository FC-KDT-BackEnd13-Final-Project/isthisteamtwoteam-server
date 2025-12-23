package org.etmetmy.bn_server.domain.request.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ApprovalNotiResponse {

    private SummaryDTO summary;
    private List<CategoryDTO> categories;

    public static class Converter {
        public static ApprovalNotiResponse of(SummaryDTO summary, List<CategoryDTO> categories) {
            return ApprovalNotiResponse.builder()
                    .summary(summary)
                    .categories(categories)
                    .build();
        }
    }
}

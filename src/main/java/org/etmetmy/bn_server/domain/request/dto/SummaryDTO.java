package org.etmetmy.bn_server.domain.request.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.etmetmy.bn_server.domain.post.entity.RequestStatus;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class SummaryDTO {

    @JsonProperty("pending_cnt")
    private Long pendingCnt;

    @JsonProperty("approved_cnt")
    private Long approvedCnt;

    @JsonProperty("rejected_cnt")
    private Long rejectedCnt;

    public static class Converter {
        public static SummaryDTO from(List<Object[]> statusCounts) {
            Long pendingCnt = 0L;
            Long approvedCnt = 0L;
            Long rejectedCnt = 0L;

            for (Object[] row : statusCounts) {
                RequestStatus status = (RequestStatus) row[0];
                Long count = (Long) row[1];

                switch (status) {
                    case STATUS_PENDING -> pendingCnt = count;
                    case STATUS_APPROVED -> approvedCnt = count;
                    case STATUS_REJECTED -> rejectedCnt = count;
                }
            }
            return SummaryDTO.builder()
                    .pendingCnt(pendingCnt)
                    .approvedCnt(approvedCnt)
                    .rejectedCnt(rejectedCnt)
                    .build();
        }
    }

}

package org.etmetmy.bn_server.domain.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class DashBoardResponse {

    private Stats stats;
    private List<DashBoardStatusResponseDTO> pendingList;
    private List<DashBoardStatusResponseDTO> rejectedList;
    private List<DashBoardStatusResponseDTO> inProgressList;
    private List<DashBoardStatusResponseDTO> maintenanceList;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Stats {
        private int pendingApproval;
        private int rejected;
        private int inProgress;
        private int maintenance;
    }

    public static class Converter{
        public static DashBoardResponse of(List<DashBoardStatusResponseDTO> pendingList,
                                           List<DashBoardStatusResponseDTO> rejectedList,
                                           List<DashBoardStatusResponseDTO> inProgressList,
                                           List<DashBoardStatusResponseDTO> maintenanceList) {
            Stats stats = Stats.builder()
                    .pendingApproval(pendingList.size())
                    .rejected(rejectedList.size())
                    .inProgress(inProgressList.size())
                    .maintenance(maintenanceList.size())
                    .build();

            return DashBoardResponse.builder()
                    .stats(stats)
                    .pendingList(pendingList)
                    .rejectedList(rejectedList)
                    .inProgressList(inProgressList)
                    .maintenanceList(maintenanceList)
                    .build();
        }
    }
}
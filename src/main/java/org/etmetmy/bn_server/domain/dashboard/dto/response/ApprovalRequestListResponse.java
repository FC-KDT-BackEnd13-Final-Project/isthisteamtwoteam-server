package org.etmetmy.bn_server.domain.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ApprovalRequestListResponse {

    private StatusCount statusCount;
    private StageCount stageCount;
    private List<ApprovalRequestResponse> requirements;
    private List<ApprovalRequestResponse> screenDesign;
    private List<ApprovalRequestResponse> designPublishing;
    private List<ApprovalRequestResponse> development;
    private List<ApprovalRequestResponse> qa;
    private List<ApprovalRequestResponse> maintenance;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class StatusCount {
        private int pending;
        private int approved;
        private int rejected;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class StageCount {
        private int requirements;
        private int screenDesign;
        private int designPublishing;
        private int development;
        private int qa;
        private int maintenance;
    }

    public static class Converter {
        public static ApprovalRequestListResponse of(List<ApprovalRequestResponse> allPosts) {

            // 상태별 카운팅
            int pendingCount = (int) allPosts.stream()
                    .filter(post -> post.getRequestStatus() == org.etmetmy.bn_server.domain.post.entity.RequestStatus.STATUS_PENDING)
                    .count();
            int approvedCount = (int) allPosts.stream()
                    .filter(post -> post.getRequestStatus() == org.etmetmy.bn_server.domain.post.entity.RequestStatus.STATUS_APPROVED)
                    .count();
            int rejectedCount = (int) allPosts.stream()
                    .filter(post -> post.getRequestStatus() == org.etmetmy.bn_server.domain.post.entity.RequestStatus.STATUS_REJECTED)
                    .count();

            StatusCount statusCount = StatusCount.builder()
                    .pending(pendingCount)
                    .approved(approvedCount)
                    .rejected(rejectedCount)
                    .build();

            // 단계별 필터링 및 카운팅
            List<ApprovalRequestResponse> requirements = allPosts.stream()
                    .filter(post -> "요구사항 정의".equals(post.getPostStageName()))
                    .toList();

            List<ApprovalRequestResponse> screenDesign = allPosts.stream()
                    .filter(post -> "화면 설계".equals(post.getPostStageName()))
                    .toList();

            List<ApprovalRequestResponse> designPublishing = allPosts.stream()
                    .filter(post -> "디자인, 퍼블리싱".equals(post.getPostStageName()))
                    .toList();

            List<ApprovalRequestResponse> development = allPosts.stream()
                    .filter(post -> "개발".equals(post.getPostStageName()))
                    .toList();

            List<ApprovalRequestResponse> qa = allPosts.stream()
                    .filter(post -> "검수".equals(post.getPostStageName()))
                    .toList();

            List<ApprovalRequestResponse> maintenance = allPosts.stream()
                    .filter(post -> "유지보수".equals(post.getPostStageName()))
                    .toList();

            StageCount stageCount = StageCount.builder()
                    .requirements(requirements.size())
                    .screenDesign(screenDesign.size())
                    .designPublishing(designPublishing.size())
                    .development(development.size())
                    .qa(qa.size())
                    .maintenance(maintenance.size())
                    .build();

            return ApprovalRequestListResponse.builder()
                    .statusCount(statusCount)
                    .stageCount(stageCount)
                    .requirements(requirements)
                    .screenDesign(screenDesign)
                    .designPublishing(designPublishing)
                    .development(development)
                    .qa(qa)
                    .maintenance(maintenance)
                    .build();
        }
    }
}


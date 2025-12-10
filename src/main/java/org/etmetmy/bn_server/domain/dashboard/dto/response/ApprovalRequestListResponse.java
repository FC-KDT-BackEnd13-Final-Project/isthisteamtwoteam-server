package org.etmetmy.bn_server.domain.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.etmetmy.bn_server.domain.post.entity.RequestStatus;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    /*    public static class Converter {
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
        }*/
    public static class Converter {
        public static ApprovalRequestListResponse of(List<ApprovalRequestResponse> allPosts) {
            StatusCount statusCount = buildStatusCount(allPosts);

            Map<String, List<ApprovalRequestResponse>> postsByStage = groupPostsByStage(allPosts);
            StageCount stageCount = buildStageCount(postsByStage);

            return ApprovalRequestListResponse.builder()
                    .statusCount(statusCount)
                    .stageCount(stageCount)
                    .requirements(postsByStage.get("요구사항 정의"))
                    .screenDesign(postsByStage.get("화면 설계"))
                    .designPublishing(postsByStage.get("디자인, 퍼블리싱"))
                    .development(postsByStage.get("개발"))
                    .qa(postsByStage.get("검수"))
                    .maintenance(postsByStage.get("유지보수"))
                    .build();
        }

        //    pending, approved, rejected 카운트
        private static StatusCount buildStatusCount(List<ApprovalRequestResponse> posts) {
            Map<RequestStatus, Long> statusCounts = posts.stream()
                    .collect(Collectors.groupingBy(
                            ApprovalRequestResponse::getRequestStatus,
                            Collectors.counting()
                    ));

            return StatusCount.builder()
                    .pending(statusCounts.getOrDefault(RequestStatus.STATUS_PENDING, 0L).intValue())
                    .approved(statusCounts.getOrDefault(RequestStatus.STATUS_APPROVED, 0L).intValue())
                    .rejected(statusCounts.getOrDefault(RequestStatus.STATUS_REJECTED, 0L).intValue())
                    .build();
        }

        // 각 단계별 게시글 갯수 세기
        private static StageCount buildStageCount(
                Map<String, List<ApprovalRequestResponse>> postsByStage) {
            return StageCount.builder()
                    .requirements(postsByStage.getOrDefault("요구사항 정의", List.of()).size())
                    .screenDesign(postsByStage.getOrDefault("화면 설계", List.of()).size())
                    .designPublishing(postsByStage.getOrDefault("디자인, 퍼블리싱", List.of()).size())
                    .development(postsByStage.getOrDefault("개발", List.of()).size())
                    .qa(postsByStage.getOrDefault("검수", List.of()).size())
                    .maintenance(postsByStage.getOrDefault("유지보수", List.of()).size())
                    .build();
        }

        // 같은 단계 게시글 그룹핑
        private static Map<String, List<ApprovalRequestResponse>> groupPostsByStage(
                List<ApprovalRequestResponse> posts) {
            return posts.stream()
                    .collect(Collectors.groupingBy(ApprovalRequestResponse::getPostStageName));
        }
    }
}
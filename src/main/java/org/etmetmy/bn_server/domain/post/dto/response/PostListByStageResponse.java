package org.etmetmy.bn_server.domain.post.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.etmetmy.bn_server.domain.file.dto.response.FileListResponse;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
public class PostListByStageResponse {

    private StageCount stageCount;
    private List<PostListResponse> requirements;
    private List<PostListResponse> screenDesign;
    private List<PostListResponse> designPublishing;
    private List<PostListResponse> development;
    private List<PostListResponse> qa;
    private List<PostListResponse> maintenance;

    private List<FileListResponse> files;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class StageCount {
        private int requirementsCnt;
        private int screenDesignCnt;
        private int designPublishingCnt;
        private int developmentCnt;
        private int qaCnt;
        private int maintenanceCnt;
        private int filesCnt;
    }

    public static class Converter {
        public static PostListByStageResponse of(List<PostListResponse> allPosts, List<FileListResponse> files) {
            Map<String, List<PostListResponse>> postsByStage = groupPostsByStage(allPosts);
            StageCount stageCount = buildStageCount(postsByStage, files);

            return PostListByStageResponse.builder()
                    .stageCount(stageCount)
                    .requirements(postsByStage.getOrDefault("요구사항 정의", List.of()))
                    .screenDesign(postsByStage.getOrDefault("화면 설계", List.of()))
                    .designPublishing(postsByStage.getOrDefault("디자인, 퍼블리싱", List.of()))
                    .development(postsByStage.getOrDefault("개발", List.of()))
                    .qa(postsByStage.getOrDefault("검수", List.of()))
                    .maintenance(postsByStage.getOrDefault("유지보수", List.of()))
                    .files(files)
                    .build();
        }

        // 각 단계별 게시글 갯수 및 파일 갯수 세기
        private static StageCount buildStageCount(Map<String, List<PostListResponse>> postsByStage, List<FileListResponse> files) {
            return StageCount.builder()
                    .requirementsCnt(postsByStage.getOrDefault("요구사항 정의", List.of()).size())
                    .screenDesignCnt(postsByStage.getOrDefault("화면 설계", List.of()).size())
                    .designPublishingCnt(postsByStage.getOrDefault("디자인, 퍼블리싱", List.of()).size())
                    .developmentCnt(postsByStage.getOrDefault("개발", List.of()).size())
                    .qaCnt(postsByStage.getOrDefault("검수", List.of()).size())
                    .maintenanceCnt(postsByStage.getOrDefault("유지보수", List.of()).size())
                    .filesCnt(files.size())
                    .build();
        }

        // 같은 단계 게시글 그룹핑
        private static Map<String, List<PostListResponse>> groupPostsByStage(List<PostListResponse> posts) {
            return posts.stream()
                    .collect(Collectors.groupingBy(PostListResponse::getStageName));
        }
    }
}

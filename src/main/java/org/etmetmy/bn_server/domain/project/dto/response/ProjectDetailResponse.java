package org.etmetmy.bn_server.domain.project.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.memo.entity.Memo;
import org.etmetmy.bn_server.domain.project.entity.Project;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectDetailResponse {

    @JsonProperty("projectId")
    private Long projectId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("stage")
    private String stage;

    @JsonProperty("coverImage")
    private String coverImage;

    private Long projectMemoId;
    private String projectMemoContent;

    private Long userMemoId;
    private String userMemoContent;

    private LocalDate startDate;
    private LocalDate endDate;

    private String companyName;

    public static class Converter {
        public static ProjectDetailResponse from(Project project, Memo projectMemo, Memo userMemo) {
            return ProjectDetailResponse.builder()
                    .projectId(project.getId())
                    .name(project.getProjectName())
                    .stage(project.getStage() != null ? project.getStage().getStageName() : null)
                    .coverImage(project.getProjectImageUrl()) // TODO: 커버 이미지 구현 필요
                    .projectMemoId(projectMemo.getMemoId())
                    .projectMemoContent(projectMemo.getContent())
                    .userMemoId(userMemo.getMemoId())
                    .userMemoContent(userMemo.getContent())
                    .startDate(project.getStartDate())
                    .endDate(project.getEndDate())
                    .companyName(project.getCompany().getCompanyName())
                    .build();
        }
    }
}
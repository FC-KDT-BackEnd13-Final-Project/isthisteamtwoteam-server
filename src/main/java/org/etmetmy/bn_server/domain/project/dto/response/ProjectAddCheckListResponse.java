package org.etmetmy.bn_server.domain.project.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.etmetmy.bn_server.domain.project.entity.ProjectCheckList;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class ProjectAddCheckListResponse {
    private Long id;
    private Long projectId;
    private Long checklistId;
    private String checklistContent;
    private Long answererId;
    private Boolean checked;

    public static class Converter {

        public static ProjectAddCheckListResponse from(ProjectCheckList projectCheckList) {
            return ProjectAddCheckListResponse.builder()
                    .id(projectCheckList.getProjectCheckListId())
                    .projectId(projectCheckList.getProject().getId())
                    .checklistId(projectCheckList.getCheckList().getCheckListId())
                    .checklistContent(projectCheckList.getCheckList().getContent())
                    .answererId(projectCheckList.getAnswererId() != null ? projectCheckList.getAnswererId().getId() : null)
                    .checked(projectCheckList.getChecked())
                    .build();
        }

        public static List<ProjectAddCheckListResponse> from(List<ProjectCheckList> projectCheckLists) {
            return projectCheckLists.stream()
                    .map(Converter::from)
                    .collect(Collectors.toList());
        }
    }
}

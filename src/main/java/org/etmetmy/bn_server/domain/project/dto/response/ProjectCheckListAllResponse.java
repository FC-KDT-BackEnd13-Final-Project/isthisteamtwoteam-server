package org.etmetmy.bn_server.domain.project.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.etmetmy.bn_server.domain.project.entity.ProjectCheckList;

import java.util.List;

@Getter
@Builder
public class ProjectCheckListAllResponse {
    private Long id;
    private Long projectId;
    private Long checkListId;
    private String checkListContent;
    private boolean checked;

    public static class Converter{
        public static ProjectCheckListAllResponse from(ProjectCheckList projectCheckList) {
         return ProjectCheckListAllResponse.builder()
                 .id(projectCheckList.getProjectCheckListId())
                 .projectId(projectCheckList.getProject().getId())
                 .checkListId(projectCheckList.getCheckList().getCheckListId())
                 .checkListContent(projectCheckList.getCheckList().getContent())
                 .checked(projectCheckList.getChecked())
                 .build();
        }

        public static List<ProjectCheckListAllResponse> from(List<ProjectCheckList> projectCheckLists) {
            return projectCheckLists.stream()
                    .map(Converter::from)
                    .toList();
        }
    }
}

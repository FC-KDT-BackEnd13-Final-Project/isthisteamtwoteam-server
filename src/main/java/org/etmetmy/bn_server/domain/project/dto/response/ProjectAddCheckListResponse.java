package org.etmetmy.bn_server.domain.project.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.etmetmy.bn_server.domain.checkList.entity.CheckList;
import org.etmetmy.bn_server.domain.project.entity.ProjectCheckList;

import java.util.List;
import java.util.Map;
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

        public static ProjectAddCheckListResponse from(ProjectCheckList projectCheckList, CheckList checkList) {
            return ProjectAddCheckListResponse.builder()
                    .id(projectCheckList.getProjectCheckListId())
                    .projectId(projectCheckList.getProject().getId())
                    .checklistId(projectCheckList.getCheckListId())
                    .checklistContent(checkList.getContent())
                    .answererId(projectCheckList.getAnswererId().getId())
                    .checked(projectCheckList.getChecked())
                    .build();
        }

        public static List<ProjectAddCheckListResponse> from(List<ProjectCheckList> projectCheckLists, Map<Long, CheckList> checkListMap) {
            return projectCheckLists.stream()
                    .map(projectCheckList -> {
                        CheckList checkList = checkListMap.get(projectCheckList.getCheckListId());
                        return from(projectCheckList, checkList);
                    })
                    .collect(Collectors.toList());
        }
    }
}

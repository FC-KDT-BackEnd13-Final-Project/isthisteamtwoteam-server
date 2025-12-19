package org.etmetmy.bn_server.domain.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.checkList.entity.CheckList;
import org.etmetmy.bn_server.domain.project.entity.ProjectCheckList;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectCreateCheckListResponse {
    private Long projectCheckListId;
    private Long checkListId;
    private String content;
    private Boolean checked;
    private Long answererId;
    public static class Converter {
        public static ProjectCreateCheckListResponse from(
                ProjectCheckList projectCheckList,
                CheckList checkList) {
            return ProjectCreateCheckListResponse.builder()
                    .projectCheckListId(projectCheckList.getProjectCheckListId())
                    .checkListId(checkList.getCheckListId())
                    .content(checkList.getContent())
                    .checked(projectCheckList.getChecked())
                    .answererId(projectCheckList.getAnswererId() != null ? projectCheckList.getAnswererId().getId() : null)
                    .build();
        }
    }
}

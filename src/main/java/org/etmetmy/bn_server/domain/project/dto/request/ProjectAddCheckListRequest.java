package org.etmetmy.bn_server.domain.project.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.checkList.entity.CheckList;
import org.etmetmy.bn_server.domain.project.entity.Project;
import org.etmetmy.bn_server.domain.project.entity.ProjectCheckList;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectAddCheckListRequest {

    private List<Long>  checklistIds;

    public static class Converter{

        public static ProjectCheckList toEntity(Project project, Long checkListId) {
            return ProjectCheckList.builder()
                    .project(project)
                    .checkListId(checkListId)
                    .answererId(null)
                    .checked(false)
                    .build();
        }
    }
}

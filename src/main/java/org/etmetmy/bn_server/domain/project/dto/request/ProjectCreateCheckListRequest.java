package org.etmetmy.bn_server.domain.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.checkList.entity.CheckList;
import org.etmetmy.bn_server.domain.project.entity.Project;
import org.etmetmy.bn_server.domain.project.entity.ProjectCheckList;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectCreateCheckListRequest {
    @NotBlank(message = "체크리스트 이름은 필수입니다.")
    private String content;

    public static class Converter {
        public static CheckList toEntity(ProjectCreateCheckListRequest request) {
            return CheckList.builder()
                    .content(request.getContent())
                    .build();
        }

        // ProjectCheckList 엔티티로 변환
        public static ProjectCheckList toProjectCheckListEntity(Project project, Long checkListId) {
            return ProjectCheckList.builder()
                    .project(project)
                    .checkListId(checkListId)
                    .answererId(null)
                    .checked(false)
                    .build();
        }
    }
}

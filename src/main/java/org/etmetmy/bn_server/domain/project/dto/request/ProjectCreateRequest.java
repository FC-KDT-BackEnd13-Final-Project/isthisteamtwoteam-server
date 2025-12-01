package org.etmetmy.bn_server.domain.project.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.etmetmy.bn_server.domain.company.entity.Company;
import org.etmetmy.bn_server.domain.memo.entity.Memo;
import org.etmetmy.bn_server.domain.post.entity.Stage;
import org.etmetmy.bn_server.domain.project.entity.Project;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectCreateRequest {
    private String projectName;
    private String startDate;
    private String endDate;
    private List<ProjectMemberRequest> members;
    private List<Integer> selectedChecklistIds;
    private Long companyId;
    private String memo;
    private String stageName;

    //내부 converter
    public static class Converter{

        public static Project toEntity(ProjectCreateRequest request, Long createdBy, Stage stage, Memo memo){
            return Project.builder()
                    .projectName(request.getProjectName())
                    .startDate(parseDate(request.getStartDate()))
                    .endDate(parseDate(request.getEndDate()))
                    .companyEntity(Company.builder().companyId(request.getCompanyId()).build())
                    .stage(stage)
                    .memo(memo)
                    .createdBy(createdBy)
                    .build();
        }

        private  static LocalDate parseDate(String date){
            if (date == null || date.isBlank()){
                return null;
            }
            return LocalDate.parse(date);
        }


    }


}

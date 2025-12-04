package org.etmetmy.bn_server.domain.project.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.company.entity.Company;
import org.etmetmy.bn_server.domain.post.entity.Stage;
import org.etmetmy.bn_server.domain.project.entity.Project;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectCreateRequest {
    private String projectName;
    private String startDate;
    private String endDate;

    @JsonProperty("members")
    //@JsonDeserialize(using = MemberListDeserializer.class)
    private List<Long> members;
    private List<Integer> selectedChecklistIds;
    private Long companyId;
    private String memo;
    private String stage;

    //내부 converter
    public static class Converter{

        public static Project toEntity(ProjectCreateRequest request, Long createdBy, Stage startStage) {
            return Project.builder()
                    .projectName(request.getProjectName())
                    .startDate(parseDate(request.getStartDate()))
                    .endDate(parseDate(request.getEndDate()))
                    .company(isValidCompanyId(request.getCompanyId())
                            ? Company.builder().companyId(request.getCompanyId()).build()
                            : null)
                    .stage(startStage)
                    .createdBy(createdBy)
                    .build();
        }

        private static boolean isValidCompanyId(Long companyId) {
            return companyId != null && companyId > 0;
        }

        private static LocalDate parseDate(String date) {
            if (date == null || date.isBlank()) {
                return null;
            }
            return LocalDate.parse(date);
        }
    }
}

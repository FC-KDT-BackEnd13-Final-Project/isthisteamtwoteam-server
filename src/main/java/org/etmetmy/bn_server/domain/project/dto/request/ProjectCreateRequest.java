package org.etmetmy.bn_server.domain.project.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.company.entity.Company;
import org.etmetmy.bn_server.domain.post.entity.Stage;
import org.etmetmy.bn_server.domain.project.entity.Project;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectCreateRequest {
    private String projectName;
    private String projectImageUrl; // URL만 저장(파일 업로드 후)

    private String startDate;
    private String endDate;
    private Long createdBy;

    private List<Long> members;
    private List<Integer> selectedChecklistIds;

    private Long companyId;
    private String memo;
    private String stage;

    private Boolean isDeleted;

    //내부 converter
    public static class Converter {
        public static Project toEntity(ProjectCreateRequest request, Company company, Long loginUserId, Stage startStage) {
            return Project.builder()
                    .projectName(request.getProjectName())
                    .startDate(parseDate(request.getStartDate()))
                    .endDate(parseDate(request.getEndDate()))
                    .company(company)
                    .isDeleted(false)
                    .stage(startStage)
                    .createdBy(loginUserId)
                    .build();
        }

        private static LocalDate parseDate(String date) {
            if (date == null || date.isBlank()) {
                return null;
            }
            return LocalDate.parse(date);
        }
    }
}

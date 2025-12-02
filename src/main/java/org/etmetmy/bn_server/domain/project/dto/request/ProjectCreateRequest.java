package org.etmetmy.bn_server.domain.project.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.etmetmy.bn_server.domain.company.entity.Company;
import org.etmetmy.bn_server.domain.post.entity.Stage;
import org.etmetmy.bn_server.domain.project.entity.Project;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
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

//    public static class MemberListDeserializer extends JsonDeserializer<List<Long>> {
//        @Override
//        public List<Long> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
//            JsonNode node = p.getCodec().readTree(p);
//            List<Long> userIds = new ArrayList<>();
//
//            if (node.isArray()) {
//                for (JsonNode element : node) {
//                    if (element.isNumber()) {
//                        userIds.add(element.asLong());
//                    }else if (element.isObject() && element.has("userId")) {
//                        userIds.add(element.get("userId").asLong());
//                }
//            }
//            return userIds;
//
//        }
//    }

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

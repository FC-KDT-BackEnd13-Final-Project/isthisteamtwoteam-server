package org.etmetmy.bn_server.domain.project.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectDateUpdateRequest {
    private String startDate;  // JSON: "startDate"
    private String endDate;    // JSON: "endDate"

    public static class Converter {
        public static ProjectDateUpdateRequest from(String startDate, String endDate) {
            return new ProjectDateUpdateRequest(startDate, endDate);
        }

        public static ProjectDateUpdateRequest from(LocalDate startDate, LocalDate endDate) {
            return new ProjectDateUpdateRequest(
                    startDate.toString(),
                    endDate.toString()
            );
        }
    }
}

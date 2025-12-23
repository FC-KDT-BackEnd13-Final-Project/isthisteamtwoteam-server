package org.etmetmy.bn_server.domain.project.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectUpdateRequest {
    // 프로젝트 관련
    private String projectName;
    private String startDate;
    private String endDate;
    private String stageName;

    @JsonIgnore //swagger 에서 입력 무시용
    private String projectImageUrl; // URL만 저장(파일 업로드 후)

    // 프로젝트 멤버
    private List<Long> members;

    // 회사 관련
    private Long companyId;

    // 메모 관련
    private String memoContent;

    // 프로젝트 체크리스트 관련
    private List<Integer> selectedChecklistIds;
}

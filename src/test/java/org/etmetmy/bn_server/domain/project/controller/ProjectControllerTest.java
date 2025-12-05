package org.etmetmy.bn_server.domain.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.etmetmy.bn_server.config.SecurityConfig;
import org.etmetmy.bn_server.domain.project.dto.request.ProjectCreateRequest;
import org.etmetmy.bn_server.domain.project.dto.request.ProjectDateUpdateRequest;
import org.etmetmy.bn_server.domain.project.dto.request.ProjectMemberRequest;
import org.etmetmy.bn_server.domain.project.dto.request.ProjectStageUpdateRequest;
import org.etmetmy.bn_server.domain.project.dto.response.ProjectMemberResponse;
import org.etmetmy.bn_server.domain.project.dto.response.ProjectResponse;
import org.etmetmy.bn_server.domain.project.dto.response.ProjectStageUpdateResponse;
import org.etmetmy.bn_server.domain.project.dto.response.ProjectTrashResponse;
import org.etmetmy.bn_server.domain.project.service.ProjectService;
import org.etmetmy.bn_server.exception.custom.InvalidInputException;
import org.etmetmy.bn_server.exception.handler.GlobalExceptionHandler;
import org.etmetmy.bn_server.global.ExceptionControllerAdvice;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, ExceptionControllerAdvice.class})
class ProjectControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ProjectService projectService;

    @Test
    @DisplayName("프로젝트 생성 후 ID 반환")
    void createProject_returnsId() throws Exception {
        Long userId = 50L;
        ProjectCreateRequest request = new ProjectCreateRequest(
                "신규 프로젝트",
                "2025-01-01",
                "2025-02-01",
                List.of(1L, 2L),
                List.of(),
                10L,
                "메모",
                "개발"
        );

        when(projectService.createProject(any(ProjectCreateRequest.class), eq(userId))).thenReturn(100L);
        when(projectService.getProjectById(100L)).thenReturn(ProjectResponse.builder()
                .projectId(100L)
                .projectName("신규 프로젝트")
                .build());

        mockMvc.perform(post("/admin/projects")
                        .sessionAttr("userId", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("프로젝트 생성 성공"))
                .andExpect(jsonPath("$.response").value(100));
    }

    @Test
    @DisplayName("프로젝트 목록 조회 성공")
    void getProjects_returnsList() throws Exception {
        List<ProjectResponse> responses = List.of(
                ProjectResponse.builder().projectId(1L).projectName("A 프로젝트").build(),
                ProjectResponse.builder().projectId(2L).projectName("B 프로젝트").build()
        );

        when(projectService.getAllProjects()).thenReturn(responses);

        mockMvc.perform(get("/admin/projects"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("프로젝트 목록조회 성공"))
                .andExpect(jsonPath("$.response.length()").value(2))
                .andExpect(jsonPath("$.response[0].projectName").value("A 프로젝트"));
    }

    @Test
    @DisplayName("프로젝트 멤버 조회 성공")
    void getProjectMembers_returnsMembers() throws Exception {
        List<ProjectMemberResponse> members = List.of(
                ProjectMemberResponse.builder().projectMemberId(1L).userId(10L).userName("홍길동").build(),
                ProjectMemberResponse.builder().projectMemberId(2L).userId(11L).userName("임꺽정").build()
        );

        when(projectService.getProjectMembers(5L)).thenReturn(members);

        mockMvc.perform(get("/admin/projects/{projectId}/members", 5L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("프로젝트 멤버 조회 성공"))
                .andExpect(jsonPath("$.response.length()").value(2))
                .andExpect(jsonPath("$.response[1].userName").value("임꺽정"));
    }

    @Test
    @DisplayName("프로젝트 진행단계 수정 성공")
    void updateProjectStage_returnsResponse() throws Exception {
        Long projectId = 7L;
        Long userId = 3L;
        Long stageId = 4L;
        ProjectStageUpdateResponse response = ProjectStageUpdateResponse.Converter.of(stageId, userId);

        when(projectService.updateProjectStage(eq(projectId), any(ProjectStageUpdateRequest.class), eq(userId)))
                .thenReturn(response);

        mockMvc.perform(patch("/admin/projects/{projectId}/stage", projectId)
                        .sessionAttr("userId", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ProjectStageUpdateRequest(stageId))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("프로젝트 진행단계 수정 성공"))
                .andExpect(jsonPath("$.response.stageId").value(stageId.intValue()))
                .andExpect(jsonPath("$.response.updatedBy").value(userId));
    }

    @Test
    @DisplayName("프로젝트 날짜 수정 중 잘못된 입력 예외 처리")
    void updateProjectDate_handlesInvalidInput() throws Exception {
        Long projectId = 9L;
        ProjectDateUpdateRequest request = new ProjectDateUpdateRequest("2025-03-01", "2025-02-01");

        when(projectService.updateProjectDate(eq(projectId), any(ProjectDateUpdateRequest.class)))
                .thenThrow(new InvalidInputException("종료일은 시작일보다 이를 수 없습니다."));

        mockMvc.perform(patch("/admin/projects/{projectId}/date", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"))
                .andExpect(jsonPath("$.message").value("종료일은 시작일보다 이를 수 없습니다."));
    }

    @Test
    @DisplayName("프로젝트 멤버 추가 시 세션 사용자 ID 사용")
    void addProjectMembers_usesSessionUser() throws Exception {
        Long projectId = 6L;
        Long userId = 2L;
        List<ProjectMemberRequest> requests = List.of(
                new ProjectMemberRequest(10L),
                new ProjectMemberRequest(11L)
        );

        when(projectService.addProjectMembers(eq(projectId), any(List.class), eq(userId))).thenReturn(2);

        mockMvc.perform(post("/admin/projects/{projectId}/members", projectId)
                        .sessionAttr("userId", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("프로젝트 멤버 추가 성공"))
                .andExpect(jsonPath("$.response").value(2));
    }

    @Test
    @DisplayName("프로젝트 삭제 시 휴지통 정보 반환")
    void deleteProject_returnsTrashResponse() throws Exception {
        Long projectId = 13L;
        ProjectTrashResponse response = ProjectTrashResponse.builder()
                .projectId(projectId)
                .build();

        when(projectService.deleteProject(projectId)).thenReturn(response);

        mockMvc.perform(delete("/admin/projects/{projectId}", projectId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("프로젝트 휴지통 이동 완료"))
                .andExpect(jsonPath("$.response.projectId").value(projectId));
    }
}

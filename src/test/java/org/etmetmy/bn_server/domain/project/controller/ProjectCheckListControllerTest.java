package org.etmetmy.bn_server.domain.project.controller;

import org.etmetmy.bn_server.BaseControllerTest;
import org.etmetmy.bn_server.domain.checkList.entity.CheckList;
import org.etmetmy.bn_server.domain.checkList.repository.CheckListRepository;
import org.etmetmy.bn_server.domain.company.entity.Company;
import org.etmetmy.bn_server.domain.company.entity.CompanyType;
import org.etmetmy.bn_server.domain.company.repository.CompanyRepository;
import org.etmetmy.bn_server.domain.project.entity.Project;
import org.etmetmy.bn_server.domain.project.entity.ProjectCheckList;
import org.etmetmy.bn_server.domain.project.repository.ProjectCheckListRepository;
import org.etmetmy.bn_server.domain.project.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@AutoConfigureMockMvc
public class ProjectCheckListControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private CheckListRepository checkListRepository;

    @Autowired
    private ProjectCheckListRepository projectCheckListRepository;

    @Autowired
    private CompanyRepository companyRepository;

    private Project project;
    private CheckList checkList1;
    private CheckList checkList2;
    private CheckList checkList3;
    private Company company;

    @BeforeEach
    void setup() {
        // Company 생성 (Project의 필수 필드)
        company = companyRepository.save(Company.builder()
                .companyName("테스트 회사")
                .type(CompanyType.CUSTOMER)
                .build());

        // Project 생성 (필수 필드 모두 채움)
        project = projectRepository.save(Project.builder()
                .projectName("테스트 프로젝트")
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 12, 31))
                .company(company)
                .build());

        // 여러 개의 CheckList 생성
        checkList1 = checkListRepository.save(CheckList.builder()
                .content("체크리스트 1번")
                .build());

        checkList2 = checkListRepository.save(CheckList.builder()
                .content("체크리스트 2번")
                .build());

        checkList3 = checkListRepository.save(CheckList.builder()
                .content("체크리스트 3번")
                .build());
    }

    @Test
    @DisplayName("프로젝트에 여러 체크리스트 추가 성공")
    public void addMultipleChecklistsToProjectTest() throws Exception {
        // Given: 여러 체크리스트 ID를 JSON으로 작성
        String requestJson = String.format(
                "{\"checklistIds\": [%d, %d, %d]}",
                checkList1.getCheckListId(),
                checkList2.getCheckListId(),
                checkList3.getCheckListId()
        );

        // When & Then
        mockMvc.perform(
                        post("/admin/projects/" + project.getId() + "/checklists")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                                .with(csrf())
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("체크리스트를 할당했습니다"))
                .andExpect(jsonPath("$.response").isArray())
                .andExpect(jsonPath("$.response", hasSize(3)))
                .andExpect(jsonPath("$.response[0].projectId").value(project.getId()))
                .andExpect(jsonPath("$.response[0].checklistContent").value("체크리스트 1번"))
                .andExpect(jsonPath("$.response[1].checklistContent").value("체크리스트 2번"))
                .andExpect(jsonPath("$.response[2].checklistContent").value("체크리스트 3번"))
                .andExpect(jsonPath("$.response[0].checked").value(false))
                .andExpect(jsonPath("$.response[0].answererId").isEmpty());
    }

    @Test
    @DisplayName("단일 체크리스트 추가 성공")
    public void addSingleChecklistToProjectTest() throws Exception {
        // Given: 단일 체크리스트 ID를 JSON으로 작성
        String requestJson = String.format(
                "{\"checklistIds\": [%d]}",
                checkList1.getCheckListId()
        );

        // When & Then
        mockMvc.perform(
                        post("/admin/projects/" + project.getId() + "/checklists")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                                .with(csrf())
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("체크리스트를 할당했습니다"))
                .andExpect(jsonPath("$.response").isArray())
                .andExpect(jsonPath("$.response", hasSize(1)))
                .andExpect(jsonPath("$.response[0].checklistContent").value("체크리스트 1번"));
    }

    @Test
    @DisplayName("존재하지 않는 프로젝트에 체크리스트 추가 실패")
    public void addChecklistToNonExistentProjectTest() throws Exception {
        // Given: 존재하지 않는 프로젝트 ID
        long nonExistentProjectId = 99999L;
        String requestJson = String.format(
                "{\"checklistIds\": [%d]}",
                checkList1.getCheckListId()
        );

        // When & Then
        mockMvc.perform(
                        post("/admin/projects/" + nonExistentProjectId + "/checklists")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                                .with(csrf())
                )
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("존재하지 않는 체크리스트 ID로 추가 실패")
    public void addNonExistentChecklistToProjectTest() throws Exception {
        // Given: 존재하지 않는 체크리스트 ID를 JSON으로 작성
        String requestJson = "{\"checklistIds\": [99999]}";

        // When & Then
        mockMvc.perform(
                        post("/admin/projects/" + project.getId() + "/checklists")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                                .with(csrf())
                )
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("빈 체크리스트 리스트로 추가 시도")
    public void addEmptyChecklistListToProjectTest() throws Exception {
        // Given: 빈 리스트를 JSON으로 작성
        String requestJson = "{\"checklistIds\": []}";

        // When & Then
        mockMvc.perform(
                        post("/admin/projects/" + project.getId() + "/checklists")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                                .with(csrf())
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").isArray())
                .andExpect(jsonPath("$.response", hasSize(0)));
    }

    @Test
    @DisplayName("프로젝트 체크리스트 전체 조회 성공 - 여러 개")
    public void getCheckListsSuccessWithMultipleItemsTest() throws Exception {
        // Given: 프로젝트에 체크리스트 3개 추가
        ProjectCheckList projectCheckList1 = projectCheckListRepository.save(
                ProjectCheckList.builder()
                        .project(project)
                        .checkListId(checkList1.getCheckListId())
                        .checked(false)
                        .build()
        );

        ProjectCheckList projectCheckList2 = projectCheckListRepository.save(
                ProjectCheckList.builder()
                        .project(project)
                        .checkListId(checkList2.getCheckListId())
                        .checked(true)
                        .build()
        );

        ProjectCheckList projectCheckList3 = projectCheckListRepository.save(
                ProjectCheckList.builder()
                        .project(project)
                        .checkListId(checkList3.getCheckListId())
                        .checked(false)
                        .build()
        );

        // When & Then
        mockMvc.perform(
                        get("/admin/projects/" + project.getId() + "/checklists")
                                .with(csrf())
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("프로젝트 체크리스트를 불러왔습니다."))
                .andExpect(jsonPath("$.response").isArray())
                .andExpect(jsonPath("$.response", hasSize(3)))
                .andExpect(jsonPath("$.response[0].projectId").value(project.getId()))
                .andExpect(jsonPath("$.response[0].checkListId").value(checkList1.getCheckListId()))
                .andExpect(jsonPath("$.response[0].checkListContent").value("체크리스트 1번"))
                .andExpect(jsonPath("$.response[0].checked").value(false))
                .andExpect(jsonPath("$.response[1].checkListId").value(checkList2.getCheckListId()))
                .andExpect(jsonPath("$.response[1].checkListContent").value("체크리스트 2번"))
                .andExpect(jsonPath("$.response[1].checked").value(true))
                .andExpect(jsonPath("$.response[2].checkListId").value(checkList3.getCheckListId()))
                .andExpect(jsonPath("$.response[2].checkListContent").value("체크리스트 3번"))
                .andExpect(jsonPath("$.response[2].checked").value(false));
    }

    @Test
    @DisplayName("프로젝트 체크리스트 전체 조회 성공 - 빈 리스트")
    public void getCheckListsSuccessWithEmptyListTest() throws Exception {
        // Given: 프로젝트에 체크리스트가 하나도 없음 (setup에서 생성한 project 사용)

        // When & Then
        mockMvc.perform(
                        get("/admin/projects/" + project.getId() + "/checklists")
                                .with(csrf())
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("프로젝트 체크리스트를 불러왔습니다."))
                .andExpect(jsonPath("$.response").isArray())
                .andExpect(jsonPath("$.response", hasSize(0)));
    }

    @Test
    @DisplayName("존재하지 않는 프로젝트의 체크리스트 조회 실패")
    public void getCheckListsWithNonExistentProjectTest() throws Exception {
        // Given: 존재하지 않는 프로젝트 ID
        long nonExistentProjectId = 99999L;

        // When & Then
        mockMvc.perform(
                        get("/admin/projects/" + nonExistentProjectId + "/checklists")
                                .with(csrf())
                )
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("프로젝트 체크리스트 전체 조회 성공 - 단일 항목")
    public void getCheckListsSuccessWithSingleItemTest() throws Exception {
        // Given: 프로젝트에 체크리스트 1개만 추가
        ProjectCheckList projectCheckList = projectCheckListRepository.save(
                ProjectCheckList.builder()
                        .project(project)
                        .checkListId(checkList1.getCheckListId())
                        .checked(false)
                        .build()
        );

        // When & Then
        mockMvc.perform(
                        get("/admin/projects/" + project.getId() + "/checklists")
                                .with(csrf())
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("프로젝트 체크리스트를 불러왔습니다."))
                .andExpect(jsonPath("$.response").isArray())
                .andExpect(jsonPath("$.response", hasSize(1)))
                .andExpect(jsonPath("$.response[0].projectId").value(project.getId()))
                .andExpect(jsonPath("$.response[0].checkListId").value(checkList1.getCheckListId()))
                .andExpect(jsonPath("$.response[0].checkListContent").value("체크리스트 1번"))
                .andExpect(jsonPath("$.response[0].checked").value(false));
    }
}

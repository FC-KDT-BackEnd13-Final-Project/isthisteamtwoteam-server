package org.etmetmy.bn_server.domain.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.etmetmy.bn_server.domain.checkList.entity.CheckList;
import org.etmetmy.bn_server.domain.checkList.repository.CheckListRepository;
import org.etmetmy.bn_server.domain.project.entity.Project;
import org.etmetmy.bn_server.domain.project.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class ProjectCheckListTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private CheckListRepository checkListRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Project project;
    private CheckList checkList;

    @BeforeEach
    void setup() {
        project = projectRepository.save(Project.builder()
                .projectName("테스트 프로젝트")
                .build());

        checkList = checkListRepository.save(CheckList.builder()
                .content("설명 text")
                .build());
    }

    @Test
    @DisplayName("프로젝트에 체크리스트 추가 성공")
    public void addChecklistToProjectTest() throws Exception {
        mockMvc.perform(
                        post("/api/v1/project/" + project.getId() + "/checklist/" + checkList.getCheckListId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(csrf())
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("체크리스트 등록 완료"));
    }

    @Test
    @DisplayName("프로젝트 체크리스트 조회 성공")
    public void getProjectChecklistTest() throws Exception {
        // 사전 체크리스트 등록
        mockMvc.perform(
                        post("/api/v1/project/" + project.getId() + "/checklist/" + checkList.getCheckListId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(csrf())
                )
                .andExpect(status().isOk());

        // 조회 테스트 수행
        mockMvc.perform(
                        get("/api/v1/project/" + project.getId() + "/checklist")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("테스트 체크리스트"));
    }
}
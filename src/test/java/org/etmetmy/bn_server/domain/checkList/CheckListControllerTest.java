package org.etmetmy.bn_server.domain.checkList;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.etmetmy.bn_server.domain.checkList.controller.CheckListController;
import org.etmetmy.bn_server.domain.checkList.dto.response.CheckListResponse;
import org.etmetmy.bn_server.domain.checkList.service.CheckListService;
import org.etmetmy.bn_server.global.StatusCode;
import org.etmetmy.bn_server.global.page.PageRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CheckListController.class)
@Import(org.etmetmy.bn_server.config.SecurityConfig.class)
class CheckListControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    //Spring Boot 3.5.7에서는 @MockitoBean 미지원으로 @MockBean 사용
    @MockBean
    private CheckListService checkListService;

    @Test
    @DisplayName("체크리스트 목록 조회 성공 (검색어 없음)")
    void getAllCheckLists_Success_WithoutKeyword() throws Exception {
        // given
        List<CheckListResponse> responseList = Arrays.asList(
                CheckListResponse.builder().id(1L).content("체크리스트 내용 1").build(),
                CheckListResponse.builder().id(2L).content("두 번째 체크리스트").build()
        );
        Page<CheckListResponse> mockPage = new PageImpl<>(responseList);

        when(checkListService.getCheckLists(any(PageRequest.class))).thenReturn(mockPage);

        // when & then
        mockMvc.perform(get("/admin/checklists")
                        .param("page", "0")
                        .param("size", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(StatusCode.CHECKLISTS_FOUND.getMessage()))
                .andExpect(jsonPath("$.response.content").isArray())
                .andExpect(jsonPath("$.response.content.length()").value(2))
                .andExpect(jsonPath("$.response.content[0].id").value(1L))
                .andExpect(jsonPath("$.response.content[0].content").value("체크리스트 내용 1"))
                .andExpect(jsonPath("$.response.pageInfo.totalElements").value(2));

        verify(checkListService).getCheckLists(any(PageRequest.class));
    }

    @Test
    @DisplayName("체크리스트 목록 조회 성공 (검색어 있음)")
    void getAllCheckLists_Success_WithKeyword() throws Exception {
        // given
        String keyword = "동균";
        List<CheckListResponse> responseList = Collections.singletonList(
                CheckListResponse.builder().id(3L).content("검색된 체크리스트").build()
        );
        Page<CheckListResponse> mockPage = new PageImpl<>(responseList);

        when(checkListService.searchCheckLists(eq(keyword), any(PageRequest.class))).thenReturn(mockPage);

        // when & then
        mockMvc.perform(get("/admin/checklists")
                        .param("page", "0")
                        .param("size", "5")
                        .param("keyword", keyword)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(StatusCode.CHECKLISTS_FOUND.getMessage()))
                .andExpect(jsonPath("$.response.content[0].id").value(3L))
                .andExpect(jsonPath("$.response.content[0].content").value("검색된 체크리스트"));

        verify(checkListService).searchCheckLists(eq(keyword), any(PageRequest.class));
    }
}
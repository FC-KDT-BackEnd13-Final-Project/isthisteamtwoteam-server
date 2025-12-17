package org.etmetmy.bn_server.domain.file;

import org.etmetmy.bn_server.config.SecurityConfig;
import org.etmetmy.bn_server.domain.file.controller.FileController;
import org.etmetmy.bn_server.global.ExceptionControllerAdvice;
import org.etmetmy.bn_server.domain.file.service.FileService;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.etmetmy.bn_server.exception.custom.ProjectNotFoundException;
import org.etmetmy.bn_server.exception.custom.ProjectPermissionDeniedException;
import org.etmetmy.bn_server.exception.handler.GlobalExceptionHandler;
import org.etmetmy.bn_server.web.SessionConst;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FileController.class)
@Import({SecurityConfig.class, ExceptionControllerAdvice.class, GlobalExceptionHandler.class})
public class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    //Spring Boot 3.5.7에서는 @MockitoBean 미지원으로 @MockBean 사용
    @MockBean
    private FileService fileService;

    @Test
    @DisplayName("로그인 후 프로젝트 파일 조회 성공")
    void getFiles_Success() throws Exception {
        List<ActiveFileListDTO> files = List.of(
                ActiveFileListDTO.builder()
                        .fileId(1L)
                        .fileTitle("project-plan.pdf")
                        .filePath("/uploads/2025/12/project-plan.pdf")
                        .fileType("application/pdf")
                        .fileSize(2048576L)
                        .build(),
                ActiveFileListDTO.builder()
                        .fileId(2L)
                        .fileTitle("design-mockup.png")
                        .filePath("/uploads/2025/12/design-mockup.png")
                        .fileType("image/png")
                        .fileSize(1024000L)
                        .build()
        );

        when(fileService.findAllByProjectId(eq(1L), any())).thenReturn(files);

        User mockUser = User.builder()
                .id(5L)
                .email("test@example.com")
                .build();

        mockMvc.perform(get("/users/projects/{projectId}/files", 1L)
                .sessionAttr(SessionConst.LOGIN_MEMBER, mockUser)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.length()").value(2))
                .andExpect(jsonPath("$.response[0].fileTitle").value("project-plan.pdf"))
                .andExpect(jsonPath("$.response[1].fileTitle").value("design-mockup.png"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("파일 목록 조회 성공"));

        verify(fileService).findAllByProjectId(eq(1L), any());
    }

    @Test
    @DisplayName("프로젝트 접근 권한이 없는 경우 403 에러 발생")
    void getFiles_Forbidden() throws Exception {
        User mockUser = User.builder()
                .id(5L)
                .email("test@example.com")
                .build();

        when(fileService.findAllByProjectId(eq(1L), any()))
                .thenThrow(new ProjectPermissionDeniedException());

        mockMvc.perform(get("/users/projects/{projectId}/files", 1L)
                        .sessionAttr(SessionConst.LOGIN_MEMBER, mockUser)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("P003"))
                .andExpect(jsonPath("$.message").value("프로젝트 접근 권한이 없습니다."))
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("존재하지 않는 프로젝트 조회 시 404 에러 발생")
    void getFiles_ProjectNotFound() throws Exception {
        User mockUser = User.builder()
                .id(5L)
                .email("test@example.com")
                .build();

        when(fileService.findAllByProjectId(eq(999L), any()))
                .thenThrow(new ProjectNotFoundException());

        mockMvc.perform(get("/users/projects/{projectId}/files", 999L)
                        .sessionAttr(SessionConst.LOGIN_MEMBER, mockUser)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("P001"))
                .andExpect(jsonPath("$.message").value("프로젝트를 찾을 수 없습니다."))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("파일이 없는 프로젝트 조회 시 빈 배열 반환")
    void getFiles_EmptyList() throws Exception {
        User mockUser = User.builder()
                .id(5L)
                .email("test@example.com")
                .build();

        when(fileService.findAllByProjectId(eq(2L), any()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/users/projects/{projectId}/files", 2L)
                        .sessionAttr(SessionConst.LOGIN_MEMBER, mockUser)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("파일 목록 조회 성공"))
                .andExpect(jsonPath("$.response").isArray())
                .andExpect(jsonPath("$.response.length()").value(0));

        verify(fileService).findAllByProjectId(eq(2L), any());
    }
}

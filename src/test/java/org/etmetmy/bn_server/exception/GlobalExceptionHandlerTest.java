package org.etmetmy.bn_server.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.etmetmy.bn_server.exception.custom.ProjectNotFoundException;
import org.etmetmy.bn_server.exception.custom.UnauthorizedException;
import org.etmetmy.bn_server.exception.handler.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = GlobalExceptionHandlerTest.TestController.class)
@Import({GlobalExceptionHandler.class, GlobalExceptionHandlerTest.TestController.class})
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("ProjectNotFoundException 발생 시 404 응답과 에러 정보 반환")
    void handleProjectNotFoundException() throws Exception {
        // when & then
        mockMvc.perform(get("/test/project-not-found"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("P001"))
                .andExpect(jsonPath("$.message").value("프로젝트를 찾을 수 없습니다."))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/test/project-not-found"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("UnauthorizedException 발생 시 401 응답과 에러 정보 반환")
    void handleUnauthorizedException() throws Exception {
        // when & then
        mockMvc.perform(get("/test/unauthorized"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("A001"))
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.path").value("/test/unauthorized"));
    }

    @Test
    @DisplayName("커스텀 메시지를 가진 예외 처리")
    void handleCustomMessageException() throws Exception {
        // when & then
        mockMvc.perform(get("/test/custom-message"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("P001"))
                .andExpect(jsonPath("$.message").value("ID 999번 프로젝트를 찾을 수 없습니다"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("예상치 못한 예외 발생 시 500 응답 반환")
    void handleUnexpectedException() throws Exception {
        // when & then
        mockMvc.perform(get("/test/unexpected"))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("C004"))
                .andExpect(jsonPath("$.message").value("서버 오류가 발생했습니다."))
                .andExpect(jsonPath("$.status").value(500));
    }

    @Test
    @DisplayName("유효성 검사 실패 시 400 응답과 필드 에러 반환")
    void handleValidationException() throws Exception {
        // given
        String invalidRequest = """
        {
            "projectName": "",
            "email": "invalid-email"
        }
        """;

        mockMvc.perform(post("/test/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"))
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.fieldErrors[0].field").exists())
                .andExpect(jsonPath("$.fieldErrors[0].reason").exists());
    }

    // 테스트용 컨트롤러
    @RestController
    static class TestController {

        @GetMapping("/test/project-not-found")
        public void throwProjectNotFoundException() {
            throw new ProjectNotFoundException();
        }

        @GetMapping("/test/unauthorized")
        public void throwUnauthorizedException() {
            throw new UnauthorizedException();
        }

        @GetMapping("/test/custom-message")
        public void throwCustomMessageException() {
            throw new ProjectNotFoundException("ID 999번 프로젝트를 찾을 수 없습니다");
        }

        @GetMapping("/test/unexpected")
        public void throwUnexpectedException() {
            throw new RuntimeException("예상치 못한 에러");
        }

        @PostMapping("/test/validate")
        public void handleValidationException(@Valid @RequestBody TestRequest request) {
            // 유효성 검사 실패 시 자동으로 MethodArgumentNotValidException 발생
        }
    }

    // 테스트용 DTO
    record TestRequest(
            @NotBlank(message = "프로젝트명은 필수입니다")
            String projectName,

            @Email(message = "유효한 이메일 형식이어야 합니다")
            @NotBlank(message = "이메일은 필수입니다")
            String email
    ) {}
}
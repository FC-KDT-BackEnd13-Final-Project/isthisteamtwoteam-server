package org.etmetmy.bn_server.domain.post.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.etmetmy.bn_server.domain.company.entity.Company;
import org.etmetmy.bn_server.domain.company.repository.CompanyRepository;
import org.etmetmy.bn_server.domain.post.dto.request.PostCreateRequest;
import org.etmetmy.bn_server.domain.post.entity.Stage;
import org.etmetmy.bn_server.domain.project.entity.Project;
import org.etmetmy.bn_server.domain.project.repository.ProjectRepository;
import org.etmetmy.bn_server.domain.user.entity.Role;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.etmetmy.bn_server.domain.user.repository.UserRepository;
import org.etmetmy.bn_server.web.SessionConst;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private EntityManager entityManager;

    private MockHttpSession session;
    private Long testProjectId;
    private Long testStageId;
    private User testUser;

    @BeforeEach
    void setUp() {
        // 1. Company 생성
        Company testCompany = Company.builder()
                .companyName("테스트컴퍼니")
                .companyAddress("서울시 강남구")
                .companyCeo("홍길동")
                .companyPhone("010-1234-5678")
                .build();
        companyRepository.saveAndFlush(testCompany);

        // 2. User 생성
        testUser = User.builder()
                .name("테스트유저")
                .email("test@example.com")
                .password("password123")
                .phone("010-1111-2222")
                .company(testCompany)
                .role(Role.DEVELOPER)
                .build();
        userRepository.saveAndFlush(testUser);

        // 3. Project 생성
        Project testProject = Project.builder()
                .projectName("테스트 프로젝트")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(3))
                .company(testCompany)
                .createdBy(testUser.getId())
                .build();
        projectRepository.saveAndFlush(testProject);
        testProjectId = testProject.getId();

        // 4. Stage 생성
        Stage testStage = Stage.builder()
                .stageName("개발 단계")
                .build();
        entityManager.persist(testStage);
        entityManager.flush();
        testStageId = testStage.getStageId();

        // 5. 세션 설정 - User 객체를 세션에 저장
        session = new MockHttpSession();
        session.setAttribute(SessionConst.LOGIN_MEMBER, testUser);
    }

    @Test
    @DisplayName("게시글 작성 성공 테스트 - 기본 필드만")
    public void createPost_Success_WithRequiredFields() throws Exception {
        // given
        PostCreateRequest request = PostCreateRequest.builder()
                .title("테스트 게시글 제목")
                .content("테스트 게시글 본문입니다.")
                .stageId(testStageId)
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/users/projects/{projectId}/posts", testProjectId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("게시글 작성 성공"))
                .andExpect(jsonPath("$.data.title").value("테스트 게시글 제목"))
                .andExpect(jsonPath("$.data.content").value("테스트 게시글 본문입니다."))
                .andExpect(jsonPath("$.data.stageId").value(testStageId))
                .andExpect(jsonPath("$.data.postId").exists())
                .andExpect(jsonPath("$.data.createdAt").exists());
    }

    @Test
    @DisplayName("게시글 작성 성공 테스트 - 파일 URL 포함")
    public void createPost_Success_WithFileUrls() throws Exception {
        // given
        PostCreateRequest request = PostCreateRequest.builder()
                .title("파일이 포함된 게시글")
                .content("파일이 첨부된 게시글입니다.")
                .stageId(testStageId)
                .fileUrls(Arrays.asList(
                        "https://s3.amazonaws.com/bucket/file1.pdf",
                        "https://s3.amazonaws.com/bucket/file2.jpg"
                ))
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/users/projects/{projectId}/posts", testProjectId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("파일이 포함된 게시글"))
                .andExpect(jsonPath("$.data.files").isArray());
    }

    @Test
    @DisplayName("게시글 작성 성공 테스트 - 링크 URL 포함")
    public void createPost_Success_WithLinkUrls() throws Exception {
        // given
        PostCreateRequest request = PostCreateRequest.builder()
                .title("링크가 포함된 게시글")
                .content("링크가 포함된 게시글입니다.")
                .stageId(testStageId)
                .linkUrls(Arrays.asList(
                        "https://example.com/reference1",
                        "https://example.com/reference2"
                ))
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/users/projects/{projectId}/posts", testProjectId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("링크가 포함된 게시글"))
                .andExpect(jsonPath("$.data.linkUrls").isArray());
    }

    @Test
    @DisplayName("게시글 작성 성공 테스트 - 파일과 링크 모두 포함")
    public void createPost_Success_WithFilesAndLinks() throws Exception {
        // given
        PostCreateRequest request = PostCreateRequest.builder()
                .title("파일과 링크가 모두 포함된 게시글")
                .content("파일과 링크가 모두 첨부된 게시글입니다.")
                .stageId(testStageId)
                .fileUrls(Arrays.asList("https://s3.amazonaws.com/bucket/file1.pdf"))
                .linkUrls(Arrays.asList("https://example.com/reference1"))
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/users/projects/{projectId}/posts", testProjectId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.files").isArray())
                .andExpect(jsonPath("$.data.linkUrls").isArray());
    }

    @Test
    @DisplayName("게시글 작성 실패 - 제목 누락")
    public void createPost_Fail_TitleMissing() throws Exception {
        // given
        PostCreateRequest request = PostCreateRequest.builder()
                .content("본문만 있는 게시글")
                .stageId(testStageId)
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/users/projects/{projectId}/posts", testProjectId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("게시글 작성 실패 - 제목 빈 문자열")
    public void createPost_Fail_TitleBlank() throws Exception {
        // given
        PostCreateRequest request = PostCreateRequest.builder()
                .title("")
                .content("본문 내용")
                .stageId(testStageId)
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/users/projects/{projectId}/posts", testProjectId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("게시글 작성 실패 - 제목 길이 초과 (200자 초과)")
    public void createPost_Fail_TitleTooLong() throws Exception {
        // given
        String longTitle = "a".repeat(201); // 201자
        PostCreateRequest request = PostCreateRequest.builder()
                .title(longTitle)
                .content("본문 내용")
                .stageId(testStageId)
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/users/projects/{projectId}/posts", testProjectId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("게시글 작성 실패 - 본문 누락")
    public void createPost_Fail_ContentMissing() throws Exception {
        // given
        PostCreateRequest request = PostCreateRequest.builder()
                .title("제목만 있는 게시글")
                .stageId(testStageId)
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/users/projects/{projectId}/posts", testProjectId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("게시글 작성 실패 - 본문 빈 문자열")
    public void createPost_Fail_ContentBlank() throws Exception {
        // given
        PostCreateRequest request = PostCreateRequest.builder()
                .title("테스트 제목")
                .content("")
                .stageId(testStageId)
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/users/projects/{projectId}/posts", testProjectId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("게시글 작성 실패 - stageId 누락")
    public void createPost_Fail_StageIdMissing() throws Exception {
        // given
        PostCreateRequest request = PostCreateRequest.builder()
                .title("테스트 제목")
                .content("테스트 본문")
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/users/projects/{projectId}/posts", testProjectId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("게시글 작성 실패 - 세션 없음 (로그인하지 않은 사용자)")
    public void createPost_Fail_NoSession() throws Exception {
        // given
        PostCreateRequest request = PostCreateRequest.builder()
                .title("테스트 제목")
                .content("테스트 본문")
                .stageId(testStageId)
                .build();

        // when & then - 세션 없이 요청
        mockMvc.perform(post("/api/v1/users/projects/{projectId}/posts", testProjectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().is4xxClientError()); // 401 또는 302 등 에러 발생 예상
    }

    @Test
    @DisplayName("게시글 작성 성공 - 제목 최대 길이 (200자)")
    public void createPost_Success_MaxTitleLength() throws Exception {
        // given
        String maxTitle = "a".repeat(200); // 정확히 200자
        PostCreateRequest request = PostCreateRequest.builder()
                .title(maxTitle)
                .content("본문 내용")
                .stageId(testStageId)
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/users/projects/{projectId}/posts", testProjectId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
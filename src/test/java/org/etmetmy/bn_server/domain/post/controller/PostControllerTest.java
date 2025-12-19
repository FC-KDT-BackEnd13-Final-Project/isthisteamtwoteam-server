package org.etmetmy.bn_server.domain.post.controller;

import org.etmetmy.bn_server.domain.company.entity.Company;
import org.etmetmy.bn_server.domain.company.repository.CompanyRepository;
import org.etmetmy.bn_server.domain.post.entity.Post;
import org.etmetmy.bn_server.domain.post.entity.Stage;
import org.etmetmy.bn_server.domain.post.repository.PostRepository;
import org.etmetmy.bn_server.domain.post.repository.StageRepository;
import org.etmetmy.bn_server.domain.project.entity.Project;
import org.etmetmy.bn_server.domain.project.repository.ProjectRepository;
import org.etmetmy.bn_server.domain.user.entity.Role;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.etmetmy.bn_server.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@AutoConfigureMockMvc
public class PostControllerTest {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    StageRepository stageRepository;

    @Autowired
    PostRepository postRepository;

    Company testCompany;
    User testUser;
    Project testProject;
    Stage stage1;
    Stage stage2;
    Stage stage3;
    Post completedPost;
    Post uncompletedPost;
    Post stage1Post;
    Post stage2Post;

    @BeforeEach
    void setUp() {
        // 1. 회사 생성
        testCompany = Company.builder()
                .companyName("테스트컴퍼니")
                .companyAddress("서울시 강남구")
                .companyCeo("홍길동")
                .companyPhone("010-1234-5678")
                .build();
        companyRepository.saveAndFlush(testCompany);

        // 2. 사용자 생성
        testUser = User.builder()
                .name("테스트유저")
                .email("test@test.com")
                .password("password123")
                .phone("010-1111-2222")
                .company(testCompany)
                .role(Role.DEVELOPER)
                .build();
        userRepository.saveAndFlush(testUser);

        // 3. 프로젝트 생성
        testProject = Project.builder()
                .projectName("테스트프로젝트")
                .startDate(java.time.LocalDate.now())
                .endDate(java.time.LocalDate.now().plusDays(30))
                .company(testCompany)
                .build();
        projectRepository.saveAndFlush(testProject);

        // 4. 스테이지 생성
        stage1 = Stage.builder()
                .stageName("요구사항 정의")
                .build();
        stage2 = Stage.builder()
                .stageName("화면 설계")
                .build();
        stage3 = Stage.builder()
                .stageName("개발")
                .build();
        stageRepository.saveAndFlush(stage1);
        stageRepository.saveAndFlush(stage2);
        stageRepository.saveAndFlush(stage3);

        // 5. 게시글 생성 - 완료된 게시글
        completedPost = Post.builder()
                .project(testProject)
                .user(testUser)
                .stage(stage1)
                .title("완료된 게시글")
                .content("완료된 게시글 내용")
                .isCompleted(true)
                .postNumber(1L)
                .createdIp("127.0.0.1")
                .build();
        postRepository.saveAndFlush(completedPost);

        // 6. 게시글 생성 - 미완료 게시글
        uncompletedPost = Post.builder()
                .project(testProject)
                .user(testUser)
                .stage(stage2)
                .title("미완료 게시글")
                .content("미완료 게시글 내용")
                .isCompleted(false)
                .postNumber(2L)
                .createdIp("127.0.0.1")
                .build();
        postRepository.saveAndFlush(uncompletedPost);

        // 7. 게시글 생성 - stage1의 게시글
        stage1Post = Post.builder()
                .project(testProject)
                .user(testUser)
                .stage(stage1)
                .title("요구사항 정의 게시글")
                .content("요구사항 정의 단계 게시글")
                .isCompleted(false)
                .postNumber(3L)
                .createdIp("127.0.0.1")
                .build();
        postRepository.saveAndFlush(stage1Post);

        // 8. 게시글 생성 - stage2의 게시글
        stage2Post = Post.builder()
                .project(testProject)
                .user(testUser)
                .stage(stage2)
                .title("화면 설계 게시글")
                .content("화면 설계 단계 게시글")
                .isCompleted(false)
                .postNumber(4L)
                .createdIp("127.0.0.1")
                .build();
        postRepository.saveAndFlush(stage2Post);
    }

    @Test
    @DisplayName("필터 파라미터로 전체 게시글 조회 - filter=all")
    public void getPostListWithFilterAll() throws Exception {
        mockMvc.perform(
                        get("/api/v1/users/projects/{projectId}/posts", testProject.getId())
                                .param("filter", "all")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("게시글 목록 조회 성공"))
                .andExpect(jsonPath("$.response", hasSize(4)));  // 전체 4개
    }

    @Test
    @DisplayName("필터 파라미터로 완료된 게시글만 조회 - filter=finished")
    public void getPostListWithFilterFinished() throws Exception {
        mockMvc.perform(
                        get("/api/v1/users/projects/{projectId}/posts", testProject.getId())
                                .param("filter", "finished")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("게시글 목록 조회 성공"))
                .andExpect(jsonPath("$.response", hasSize(1)))  // 완료된 게시글 1개
                .andExpect(jsonPath("$.response[0].isCompleted").value(true))
                .andExpect(jsonPath("$.response[0].title").value("완료된 게시글"));
    }

    @Test
    @DisplayName("필터 파라미터로 미완료 게시글만 조회 - filter=unfinished")
    public void getPostListWithFilterUnfinished() throws Exception {
        mockMvc.perform(
                        get("/api/v1/users/projects/{projectId}/posts", testProject.getId())
                                .param("filter", "unfinished")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("게시글 목록 조회 성공"))
                .andExpect(jsonPath("$.response", hasSize(3)))  // 미완료 게시글 3개
                .andExpect(jsonPath("$.response[*].isCompleted", everyItem(is(false))));
    }

    @Test
    @DisplayName("stage 파라미터로 스테이지별 게시글 조회 - stage=요구사항 정의")
    public void getPostListWithStageParameter() throws Exception {
        mockMvc.perform(
                        get("/api/v1/users/projects/{projectId}/posts", testProject.getId())
                                .param("stage", "요구사항 정의")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("게시글 단계별 조회 성공"))
                .andExpect(jsonPath("$.response", hasSize(2)))  // 요구사항 정의 단계 게시글 2개
                .andExpect(jsonPath("$.response[*].stageName", everyItem(is("요구사항 정의"))));
    }

    @Test
    @DisplayName("stage 파라미터로 스테이지별 게시글 조회 - stage=화면 설계")
    public void getPostListWithStageParameterDesign() throws Exception {
        mockMvc.perform(
                        get("/api/v1/users/projects/{projectId}/posts", testProject.getId())
                                .param("stage", "화면 설계")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("게시글 단계별 조회 성공"))
                .andExpect(jsonPath("$.response", hasSize(2)))  // 화면 설계 단계 게시글 2개
                .andExpect(jsonPath("$.response[*].stageName", everyItem(is("화면 설계"))));
    }

    @Test
    @DisplayName("stage 파라미터로 전체 게시글 조회 - stage=all")
    public void getPostListWithStageAll() throws Exception {
        mockMvc.perform(
                        get("/api/v1/users/projects/{projectId}/posts", testProject.getId())
                                .param("stage", "all")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("게시글 단계별 조회 성공"))
                .andExpect(jsonPath("$.response", hasSize(4)));  // 전체 4개
    }

    @Test
    @DisplayName("잘못된 filter 파라미터 사용 시 에러 발생")
    public void getPostListWithInvalidFilter() throws Exception {
        mockMvc.perform(
                        get("/api/v1/users/projects/{projectId}/posts", testProject.getId())
                                .param("filter", "invalid_filter")
                )
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("존재하지 않는 stage로 조회 시 에러 발생")
    public void getPostListWithNonExistentStage() throws Exception {
        mockMvc.perform(
                        get("/api/v1/users/projects/{projectId}/posts", testProject.getId())
                                .param("stage", "존재하지않는단계")
                )
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("filter 파라미터 없이 조회 시 기본값(all) 적용")
    public void getPostListWithoutFilterParameter() throws Exception {
        mockMvc.perform(
                        get("/api/v1/users/projects/{projectId}/posts", testProject.getId())
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.response", hasSize(4)));  // 기본값 all로 전체 조회
    }

    @Test
    @DisplayName("게시글 상세 조회 성공")
    public void getPostDetailSuccess() throws Exception {
        mockMvc.perform(
                        get("/api/v1/users/projects/{projectId}/posts/{postId}",
                                testProject.getId(),
                                completedPost.getPostId())
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("게시글 조회 성공"))
                .andExpect(jsonPath("$.response.postId").value(completedPost.getPostId()))
                .andExpect(jsonPath("$.response.title").value("완료된 게시글"));
    }

    @Test
    @DisplayName("존재하지 않는 게시글 조회 시 404 에러")
    public void getPostDetailNotFound() throws Exception {
        mockMvc.perform(
                        get("/api/v1/users/projects/{projectId}/posts/{postId}",
                                testProject.getId(),
                                99999L)  // 존재하지 않는 postId
                )
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }
}
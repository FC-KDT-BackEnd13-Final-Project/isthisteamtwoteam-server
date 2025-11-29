package org.etmetmy.bn_server.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.etmetmy.bn_server.domain.company.entity.Company;
import org.etmetmy.bn_server.domain.user.dto.entity.UserDto;
import org.etmetmy.bn_server.domain.user.dto.request.UserLoginDto;
import org.etmetmy.bn_server.domain.user.entity.Role;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.etmetmy.bn_server.domain.user.repository.CompanyRepository;
import org.etmetmy.bn_server.domain.user.service.UserService;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    UserService userService;

    Company testCompany;
    Long userId;
    UserLoginDto userLoginDto;
    UserDto testUserDto;


    @BeforeEach
    void setUp(){
        testCompany = Company.builder()
                .companyName("진용컴퍼니")
                .companyAddress("강남 케케빌딩")
                .companyCeo("김길동")
                .companyPhone("010-1112-2322")
                .build();
        companyRepository.saveAndFlush(testCompany);

        testUserDto = UserDto.builder()
                .name("홍길동")
                .email("aaa@naver.com")
                .password("12345")
                .phone("010-1111-2222")
                .company("진용컴퍼니")
                .role(Role.DEVELOPER)
                .build();

        userId = userService.joinUser(testUserDto);

        userLoginDto = UserLoginDto.builder()
                .email("aaa@naver.com")
                .password("12345")
                .build();


    }

    @Test
    @DisplayName("로그인 성공 테스트")
    public void LoginSuccess(){
        User user = userService.login(userLoginDto);
        assertThat(user.getName()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("로그인 성공 후 세션에 사용자 정보가 저장되는지 정보")
    public void LoginSessionTest() throws Exception {
        MvcResult loginResult = mockMvc.perform(
                        post("/api/v1/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(userLoginDto))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession();
        User loginUser = (User) session.getAttribute(SessionConst.LOGIN_MEMBER);
        Role userRole = (Role) session.getAttribute(SessionConst.USER_ROLE);

        System.out.println("유저이름 : " + loginUser.getName());
        assertThat(loginUser).isNotNull();
        assertThat(loginUser.getEmail()).isEqualTo("aaa@naver.com");
        assertThat(loginUser.getName()).isEqualTo("홍길동");
        assertThat(userRole).isEqualTo(Role.DEVELOPER);
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 오류")
    public void FailLoginByPassword() throws Exception {
        UserLoginDto userFailedLoginDtoByPassword = UserLoginDto.builder()
                .email("aaa@naver.com")
                .password("11")
                .build();


        MvcResult result = mockMvc.perform(
                post("/api/v1/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userFailedLoginDtoByPassword))
        ).andReturn();

        MockHttpSession session = (MockHttpSession)  result.getRequest().getSession();
        User user = (User)session.getAttribute(SessionConst.LOGIN_MEMBER);
        Role role = (Role)session.getAttribute(SessionConst.USER_ROLE);

        assertThat(user).isNull();
        assertThat(role).isNull();
    }

    @Test
    @DisplayName("권한없는 사용자가 권한없는 메서드 호출 시 401 에러 발생")
    public void callMethodByNotAuthorizer() throws Exception {


        mockMvc.perform(
                post("/api/v1/admin/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUserDto))
        )
                .andDo(print())
                .andExpect(status().isFound());  // 302 검증
    }

    @Test
    @DisplayName("DEVELOPER 권한으로 ADMIN API 호출 시 403 에러")
    public void callAdminApiWithDeveloperRole() throws Exception {
        // given - DEVELOPER로 로그인
        MvcResult loginResult = mockMvc.perform(
                        post("/api/v1/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(userLoginDto))
                )
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession();

        System.out.println("세션 권한 : " + session.getAttribute(SessionConst.USER_ROLE));
        // when & then - ADMIN API 호출 시 403 에러
        UserDto newUserDto = UserDto.builder()
                .name("신규유저")
                .email("new@naver.com")
                .password("12345")
                .phone("010-9999-9999")
                .company("진용컴퍼니")
                .role(Role.CUSTOMER)
                .build();

        mockMvc.perform(
                        post("/api/v1/admin/user")
                                .session(session)  //  로그인 세션 전달
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(newUserDto))
                )
                .andDo(print())
                .andExpect(status().isForbidden());  //  403 검증
    }

    @Test
    @DisplayName("ADMIN 권한으로 회원 생성 성공")
    public void createUserWithAdminRole() throws Exception {
        // given - ADMIN 권한 사용자 생성 및 로그인
        UserDto adminDto = UserDto.builder()
                .name("관리자")
                .email("admin@naver.com")
                .password("12345")
                .phone("010-8888-8888")
                .company("진용컴퍼니")
                .role(Role.ADMIN)
                .build();
        userService.joinUser(adminDto);

        UserLoginDto adminLoginDto = UserLoginDto.builder()
                .email("admin@naver.com")
                .password("12345")
                .build();

        MvcResult loginResult = mockMvc.perform(
                        post("/api/v1/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(adminLoginDto))
                )
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession adminSession = (MockHttpSession) loginResult.getRequest().getSession();

        // when & then - ADMIN으로 회원 생성 성공
        UserDto newUserDto = UserDto.builder()
                .name("신규유저")
                .email("new@naver.com")
                .password("12345")
                .phone("010-9999-9999")
                .company("진용컴퍼니")
                .role(Role.CUSTOMER)
                .build();

        mockMvc.perform(
                        post("/api/v1/admin/user")
                                .session(adminSession)  // ✅ ADMIN 세션 전달
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(newUserDto))
                )
                .andDo(print())
                .andExpect(status().isOk());  // ✅ 200 성공
    }

}
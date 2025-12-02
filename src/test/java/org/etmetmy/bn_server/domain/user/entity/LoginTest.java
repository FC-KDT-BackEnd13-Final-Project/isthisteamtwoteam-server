package org.etmetmy.bn_server.domain.user.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.etmetmy.bn_server.domain.company.entity.Company;
import org.etmetmy.bn_server.domain.company.repository.CompanyRepository;
import org.etmetmy.bn_server.domain.dto.entity.UserDto;
import org.etmetmy.bn_server.domain.dto.request.UserLoginDto;
import org.etmetmy.bn_server.domain.entity.Role;
import org.etmetmy.bn_server.domain.entity.User;
import org.etmetmy.bn_server.domain.service.UserService;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;  // ✅ 이 import 추가!
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class LoginTest {

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

    @BeforeEach
    void setUp(){
        testCompany = Company.builder()
                .companyName("진용컴퍼니")
                .companyAddress("강남 케케빌딩")
                .companyCeo("김길동")
                .companyPhone("010-1112-2322")
                .build();
        companyRepository.saveAndFlush(testCompany);

        UserDto testUserDto = UserDto.builder()
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
                                .with(csrf())
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
}
package org.etmetmy.bn_server.domain.user.entity;

import org.etmetmy.bn_server.domain.company.entity.Company;
import org.etmetmy.bn_server.domain.user.dto.entity.UserDto;
import org.etmetmy.bn_server.domain.user.repository.CompanyRepository;
import org.etmetmy.bn_server.domain.user.repository.UserRepository;
import org.etmetmy.bn_server.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional  // 각 테스트 후 자동 롤백
class UserTest {

    @Autowired
    UserService userService;

    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    UserRepository userRepository;

    Company testCompany;

    @BeforeEach
    void setUp(){
        testCompany = Company.builder()
                .companyName("진용컴퍼니")
                .companyAddress("강남 케케빌딩")
                .companyCeo("김길동")
                .companyPhone("010-1112-2322")
                .build();
        companyRepository.saveAndFlush(testCompany);
    }

    @Test
    @DisplayName("회원가입 성공 - 정상적인 사용자 정보로 가입")
    public void createUser(){
        // given
        UserDto testUserDto = UserDto.builder()
                .name("홍길동")
                .email("aaa@naver.com")
                .password("12345")
                .phone("010-1111-2222")
                .company("진용컴퍼니")
                .role(Role.DEVELOPER)
                .build();

        // when
        Long userId = userService.joinUser(testUserDto);

        // then
        assertThat(userId).isNotNull();

        User savedUser = userRepository.findById(userId).orElseThrow();

        assertThat(savedUser.getName()).isEqualTo("홍길동");
        assertThat(savedUser.getEmail()).isEqualTo("aaa@naver.com");
        assertThat(savedUser.getPhone()).isEqualTo("010-1111-2222");
        assertThat(savedUser.getCompany().getCompanyName()).isEqualTo("진용컴퍼니");
        assertThat(savedUser.getRole()).isEqualTo(Role.DEVELOPER);
    }
}
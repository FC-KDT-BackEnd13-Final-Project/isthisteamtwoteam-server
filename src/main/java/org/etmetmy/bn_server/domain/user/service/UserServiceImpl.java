package org.etmetmy.bn_server.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.user.dto.request.UserLoginDto;
import org.etmetmy.bn_server.domain.user.entity.Company;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.etmetmy.bn_server.domain.user.dto.entity.UserDto;
import org.etmetmy.bn_server.domain.user.repository.UserRepository;
import org.etmetmy.bn_server.global.CustomException;
import org.etmetmy.bn_server.global.StatusCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    private final CompanyService companyService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Long joinUser(UserDto userDto) {
        Company company = companyService.findByCompanyName(userDto.getCompany())
                .orElseThrow(() -> new CustomException(StatusCode.COMPANY_NOT_FOUND));

        User newUser = UserDto.Converter.toUser(userDto,company);

        // 비밀번호 암호화
        newUser.setPassword(passwordEncoder.encode(userDto.getPassword()));

        Long userId = userRepository.save(newUser).getId();

        return userId;
    }

    // UserService
    public User login(UserLoginDto loginDto) {
        User user = userRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new CustomException(StatusCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            throw new CustomException(StatusCode.PASSWORD_NOT_MATCH);
        }

        return user;
    }
}

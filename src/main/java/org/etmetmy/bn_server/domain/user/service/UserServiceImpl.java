package org.etmetmy.bn_server.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.company.entity.Company;
import org.etmetmy.bn_server.domain.company.repository.CompanyRepository;
import org.etmetmy.bn_server.domain.company.service.CompanyService;
import org.etmetmy.bn_server.domain.user.dto.request.UserUpdateRequest;
import org.etmetmy.bn_server.domain.user.dto.entity.UserDto;
import org.etmetmy.bn_server.domain.user.dto.request.UserLoginDto;
import org.etmetmy.bn_server.domain.user.dto.response.*;
import org.etmetmy.bn_server.domain.user.entity.Role;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.etmetmy.bn_server.domain.user.repository.UserRepository;
import org.etmetmy.bn_server.exception.code.ErrorCode;
import org.etmetmy.bn_server.exception.custom.BusinessException;
import org.etmetmy.bn_server.exception.custom.UserNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final CompanyService companyService;
    private final PasswordEncoder passwordEncoder;

    // 회원 정보 수정 (Update)
    @Transactional
    public Long updateMember(Long memberId, UserUpdateRequest request) {
        // 1. 회원 찾기
        User user = userRepository.findById(memberId)
                .orElseThrow(UserNotFoundException::new);
        // 2. 바꿀 회사 찾기
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        // 3. 정보 변경 (dirty checking)
        user.updateInfo(request.getName(), request.getEmail(), company, request.getRole());

        return user.getId();
    }

    // 2. 회원 삭제 (Delete)
    @Transactional
    public Long deleteMember(Long memberId) {
        // 존재 여부 확인 후 삭제
        User user = userRepository.findById(memberId)
                .orElseThrow(UserNotFoundException::new);

        Long userId = user.getId();
        userRepository.delete(user);
        return userId;
    }


    // 검색 조회
    @Override
    public UserDataResponse searchUsers(String name, String email) {

        // 1. 회원 목록 조회
        List<User> allMembers = userRepository.findByNamicMembers(name, email, null,null);

        // 2. 회사 목록 조회
        List<Company> allCompanies = companyRepository.findAll();

        // 3. 회원 목록 역할별 분리

        // 개발사 회원 DTO 변환
        List<DeveloperUserResponse> developerResponses = allMembers.stream()
                .filter(user -> user.getRole() == Role.DEVELOPER)
                .map(DeveloperUserResponse::new)
                .toList();

        // 고객사 회원 DTO 변환
        List<CustomerUserResponse> clientResponses = allMembers.stream()
                .filter(user -> user.getRole() == Role.CUSTOMER)
                .map(CustomerUserResponse::new)
                .toList();

        // 4. UserItems 객체 생성
        UserItems<DeveloperUserResponse> developerItems = new UserItems<>(developerResponses);
        UserItems<CustomerUserResponse> clientItems = new UserItems<>(clientResponses);

        // 5. 회사 목록 DTO 변환 -> UserItems 객체 생성
        List<CompanySearchResponse> companyResponses = allCompanies.stream()
                .map(CompanySearchResponse::new)
                .toList();

        UserItems<CompanySearchResponse> companyItems = new UserItems<>(companyResponses);

        return new UserDataResponse(
                developerItems,
                clientItems,
                companyItems
        );
    }


    // UserService
    @Override
    @Transactional
    public User login(UserLoginDto loginDto) {
        User user = userRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(UserNotFoundException::new);

        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }
        return user;
    }

    //
    @Override
    public UserProfileImgNameResponse getProfileImgName(Long userId) {
        UserProfileImgNameResponse profileImgAndNameByUserId = userRepository.findProfileImgAndNameByUserId(userId)
                .orElseThrow(UserNotFoundException::new);
        return profileImgAndNameByUserId;
    }

    @Override
    @Transactional
    public Long joinUser(UserDto userDto) {
        Company company = companyService.findByCompanyName(userDto.getCompany())
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        User newUser = UserDto.Converter.toUser(userDto,company);

        // 비밀번호 암호화
        newUser.setPassword(passwordEncoder.encode(userDto.getPassword()));

        Long userId = userRepository.save(newUser).getId();

        return userId;
    }
}
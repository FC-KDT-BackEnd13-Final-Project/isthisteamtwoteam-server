package org.etmetmy.bn_server.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.company.entity.Company;
import org.etmetmy.bn_server.domain.company.repository.CompanyRepository;
import org.etmetmy.bn_server.domain.company.service.CompanyService;
import org.etmetmy.bn_server.domain.project.repository.ProjectMemberRepository;
import org.etmetmy.bn_server.domain.user.dto.request.*;
import org.etmetmy.bn_server.domain.user.dto.entity.UserDto;
import org.etmetmy.bn_server.domain.user.dto.response.*;
import org.etmetmy.bn_server.domain.user.entity.PasswordResetCode;
import org.etmetmy.bn_server.domain.user.entity.Role;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.etmetmy.bn_server.domain.user.repository.PasswordResetCodeRepository;
import org.etmetmy.bn_server.domain.user.repository.UserRepository;
import org.etmetmy.bn_server.config.MailServiceProvider;
import org.etmetmy.bn_server.exception.code.ErrorCode;
import org.etmetmy.bn_server.exception.custom.BusinessException;
import org.etmetmy.bn_server.exception.custom.UserNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final CompanyService companyService;
    private final PasswordEncoder passwordEncoder;
    private final ProjectMemberRepository projectMemberRepository;
    private final PasswordResetCodeRepository passwordResetCodeRepository;
    private final JavaMailSender mailSender;

    private static final int RESET_CODE_EXPIRES_SECONDS = 300;


    // 회원 정보 수정 (Update)
    @Override
    @Transactional
    public Long updateMember(Long memberId, UserUpdateRequest request) {
        // 1. 회원 찾기
        User user = userRepository.findById(memberId)
                .orElseThrow(UserNotFoundException::new);

        // 2. 바꿀 회사 찾기 (companyName이 null이 아닌 경우에만)
        Company company = null;
        if (request.getCompanyName() != null && !request.getCompanyName().isEmpty()) {
            company = companyRepository.findByCompanyName(request.getCompanyName())
                    .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));
        }

        // 3. 정보 변경 (dirty checking)
        user.updateInfo(request.getName(), request.getEmail(), request.getPhone(), company, request.getRole());

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return user.getId();
    }

    // 2. 회원 삭제 (Delete)
    @Transactional
    public Long deleteMember(Long memberId) {
        // 존재 여부 확인 후 삭제
        User user = userRepository.findById(memberId)
                .orElseThrow(UserNotFoundException::new);

        Long userId = user.getId();

        // 먼저 해당 유저의 모든 프로젝트 멤버 삭제
        projectMemberRepository.deleteByUserId(userId);

        // 그 다음 유저 삭제
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
        List<AdminUserResponse> adminResponses = AdminUserResponse.Converter.fromList(allMembers);

        List<DeveloperUserResponse> developerResponses = DeveloperUserResponse.Converter.fromList(allMembers);

        List<CustomerUserResponse> customerResponses = CustomerUserResponse.Converter.fromList(allMembers);

        // 4. UserItems 객체 생성
        UserItems<AdminUserResponse> adminItems = UserItems.create(adminResponses);
        UserItems<DeveloperUserResponse> developerItems = UserItems.create(developerResponses);
        UserItems<CustomerUserResponse> customerItems = UserItems.create(customerResponses);

        // 5. 회사 목록 DTO 변환
        List<CompanySearchResponse> companyResponses = CompanySearchResponse.Converter.fromList(allCompanies);

        UserItems<CompanySearchResponse> companyItems = UserItems.create(companyResponses);

        return UserDataResponse.Converter.createResponse(
                adminItems,
                developerItems,
                customerItems,
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

    @Override
    @Transactional
    public void changePassword(Long userId, UserChangePasswordRequest request) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        // 2. 현재 비밀번호 확인
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        // 3. 비밀번호 암호화 후 변경
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(encodedPassword);
    }

    // 비밀번호 찾기 - 인증 코드 발송
    @Override
    @Transactional
    public PasswordFindResponse sendPasswordResetCode(PasswordFindRequest request) {
        String email = request.getEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("가입되지 않은 이메일입니다."));

        String code = generate6DigitCode();

        // 기존 코드가 있으면 재사용, 없으면 새로 생성
        PasswordResetCode resetCode = passwordResetCodeRepository.findByUserId(user.getId())
                .map(existing -> {
                    existing.reset(email, code);
                    return existing;
                })
                .orElseGet(() -> new PasswordResetCode(user, email, code));

        passwordResetCodeRepository.save(resetCode);

        sendResetCodeMail(email, code);

        return PasswordFindResponse.of(email, RESET_CODE_EXPIRES_SECONDS);
    }

    // 비밀번호 재설정 - 코드 검증 및 비밀번호 변경
    @Override
    @Transactional
    public PasswordResetResponse resetPassword(PasswordResetVerifyCodeRequest request) {
        String email = request.getEmail();
        String code = request.getCode();
        String newPassword = request.getNewPassword();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("가입되지 않은 이메일입니다."));

        PasswordResetCode resetCode = passwordResetCodeRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESET_CODE_NOT_FOUND));

        if (resetCode.isUsed()) {
            throw new BusinessException(ErrorCode.RESET_CODE_ALREADY_USED);
        }

        if (resetCode.isExpired()) {
            throw new BusinessException(ErrorCode.EXPIRED_RESET_CODE);
        }

        if (!resetCode.getCode().equals(code)) {
            throw new BusinessException(ErrorCode.INVALID_RESET_CODE);
        }

        user.setPassword(passwordEncoder.encode(newPassword));

        resetCode.markAsUsed();
        passwordResetCodeRepository.save(resetCode);

        return new PasswordResetResponse(user.getId());
    }

    // 이메일 발송 처리
    private void sendResetCodeMail(String email, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        //message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("[BN] 비밀번호 재설정 인증코드");
        message.setText(
                "비밀번호 재설정 인증코드입니다.\n\n" +
                        "인증코드: " + code + "\n\n" +
                        "인증코드는 5분 후 만료됩니다."
        );

        mailSender.send(message);
    }

    // 6자리 인증 코드 생성
    private String generate6DigitCode() {
        SecureRandom r = new SecureRandom();
        int n = r.nextInt(900000) + 100000;
        return String.valueOf(n);
    }


}

package org.etmetmy.bn_server.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.company.entity.Company;
import org.etmetmy.bn_server.domain.company.entity.CompanyType;
import org.etmetmy.bn_server.domain.company.repository.CompanyRepository;
import org.etmetmy.bn_server.domain.user.dto.MemberSearchCondition;
import org.etmetmy.bn_server.domain.user.dto.MemberUpdateRequest;
import org.etmetmy.bn_server.domain.user.dto.entity.UserDto;
import org.etmetmy.bn_server.domain.user.dto.request.UserLoginDto;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.etmetmy.bn_server.domain.user.repository.UserRepository;
import org.etmetmy.bn_server.domain.user.specification.UserSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserService {
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    // 회원 정보 수정 (Update)
    @Transactional
    Long updateMember(Long memberId, MemberUpdateRequest request);
    public void updateMember(Long memberId, MemberUpdateRequest request) {
        // 1. 회원 찾기
        User user = userRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 2. 바꿀 회사 찾기
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회사입니다."));

        // 3. 정보 변경 (dirty checking)
        user.updateInfo(request.getName(), request.getEmail(), company, request.getRole());
    }

    // 2. 회원 삭제 (Delete)
    @Transactional
    Long deleteMember(Long memberId);
    public void deleteMember(Long memberId) {
        // 존재 여부 확인 후 삭제
        User user = userRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        userRepository.delete(user);
    }


    // 검색 & 조회 기능
    public List<User> searchMembers(String name, String email, String companyName, String type) {
        // 1. 초기화 (조건 없음)
        Specification<User> spec = Specification.where(null);

    List<User> searchMembers(String name, String email, String companyName, String type);
    Long joinUser(UserDto userDto);
    User login(UserLoginDto loginDto);
        // 2. 이름 조건 추가
        if (name != null && !name.isBlank()) {
            spec = spec.and(UserSpecification.likeName(name));
        }

        // 3. 이메일 조건 추가
        if (email != null && !email.isBlank()) {
            spec = spec.and(UserSpecification.likeEmail(email));
        }

        // 4. 회사명 조건 추가
        if (companyName != null && !companyName.isBlank()) {
            spec = spec.and(UserSpecification.likeCompanyName(companyName));
        }

        // 5. 회사 타입 조건 추가 (DEVELOPER or CLIENT)
        if (type != null && !type.isBlank()) {
            try {
                // 문자로 들어온 "DEVELOPER"를 Enum으로 변환
                CompanyType companyType = CompanyType.valueOf(type.toUpperCase());
                spec = spec.and(UserSpecification.equalCompanyType(companyType));
            } catch (IllegalArgumentException e) {
                // 이상한 타입이 들어오면 무시
            }
        }

        return userRepository.findAll(spec);
    }
}
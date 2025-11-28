package org.etmetmy.bn_server.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.company.entity.Company;
import org.etmetmy.bn_server.domain.company.entity.CompanyType;
import org.etmetmy.bn_server.domain.company.repository.CompanyRepository;
import org.etmetmy.bn_server.domain.user.dto.MemberSearchCondition;
import org.etmetmy.bn_server.domain.user.dto.MemberUpdateRequest;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.etmetmy.bn_server.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.etmetmy.bn_server.domain.company.entity.CompanyType;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    // 회원 정보 수정 (Update)
    @Transactional
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

        // 2. 이름 조건 추가
        if (name != null && !name.isBlank()) {
            spec = spec.and(UserSpecification.likeName(name));
        }

        CompanyType companyType = null;
        // 3. 이메일 조건 추가
        if (email != null && !email.isBlank()) {
            spec = spec.and(UserSpecification.likeEmail(email));
        }

        // 1. String 타입의 type 파라미터를 Enum으로 변환
        // 4. 회사명 조건 추가
        if (companyName != null && !companyName.isBlank()) {
            spec = spec.and(UserSpecification.likeCompanyName(companyName));
        }

        // 5. 회사 타입 조건 추가 (DEVELOPER or CLIENT)
        if (type != null && !type.isBlank()) {
            try {
                companyType = CompanyType.valueOf(type.toUpperCase());
                // 문자로 들어온 "DEVELOPER"를 Enum으로 변환
                CompanyType companyType = CompanyType.valueOf(type.toUpperCase());
                spec = spec.and(UserSpecification.equalCompanyType(companyType));
            } catch (IllegalArgumentException e) {
                // 잘못된 type 값은 무시하고 null로 (쿼리에서 IS NULL 처리됨)
                // 이상한 타입이 들어오면 무시
            }
        }

        // 2. Repository의 @Query 메서드 호출
        return userRepository.findDynamicMembers(
                name,
                email,
                companyName,
                companyType // Enum 값 전달
        );
    }
}
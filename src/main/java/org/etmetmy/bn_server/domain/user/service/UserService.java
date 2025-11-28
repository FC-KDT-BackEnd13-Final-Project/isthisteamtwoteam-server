package org.etmetmy.bn_server.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.etmetmy.bn_server.domain.company.entity.Company;
import org.etmetmy.bn_server.domain.company.entity.CompanyType;
import org.etmetmy.bn_server.domain.company.repository.CompanyRepository;
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

        CompanyType companyType = null;

        // 1. String 타입의 type 파라미터를 Enum으로 변환
        if (type != null && !type.isBlank()) {
            try {
                companyType = CompanyType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                // 잘못된 type 값은 무시하고 null로 (쿼리에서 IS NULL 처리됨)
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
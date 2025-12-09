package org.etmetmy.bn_server.domain.user.repository;

import org.etmetmy.bn_server.domain.company.entity.CompanyType;
import org.etmetmy.bn_server.domain.project.dto.response.ProjectMemberSearchResponse;
import org.etmetmy.bn_server.domain.user.dto.response.UserProfileImgNameResponse;
import org.etmetmy.bn_server.domain.user.entity.Role;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param; // Param import
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u JOIN u.company c " +
            "WHERE (:name IS NULL OR u.name LIKE %:name%) " +
            "AND (:email IS NULL OR u.email LIKE %:email%) " +
            "AND (:companyName IS NULL OR c.companyName LIKE %:companyName%) " +
            "AND (:companyType IS NULL OR c.type = :companyType)")
    List<User> findByNamicMembers(
            @Param("name") String name,
            @Param("email") String email,
            @Param("companyName") String companyName,
            @Param("companyType") CompanyType companyType // Enum 타입으로 받음
    );
    @Query("SELECT new org.etmetmy.bn_server.domain.user.dto.response.UserProfileImgNameResponse(u.profileImg, u.name) " +
            "FROM User u " +
            "WHERE u.id = :userId")
    Optional<UserProfileImgNameResponse> findProfileImgAndNameByUserId(@Param("userId") Long userId);



     //회사 타입(개발사/고객사) 기준 전체 사원/담당자 조회
    @Query("SELECT new org.etmetmy.bn_server.domain.project.dto.response.ProjectMemberSearchResponse(" +
            "u.id, u.name, u.email, u.company.companyId, u.company.companyName) " +
            "FROM User u " +
            "WHERE u.company.type = :companyType")
    List<ProjectMemberSearchResponse> findByCompanyType(@Param("companyType") CompanyType companyType);

    // 개발사 조회 - 회사명 NULL로 반환 (Admin 제외)
    @Query("SELECT new org.etmetmy.bn_server.domain.project.dto.response.ProjectMemberSearchResponse(" +
            "u.id, u.name, u.email, c.companyId, NULL) " +    // 회사명 NULL
            "FROM User u " +
            "LEFT JOIN u.company c " +
            "WHERE u.role <> org.etmetmy.bn_server.domain.user.entity.Role.ADMIN " +
            "AND (" +
            "       (c IS NOT NULL AND c.type = org.etmetmy.bn_server.domain.company.entity.CompanyType.DEVELOPER) " +
            "    OR (c IS NULL AND u.role = org.etmetmy.bn_server.domain.user.entity.Role.DEVELOPER) " +
            ")")
    List<ProjectMemberSearchResponse> findDeveloperCandidates();

    //고객사 조회 (관리자 제외, 회사 있으면 type=CLIENT, 없으면 role=CUSTOMER)

    @Query("SELECT new org.etmetmy.bn_server.domain.project.dto.response.ProjectMemberSearchResponse(" +
            "u.id, u.name, u.email, c.companyId, c.companyName) " +  // 회사명 포함
            "FROM User u " +
            "LEFT JOIN u.company c " +
            "WHERE u.role <> org.etmetmy.bn_server.domain.user.entity.Role.ADMIN " +
            "AND (" +
            "       (c IS NOT NULL AND c.type = org.etmetmy.bn_server.domain.company.entity.CompanyType.CLIENT) " +
            "    OR (c IS NULL AND u.role = org.etmetmy.bn_server.domain.user.entity.Role.CUSTOMER) " +
            ")")
    List<ProjectMemberSearchResponse> findClientCandidates();


}
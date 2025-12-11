package org.etmetmy.bn_server.domain.user.repository;

import org.etmetmy.bn_server.domain.company.entity.CompanyType;
import org.etmetmy.bn_server.domain.user.dto.response.UserProfileImgNameResponse;
import org.etmetmy.bn_server.domain.user.entity.Role;
import org.etmetmy.bn_server.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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


    // 개발사 조회 (Admin 제외, role=DEVELOPER)
    @Query("SELECT u FROM User u WHERE u.role = 'DEVELOPER' ")
    List<User> findDeveloperCandidates();

    // 고객사 조회 (관리자 제외, role=CUSTOMER)
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.company c WHERE u.role = 'CUSTOMER' ")
    List<User> findClientCandidates();
}

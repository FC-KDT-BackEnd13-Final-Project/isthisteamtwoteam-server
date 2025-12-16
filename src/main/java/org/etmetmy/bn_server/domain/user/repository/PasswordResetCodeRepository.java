package org.etmetmy.bn_server.domain.user.repository;

import org.etmetmy.bn_server.domain.user.entity.PasswordResetCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetCodeRepository extends JpaRepository<PasswordResetCode, Long> {
    Optional<PasswordResetCode> findByEmail(String email);

    Optional<PasswordResetCode> findByUserId(Long userId);


}

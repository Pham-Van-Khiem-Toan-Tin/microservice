package com.ecommerce.identityservice.repository;

import com.ecommerce.identityservice.entity.UserEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Map;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {
    @Query(value = "SELECT u.email, u.password, u.first_name, u.last_name, u.lock_time, u.login_fail_count, u.unlock_time, u.block, r.name AS role, GROUP_CONCAT(DISTINCT rfs.function_id) AS function_group, GROUP_CONCAT(DISTINCT rfs.subfunction_id) AS subfunction_group " +
            "FROM users u " +
            "JOIN roles r " +
            "ON u.role = r.id " +
            "JOIN role_function_subfunction rfs " +
            "ON r.id = rfs.role_id " +
            "WHERE u.email = :e " +
            "GROUP BY u.email", nativeQuery = true)
    Map<String, Object> findAuthProfile(@Param("e") String email);
    @Modifying
    @Transactional
    @Query(value = "update UserEntity u set u.lockTime = :lt, u.unlockTime = :ut, u.loginFailCount = :lgc where u.email = :em")
    int updateTemporaryLock(@Param("em") String id, @Param("lt") LocalDateTime lockTime, @Param("ut") LocalDateTime unlockTime, @Param("lgc") Integer loginFailCount);

    UserEntity findByEmail(String email);
}

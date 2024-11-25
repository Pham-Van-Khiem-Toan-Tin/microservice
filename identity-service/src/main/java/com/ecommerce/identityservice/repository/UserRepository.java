package com.ecommerce.identityservice.repository;

import com.ecommerce.identityservice.dto.UserDTO;
import com.ecommerce.identityservice.entity.UserEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, String> {
    @Query(value = "SELECT u.email, u.password, u.first_name, u.last_name, u.lock_time, u.login_fail_count, u.unlock_time, u.block, r.name AS role, GROUP_CONCAT(DISTINCT rfs.function_id) AS function_group, GROUP_CONCAT(DISTINCT rfs.subfunction_id) AS subfunction_group " +
            "FROM users u " +
            "JOIN roles r " +
            "ON u.role = r.id " +
            "JOIN role_function_subfunction rfs " +
            "ON r.id = rfs.role_id " +
            "WHERE u.email = :e " +
            "GROUP BY u.email", nativeQuery = true)
    Map<String, Object> findUserDetailById(@Param("e") String email);

    @Modifying
    @Transactional
    @Query(value = "update users as u set u.lock_time = :lt, u.unlock_time = :ut, u.login_fail_count = :lgc where u.email = :em", nativeQuery = true)
    int updateTemporaryLock(@Param("em") String id, @Param("lt") LocalDateTime lockTime, @Param("ut") LocalDateTime unlockTime, @Param("lgc") Integer loginFailCount);
}

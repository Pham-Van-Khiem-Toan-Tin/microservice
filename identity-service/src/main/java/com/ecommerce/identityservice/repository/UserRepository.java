package com.ecommerce.identityservice.repository;

import com.ecommerce.identityservice.dto.UserDTO;
import com.ecommerce.identityservice.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, String> {
    @Query(value = "SELECT u.email, u.password, u.first_name, u.last_name, r.name AS role, GROUP_CONCAT(DISTINCT rfs.function_id) AS function_group, GROUP_CONCAT(DISTINCT rfs.subfunction_id) AS subfunction_group " +
            "FROM users u " +
            "JOIN roles r " +
            "ON u.role = r.id " +
            "JOIN role_function_subfunction rfs " +
            "ON r.id = rfs.role_id " +
            "WHERE u.email = :e " +
            "GROUP BY u.email", nativeQuery = true)
    Map<String, Object> findUserDetailById(@Param("e") String email);
}

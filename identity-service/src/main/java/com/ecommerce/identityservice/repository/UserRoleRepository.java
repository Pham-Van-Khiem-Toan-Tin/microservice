package com.ecommerce.identityservice.repository;

import com.ecommerce.identityservice.entity.UserRole;
import com.ecommerce.identityservice.entity.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
    @Query("select u from UserRole u where u.user.email = :userId")
    Set<UserRole> findAllByUserId(@Param("userId") String userId);
}

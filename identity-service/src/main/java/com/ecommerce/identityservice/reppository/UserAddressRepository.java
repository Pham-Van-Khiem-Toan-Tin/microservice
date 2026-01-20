package com.ecommerce.identityservice.reppository;

import com.ecommerce.identityservice.entity.UserAddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserAddressRepository extends JpaRepository<UserAddressEntity, UUID> {
    List<UserAddressEntity> findByUserId(UUID user_id);
    long countByUserId(UUID userId);
    Optional<UserAddressEntity> findFirstByUserIdAndIsDefaultTrue(UUID userId);
    List<UserAddressEntity> findAllByUserIdOrderByIsDefaultDescCreatedAtDesc(UUID userId);

    // Tìm địa chỉ mặc định cũ để update khi user chọn địa chỉ mới làm mặc định
    Optional<UserAddressEntity> findByUserIdAndIsDefaultTrue(UUID userId);
}

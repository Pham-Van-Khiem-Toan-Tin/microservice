package com.ecommerce.identityservice.repository;

import com.ecommerce.identityservice.entity.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<ClientEntity, String> {

}

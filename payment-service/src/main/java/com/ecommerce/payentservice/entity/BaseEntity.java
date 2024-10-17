package com.ecommerce.payentservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Data;

import java.util.Date;

@Data
public class BaseEntity {
    @Column(nullable = false)
    private Date createdAt;
    @Column
    private Date updatedAt;
}

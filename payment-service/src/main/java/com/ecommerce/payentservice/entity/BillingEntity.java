package com.ecommerce.payentservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@Table(name = "billing")
public class BillingEntity {
    @Id
    private String customerId;
    @Column
    private String firstName;
    @Column
    private String lastName;
    @Column
    private String company;
    @Column
    private String address;
    @Column
    private String country;
    @Column
    private String states;
    @Column
    private String zipCode;
    @Column
    private String email;
    @Column
    private String phoneNumber;
    @Column(nullable = false)
    private LocalDateTime createdAt;
    @Column
    private LocalDateTime updatedAt;
}

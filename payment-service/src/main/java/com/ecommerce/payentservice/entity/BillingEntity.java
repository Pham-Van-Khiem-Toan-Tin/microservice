package com.ecommerce.payentservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@Table(name = "billing")
public class BillingEntity extends BaseEntity {
    @Id
    private String customerId;
    @Column(nullable = false)
    private String firstName;
    @Column(nullable = false)
    private String lastName;
    @Column
    private String company;
    @Column(nullable = false)
    private String addressStreet;
    @Column(nullable = false)
    private String country;
    @Column(nullable = false)
    private String states;
    @Column(nullable = false)
    private String zipCode;
    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    private String phoneNumber;
    @Column(nullable = false)
    private Date createdAt;
    @Column
    private Date updatedAt;
}
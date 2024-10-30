package com.ecommerce.payentservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@Table(name = "payment")
public class PaymentEntity extends BaseEntity {
    @Id
    private Long customerId;
    @Column(nullable = false)
    private String firstName;
    @Column(nullable = false)
    private String lastName;
    @Column
    private String companyName;
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

package com.ecommerce.payentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {
    private String firstName;
    private String lastName;
    private String address;
    private String company;
    private String email;
    private String phoneNumber;
    private String country;
    private Long states;
    private String zipCode;
}

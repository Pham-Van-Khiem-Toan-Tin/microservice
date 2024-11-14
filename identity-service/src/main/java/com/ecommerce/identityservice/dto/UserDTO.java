package com.ecommerce.identityservice.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserDTO {
    private String firstName;
    private String lastName;
    private String role;
    private String email;
    private String phoneNumber;
    private String avatar;
    private BillingDTO billing;
    private List<OrderDTO> orderList;
}

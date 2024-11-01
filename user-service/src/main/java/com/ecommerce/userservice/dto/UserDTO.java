package com.ecommerce.userservice.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserDTO {
    private String firstName;
    private String lastName;
    private String role;
    private String avatar;
    private String phoneNumber;
    private String email;
    private BillingDTO billing;
    private List<OrderDTO> orderList;
}

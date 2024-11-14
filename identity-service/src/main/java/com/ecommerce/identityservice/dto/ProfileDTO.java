package com.ecommerce.identityservice.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProfileDTO {
    private String given_name;
    private String family_name;
    private String email;
    private String phoneNumber;
    private String avatar;
    private List<String> groups;
}

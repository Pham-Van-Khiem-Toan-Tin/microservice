package com.ecommerce.identityservice.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UpdateProfileForm {
    private String firstName;
    private String lastName;
    private String phone;
    private MultipartFile avatar;
}

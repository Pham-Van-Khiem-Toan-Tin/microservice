package com.ecommerce.identityservice.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AuthoritiesWithParentForm {
    @NotNull(message = "Dữ liệu không hợp lệ.")
    @NotBlank(message = "Dữ liệu không hợp lệ.")
    private String clientId;
    @NotNull(message = "Dữ liệu không hợp lệ.")
    @NotBlank(message = "Dữ liệu không hợp lệ.")
    private String id;
    @NotNull(message = "Dữ liệu không hợp lệ.")
    @NotBlank(message = "Dữ liệu không hợp lệ.")
    private String name;
    @NotNull(message = "Dữ liệu không hợp lệ.")
    @NotBlank(message = "Dữ liệu không hợp lệ.")
    private String description;
    @NotNull(message = "Dữ liệu không hợp lệ.")
    @NotBlank(message = "Dữ liệu không hợp lệ.")
    private String parentId;
}

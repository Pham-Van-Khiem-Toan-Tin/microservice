package com.ecommerce.catalogservice.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CloudinaryUploadResult {
    private String url;
    private String publicId;
}

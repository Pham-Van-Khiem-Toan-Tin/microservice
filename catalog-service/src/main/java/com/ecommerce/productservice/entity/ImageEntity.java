package com.ecommerce.productservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ImageEntity {
    private String bucket;
    private String key;
    private String url;
    private String etag;
    private String contentType;
}

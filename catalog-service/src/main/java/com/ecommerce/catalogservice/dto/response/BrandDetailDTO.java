package com.ecommerce.catalogservice.dto.response;

import com.ecommerce.catalogservice.entity.BrandStatus;
import com.ecommerce.catalogservice.entity.ImageEntity;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class BrandDetailDTO {
    private String id;
    private String name;
    private String slug;
    private String description;
    private List<CategoryOptionDTO> categories;
    private ImageEntity logo;
    private BrandStatus status;
    private String createdDate;
    private String updatedDate;
}

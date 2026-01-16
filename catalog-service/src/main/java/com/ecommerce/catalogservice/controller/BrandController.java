package com.ecommerce.catalogservice.controller;

import com.ecommerce.catalogservice.dto.request.brand.BrandCreateForm;
import com.ecommerce.catalogservice.dto.request.brand.BrandEditForm;
import com.ecommerce.catalogservice.dto.request.brand.BrandSearchField;
import com.ecommerce.catalogservice.dto.response.ApiResponse;
import com.ecommerce.catalogservice.dto.response.BrandDTOS;
import com.ecommerce.catalogservice.dto.response.BrandDetailDTO;
import com.ecommerce.catalogservice.dto.response.BrandOptionDTOS;
import com.ecommerce.catalogservice.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import static com.ecommerce.catalogservice.constants.Constants.*;

import java.util.List;

@RestController
@RequestMapping("brands")
public class BrandController {
    @Autowired
    private BrandService brandService;

    @PreAuthorize("hasAuthority('VIEW_BRAND_LIST')")
    @GetMapping
    public Page<BrandDTOS> getBrands(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false, name = "fields") List<BrandSearchField> fields,
            @RequestParam(defaultValue = "name:asc") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Sort sortObj = parseSort(sort, Sort.by("id").ascending());
        Pageable pageable = PageRequest.of(page, size, sortObj);
        return brandService.searchBrands(keyword, fields, pageable);
    }
    @PreAuthorize("hasAuthority('VIEW_BRAND_LIST')")
    @GetMapping("/options")
    public List<BrandOptionDTOS> getBrandOptions() {
        return brandService.getBrandOptions();
    }
    @PreAuthorize("hasAuthority('VIEW_BRAND')")
    @GetMapping("/{id}")
    public BrandDetailDTO getBrand(@PathVariable String id) {
        return brandService.getBrand(id);
    }
    @PreAuthorize("hasAuthority('CREATE_BRAND')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Void> createBrand(
            @RequestPart("data") BrandCreateForm brandDTOS,
            @RequestPart("logo") MultipartFile logo) {
        brandService.addBrand(brandDTOS, logo);
        return ApiResponse.ok(BRAND_CREATE_SUCCESS);
    }
    @PreAuthorize("hasAuthority('EDIT_BRAND')")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Void> updateBrand(
            @RequestPart("data") BrandEditForm form,
            @RequestPart(value = "logo", required = false) MultipartFile logo,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @PathVariable String id) {
        brandService.updateBrand(form, logo, idempotencyKey, id);
        return ApiResponse.ok(BRAND_EDIT_SUCCESS);
    }
    @PreAuthorize("hasAuthority('EDIT_BRAND')")
    @PatchMapping("/{id}")
    public ApiResponse<Void> changeActive(@PathVariable String id) {
        brandService.toggleActiveBrand(id);
        return ApiResponse.ok(BRAND_EDIT_SUCCESS);
    }
    @PreAuthorize("hasAuthority('DELETE_BRAND')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> updateBrand(@PathVariable String id) {
        brandService.deleteBrand(id);
        return ApiResponse.ok(BRAND_EDIT_SUCCESS);
    }


    private Sort parseSort(String raw, Sort fallback) {
        try {
            if (raw == null || raw.isBlank()) return fallback;
            String[] parts = raw.split(":");
            String field = parts[0];
            Sort.Direction dir =
                    (parts.length > 1 && "desc".equalsIgnoreCase(parts[1]))
                            ? Sort.Direction.DESC
                            : Sort.Direction.ASC;
            return Sort.by(dir, field);
        } catch (Exception e) {
            return fallback;
        }
    }
}

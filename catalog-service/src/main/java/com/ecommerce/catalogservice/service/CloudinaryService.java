package com.ecommerce.catalogservice.service;

import com.cloudinary.Cloudinary;
import com.ecommerce.catalogservice.constants.Constants;
import com.ecommerce.catalogservice.dto.response.BusinessException;
import com.ecommerce.catalogservice.dto.response.CloudinaryUploadResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class CloudinaryService {
    @Autowired
    private Cloudinary cloudinary;

    public CloudinaryUploadResult uploadImage(MultipartFile file, String folder) {
        try {
            Map<String, Object> options = Map.of(
                    "folder", folder,
                    "resource_type", "image"
            );
            Map<?, ?> res = cloudinary.uploader().upload(file.getBytes(), options);
            return CloudinaryUploadResult
                    .builder()
                    .url((String) res.get("secure_url"))
                    .publicId((String) res.get("public_id"))
                    .build();
        } catch (Exception e) {
            throw new BusinessException(Constants.INTERNAL_ERROR);
        }
    }

    public void deleteImage(String publicId, String folder) {
        try {
            Map<String, Object> options = Map.of(
                    "folder", folder,
                    "resource_type", "image"
            );
            Map<?, ?> res = cloudinary.uploader().destroy(publicId, options);
            String status = (String) res.get("result");

            if (!"ok".equals(status)) {
                // Trường hợp ảnh không tồn tại hoặc lỗi khác từ phía Cloudinary
                throw new BusinessException(Constants.INTERNAL_ERROR);
            }
        } catch (Exception e) {
            throw new BusinessException(Constants.INTERNAL_ERROR);
        }
    }

    public void deleteManyImage(List<String> publicIds) {
        try {
            Map<String, Object> options = Map.of(
                    "resource_type", "image"
            );
            Map<?, ?> res = cloudinary.api().deleteResources(publicIds, options);
            Map<String, String> deletedStatus = (Map<String, String>) res.get("deleted");
            if (deletedStatus != null) {
                List<String> failedIds = new ArrayList<>();

                deletedStatus.forEach((id, status) -> {
                    // Status thường là: "deleted", "not_found"
                    if (!"deleted".equals(status)) {
                        failedIds.add(id + " (" + status + ")");
                    }
                });

                if (!failedIds.isEmpty()) {
                    // Chỉ System.err hoặc Log.warn chứ KHÔNG NÊN throw Exception
                    // Vì mục đích là dọn dẹp, nếu không xóa được (do không tồn tại) thì cũng coi như xong.
                    log.error("Cloudinary Bulk Delete Warning - Failed items: {}", failedIds);
                }
            }
        } catch (Exception e) {
            log.error("Cloudinary Bulk Delete Warning", e);
            throw new BusinessException(Constants.INTERNAL_ERROR);
        }
    }
}

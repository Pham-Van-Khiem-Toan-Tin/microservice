package com.ecommerce.catalogservice.service;

import com.cloudinary.Cloudinary;
import com.ecommerce.catalogservice.constants.Constants;
import com.ecommerce.catalogservice.dto.response.BusinessException;
import com.ecommerce.catalogservice.dto.response.CloudinaryUploadResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

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
            Map<?,?> res = cloudinary.uploader().upload(file.getBytes(), options);
            return CloudinaryUploadResult
                    .builder()
                    .url((String) res.get("secure_url"))
                    .publicId((String) res.get("public_id"))
                    .build();
        } catch (Exception e) {
            throw new BusinessException(Constants.CREATE_CATEGORY_FAIL);
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
                throw new BusinessException(Constants.UPDATE_CATEGORY_FAIL);
            }
        } catch (Exception e) {
            throw new BusinessException(Constants.UPDATE_CATEGORY_FAIL);
        }
    }
}

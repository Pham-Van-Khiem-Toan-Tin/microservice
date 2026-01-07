//package com.ecommerce.catalogservice.service.impl;
//
//import com.ecommerce.catalogservice.dto.request.product.ProductCreateForm;
//import com.ecommerce.catalogservice.dto.response.BusinessException;
//import com.ecommerce.catalogservice.repository.BrandRepository;
//import com.ecommerce.catalogservice.repository.CategoryRepository;
//import com.ecommerce.catalogservice.repository.ProductRepository;
//import com.ecommerce.catalogservice.repository.SkuRepository;
//import com.ecommerce.catalogservice.service.CloudinaryService;
//import com.ecommerce.catalogservice.service.ProductService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.util.StringUtils;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import static com.ecommerce.catalogservice.constants.Constants.*;
//
//@Service
//public class ProductServiceImpl implements ProductService {
//    @Autowired
//    private ProductRepository productRepository;
//    @Autowired
//    private CategoryRepository categoryRepository;
//    @Autowired
//    private BrandRepository brandRepository;
//    @Autowired
//    private CloudinaryService cloudinaryService;
//    @Autowired
//    private SkuRepository skuRepository;
//    @Transactional
//    @Override
//    public void addProduct(ProductCreateForm form) {
//        if (!StringUtils.hasText(form.getName())
//                || !StringUtils.hasText(form.getDescription())
//                || !StringUtils.hasText(form.getSlug())
//                || !StringUtils.hasText(form.getShortDescription())
//                || !StringUtils.hasText(form.getBrandId())
//                || !StringUtils.hasText(form.getCategoryId())
//                || form.getSpecs() == null || form.getSpecs().isEmpty()
//                || form.getThumbnail() == null || form.getThumbnail().isEmpty()
//                || form.getGallery() == null || form.getGallery().isEmpty()
//        ) throw new BusinessException(VALIDATE_FAIL);
//        String thumbnailId = null;
//        List<String> galleryIds = null;
//        List<String> skuIds = new ArrayList<>();
//        try {
////            CloudinaryUploadResult thumbnailResult = cloudinaryService.uploadImage(form.getThumbnail(), "products");
////            thumbnailId = thumbnailResult.getPublicId();
////            List<ImageEntity> galleryImages = new ArrayList<>();
////            for (MultipartFile file : form.getGallery()) {
////                CloudinaryUploadResult galleryResult = cloudinaryService.uploadImage(file, "products");
////                ImageEntity galleryItem = new ImageEntity(galleryResult.getUrl(), galleryResult.getPublicId());
////                galleryImages.add(galleryItem);
////            }
////            galleryIds = galleryImages.stream().map(ImageEntity::getImagePublicId).toList();
////            ImageEntity thumbnail = new ImageEntity(thumbnailResult.getUrl(), thumbnailResult.getPublicId());
////            CategoryEntity categoryEntity = categoryRepository.findById(form.getCategoryId()).orElseThrow(
////                    () -> new BusinessException(VALIDATE_FAIL)
////            );
////            BrandEntity brandEntity = brandRepository.findById(form.getBrandId()).orElseThrow(
////                    () -> new BusinessException(VALIDATE_FAIL)
////            );
////            Instant currentTime = Instant.now();
////            ProductEntity product = ProductEntity.builder()
////                    .name(form.getName())
////                    .slug(form.getSlug())
////                    .thumbnail(thumbnail)
////                    .gallery(galleryImages)
////                    .brandId(brandEntity.getId())
////                    .hasVariant(form.getHasVariants())
////                    .numberOfReviews(0)
////                    .averageRating(0.0)
////                    .specs(form.getSpecs())
////                    .attributes(form.getAttributes())
////                    .status(ProductStatus.draft)
////                    .categoryId(categoryEntity.getId())
////                    .description(form.getDescription())
////                    .shortDescription(form.getShortDescription())
////                    .createdAt(currentTime)
////                    .updatedAt(currentTime)
////                    .build();
////            ProductEntity updatedProduct = productRepository.save(product);
////            if (!form.getHasVariants()) {
////                SkuEntity skuEntity = SkuEntity.builder()
////                        .spuId(updatedProduct.getId())
////                        .name(form.getName())
////                        .price(form.getPrice())
////                        .originalPrice(form.getPrice())
////                        .stock(form.getStock())
////                        .isAvailable(true)
////                        .soldCount(0L)
////                        .image(thumbnail)
////                        .createdAt(updatedProduct.getCreatedAt())
////                        .updatedAt(updatedProduct.getUpdatedAt())
////                        .build();
////                skuRepository.save(skuEntity);
////            } else {
////                List<SkuEntity> skuEntities = new ArrayList<>();
////                for (SkuItemForm sku : form.getSkus()) {
//////                    CloudinaryUploadResult skuResult = cloudinaryService.uploadImage(sku.getImage(), "skus");
//////                    skuIds.add(skuResult.getPublicId());
//////                    ImageEntity skuImageItem = new ImageEntity(skuResult.getUrl(), skuResult.getPublicId());
////                    SkuEntity skuItem = SkuEntity.builder()
////                            .spuId(updatedProduct.getId())
////                            .name(sku.getName())
//////                            .image(skuImageItem)
////                            .price(sku.getPrice())
////                            .originalPrice(sku.getPrice())
//////                            .stock(sku.getStock())
////                            .isAvailable(true)
////                            .soldCount(0L)
////                            .createdAt(updatedProduct.getCreatedAt())
////                            .updatedAt(updatedProduct.getUpdatedAt())
////                            .build();
////                    skuEntities.add(skuItem);
////                }
////                skuRepository.saveAll(skuEntities);
////            }
//        } catch (Exception e) {
//            if (StringUtils.hasText(thumbnailId)) {
//                cloudinaryService.deleteImage(thumbnailId, "products");
//            }
//            if (galleryIds != null) {
//                cloudinaryService.deleteManyImage(galleryIds);
//            }
//            if (!skuIds.isEmpty()) {
//                cloudinaryService.deleteManyImage(skuIds);
//            }
//            throw e;
//        }
//    }
////    @Autowired
////    private AttributeSetRepository attributeSetRepository;
//}

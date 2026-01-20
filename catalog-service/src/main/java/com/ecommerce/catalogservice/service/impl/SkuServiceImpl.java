package com.ecommerce.catalogservice.service.impl;

import com.ecommerce.catalogservice.dto.response.cart.SkuCartDTO;
import com.ecommerce.catalogservice.dto.response.cart.SkuSelectionDTO;
import com.ecommerce.catalogservice.entity.ProductEntity;
import com.ecommerce.catalogservice.entity.ProductOptionItem;
import com.ecommerce.catalogservice.entity.ProductVariantGroup;
import com.ecommerce.catalogservice.entity.SkuEntity;
import com.ecommerce.catalogservice.repository.ProductRepository;
import com.ecommerce.catalogservice.repository.SkuRepository;
import com.ecommerce.catalogservice.service.SkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SkuServiceImpl implements SkuService {
    @Autowired
    private SkuRepository skuRepository;
    @Autowired
    private ProductRepository productRepository;

    @Override
    public List<SkuCartDTO> getSkuCartDTO(List<String> skuIds) {
        List<SkuEntity> skus = skuRepository.findAllByIdIn(skuIds);
        Set<String> productIds = skus.stream().map(SkuEntity::getSpuId)
                .collect(Collectors.toSet());
        List<ProductEntity> products = productRepository.findAllByIdIn(productIds);
        Map<String, ProductEntity> productMap = products.stream()
                .collect(Collectors.toMap(ProductEntity::getId, p -> p));

        return skus.stream().map(
                sk -> SkuCartDTO.builder()
                        .id(sk.getId())
                        .spuId(sk.getSpuId())
                        .skuCode(sk.getSkuCode())
                        .spuName(productMap.get(sk.getSpuId()).getName())
                        .skuName(sk.getName())
                        .thumbnail(sk.getThumbnail().getImageUrl())
                        .price(sk.getPrice())
                        .selections(sk.getSelections().stream()
                                .map(
                                        o -> {
                                            Map<String,List<ProductOptionItem>> optionMap = productMap
                                                    .get(sk.getSpuId())
                                                    .getVariantGroups()
                                                    .stream()
                                                    .collect(Collectors.toMap(ProductVariantGroup::getId, ProductVariantGroup::getValues));
                                            Map<String, String> groupMapName = productMap
                                                    .get(sk.getSpuId())
                                                    .getVariantGroups()
                                                    .stream()
                                                    .collect(Collectors.toMap(ProductVariantGroup::getId, ProductVariantGroup::getLabel));
                                            return SkuSelectionDTO
                                                    .builder()
                                                    .groupName(groupMapName.get(o.getGroupId()))
                                                    .label(Objects.requireNonNull(optionMap
                                                            .get(o.getGroupId())
                                                            .stream()
                                                            .filter(l -> l.getId().equals(o.getValueId()))
                                                            .findFirst()
                                                            .orElse(null)).getValue()
                                                    )
                                                    .build();
                                        }
                                )
                                .toList())
                        .build()
        ).toList();
    }
}

package com.ecommerce.catalogservice.service.impl;

import static com.ecommerce.catalogservice.constants.Constants.*;

import com.ecommerce.catalogservice.dto.request.*;
import com.ecommerce.catalogservice.dto.response.AttributeDTO;
import com.ecommerce.catalogservice.dto.response.BusinessException;
import com.ecommerce.catalogservice.entity.AttributeDataType;
import com.ecommerce.catalogservice.entity.AttributeEntity;
import com.ecommerce.catalogservice.entity.OptionEntity;
import com.ecommerce.catalogservice.repository.AttributeRepository;
import com.ecommerce.catalogservice.service.AttributeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AttributeServiceImpl implements AttributeService {
    @Autowired
    private AttributeRepository attributeRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Page<AttributeDTO> search(String keyword, List<AttributeSearchField> fields, Pageable pageable) {
        Query query = new Query();
        Set<AttributeSearchField> fs = (fields == null || fields.isEmpty())
                ? EnumSet.of(AttributeSearchField.label)
                : EnumSet.copyOf(fields);
        if (StringUtils.hasText(keyword)) {
            List<Criteria> ors = new ArrayList<>();
            for (AttributeSearchField field : fs) {
                switch (field) {
                    case label -> ors.add(Criteria.where("label").regex(keyword, "i"));
                    case code -> ors.add(Criteria.where("code").regex(keyword, "i"));
                    case type -> ors.add(Criteria.where("type").regex(keyword, "i"));
                }
            }
            query.addCriteria(new Criteria().orOperator(ors));
        }
        long total = mongoTemplate.count(query, AttributeEntity.class);
        query.with(pageable);
        List<AttributeEntity> atbs = mongoTemplate.find(query, AttributeEntity.class);
        List<AttributeDTO> attributeDTOS = atbs.stream().map(
                        atb -> AttributeDTO.builder()
                                .id(atb.getId())
                                .code(atb.getCode())
                                .label(atb.getLabel())
                                .dataType(atb.getDataType())
                                .build()
                )
                .toList();
        return new PageImpl<>(attributeDTOS, pageable, total);
    }

    @Override
    public List<AttributeDetailDTO> searchAttributeOption(AttributeOptionForm form) {
        Query query = new Query();
        if (form.getAttributeIds() != null && !form.getAttributeIds().isEmpty()) {
            query.addCriteria(Criteria.where("_id").nin(form.getAttributeIds()));
        }
        if (StringUtils.hasText(form.getKeyword())) {
            Criteria criteria = new Criteria().orOperator(
                    Criteria.where("code").regex(form.getKeyword(), "i"),
                    Criteria.where("label").regex(form.getKeyword(), "i")
            );
            query.addCriteria(criteria);
        }
        List<AttributeEntity> attributes = mongoTemplate.find(query, AttributeEntity.class);
        return attributes.stream().map(
                at -> AttributeDetailDTO.builder()
                        .id(at.getId())
                        .code(at.getCode())
                        .label(at.getLabel())
                        .dataType(at.getDataType())
                        .unit(at.getUnit())
                        .options(at.getOptions())
                        .build()
        ).toList();
    }

    @Override
    public AttributeDetailDTO getAttributeDetail(String id) {
        if (!StringUtils.hasText(id))
            throw new BusinessException(VALIDATE_FAIL);
        AttributeEntity attribute = attributeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(VALIDATE_FAIL));

        return AttributeDetailDTO.builder()
                .id(attribute.getId())
                .code(attribute.getCode())
                .label(attribute.getLabel())
                .dataType(attribute.getDataType())
                .options(attribute.getOptions())
                .unit(attribute.getUnit())
                .build();
    }

    @Override
    public void addAttribute(AttributeCreateForm form) {
        if (!StringUtils.hasText(form.getLabel())
                || !StringUtils.hasText(form.getCode())
                || !StringUtils.hasText(form.getDataType().toString())
        )
            throw new BusinessException(VALIDATE_FAIL);
        if (attributeRepository.existsByCode(form.getCode()))
            throw new BusinessException(VALIDATE_FAIL);
        AttributeDataType type = form.getDataType();
        List<OptionEntity> options = form.getOptions();
        if (type == AttributeDataType.SELECT || type == AttributeDataType.MULTI_SELECT) {
            if (options == null || options.isEmpty())
                throw new BusinessException(VALIDATE_FAIL);
        } else {
            if (options != null && !options.isEmpty())
                throw new BusinessException(VALIDATE_FAIL);
        }
        if (type == AttributeDataType.NUMBER && form.getUnit() == null)
            throw new BusinessException(VALIDATE_FAIL);
        Instant now = Instant.now();
        AttributeEntity entity = AttributeEntity
                .builder()
                .code(form.getCode())
                .label(form.getLabel())
                .dataType(type)
                .unit(normalizeUnit(form.getUnit(), type))
                .options(form.getOptions())
                .createdAt(now)
                .updatedAt(now)
                .build();
        attributeRepository.save(entity);
    }

    @Override
    public void updateAttribute(AttributeEditForm form, String id) {
        if (!StringUtils.hasText(form.getLabel())
                || !StringUtils.hasText(form.getCode())
                || !StringUtils.hasText(form.getDataType().toString())
                || !StringUtils.hasText(form.getId())
                || !id.equals(form.getId()))
            throw new BusinessException(VALIDATE_FAIL);
        AttributeEntity attribute = attributeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(VALIDATE_FAIL));
        AttributeDataType type = form.getDataType();
        List<OptionEntity> options = form.getOptions();
        if (type == AttributeDataType.SELECT || type == AttributeDataType.MULTI_SELECT) {
            if (options == null || options.isEmpty())
                throw new BusinessException(VALIDATE_FAIL);
        } else {
            if (options != null && !options.isEmpty())
                throw new BusinessException(VALIDATE_FAIL);
        }
        Instant now = Instant.now();
        attribute.setCode(form.getCode());
        attribute.setLabel(form.getLabel());
        attribute.setDataType(type);
        attribute.setUnit(normalizeUnit(form.getUnit(), type));
        attribute.setOptions(form.getOptions());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        attribute.setUpdatedAt(now);
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            attribute.setUpdatedBy(jwt.getSubject());
        }
        attributeRepository.save(attribute);

    }

    @Override
    public void deleteAttribute(String id) {
        if (!StringUtils.hasText(id))
            throw new BusinessException(VALIDATE_FAIL);
        AttributeEntity entity = attributeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(VALIDATE_FAIL));
        attributeRepository.delete(entity);
    }

    private String normalizeUnit(String unit, AttributeDataType type) {
        if (unit == null) return null;
        String u = unit.trim();
        if (u.isEmpty()) return null;

        // thường chỉ meaningful cho NUMBER
        if (type != AttributeDataType.NUMBER) return null;

        return u;
    }

    private List<String> normalizeOptions(List<String> options, AttributeDataType type) {
        if (!(type == AttributeDataType.SELECT || type == AttributeDataType.MULTI_SELECT)) return null;
        return options.stream()
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .collect(Collectors.toList());
    }
}

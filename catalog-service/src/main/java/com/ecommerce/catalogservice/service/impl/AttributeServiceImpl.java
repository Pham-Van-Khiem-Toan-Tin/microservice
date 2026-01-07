package com.ecommerce.catalogservice.service.impl;

import static com.ecommerce.catalogservice.constants.Constants.*;

import com.ecommerce.catalogservice.dto.request.attribute.*;
import com.ecommerce.catalogservice.dto.response.AttributeDTO;
import com.ecommerce.catalogservice.dto.response.BusinessException;
import com.ecommerce.catalogservice.entity.AttributeDataType;
import com.ecommerce.catalogservice.entity.AttributeEntity;
import com.ecommerce.catalogservice.entity.OptionEntity;
import com.ecommerce.catalogservice.repository.AttributeRepository;
import com.ecommerce.catalogservice.repository.CategoryRepository;
import com.ecommerce.catalogservice.repository.ProductRepository;
import com.ecommerce.catalogservice.service.AttributeService;
import com.ecommerce.catalogservice.utils.AuthenticationUtils;
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
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ProductRepository productRepository;

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
                                .active(atb.getActive())
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
        query.addCriteria(Criteria.where("active").is(true));
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
        List<AttributeOptionCreate> options = form.getOptions();
        if (type == AttributeDataType.SELECT || type == AttributeDataType.MULTI_SELECT) {
            if (options == null || options.isEmpty())
                throw new BusinessException(VALIDATE_FAIL);
            List<String> attributeOptionValueList = options.stream().map(AttributeOptionCreate::getValue).toList();
            Set<String> attributeOptionValueSet = options.stream().map(AttributeOptionCreate::getValue).collect(Collectors.toSet());
            if (attributeOptionValueList.size() != attributeOptionValueSet.size())
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
                .active(true)
                .deleted(false)
                .unit(normalizeUnit(form.getUnit(), type))
                .options(form.getOptions().stream().map(
                        item -> new OptionEntity(
                                item.getId(),
                                item.getLabel(),
                                item.getValue(),
                                true,
                                false
                        )
                ).toList())
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
        AttributeDataType newType = form.getDataType();
        List<OptionEntity> incoming = form.getOptions();
        boolean isSelectType = newType == AttributeDataType.SELECT
                || newType == AttributeDataType.MULTI_SELECT;
        if (isSelectType && (incoming == null || incoming.isEmpty())) throw new BusinessException(VALIDATE_FAIL);
        if (!isSelectType && incoming != null && !incoming.isEmpty()) throw new BusinessException(VALIDATE_FAIL);
        boolean attributeUsedInCategory = categoryRepository.existsByAttributeConfigsCode(attribute.getCode());
        boolean attributeUsedInProduct = productRepository.existsBySpecsCode(attribute.getCode());
        if (!form.getCode().equals(attribute.getCode())
                && (attributeUsedInCategory || attributeUsedInProduct))
            throw new BusinessException(VALIDATE_FAIL);
        if (attribute.getDataType() != null
                && attribute.getDataType() != newType
                && (attributeUsedInCategory || attributeUsedInProduct))
            throw new BusinessException(VALIDATE_FAIL);
        // Merge options an toàn (KHÔNG đè thẳng)
        if (isSelectType) {
            List<OptionEntity> merged = mergeOptionsSafely(
                    attribute.getCode(),          // code hiện tại (đã lock nếu dùng)
                    attribute.getOptions(),
                    incoming
            );
            attribute.setOptions(merged);
        } else {
            attribute.setOptions(null);
        }
        Instant now = Instant.now();
        attribute.setCode(form.getCode());
        attribute.setLabel(form.getLabel());
        attribute.setDataType(newType);
        attribute.setUnit(normalizeUnit(form.getUnit(), newType));
        attribute.setOptions(form.getOptions());
        attribute.setUpdatedAt(now);
        attribute.setUpdatedBy(AuthenticationUtils.getUserId());

        attributeRepository.save(attribute);

    }

    private List<OptionEntity> mergeOptionsSafely(
            String attributeCode,
            List<OptionEntity> existing,
            List<OptionEntity> incoming
    ) {
        existing = existing == null ? List.of() : existing;

        // map option cũ theo id
        Map<String, OptionEntity> oldById = existing.stream()
                .filter(o -> StringUtils.hasText(o.getId()))
                .collect(Collectors.toMap(OptionEntity::getId, o -> o, (a, b) -> a));

        Set<String> incomingIds = new HashSet<>();
        List<OptionEntity> result = new ArrayList<>();

        // 1) Upsert incoming
        for (OptionEntity in : incoming) {
            if (!StringUtils.hasText(in.getLabel()) || !StringUtils.hasText(in.getValue()))
                throw new BusinessException(VALIDATE_FAIL);

            String inId = StringUtils.hasText(in.getId()) ? in.getId() : UUID.randomUUID().toString();
            incomingIds.add(inId);

            OptionEntity old = oldById.get(inId);

            if (old == null) {
                // option mới
                result.add(new OptionEntity(
                        inId,
                        in.getLabel().trim(),
                        in.getValue().trim(),
                        in.getActive() != null ? in.getActive() : true,
                        false
                ));
            } else {
                // option cũ: nếu đã dùng -> không cho đổi value
                boolean used = isOptionUsedSomewhere(attributeCode, old.getId());

                if (used && !Objects.equals(old.getValue(), in.getValue()))
                    throw new BusinessException(VALIDATE_FAIL);

                boolean nextActive = in.getActive() != null ? in.getActive() : Boolean.TRUE.equals(old.getActive());
                boolean nextDeprecated = Boolean.TRUE.equals(old.getDeprecated());

                // nếu đã dùng mà tắt -> deprecated
                if (used && !nextActive) nextDeprecated = true;

                result.add(new OptionEntity(
                        old.getId(),
                        in.getLabel().trim(),
                        used ? old.getValue() : in.getValue().trim(),
                        nextActive,
                        nextDeprecated
                ));
            }
        }

        // 2) Option bị “xóa” khỏi incoming
        for (OptionEntity old : existing) {
            if (!StringUtils.hasText(old.getId())) continue;
            if (incomingIds.contains(old.getId())) continue;

            boolean used = isOptionUsedSomewhere(attributeCode, old.getId());

            if (used) {
                // không được remove -> set inactive + deprecated
                result.add(new OptionEntity(
                        old.getId(),
                        old.getLabel(),
                        old.getValue(),
                        false,
                        true
                ));
            }
            // nếu chưa used => bỏ luôn (hard remove) OK
        }

        // 3) Chống trùng value
        Set<String> seen = new HashSet<>();
        for (OptionEntity o : result) {
            String key = (o.getValue() == null) ? "" : o.getValue().trim().toLowerCase();
            if (!seen.add(key)) throw new BusinessException(VALIDATE_FAIL);
        }

        return result;
    }

    private boolean isOptionUsedSomewhere(String attributeCode, String optionId) {
        // 1) Category đang allow option này?
        // Query tối ưu nhất là exists + index trên:
        // { "attribute_configs.code": 1, "attribute_configs.allowed_option_ids": 1 }
        return categoryRepository.existsAttrConfigUsingOption(attributeCode, optionId);

        // 2) Nếu bạn có SKU lưu selections.optionId/value thì check thêm ở skuRepository (nếu cần)
    }

    @Override
    public void deleteAttribute(String id) {
        AttributeEntity attribute = attributeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(VALIDATE_FAIL));

        boolean usedInCategory = categoryRepository.existsByAttributeConfigsCode(attribute.getCode());
        boolean usedInProduct  = productRepository.existsBySpecsCode(attribute.getCode());
        String userId = AuthenticationUtils.getUserId();
        Instant now = Instant.now();
        if (usedInCategory || usedInProduct) {
            // soft delete
            attribute.setActive(false);      // hoặc setDeleted(true)
            attribute.setDeleted(true);
            attribute.setDeletedAt(now);
            attribute.setDeletedBy(userId);

            // nếu là select-type thì tiện tay “đóng băng” option
            if (attribute.getOptions() != null) {
                List<OptionEntity> closed = attribute.getOptions().stream()
                        .map(o -> new OptionEntity(o.getId(), o.getLabel(), o.getValue(), false, true))
                        .toList();
                attribute.setOptions(closed);
            }

            attribute.setUpdatedAt(now);
            attribute.setUpdatedBy(userId);
            attributeRepository.save(attribute);
            return;
        }

        // chưa dùng -> hard delete ok
        attributeRepository.deleteById(id);
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

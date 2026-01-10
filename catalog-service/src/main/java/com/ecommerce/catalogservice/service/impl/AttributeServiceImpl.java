package com.ecommerce.catalogservice.service.impl;

import static com.ecommerce.catalogservice.constants.Constants.*;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

import com.ecommerce.catalogservice.dto.request.attribute.*;
import com.ecommerce.catalogservice.dto.response.attribute.*;
import com.ecommerce.catalogservice.dto.response.BusinessException;
import com.ecommerce.catalogservice.entity.AttributeDataType;
import com.ecommerce.catalogservice.entity.AttributeEntity;
import com.ecommerce.catalogservice.entity.OptionEntity;
import com.ecommerce.catalogservice.repository.AttributeRepository;
import com.ecommerce.catalogservice.repository.CategoryRepository;
import com.ecommerce.catalogservice.repository.ProductRepository;
import com.ecommerce.catalogservice.service.AttributeService;
import com.ecommerce.catalogservice.utils.AuthenticationUtils;
import com.ecommerce.catalogservice.utils.SlugUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
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
        List<AttributeEntity> attributeEntityList = mongoTemplate.find(query, AttributeEntity.class);
        List<String> codes = attributeEntityList.stream().map(AttributeEntity::getCode).filter(StringUtils::hasText).toList();
        Map<String, Long> catCounts = countCategoriesByAttributeCodes(codes);
        Map<String, Long> prodCounts = countProductsBySpecCodes(codes);

        List<AttributeDTO> attributeDTOS = attributeEntityList.stream().map(
                        atb -> {
                            long categoryCount = catCounts.getOrDefault(atb.getCode(), 0L);
                            long productCount = prodCounts.getOrDefault(atb.getCode(), 0L);
                            UsageDTO usage = UsageDTO.builder()
                                    .usedInProducts(productCount > 0)
                                    .usedInCategories(categoryCount > 0)
                                    .categoryCount(categoryCount)
                                    .productCount(productCount)
                                    .build();

                            CapabilitiesDTO cap = getCapabilities(usage, atb);
                            return AttributeDTO.builder()
                                    .id(atb.getId())
                                    .code(atb.getCode())
                                    .active(atb.getActive())
                                    .label(atb.getLabel())
                                    .dataType(atb.getDataType())
                                    .usage(usage)
                                    .capabilities(cap)
                                    .build();
                        }
                )
                .toList();
        return new PageImpl<>(attributeDTOS, pageable, total);
    }

    private Map<String, Long> countCategoriesByAttributeCodes(List<String> codes) {
        if (codes == null || codes.isEmpty()) return Collections.emptyMap();

        Aggregation agg = newAggregation(
                match(Criteria.where("attribute_configs.code").in(codes)),
                unwind("attributeConfigs"),
                match(Criteria.where("attribute_configs.code").in(codes)),
                group("attribute_configs.code").count().as("cnt")
        );

        AggregationResults<CodeCountDTO> rs =
                mongoTemplate.aggregate(agg, "categories", CodeCountDTO.class);

        return rs.getMappedResults().stream()
                .collect(Collectors.toMap(CodeCountDTO::getId, CodeCountDTO::getCnt));
    }

    private Map<String, Long> countProductsBySpecCodes(List<String> codes) {
        if (codes == null || codes.isEmpty()) return Collections.emptyMap();

        Aggregation agg = newAggregation(
                match(Criteria.where("specs.code").in(codes)),
                unwind("specs"),
                match(Criteria.where("specs.code").in(codes)),
                group("specs.code").count().as("cnt")
        );

        AggregationResults<CodeCountDTO> rs =
                mongoTemplate.aggregate(agg, "products", CodeCountDTO.class);

        return rs.getMappedResults().stream()
                .collect(Collectors.toMap(CodeCountDTO::getId, CodeCountDTO::getCnt));
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
        UsageDTO usageDTO = getUsage(attribute.getCode());
        return AttributeDetailDTO.builder()
                .id(attribute.getId())
                .code(attribute.getCode())
                .label(attribute.getLabel())
                .active(attribute.getActive())
                .deleted(attribute.getDeleted())
                .dataType(attribute.getDataType())
                .usage(usageDTO)
                .capabilities(getCapabilities(usageDTO, attribute))
                .options(attribute.getOptions())
                .unit(attribute.getUnit())
                .build();
    }

    private UsageDTO getUsage(String code) {
        long categoryCount = categoryRepository.countUsingAttributeCode(code);
        long productCount = productRepository.countUsingSpecCode(code);

        return UsageDTO.builder()
                .usedInProducts(productCount > 0)
                .usedInCategories(categoryCount > 0)
                .categoryCount(categoryCount)
                .productCount(productCount)
                .build();
    }

    private CapabilitiesDTO getCapabilities(UsageDTO usage, AttributeEntity entity) {
        boolean usedAnywhere = usage.getUsedInCategories() || usage.getUsedInProducts();

        // rule gợi ý (bạn có thể strict hơn tuỳ business)
        boolean canDelete = !usedAnywhere;
        boolean canChangeCode = false;                 // luôn false (immutable)
        boolean canChangeDataType = !usage.getUsedInProducts();     // hoặc !usage.usedInProducts()
        boolean canRemoveOptions = !usage.getUsedInProducts();
        return CapabilitiesDTO.builder()
                .canDelete(canDelete)
                .canChangeCode(canChangeCode)
                .canChangeDataType(canChangeDataType)
                .canRemoveOptions(canRemoveOptions)
                .canEditLabel(!entity.getDeleted())
                .canToggleActive(!entity.getDeleted())
                .canAddOptions(!entity.getDeleted())
                .canEditOptionLabel(!entity.getDeleted())
                .canToggleOptionActive(!entity.getDeleted())
                .canHardDeleteOptions(!usedAnywhere)
                .build();

    }

    @Override
    public void addAttribute(AttributeCreateForm form) {
        if (!StringUtils.hasText(form.getLabel())
                || !StringUtils.hasText(form.getDataType().toString())
        )
            throw new BusinessException(VALIDATE_FAIL);
        String code = SlugUtils.toSlug(form.getLabel());
        if (attributeRepository.existsByCode(code))
            throw new BusinessException(VALIDATE_FAIL);
        AttributeDataType type = form.getDataType();
        List<AttributeOptionCreate> options = form.getOptions();
        if (type == AttributeDataType.SELECT || type == AttributeDataType.MULTI_SELECT) {
            if (options == null || options.isEmpty())
                throw new BusinessException(VALIDATE_FAIL);
            Boolean isValidate = options.stream().allMatch(o -> StringUtils.hasText(o.getLabel()));
            if (!isValidate) throw new BusinessException(VALIDATE_FAIL);
            List<String> attributeOptionValueList = options.stream().map(o -> SlugUtils.toSlug(o.getLabel())).toList();
            Set<String> attributeOptionValueSet = options.stream().map(o -> SlugUtils.toSlug(o.getLabel())).collect(Collectors.toSet());
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
                .code(code)
                .label(form.getLabel())
                .dataType(type)
                .active(form.getActive())
                .unit(normalizeUnit(form.getUnit(), type))
                .options(form.getOptions().stream().map(
                        item -> new OptionEntity(
                                SlugUtils.toSlug(item.getLabel()),
                                item.getLabel(),
                                item.getActive(),
                                false,
                                item.getDisplayOrder()
                        )
                ).toList())
                .createdAt(now)
                .updatedAt(now)
                .build();
        attributeRepository.save(entity);
    }

    @Override
    public void updateAttribute(AttributeEditForm form, String id) {
        // 0) Basic validate
        if (form == null
                || !StringUtils.hasText(id)
                || !StringUtils.hasText(form.getId())
                || !id.equals(form.getId())
                || !StringUtils.hasText(form.getLabel())
                || !StringUtils.hasText(form.getCode())
                || form.getDataType() == null
                || form.getActive() == null) {
            throw new BusinessException(VALIDATE_FAIL);
        }

        AttributeEntity attribute = attributeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(VALIDATE_FAIL));

        AttributeDataType oldType = attribute.getDataType();
        AttributeDataType newType = form.getDataType();

        boolean oldIsSelectType = oldType == AttributeDataType.SELECT || oldType == AttributeDataType.MULTI_SELECT;
        boolean newIsSelectType = newType == AttributeDataType.SELECT || newType == AttributeDataType.MULTI_SELECT;

        // 1) Used check (code is the key in products.specs.code & categories.attribute_configs.code)
        boolean usedInCategory = categoryRepository.existsByAttributeConfigsCode(attribute.getCode());
        boolean usedInProduct = productRepository.existsBySpecsCode(attribute.getCode());
        boolean used = usedInCategory || usedInProduct;

        // 2) Prevent changing code if used (VERY IMPORTANT in your schema)
        String incomingCode = form.getCode().trim();
        if (!incomingCode.equals(attribute.getCode()) && used) {
            throw new BusinessException(VALIDATE_FAIL);
        }

        // 3) Prevent changing datatype if used (avoid breaking existing product/category data)
        if (oldType != newType && used) {
            throw new BusinessException(VALIDATE_FAIL);
        }

        // 4) Validate options presence based on newType
        if (newIsSelectType) {
            if (form.getOptions() == null || form.getOptions().isEmpty()) {
                throw new BusinessException(VALIDATE_FAIL);
            }
        } else {
            // non-select must not carry options
            if (form.getOptions() != null && !form.getOptions().isEmpty()) {
                throw new BusinessException(VALIDATE_FAIL);
            }
        }

        // 5) Options update
        if (newIsSelectType) {
            // Case 5.1: non-select -> select (initialize fresh option list from payload)
            if (!oldIsSelectType) {
                // validate label non-blank + unique by slug
                List<String> labels = form.getOptions().stream()
                        .map(o -> o.getLabel() == null ? "" : o.getLabel().trim())
                        .toList();

                if (labels.stream().anyMatch(l -> !StringUtils.hasText(l))) {
                    throw new BusinessException(VALIDATE_FAIL);
                }

                Set<String> slugs = labels.stream()
                        .map(SlugUtils::toSlug)
                        .collect(Collectors.toSet());

                if (slugs.size() != labels.size()) {
                    throw new BusinessException(VALIDATE_FAIL);
                }

                List<OptionEntity> optionEntities = form.getOptions().stream()
                        .map(o -> new OptionEntity(
                                SlugUtils.toSlug(o.getLabel().trim()),
                                o.getLabel().trim(),
                                Boolean.TRUE.equals(o.getActive()),
                                false,
                                o.getDisplayOrder()
                        ))
                        .toList();

                attribute.setOptions(optionEntities);
            } else {
                // Case 5.2: select -> select (merge safely: soft delete if used, hard delete if not used)
                List<OptionEntity> merged = mergeOptionsSafely(
                        attribute,
                        attribute.getOptions(),
                        form.getOptions(),
                        newType
                );
                attribute.setOptions(merged);
            }
        }
        else {
            // newType is non-select
            // If old was select, at this point used must be false (blocked above), so safe to drop options
            attribute.setOptions(null);
        }

        // 6) Update remaining fields
        Instant now = Instant.now();
        attribute.setCode(incomingCode);
        attribute.setLabel(form.getLabel().trim());
        attribute.setDataType(newType);
        attribute.setActive(form.getActive());
        attribute.setUnit(normalizeUnit(form.getUnit(), newType));
        attribute.setUpdatedAt(now);
        attribute.setUpdatedBy(AuthenticationUtils.getUserId());

        attributeRepository.save(attribute);
    }

    public List<String> findUsedOptionIdsInProducts_Select(String attributeCode) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.unwind("specs"),
                Aggregation.match(Criteria.where("specs.code").is(attributeCode)),
                Aggregation.match(Criteria.where("specs.valueId").ne(null)),
                Aggregation.group().addToSet("specs.valueId").as("optionIds"),
                Aggregation.project("optionIds")
        );

        AggregationResults<Document> rs =
                mongoTemplate.aggregate(agg, "products", Document.class);

        Document doc = rs.getUniqueMappedResult();
        if (doc == null) return List.of();

        @SuppressWarnings("unchecked")
        List<String> optionIds = (List<String>) doc.get("optionIds");
        return optionIds == null ? List.of() : optionIds;
    }

    public List<String> findUsedOptionIdsInProducts_MultiSelect(String attributeCode) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.unwind("specs"),
                Aggregation.match(Criteria.where("specs.code").is(attributeCode)),
                Aggregation.match(Criteria.where("specs.valueIds").ne(null)),
                Aggregation.unwind("specs.valueIds"),
                Aggregation.group().addToSet("specs.valueIds").as("optionIds"),
                Aggregation.project("optionIds")
        );

        AggregationResults<Document> rs =
                mongoTemplate.aggregate(agg, "products", Document.class);

        Document doc = rs.getUniqueMappedResult();
        if (doc == null) return List.of();

        @SuppressWarnings("unchecked")
        List<String> optionIds = (List<String>) doc.get("optionIds");
        return optionIds == null ? List.of() : optionIds;
    }

    public List<String> findUsedOptionIdsInCategories(String attributeCode) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.unwind("attribute_configs"),
                Aggregation.match(Criteria.where("attribute_configs.code").is(attributeCode)),
                Aggregation.match(Criteria.where("attribute_configs.allowed_option_ids").ne(null)),
                Aggregation.unwind("attribute_configs.allowed_option_ids"),
                Aggregation.group().addToSet("attribute_configs.allowed_option_ids").as("optionIds"),
                Aggregation.project("optionIds")
        );

        AggregationResults<Document> rs =
                mongoTemplate.aggregate(agg, "categories", Document.class);

        Document doc = rs.getUniqueMappedResult();
        if (doc == null) return List.of();

        @SuppressWarnings("unchecked")
        List<String> optionIds = (List<String>) doc.get("optionIds");
        return optionIds == null ? List.of() : optionIds;
    }

    public List<String> findUsedOptionIdsEverywhere(String attributeCode, AttributeDataType dataType) {
        Set<String> used = new HashSet<>();

        // từ category config
        used.addAll(findUsedOptionIdsInCategories(attributeCode));

        // từ product specs
        if (dataType == AttributeDataType.SELECT) {
            used.addAll(findUsedOptionIdsInProducts_Select(attributeCode));
        } else if (dataType == AttributeDataType.MULTI_SELECT) {
            used.addAll(findUsedOptionIdsInProducts_MultiSelect(attributeCode));
        }

        return new ArrayList<>(used);
    }

    private List<OptionEntity> mergeOptionsSafely(
            AttributeEntity attribute,
            List<OptionEntity> existing,
            List<OptionEntity> incoming,
            AttributeDataType dataType
    ) {
        if (existing == null) existing = List.of();
        if (incoming == null) incoming = List.of();

        // 1) Used option ids (from products + categories)
        Set<String> usedOptionIds = new HashSet<>(findUsedOptionIdsEverywhere(attribute.getCode(), dataType));

        // 2) Build map existing by id
        Map<String, OptionEntity> existingById = existing.stream()
                .filter(o -> org.springframework.util.StringUtils.hasText(o.getId()))
                .collect(Collectors.toMap(
                        OptionEntity::getId,
                        o -> o,
                        (a, b) -> a // in case duplicated data, keep first
                ));

        Set<String> existingIds = new HashSet<>(existingById.keySet());

        // 3) Split incoming: hasId (update) vs new (no id)
        List<OptionEntity> incomingWithId = incoming.stream()
                .filter(o -> StringUtils.hasText(o.getId()))
                .toList();

        List<OptionEntity> incomingNew = incoming.stream()
                .filter(o -> !StringUtils.hasText(o.getId()))
                .toList();

        // 4) Validate: incomingWithId ids must be unique and must exist
        Set<String> incomingIds = new HashSet<>();
        for (OptionEntity o : incomingWithId) {
            String id = o.getId().trim();
            if (!incomingIds.add(id)) {
                throw new BusinessException(VALIDATE_FAIL); // duplicated id in payload
            }
            if (!existingById.containsKey(id)) {
                throw new BusinessException(VALIDATE_FAIL); // update id not found
            }
        }

        // 5) Validate: label must be non-blank for ALL incoming items
        // (Nếu bạn muốn cho phép label rỗng thì bỏ đoạn này, nhưng thường nên chặn)
        for (OptionEntity o : incoming) {
            String label = (o.getLabel() == null) ? "" : o.getLabel().trim();
            if (!StringUtils.hasText(label)) {
                throw new BusinessException(VALIDATE_FAIL);
            }
        }

        // 6) Validate: unique slug among incoming labels (new + update)
        // Đồng thời tạo set slug của incoming để kiểm tra trùng
        Set<String> incomingSlugs = new HashSet<>();
        for (OptionEntity o : incoming) {
            String slug = SlugUtils.toSlug(o.getLabel().trim());
            if (!incomingSlugs.add(slug)) {
                throw new BusinessException(VALIDATE_FAIL); // duplicated label/slug in incoming
            }
        }

        // 7) Determine deleted IDs (existing - incomingWithId)
        Set<String> deletedIds = new HashSet<>(existingIds);
        deletedIds.removeAll(incomingIds);

        // Soft delete if used, hard delete if not used
        Set<String> softDeleteIds = deletedIds.stream()
                .filter(usedOptionIds::contains)
                .collect(Collectors.toSet());

        Set<String> hardDeleteIds = deletedIds.stream()
                .filter(id -> !usedOptionIds.contains(id))
                .collect(Collectors.toSet());

        // 8) Validate: new options generate id by slug
        // allow reuse only if that id is being hard-deleted in this request
        List<OptionEntity> createdOptions = new ArrayList<>();
        for (OptionEntity o : incomingNew) {
            String label = o.getLabel().trim();
            String newId = SlugUtils.toSlug(label);

            boolean idExists = existingIds.contains(newId);
            boolean canReuse = hardDeleteIds.contains(newId);

            if (idExists && !canReuse) {
                // would collide with existing option that is not being removed (or is soft-deleted/kept)
                throw new BusinessException(VALIDATE_FAIL);
            }

            OptionEntity created = new OptionEntity(
                    newId,
                    label,
                    Boolean.TRUE.equals(o.getActive()),
                    false,
                    o.getDisplayOrder()
            );
            createdOptions.add(created);
        }

        // 9) Apply updates for incomingWithId
        List<OptionEntity> updatedOptions = new ArrayList<>();
        for (OptionEntity inc : incomingWithId) {
            OptionEntity old = existingById.get(inc.getId().trim());
            if (old == null) throw new BusinessException(VALIDATE_FAIL);

            old.setLabel(inc.getLabel().trim());
            old.setActive(Boolean.TRUE.equals(inc.getActive()));
            old.setDisplayOrder(inc.getDisplayOrder());
            old.setDeprecated(false); // since it is in incoming, treat as not deprecated
            updatedOptions.add(old);
        }

        // 10) Build soft-deleted options to keep
        List<OptionEntity> softDeletedOptions = new ArrayList<>();
        for (String id : softDeleteIds) {
            OptionEntity old = existingById.get(id);
            if (old == null) continue; // safety

            old.setActive(false);
            old.setDeprecated(true);
            // displayOrder: giữ nguyên (stable). Nếu bạn muốn đẩy xuống cuối thì set lại ở đây.
            softDeletedOptions.add(old);
        }

        // 11) Final result = updated + created + soft-deleted (hard-deleted is omitted)
        List<OptionEntity> merged = new ArrayList<>();
        merged.addAll(updatedOptions);
        merged.addAll(createdOptions);
        merged.addAll(softDeletedOptions);

        // 12) Sort stable (optional but recommended)
        merged.sort(Comparator
                .comparing(OptionEntity::getDisplayOrder, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(o -> o.getId() == null ? "" : o.getId())
        );

        return merged;
    }


    @Override
    public void deleteAttribute(String id) {
        AttributeEntity attribute = attributeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(VALIDATE_FAIL));

        boolean usedInCategory = categoryRepository.existsByAttributeConfigsCode(attribute.getCode());
        boolean usedInProduct = productRepository.existsBySpecsCode(attribute.getCode());
        String userId = AuthenticationUtils.getUserId();
        Instant now = Instant.now();
        if (usedInCategory || usedInProduct) {
            // soft delete
            attribute.setActive(false);      // hoặc setDeleted(true)
            attribute.setDeleted(true);
            // nếu là select-type thì tiện tay “đóng băng” option
            if (attribute.getOptions() != null) {
                List<OptionEntity> closed = attribute.getOptions().stream()
                        .map(o -> new OptionEntity(o.getId(), o.getLabel(), false, true, o.getDisplayOrder()))
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

    @Override
    public void changeActiveAttribute(String id) {
        if (!StringUtils.hasText(id)) throw new BusinessException(VALIDATE_FAIL);
        AttributeEntity attribute = attributeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(VALIDATE_FAIL));
        if (attribute.getDeleted())
            throw new BusinessException(VALIDATE_FAIL);
        attribute.setActive(!attribute.getActive());
        attributeRepository.save(attribute);
    }

    @Override
    public void revokeAttribute(String id) {
        if (!StringUtils.hasText(id)) throw new BusinessException(VALIDATE_FAIL);
        AttributeEntity attribute = attributeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(VALIDATE_FAIL));
        attribute.setActive(false);
        attribute.setDeleted(false);
        attribute.setUpdatedBy(AuthenticationUtils.getUserId());
        attribute.setUpdatedAt(Instant.now());
        attributeRepository.save(attribute);
    }

    @Override
    public void revokeAttributeOption(String id, RevokeOptionForm form) {
        if (!StringUtils.hasText(id)
                || !StringUtils.hasText(form.getAttributeId())
                || !StringUtils.hasText(form.getOptionId())
                || !id.equals(form.getAttributeId())) throw new BusinessException(VALIDATE_FAIL);
        AttributeEntity attribute = attributeRepository.findById(id).orElseThrow(
                () -> new BusinessException(VALIDATE_FAIL)
        );
        if (!(attribute.getDataType() == AttributeDataType.SELECT ||
                attribute.getDataType() == AttributeDataType.MULTI_SELECT)) {
            throw new BusinessException(VALIDATE_FAIL);
        }
        List<OptionEntity> options = attribute.getOptions();

        OptionEntity option = options.stream().filter(o -> o.getId().equals(form.getOptionId())).findFirst().orElseThrow(
                () -> new BusinessException(VALIDATE_FAIL)
        );
        if (!option.getDeprecated()) throw new BusinessException(VALIDATE_FAIL);
        int maxOrder = options.stream().filter(o -> !o.getDeprecated())
                .mapToInt(OptionEntity::getDisplayOrder).max().orElse(-1);
        attribute.getOptions().forEach(o -> {
            if (o.getId().equals(form.getOptionId())) {
                o.setActive(false);
                o.setDeprecated(false);
                o.setDisplayOrder(maxOrder + 1);
            }
        });
        attribute.setUpdatedBy(AuthenticationUtils.getUserId());
        attribute.setUpdatedAt(Instant.now());
        attributeRepository.save(attribute);

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

package com.ecommerce.catalogservice.dto.response.attribute;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CapabilitiesDTO {
    private Boolean canDelete;
    private Boolean canChangeCode;
    private Boolean canChangeDataType;
    private Boolean canEditLabel;
    private Boolean canToggleActive;

    private Boolean canAddOptions;
    private Boolean canEditOptionLabel;
    private Boolean canToggleOptionActive;

    private Boolean canRemoveOptions;
    private Boolean canHardDeleteOptions;
}

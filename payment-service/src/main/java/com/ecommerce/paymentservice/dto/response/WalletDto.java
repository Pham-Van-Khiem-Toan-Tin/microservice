package com.ecommerce.paymentservice.dto.response;

import com.ecommerce.paymentservice.enums.WalletStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class WalletDto {
    private String id;
    private BigDecimal balance;
    private WalletStatus status;
}

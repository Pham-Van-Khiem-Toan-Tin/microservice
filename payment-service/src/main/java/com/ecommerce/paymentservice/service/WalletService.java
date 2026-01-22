package com.ecommerce.paymentservice.service;

import com.ecommerce.paymentservice.dto.request.InternalPaymentForm;
import com.ecommerce.paymentservice.dto.response.TransactionDto;
import com.ecommerce.paymentservice.dto.response.WalletDto;
import com.ecommerce.paymentservice.entity.WalletEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface WalletService {
    BigDecimal getBalanceByUserId();
    WalletDto getWalletInfo();
    String processWalletPayment(InternalPaymentForm form) throws JsonProcessingException;
    Page<TransactionDto> getTransactionHistory(String userId, int page, int size);
}

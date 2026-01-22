package com.ecommerce.paymentservice.service.impl;

import com.ecommerce.paymentservice.dto.event.PaymentFailedResult;
import com.ecommerce.paymentservice.dto.event.PaymentSuccessResult;
import com.ecommerce.paymentservice.dto.exception.BusinessException;
import com.ecommerce.paymentservice.dto.request.InternalPaymentForm;
import com.ecommerce.paymentservice.dto.response.TransactionDto;
import com.ecommerce.paymentservice.dto.response.WalletDto;
import com.ecommerce.paymentservice.entity.OutboxEvent;
import com.ecommerce.paymentservice.entity.PaymentTransactionEntity;
import com.ecommerce.paymentservice.entity.WalletEntity;
import com.ecommerce.paymentservice.entity.WalletTransactionEntity;
import com.ecommerce.paymentservice.enums.*;
import com.ecommerce.paymentservice.repository.OutboxRepository;
import com.ecommerce.paymentservice.repository.PaymentTransactionRepository;
import com.ecommerce.paymentservice.repository.WalletRepository;
import com.ecommerce.paymentservice.repository.WalletTransactionRepository;
import com.ecommerce.paymentservice.service.WalletService;
import com.ecommerce.paymentservice.utils.AuthenticationUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static com.ecommerce.paymentservice.constants.Constants.WALLET_INVALID;

@Service
public class WalletServiceImpl implements WalletService {
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private WalletTransactionRepository transactionRepository;
    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;
    @Autowired
    private OutboxRepository outboxRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public BigDecimal getBalanceByUserId() {
        String userId = AuthenticationUtils.getUserId();
        return walletRepository.findByUserId(userId)
                .map(WalletEntity::getBalance)
                .orElse(BigDecimal.ZERO);
    }

    @Override
    public WalletDto getWalletInfo() {
        WalletEntity wallet = getOrCreateWallet();
        return WalletDto.builder()
                .id(wallet.getId().toString())
                .balance(wallet.getBalance())
                .status(wallet.getStatus())
                .build();
    }

    @Transactional
    @Override
    public String processWalletPayment(InternalPaymentForm form) throws JsonProcessingException {
        String userId = form.getUserId();
        WalletEntity wallet = walletRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new BusinessException(WALLET_INVALID));
        BigDecimal amount = BigDecimal.valueOf(form.getAmount());
        BigDecimal oldBalance = wallet.getBalance();
        boolean isAvaiable = oldBalance.compareTo(amount) > 0;
        TransactionStatus txs = null;
        BigDecimal newBalance = null;
        if (!isAvaiable) {
            txs = TransactionStatus.FAILED;
            newBalance = oldBalance;
        } else {
            txs = TransactionStatus.SUCCESS;
            newBalance = oldBalance.subtract(amount);
        }
        wallet.setBalance(newBalance);
        walletRepository.save(wallet);
        WalletTransactionEntity wtx = transactionRepository.save(WalletTransactionEntity.builder()
                .wallet(wallet)
                .amount(amount.negate()) // Số âm cho chi tiêu
                .balanceBefore(oldBalance)
                .balanceAfter(newBalance)
                .type(TransactionType.PAYMENT)
                .status(txs)
                .description("Thanh toán đơn hàng: " + form.getOrderNumber())
                .createdAt(LocalDateTime.now())
                .build());
        PaymentTransactionEntity tx = PaymentTransactionEntity.builder()
                .userId(userId)
                .amount(amount)
                .referenceId(form.getOrderNumber())
                .gateway("WALLET")
                .status(isAvaiable ? "SUCCESS" : "FAIL") // Thành công ngay lập tức
                .type(PaymentType.ORDER)
                .createdAt(LocalDateTime.now())
                .build();
        paymentTransactionRepository.save(tx);
        OutboxEvent outboxEvent = null;
        if (isAvaiable) {
            outboxEvent = OutboxEvent.builder()
                    .id(UUID.randomUUID().toString())
                    .aggregateType("payment") // Sẽ sinh ra topic: payment-service.payment.events
                    .aggregateId("WTX-" + wtx.getId())
                    .type("Payment.Succeeded")
                    .payload(objectMapper.writeValueAsString(PaymentSuccessResult.builder()
                            .orderNumber(form.getOrderNumber())
                            .amount(tx.getAmount())
                            .providerRef("WTX-" + wtx.getId())
                            .paidAt(LocalDateTime.now())
                            .method(PaymentMethod.WALLET)
                            .build()))
                    .createdAt(LocalDateTime.now())
                    .build();
        } else {
            outboxEvent = OutboxEvent.builder()
                    .id(UUID.randomUUID().toString())
                    .aggregateType("payment") // Sẽ sinh ra topic: payment-service.payment.events
                    .aggregateId("WTX-" + wtx.getId())
                    .type("Payment.Failed")
                    .payload(objectMapper.writeValueAsString(PaymentFailedResult.builder()
                            .orderNumber(form.getOrderNumber())
                            .amount(tx.getAmount())
                            .failedAt(LocalDateTime.now())
                            .method(PaymentMethod.WALLET)
                            .reason("Ví không đủ số dư")
                            .build()))
                    .createdAt(LocalDateTime.now())
                    .build();
        }
        // 6. Bắn Outbox Event để Order Service cập nhật trạng thái PAID

        outboxRepository.save(outboxEvent);
        return "WTX-" + wtx.getId();
    }

    private WalletEntity getOrCreateWallet() {
        String userId = AuthenticationUtils.getUserId();
        return walletRepository.findByUserId(userId)
                .orElseGet(() -> {
                    // Logic tạo mới ví rỗng ngay tại đây
                    WalletEntity newWallet = WalletEntity.builder()
                            .userId(userId)
                            .balance(BigDecimal.ZERO)
                            .currency("VND")
                            .status(WalletStatus.ACTIVE)
                            .build();
                    return walletRepository.save(newWallet);
                });
    }

    @Override
    public Page<TransactionDto> getTransactionHistory(String userId, int page, int size) {
        // Lưu ý: Frontend thường gửi page=1, nhưng Spring JPA bắt đầu từ 0.
        // Nếu Frontend gửi từ 0 thì bỏ "- 1" đi.
        Pageable pageable = PageRequest.of(page - 1, size);

        WalletEntity wallet = getOrCreateWallet();

        Page<WalletTransactionEntity> entityPage = transactionRepository
                .findByWalletIdOrderByCreatedAtDesc(wallet.getId(), pageable);

        return entityPage.map(transactionEntity -> TransactionDto.builder()
                .id(transactionEntity.getId().toString())
                .amount(transactionEntity.getAmount())
                .createdAt(transactionEntity.getCreatedAt())
                .description(transactionEntity.getDescription())
                .type(transactionEntity.getType())
                .build());
    }

}

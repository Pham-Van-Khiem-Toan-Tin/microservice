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
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
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
@Slf4j
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
    public String processWalletPayment(InternalPaymentForm form) {
        // 1. Gắn nhãn MDC để theo dõi xuyên suốt
        MDC.put("orderNo", form.getOrderNumber());
        MDC.put("sagaStep", "PAYMENT_WALLET_INTERNAL_EXEC");
        MDC.put("status", "STARTED");

        log.info("Bắt đầu thực hiện trừ tiền ví cho đơn hàng: {}. Số tiền: {}",
                form.getOrderNumber(), form.getAmount());

        try {
            String userId = AuthenticationUtils.getUserId();

            // 2. Lock ví (Pessimistic Lock) để tránh tranh chấp số dư (Race Condition)
            log.info("Đang lock ví của User: {} để xử lý giao dịch...", userId);
            WalletEntity wallet = walletRepository.findByUserIdWithLock(userId)
                    .orElseThrow(() -> new BusinessException(WALLET_INVALID));

            BigDecimal amount = BigDecimal.valueOf(form.getAmount());
            BigDecimal oldBalance = wallet.getBalance();

            // Kiểm tra khả dụng (Sửa lỗi chính tả isAvaiable -> isAvailable)
            boolean isAvailable = oldBalance.compareTo(amount) >= 0;
            TransactionStatus txs;
            BigDecimal newBalance;

            if (!isAvailable) {
                MDC.put("status", "FAILED");
                MDC.put("failureReason", "INSUFFICIENT_BALANCE");
                log.warn("Thanh toán thất bại: Ví không đủ số dư. Hiện có: {}, Cần: {}", oldBalance, amount);

                txs = TransactionStatus.FAILED;
                newBalance = oldBalance;
            } else {
                MDC.put("status", "PROCESSING");
                log.info("Số dư hợp lệ. Thực hiện trừ tiền: {} -> {}", oldBalance, oldBalance.subtract(amount));

                txs = TransactionStatus.SUCCESS;
                newBalance = oldBalance.subtract(amount);
            }

            // 3. Cập nhật Ví & Lưu lịch sử ví
            wallet.setBalance(newBalance);
            walletRepository.save(wallet);

            WalletTransactionEntity wtx = transactionRepository.save(WalletTransactionEntity.builder()
                    .wallet(wallet)
                    .amount(amount.negate())
                    .balanceBefore(oldBalance)
                    .balanceAfter(newBalance)
                    .type(TransactionType.PAYMENT)
                    .status(txs)
                    .description("Thanh toán đơn hàng: " + form.getOrderNumber())
                    .createdAt(LocalDateTime.now())
                    .build());

            String internalTxId = "WTX-" + wtx.getId();
            MDC.put("internalTxId", internalTxId);

            // 4. Lưu giao dịch Payment (Lớp trừu tượng cho Gateway)
            PaymentTransactionEntity tx = paymentTransactionRepository.save(PaymentTransactionEntity.builder()
                    .userId(userId)
                    .amount(amount)
                    .referenceId(form.getOrderNumber())
                    .gateway("WALLET")
                    .status(isAvailable ? "SUCCESS" : "FAIL")
                    .type(PaymentType.ORDER)
                    .createdAt(LocalDateTime.now())
                    .build());

            // 5. Chuẩn bị Outbox Event
            MDC.put("sagaStep", "PAYMENT_OUTBOX_GENERATE");
            MDC.put("status", isAvailable ? "SUCCESS" : "FAILED_BUSINESS");
            log.info("Kết thúc xử lý thanh toán ví. Kết quả: {}", txs);

            return internalTxId;

        } catch (Exception e) {
            MDC.put("status", "ERROR");
            log.error("Lỗi thảm họa khi trừ tiền ví: {}", e.getMessage());
            throw e; // Rollback transaction
        } finally {
            MDC.clear();
        }
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

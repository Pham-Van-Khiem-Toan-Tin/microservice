package com.ecommerce.paymentservice.service;

import com.ecommerce.paymentservice.dto.event.PaymentSuccessResult;
import com.ecommerce.paymentservice.dto.response.SePayWebhookDto;
import com.ecommerce.paymentservice.entity.OutboxEvent;
import com.ecommerce.paymentservice.entity.PaymentTransactionEntity;
import com.ecommerce.paymentservice.entity.WalletEntity;
import com.ecommerce.paymentservice.entity.WalletTransactionEntity;
import com.ecommerce.paymentservice.enums.*;
import com.ecommerce.paymentservice.repository.OutboxRepository;
import com.ecommerce.paymentservice.repository.PaymentTransactionRepository;
import com.ecommerce.paymentservice.repository.WalletRepository;
import com.ecommerce.paymentservice.repository.WalletTransactionRepository;
import com.ecommerce.paymentservice.utils.AuthenticationUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class SeaPayService {
    @Value("${sepay.bank-name}")
    private String bankName;
    @Value("${sepay.bank-number}")
    private String bankNumber;
    @Value("${sepay.content}")
    private String sepayContent;
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private WalletTransactionRepository walletTransactionRepository;
    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;
    @Autowired
    private OutboxRepository outboxRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Transactional
    public String generatePaymentQr(long amount, String referenceId, PaymentType type) {
        String userId = AuthenticationUtils.getUserId();
        // Tạo nội dung chuyển khoản: TZNAP 101
        String prefix = sepayContent + " " +
                ((type == PaymentType.ORDER) ? "TZORD:" : "TZNAP:");
        String effectiveReferenceId = referenceId;
        if (type == PaymentType.DEPOSIT) {
            effectiveReferenceId = userId;
        }
        String content = prefix + " " + effectiveReferenceId;
        PaymentTransactionEntity tx = PaymentTransactionEntity
                .builder()
                .userId(userId)
                .amount(BigDecimal.valueOf(amount))
                .gateway("SEPAY")
                .referenceId(referenceId)
                .type(type)
                .status("PENDING")
                .originalContent(content)
                .createdAt(LocalDateTime.now())
                .build();
        paymentTransactionRepository.save(tx);
        // Encode nội dung để tránh lỗi ký tự đặc biệt trên URL
        String encodedContent = URLEncoder.encode(content, StandardCharsets.UTF_8);
        String SEPAY_URL_BASE = "https://qr.sepay.vn/img";
        // Ghép chuỗi theo công thức SePay
        // https://qr.sepay.vn/img?acc=...&bank=...&amount=...&des=...
        return String.format("%s?acc=%s&bank=%s&amount=%d&des=%s",
                SEPAY_URL_BASE,
                bankNumber,
                bankName,
                amount,
                encodedContent
        );
    }
    @Transactional(rollbackFor = Exception.class)
    public String processWebhook(SePayWebhookDto data) {

        // 1. LỌC: Chỉ nhận tiền vào ("in")
        if (!"in".equalsIgnoreCase(data.getTransferType())) {
            return "Ignored: Money Out";
        }

        // 2. CHỐNG TRÙNG: Check ID SePay
        if (paymentTransactionRepository.existsByExternalTransId(String.valueOf(data.getId()))) {
            return "Ignored: Duplicate Transaction";
        }
        String content = data.getContent().toUpperCase();
        if (content.contains("TZORD")) {
            return processOrderPayment(data);
        } else if (content.contains("TZNAP")) {
            return processWalletDeposit(data);
        }
        return "Ignored: Invalid Content Format";
    }

    private String processOrderPayment(SePayWebhookDto data) {
        // Tách lấy mã đơn hàng (Ví dụ: TZORD ORD123 -> ORD123)
        String orderNumber = parseReferenceId(data.getContent(), "TZORD");
        if (orderNumber == null) return "Error: Cannot parse Order Number";

        // Tìm giao dịch PENDING trong hệ thống của mình
        PaymentTransactionEntity tx = paymentTransactionRepository
                .findByNormalizedReferenceId(orderNumber)
                .orElseThrow(() -> new RuntimeException("Transaction not found for order: " + orderNumber));

        // Cập nhật trạng thái giao dịch
        tx.setStatus("SUCCESS");
        tx.setExternalTransId(String.valueOf(data.getId()));
        paymentTransactionRepository.save(tx);
        try {
            // 3. Xây dựng Payload sự kiện (Khớp format với VNPAY của bạn)
            PaymentSuccessResult payload = PaymentSuccessResult.builder()
                    .orderNumber(tx.getReferenceId())
                    .amount(data.getTransferAmount())
                    .providerRef(String.valueOf(data.getId()))
                    .method(PaymentMethod.BANK)
                    .paidAt(LocalDateTime.now())
                    .build();

            // 4. Lưu vào bảng t_outbox_events (Transactional Outbox)
            OutboxEvent outbox = OutboxEvent.builder()
                    .id(UUID.randomUUID().toString())
                    .aggregateType("payment") // Sẽ sinh ra topic: payment-service.payment.events
                    .aggregateId(tx.getReferenceId())
                    .type("Payment.Succeeded")
                    .payload(objectMapper.writeValueAsString(payload))
                    .createdAt(LocalDateTime.now())
                    .build();

            outboxRepository.save(outbox);

            return "Order Processed & Event Saved to Outbox: " + orderNumber;

        } catch (JsonProcessingException e) {
            log.error("Lỗi serialize payload cho đơn hàng {}: {}", orderNumber, e.getMessage());
            throw new RuntimeException("Error processing payment event");
        }
    }
    private String processWalletDeposit(SePayWebhookDto data) {
        String rawUserId = parseReferenceId(data.getContent(), "TZNAP");
        if (rawUserId == null) return "Error: Cannot parse User ID";

        // CHỈ ép kiểu UUID tại đây
        String formattedUserId = formatToUuidString(rawUserId);

        // Lưu giao dịch
        PaymentTransactionEntity payTx = PaymentTransactionEntity.builder()
                .userId(formattedUserId)
                .amount(data.getTransferAmount())
                .gateway(data.getGateway())
                .externalTransId(String.valueOf(data.getId()))
                .status("SUCCESS")
                .type(PaymentType.DEPOSIT)
                .originalContent(data.getContent())
                .build();
        paymentTransactionRepository.save(payTx);

        // Cộng tiền ví (Giữ nguyên logic của bạn)
        WalletEntity wallet = walletRepository.findByUserId(formattedUserId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        BigDecimal oldBalance = wallet.getBalance();
        BigDecimal newBalance = oldBalance.add(data.getTransferAmount());
        wallet.setBalance(newBalance);
        walletRepository.save(wallet);
        WalletTransactionEntity walletTx = WalletTransactionEntity.builder()
                .wallet(wallet)
                .amount(data.getTransferAmount())
                .balanceBefore(oldBalance)
                .balanceAfter(newBalance)
                .type(TransactionType.DEPOSIT)
                .status(TransactionStatus.SUCCESS)
                .description("Nạp tiền tự động SePay: " + data.getContent())
                .referenceCode(String.valueOf(data.getId()))
                .build();
        walletTransactionRepository.save(walletTx);
        // Lưu lịch sử ví...
        return "Wallet Deposit Processed for User: " + formattedUserId;
    }
    private String parseReferenceId(String content, String prefix) {
        if (content == null) return null;

        // Regex này lấy tất cả ký tự là chữ, số, dấu gạch ngang hoặc khoảng trắng đứng sau prefix
        // Nó sẽ dừng lại khi gặp một ký tự đặc biệt khác hoặc kết thúc chuỗi
        Pattern pattern = Pattern.compile(prefix + "[:\\s]*([A-Z0-9\\s\\-]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            String rawId = matcher.group(1).trim();
            // QUAN TRỌNG: Xóa sạch khoảng trắng và dấu gạch ngang trước khi trả về
            return rawId.replace("-", "").replace(" ", "");
        }
        return null;
    }
    private String formatToUuidString(String rawId) {
        // Nếu chuỗi đã có dấu gạch ngang rồi thì trả về luôn (đề phòng ngân hàng đổi nết)
        if (rawId.contains("-")) {
            return rawId;
        }

        // Nếu chuỗi đủ 32 ký tự, dùng Regex chèn dấu vào đúng vị trí 8-4-4-4-12
        if (rawId.length() == 32) {
            return rawId.replaceFirst(
                    "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                    "$1-$2-$3-$4-$5"
            );
        }

        throw new IllegalArgumentException("Độ dài ID không hợp lệ: " + rawId);
    }
}

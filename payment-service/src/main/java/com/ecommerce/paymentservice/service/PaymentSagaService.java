package com.ecommerce.paymentservice.service;

import com.ecommerce.paymentservice.dto.event.*;
import com.ecommerce.paymentservice.dto.request.InternalPaymentForm;
import com.ecommerce.paymentservice.entity.OutboxEvent;
import com.ecommerce.paymentservice.entity.PaymentSessionEntity;
import com.ecommerce.paymentservice.enums.PaymentType;
import com.ecommerce.paymentservice.repository.OutboxRepository;
import com.ecommerce.paymentservice.repository.PaymentSessionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class PaymentSagaService {
    @Autowired
    private PaymentSessionRepository paymentSessionRepository;
    @Autowired
    private OutboxRepository outboxRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private SeaPayService seaPayService;
    @Autowired
    private VnPayService vnPayService;
    @Autowired
    private WalletService walletService;
    @Transactional
    public void handleInitiateRequested(PaymentInitiateRequestedPayload req) {
        UUID orderId = UUID.fromString(req.getOrderId());

        // ✅ Idempotent: đã tạo session rồi thì emit lại Payment.Initiated (nếu cần) và return
        Optional<PaymentSessionEntity> existingOpt = paymentSessionRepository.findByOrderNumber(req.getOrderNumber());

        // 2. Nếu đã tồn tại, xử lý rồi THOÁT HÀN (return của hàm chính)
        if (existingOpt.isPresent()) {
            PaymentSessionEntity existing = existingOpt.get();
            log.info("Thanh toán cho đơn hàng {} đã tồn tại.", existing.getOrderNumber());

            if (existing.getPaymentUrl() != null &&
                    ("VNPAY".equals(existing.getMethod()) || "BANK".equals(existing.getMethod()))) {
                emitPaymentInitiated(existing);
            }

            return; // ✅ Thoát hẳn hàm processPayment, không chạy xuống dưới nữa
        }

        // Tạo session
        UUID paymentId = UUID.randomUUID();
        PaymentSessionEntity session = PaymentSessionEntity.builder()
                .id(paymentId)
                .orderId(orderId)
                .orderNumber(req.getOrderNumber())
                .method(req.getMethod())
                .amount(req.getAmount())
                .status("INIT")
                .createdAt(LocalDateTime.now())
                .build();
        paymentSessionRepository.save(session);

        switch (req.getMethod()) {
            case VNPAY -> {
                // tạo url
                String res = vnPayService.createVnPayPayment(
                        req.getClientIp(),
                        req.getAmount(),
                        null,
                        PaymentType.ORDER,
                        req.getOrderNumber()
                );
                session.setPaymentUrl(res);
                session.setStatus("PENDING");
                paymentSessionRepository.save(session);

                emitPaymentInitiated(session);
            }

            case BANK -> {
                String res = seaPayService.generatePaymentQr(
                        req.getAmount(),
                        req.getOrderNumber(),
                        PaymentType.ORDER
                );
                session.setPaymentUrl(res); // nếu đây là link
                session.setQrContent("TZORD: " + req.getOrderNumber() );
                // nếu sepay trả qrContent thì set qrContent
                session.setStatus("PENDING");
                paymentSessionRepository.save(session);

                emitPaymentInitiated(session);
            }

            case WALLET -> {
                // debit ví nội bộ
                try {
                    InternalPaymentForm f = InternalPaymentForm.builder()
                            .amount(req.getAmount())
                            .orderNumber(req.getOrderNumber())
                            .build();

                    String res = walletService.processWalletPayment(f);

                    // giả sử res trả providerRef/txnId
                    session.setStatus("SUCCEEDED");
                    session.setProviderRef(res); // tuỳ response của bạn
                    paymentSessionRepository.save(session);

                } catch (Exception ex) {
                    session.setStatus("FAILED");
                    paymentSessionRepository.save(session);

                    emitPaymentFailed(session, "WALLET_DEBIT_FAILED");
                }
            }

            default -> {
                session.setStatus("FAILED");
                paymentSessionRepository.save(session);
                emitPaymentFailed(session, "UNSUPPORTED_METHOD");
            }
        }
    }

    private void emitPaymentInitiated(PaymentSessionEntity session) {
        try {
            PaymentInitiatedPayload payload = PaymentInitiatedPayload.builder()
                    .orderId(session.getOrderId().toString())
                    .paymentId(session.getId().toString())
                    .method(session.getMethod())
                    .paymentUrl(session.getPaymentUrl())
                    .qrContent(session.getQrContent())
                    .build();

            outboxRepository.save(OutboxEvent.builder()
                    .id(UUID.randomUUID().toString())
                    .aggregateType("payment")
                    .aggregateId(session.getOrderId().toString())
                    .type("Payment.Initiated")
                    .payload(objectMapper.writeValueAsString(payload))
                    .createdAt(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    private void emitPaymentFailed(PaymentSessionEntity session, String reason) {
        try {
            PaymentFailedPayload payload = PaymentFailedPayload.builder()
                    .orderId(session.getOrderId().toString())
                    .paymentId(session.getId().toString())
                    .method(session.getMethod())
                    .reason(reason)
                    .build();

            outboxRepository.save(OutboxEvent.builder()
                    .id(UUID.randomUUID().toString())
                    .aggregateType("payment")
                    .aggregateId(session.getOrderId().toString())
                    .type("Payment.InitiateFailed")
                    .payload(objectMapper.writeValueAsString(payload))
                    .createdAt(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

package com.ecommerce.paymentservice.service;

import com.ecommerce.paymentservice.config.VnPayConfig;
import com.ecommerce.paymentservice.dto.event.PaymentSuccessResult;
import com.ecommerce.paymentservice.dto.response.IpnResponse;
import com.ecommerce.paymentservice.dto.response.VNPayReturnResponse;
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
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class VnPayService {
    @Autowired
    private VnPayConfig vnPayConfig;
    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private WalletTransactionRepository walletTransactionRepository;
    @Autowired
    private OutboxRepository outboxRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public String createVnPayPayment(String req, long amount, String bankCode, PaymentType paymentType, String referenceId) {
        // 1. Lưu giao dịch PENDING (Giữ nguyên của bạn)
        try {
            MDC.put("orderNo", referenceId); // Chính là orderNumber hoặc depositId
            MDC.put("sagaStep", "PAYMENT_VNPAY_INIT");
            MDC.put("status", "STARTED");
            log.info("Khởi tạo thanh toán VNPAY: Amount={}, Type={}", amount, paymentType);
            PaymentTransactionEntity tx = new PaymentTransactionEntity();
            tx.setAmount(BigDecimal.valueOf(amount));
            tx.setUserId(AuthenticationUtils.getUserId());
            tx.setGateway("VNPAY");
            tx.setStatus("PENDING");
            tx.setCreatedAt(LocalDateTime.now());
            tx.setReferenceId(referenceId);
            tx.setType(paymentType);
            String orderInfo = paymentType == PaymentType.ORDER
                    ? "Thanh toan don hang: " + referenceId
                    : "Nap tien vao vi: " + referenceId;
            tx.setOriginalContent(orderInfo);
            paymentTransactionRepository.save(tx);

            String vnp_TxnRef = tx.getId().toString();

            // 2. Setup tham số cho VNPAY
            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_Version", "2.1.0");
            vnp_Params.put("vnp_Command", "pay");
            vnp_Params.put("vnp_TmnCode", vnPayConfig.getVnp_TmnCode());
            vnp_Params.put("vnp_Amount", String.valueOf(amount * 100L));
            vnp_Params.put("vnp_CurrCode", "VND");

            // Xử lý BankCode (Đã thêm tham số bankCode vào hàm)
            if (bankCode != null && !bankCode.isEmpty()) {
                vnp_Params.put("vnp_BankCode", bankCode);
            }

            vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
            vnp_Params.put("vnp_OrderInfo", orderInfo);
            vnp_Params.put("vnp_OrderType", "other");
            vnp_Params.put("vnp_Locale", "vn");
            vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getVnp_ReturnUrl());
            vnp_Params.put("vnp_IpAddr", req);

            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String vnp_CreateDate = formatter.format(cld.getTime());
            vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

            cld.add(Calendar.MINUTE, 15);
            String vnp_ExpireDate = formatter.format(cld.getTime());
            vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

            // --- ĐOẠN CODE CÒN THIẾU BẮT ĐẦU TẠI ĐÂY ---

            // 3. Sắp xếp các tham số theo Alphabet (Bắt buộc)
            List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
            Collections.sort(fieldNames);

            // 4. Xây dựng chuỗi HashData và QueryUrl
            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();
            Iterator<String> itr = fieldNames.iterator();

            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = vnp_Params.get(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    try {
                        // Encode dữ liệu theo chuẩn UTF-8
                        String encodedValue = URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString());

                        // Build hash data (để tính toán checksum)
                        hashData.append(fieldName).append('=').append(encodedValue);

                        // Build query string (để tạo đường link)
                        query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8.toString()))
                                .append('=')
                                .append(encodedValue);

                        if (itr.hasNext()) {
                            query.append('&');
                            hashData.append('&');
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            // 5. Tính toán SecureHash (Chữ ký bảo mật)
            String queryUrl = query.toString();
            String vnp_SecureHash = VnPayConfig.hmacSHA512(vnPayConfig.getSecretKey(), hashData.toString());

            // 6. Nối SecureHash vào URL cuối cùng
            queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
            MDC.put("transactionId", tx.getId().toString());
            MDC.put("status", "SUCCESS");
            log.info("Đã tạo link thanh toán VNPAY thành công. TransactionId={}", tx.getId());
            return vnPayConfig.getVnp_PayUrl() + "?" + queryUrl;
        } catch (Exception e) {
            MDC.put("status", "FAILED");
            log.error("Lỗi khởi tạo thanh toán VNPAY: {}", e.getMessage());
            throw e;
        } finally {
            MDC.clear();
        }
    }

    @Transactional
    public IpnResponse processIpn(HttpServletRequest request) {
        try {
            // 1. Lấy tất cả tham số từ VNPAY gửi về
            String vnp_TxnRef = request.getParameter("vnp_TxnRef");
            MDC.put("transactionId", vnp_TxnRef);
            MDC.put("sagaStep", "PAYMENT_VNPAY_IPN_CALLBACK");
            MDC.put("status", "PROCESSING");
            log.info("Nhận IPN từ VNPAY. ResponseCode={}", request.getParameter("vnp_ResponseCode"));
            Map<String, String> fields = new HashMap<>();
            for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements(); ) {
                String fieldName = params.nextElement();
                String fieldValue = request.getParameter(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    fields.put(fieldName, fieldValue);
                }
            }

            // 2. Lấy các tham số quan trọng
            String vnp_SecureHash = request.getParameter("vnp_SecureHash");
            String vnp_Amount = request.getParameter("vnp_Amount");         // Số tiền * 100
            String vnp_ResponseCode = request.getParameter("vnp_ResponseCode"); // 00 = Thành công
            String vnp_TransactionNo = request.getParameter("vnp_TransactionNo"); // Mã GD tại VNPAY

            // 3. VERIFY CHECKSUM: Xóa hash cũ, tính lại hash mới để so sánh
            if (fields.containsKey("vnp_SecureHashType")) fields.remove("vnp_SecureHashType");
            if (fields.containsKey("vnp_SecureHash")) fields.remove("vnp_SecureHash");

            String signValue = VnPayConfig.hmacSHA512(vnPayConfig.getSecretKey(), hashAllFields(fields));
            if (!signValue.equals(vnp_SecureHash)) {
                MDC.put("status", "FAILED");
                log.warn("IPN sai Checksum! Checksum nhận được: {}", vnp_SecureHash);
                return new IpnResponse("97", "Invalid Checksum");
            }

            // 4. CHECK 1: Tìm giao dịch trong DB bằng UUID
            UUID transactionId;
            try {
                transactionId = UUID.fromString(vnp_TxnRef);
            } catch (IllegalArgumentException e) {
                return new IpnResponse("01", "Order not found (Invalid UUID)");
            }

            PaymentTransactionEntity tx = paymentTransactionRepository.findById(transactionId).orElse(null);
            if (tx == null) {
                MDC.put("status", "FAILED");
                log.error("IPN không tìm thấy TransactionId trong DB!");
                return new IpnResponse("01", "Order not found");
            }
            MDC.put("orderNo", tx.getReferenceId());
            // 5. CHECK 2: Kiểm tra số tiền (VNPAY nhân 100 -> Mình phải chia 100)
            BigDecimal vnpAmountValue = new BigDecimal(vnp_Amount).divide(new BigDecimal(100));
            if (tx.getAmount().compareTo(vnpAmountValue) != 0) {
                return new IpnResponse("04", "Invalid Amount");
            }

            // 6. CHECK 3: Kiểm tra trạng thái đơn hàng (Tránh xử lý trùng lặp)
            if (!"PENDING".equals(tx.getStatus())) {
                return new IpnResponse("02", "Order already confirmed");
            }

            // 7. XỬ LÝ KẾT QUẢ
            if ("00".equals(vnp_ResponseCode)) {
                // --- TRƯỜNG HỢP THÀNH CÔNG ---
                MDC.put("status", "SUCCESS");
                log.info("IPN Thanh toán thành công! Đang thực hiện xử lý nghiệp vụ cho {}", tx.getType());
                // A. Update trạng thái Transaction
                tx.setStatus("SUCCESS");
                tx.setExternalTransId(vnp_TransactionNo); // Lưu mã GD VNPAY để đối soát
                paymentTransactionRepository.save(tx);
                if (PaymentType.DEPOSIT.equals(tx.getType())) {
                    processWalletDeposit(tx, vnp_TransactionNo);
                } else if (PaymentType.ORDER.equals(tx.getType())) {
                    processOrderPayment(tx, vnp_TransactionNo);
                }
                return new IpnResponse("00", "Confirm Success");
            } else {
                // --- TRƯỜNG HỢP THẤT BẠI (Khách hủy, lỗi bank...) ---
                MDC.put("status", "CANCELLED");
                log.info("Khách hàng đã hủy giao dịch hoặc GD thất bại tại Bank.");
                tx.setStatus("FAILED");
                paymentTransactionRepository.save(tx);

                System.out.println("IPN FAILED: " + vnp_TxnRef);
                return new IpnResponse("00", "Confirm Success");
            }

        } catch (Exception e) {
            MDC.put("status", "ERROR");
            log.error("Lỗi hệ thống khi xử lý IPN: {}", e.getMessage());
            return new IpnResponse("99", "Unknown Error");
        }
    }

    private void processOrderPayment(PaymentTransactionEntity tx, String vnp_TransactionNo) throws JsonProcessingException {
        // 1. Chuẩn bị nội dung gửi đi (Payload)
        MDC.put("sagaStep", "PAYMENT_OUTBOX_GENERATE");
        log.info("Bắt đầu tạo sự kiện Outbox cho Đơn hàng: {}", tx.getReferenceId());
        PaymentSuccessResult payload = PaymentSuccessResult.builder()
                .orderNumber(tx.getReferenceId())
                .amount(tx.getAmount())
                .providerRef(vnp_TransactionNo)
                .paidAt(LocalDateTime.now())
                .method(PaymentMethod.VNPAY)
                .build();

        // 2. Lưu vào bảng t_outbox_events của Payment Service
        OutboxEvent outbox = OutboxEvent.builder()
                .id(UUID.randomUUID().toString())
                .aggregateType("payment") // Sẽ sinh ra topic: payment-service.payment.events
                .aggregateId(tx.getReferenceId())
                .type("Payment.Succeeded")
                .payload(objectMapper.writeValueAsString(payload))
                .createdAt(LocalDateTime.now())
                .build();

        outboxRepository.save(outbox);
        log.info("Đã lưu Outbox cho thanh toán đơn hàng: {}", tx.getReferenceId());
    }

    private void processWalletDeposit(PaymentTransactionEntity tx, String vnp_TransactionNo) {
        MDC.put("sagaStep", "PAYMENT_WALLET_DEPOSIT");
        log.info("Thực hiện cộng tiền vào ví cho User: {}", tx.getUserId());
        WalletEntity wallet = walletRepository.findByUserId(tx.getUserId())
                .orElseThrow(() -> new RuntimeException("Ví không tồn tại"));

        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            log.error("Nạp tiền thất bại: Ví của User {} đang bị khóa.", tx.getUserId());
            return;
        }

        BigDecimal balanceBefore = wallet.getBalance();
        BigDecimal balanceAfter = balanceBefore.add(tx.getAmount());
        log.info("Số dư cũ: {} | Số tiền nạp: {} | Số dư mới: {}",
                balanceBefore, tx.getAmount(), balanceAfter);
        // 1. Cập nhật số dư ví
        wallet.setBalance(balanceAfter);
        walletRepository.save(wallet);

        // 2. Lưu lịch sử giao dịch ví
        WalletTransactionEntity history = WalletTransactionEntity.builder()
                .wallet(wallet)
                .amount(tx.getAmount())
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .type(TransactionType.DEPOSIT)
                .status(TransactionStatus.SUCCESS)
                .referenceCode(vnp_TransactionNo)
                .description("Nạp tiền thành công qua VNPAY")
                .build();
        walletTransactionRepository.save(history);
        log.info("Đã nạp tiền ví thành công cho giao dịch: {}", vnp_TransactionNo);
    }

    @Transactional(readOnly = true)
    public VNPayReturnResponse orderReturn(HttpServletRequest request) {
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements(); ) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");
        fields.remove("vnp_SecureHash");

        // 1. Kiểm tra chữ ký
        String signValue = VnPayConfig.hmacSHA512(vnPayConfig.getSecretKey(), hashAllFields(fields));
        if (!signValue.equals(vnp_SecureHash)) {
            return new VNPayReturnResponse(-1, null, null);
        }

        // 2. Kiểm tra mã phản hồi
        String vnp_ResponseCode = request.getParameter("vnp_ResponseCode");
        String vnp_TxnRef = request.getParameter("vnp_TxnRef");

        // Truy vấn thông tin giao dịch từ DB để biết loại (ORDER hay DEPOSIT)
        PaymentTransactionEntity tx = paymentTransactionRepository.findById(UUID.fromString(vnp_TxnRef))
                .orElse(null);

        if (tx == null) return new VNPayReturnResponse(0, null, null);

        if (!"00".equals(vnp_ResponseCode)) {
            return new VNPayReturnResponse(0, tx.getType().toString(), tx.getReferenceId());
        }

        return new VNPayReturnResponse(1, tx.getType().toString(), tx.getReferenceId());
    }

    // Hàm hỗ trợ hash dữ liệu (QUAN TRỌNG: KHÔNG ĐƯỢC XÓA)
    private String hashAllFields(Map<String, String> fields) {
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder sb = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = fields.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                sb.append(fieldName);
                sb.append("=");
                try {
                    sb.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (itr.hasNext()) {
                    sb.append("&");
                }
            }
        }
        return sb.toString();
    }
}

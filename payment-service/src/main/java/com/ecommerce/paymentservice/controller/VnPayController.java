package com.ecommerce.paymentservice.controller;

import com.ecommerce.paymentservice.config.VnPayConfig;
import com.ecommerce.paymentservice.dto.request.PaymentDTO;
import com.ecommerce.paymentservice.dto.response.IpnResponse;
import com.ecommerce.paymentservice.dto.response.VNPayReturnResponse;
import com.ecommerce.paymentservice.enums.PaymentType;
import com.ecommerce.paymentservice.service.VnPayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/vnpay")
public class VnPayController {
    @Autowired
    private VnPayService vnPayService;

    @GetMapping("/create-payment")
    public Map<String, String> createPayment(HttpServletRequest req,
                                             @RequestParam long amount,
                                             @RequestParam(required = false) String bankCode,
                                             @RequestParam String type,
                                             @RequestParam(required = false) String referenceId,
                                             @RequestParam(required = false) String ipAddress) {
        PaymentType paymentType = PaymentType.valueOf(type.toUpperCase());
        String paymentUrl = vnPayService.createVnPayPayment(req, amount, bankCode, paymentType, referenceId);

        Map<String, String> result = new HashMap<>();
        result.put("status", "ok");
        result.put("message", "success");
        result.put("url", paymentUrl);

        return result;
    }

    @GetMapping("/vnpay_ipn")
    public IpnResponse vnpayIpn(HttpServletRequest request) {
        // Gọi Service xử lý toàn bộ logic
//        return new IpnResponse();
        return vnPayService.processIpn(request);
    }

    @GetMapping("/vnpay-return")
    public void vnpayReturn(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 1. Gọi service để kiểm tra kết quả (thành công/thất bại/sai chữ ký)
        VNPayReturnResponse vnpayResponse = vnPayService.orderReturn(request);
        String baseUrl = "http://localhost:5174";
        String finalUrl;

        // Xác định trạng thái thành công/thất bại
        String statusParam = (vnpayResponse.getResult() == 1) ? "success" : "error";

        // Rẽ nhánh điều hướng dựa trên LOẠI GIAO DỊCH
        if ("DEPOSIT".equals(vnpayResponse.getType())) {
            // Nếu nạp tiền -> Về trang ví
            finalUrl = String.format("%s/my-wallet?status=%s", baseUrl, statusParam);
        }
        else if ("ORDER".equals(vnpayResponse.getType())) {
            // Nếu thanh toán đơn hàng -> Về trang thành công đơn hàng
            finalUrl = String.format("%s/order-success?orderNo=%s&status=%s",
                    baseUrl, vnpayResponse.getReferenceId(), statusParam);
        }
        else {
            // Trường hợp lỗi không xác định -> Về trang chủ hoặc trang lỗi chung
            finalUrl = baseUrl + "/payment-error";
        }
        response.sendRedirect(finalUrl);
    }
}

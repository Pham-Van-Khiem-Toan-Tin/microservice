package com.ecommerce.orderservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class SseService {
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String orderNumber) {
        // Timeout 2 phút. Nếu quá 2 phút không có URL, kết nối tự đóng.
        SseEmitter emitter = new SseEmitter(120_000L);

        emitter.onCompletion(() -> emitters.remove(orderNumber));
        emitter.onTimeout(() -> emitters.remove(orderNumber));
        emitter.onError((e) -> emitters.remove(orderNumber));

        emitters.put(orderNumber, emitter);
        log.info("Đã mở kết nối SSE cho đơn hàng: {}", orderNumber);
        return emitter;
    }

    public void sendPaymentUrl(String orderNumber, String url, LocalDateTime expiresAt) {
        SseEmitter emitter = emitters.get(orderNumber);

        if (emitter != null) {
            try {
                Map<String, Object> data = new HashMap<>();
                data.put("url", url);
                data.put("expiresAt", expiresAt.toString()); // Chuyển sang String cho chắc
                data.put("orderNumber", orderNumber);

                emitter.send(SseEmitter.event()
                        .id(UUID.randomUUID().toString()) // Thêm ID cho event
                        .name("PAYMENT_URL")
                        .data(data, MediaType.APPLICATION_JSON)); // Ép kiểu JSON

                // Chỉ gọi complete() nếu bạn muốn React chuyển trang ngay
                // emitter.complete();

                log.info("Đã đẩy URL thành công cho đơn hàng: {}", orderNumber);
            } catch (Exception e) {
                log.error("Lỗi khi gửi dữ liệu SSE cho đơn {}: {}", orderNumber, e.getMessage());
                emitter.completeWithError(e); // Đóng với trạng thái lỗi
                emitters.remove(orderNumber);
            }
        } else {
            log.warn("Khách hàng đơn {} không online (SSE null). Dữ liệu sẽ được lưu DB để chờ khách F5.", orderNumber);
        }
    }
    public void sendPaymentSuccess(String orderNumber) {
        SseEmitter emitter = emitters.get(orderNumber);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("PAYMENT_SUCCESS").data("SUCCESS"));

                // Sau khi báo thành công mới đóng kết nối
                emitter.complete();
                emitters.remove(orderNumber);
            } catch (IOException e) {
                emitters.remove(orderNumber);
            }
        }
    }
}

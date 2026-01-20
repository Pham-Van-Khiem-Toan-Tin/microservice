package com.ecommerce.orderservice.dto.request;

import com.ecommerce.orderservice.entity.PaymentMethod;
import lombok.Data;

@Data
public class OrderCreateForm {
    private String addressId;
    private PaymentMethod paymentMethod;
    private String note;
}

package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.dto.request.UpdateOrderRequest;
import com.ecommerce.orderservice.dto.response.OrderDTO;
import com.ecommerce.orderservice.dto.response.OrderDetailDTO;
import com.ecommerce.orderservice.dto.response.OrderExistenceDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {
    Page<OrderDTO> search(String keyword, List<String> fields, String sort, int page, int size);
    OrderExistenceDTO findProductExist(String productId);
    void updateOrder(String orderId, UpdateOrderRequest req);
    OrderDetailDTO findOrderDetail(String orderId);
    List<OrderDTO> findAllMyOrder();
    OrderDetailDTO findMyOrderDetail(String id);
}

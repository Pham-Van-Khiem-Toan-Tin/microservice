package com.ecommerce.orderservice.integration;

import com.ecommerce.orderservice.dto.event.ReReserveRequest;
import com.ecommerce.orderservice.dto.request.OrderCreateInventoryForm;
import com.ecommerce.orderservice.dto.request.OrderItemCheckForm;
import com.ecommerce.orderservice.dto.response.inventory.InventoryAvailableDto;
import com.ecommerce.orderservice.dto.response.inventory.InventoryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(
        name = "inventory-service",
        url = "${application.config.inventory-url}"
)
public interface InventoryFeignClient {
    @PostMapping("/inventories/batch-check")
    List<InventoryDto> getStockBySkuIds(@RequestBody List<String> skuCodes);
    @PostMapping("/inventories/check-availability")
    List<InventoryAvailableDto> checkAvailability(@RequestBody OrderCreateInventoryForm form);
    @PostMapping("/inventories/re-reserve")
    List<InventoryAvailableDto> reReserve(@RequestBody ReReserveRequest form);
}

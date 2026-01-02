package com.ecommerce.catalogservice.entity;


public enum OutboxEventType {
    PRODUCT_UPSERT, PRODUCT_DELETE, PRICE_CHANGED, STOCK_CHANGED
}

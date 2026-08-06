package com.kang.ecommercedataplatform.order.dto;

public record OrderItemPricing(
        Long productOptionId,
        int quantity,
        int unitPrice
) {
}

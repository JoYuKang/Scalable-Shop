package com.kang.ecommercedataplatform.order.dto;

public record OrderItemCreateRequest(
        Long productOptionId,
        int quantity
) {
}

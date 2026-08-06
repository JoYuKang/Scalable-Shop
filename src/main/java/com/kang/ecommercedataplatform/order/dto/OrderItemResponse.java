package com.kang.ecommercedataplatform.order.dto;

import com.kang.ecommercedataplatform.order.domain.OrderItem;

public record OrderItemResponse(
        Long id,
        Long productOptionId,
        int quantity,
        int orderPrice

) {
    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getProductOptionId(),
                item.getQuantity(),
                item.getOrderPrice()
        );
    }
}

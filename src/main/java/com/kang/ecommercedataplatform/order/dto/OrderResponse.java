package com.kang.ecommercedataplatform.order.dto;

import com.kang.ecommercedataplatform.order.domain.Order;

import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        Long memberId,
        int totalAmount,
        String status,
        LocalDateTime reservationExpiresAt,
        List<OrderItemResponse> items
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getMember().getId(),
                order.getTotalAmount(),
                order.getStatus().name(),
                order.getReservationExpiresAt(),
                order.getItems().stream().map(OrderItemResponse::from).toList()
        );
    }
}

package com.kang.ecommercedataplatform.order.dto;

import java.util.List;

public record OrderCreateRequest(
        Long memberId,
        List<OrderItemCreateRequest> items
) {
}

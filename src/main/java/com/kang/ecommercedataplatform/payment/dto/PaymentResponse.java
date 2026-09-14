package com.kang.ecommercedataplatform.payment.dto;

import com.kang.ecommercedataplatform.payment.domain.Payment;

import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        Long orderId,
        String method,
        int amount,
        String status,
        String pgTid,
        LocalDateTime approvedAt,
        String failReason
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getMethod(),
                payment.getAmount(),
                payment.getStatus().name(),
                payment.getPgTid(),
                payment.getApprovedAt(),
                payment.getFailReason()
        );
    }
}

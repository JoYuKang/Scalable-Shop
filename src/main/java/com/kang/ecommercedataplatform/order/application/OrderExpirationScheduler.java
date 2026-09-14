package com.kang.ecommercedataplatform.order.application;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 결제 안 하고 방치된 PENDING_PAYMENT 주문을 주기적으로 정리한다.
 * 실제 정리 로직(주문 취소 + 재고 반환)은 OrderService에 둬서, 이 클래스는
 * "언제 실행할지"만 책임지고 트랜잭션 경계는 OrderService가 갖게 했다.
 */
@Component
@RequiredArgsConstructor
public class OrderExpirationScheduler {

    private final OrderService orderService;

    @Scheduled(fixedDelay = 60_000)
    public void expireOverdueOrders() {
        orderService.expireOverdueOrders();
    }
}

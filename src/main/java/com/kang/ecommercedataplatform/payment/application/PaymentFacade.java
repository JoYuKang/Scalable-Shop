package com.kang.ecommercedataplatform.payment.application;

import com.kang.ecommercedataplatform.order.application.OrderService;
import com.kang.ecommercedataplatform.payment.dto.PaymentRequest;
import com.kang.ecommercedataplatform.payment.dto.PaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * OrderFacade가 "재고 락 + 여러 상품의 가격 계산"을 조율하는 것과 같은 이유로,
 * 결제도 order 도메인(상태 검증·확정)과 payment 도메인(승인 이력 저장), 그리고
 * 외부 경계인 PgClient까지 셋을 조율해야 해서 별도 Facade로 뺐다.
 * PaymentService/OrderService는 각자 자기 도메인의 트랜잭션만 책임지고,
 * "그래서 결제를 어떤 순서로 처리할지"는 전부 여기 있다.
 */
@Component
@RequiredArgsConstructor
public class PaymentFacade {

    private final OrderService orderService;
    private final PaymentService paymentService;
    private final PgClient pgClient;

    public PaymentResponse pay(Long orderId, PaymentRequest request) {
        int amount = orderService.getPayableAmount(orderId); // PENDING_PAYMENT 아니거나 만료면 여기서 예외

        Long paymentId = paymentService.createReadyPayment(orderId, request.method(), amount);
        PgApproveResult result = pgClient.approve(orderId, amount, request.method());

        if (result.success()) {
            paymentService.approve(paymentId, result.pgTid());
            orderService.confirmPayment(orderId);
        } else {
            paymentService.fail(paymentId, result.failReason());
            // 주문 상태는 그대로 PENDING_PAYMENT로 둔다 → reservationExpiresAt 전까지 재시도 가능
        }

        return paymentService.getPaymentResponse(paymentId);
    }
}

package com.kang.ecommercedataplatform.payment.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.kang.ecommercedataplatform.order.application.OrderService;
import com.kang.ecommercedataplatform.payment.dto.PaymentRequest;
import com.kang.ecommercedataplatform.payment.dto.PaymentResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * PaymentFacade가 "누구를 호출하고, 언제 어떤 걸 호출하지 않는지"를 검증하는 오케스트레이션 테스트.
 * 각 단계의 세부 로직(결제 상태 전이 등)은 PaymentServiceUnitTest/OrderServiceUnitTest에서 검증하므로
 * 여기서는 순서와 조건 분기만 확인한다.
 */
@ExtendWith(MockitoExtension.class)
class PaymentFacadeUnitTest {

    @Mock private OrderService orderService;
    @Mock private PaymentService paymentService;
    @Mock private PgClient pgClient;

    @Test
    @DisplayName("PG 승인에 성공하면 결제를 승인 처리하고 주문도 결제완료로 확정한다")
    void pay_pgApproved_approvesPaymentAndConfirmsOrder() {
        given(orderService.getPayableAmount(1L)).willReturn(24000);
        given(paymentService.createReadyPayment(1L, "CARD", 24000)).willReturn(100L);
        given(pgClient.approve(1L, 24000, "CARD")).willReturn(PgApproveResult.success("MOCK-TID"));
        given(paymentService.getPaymentResponse(100L)).willReturn(
                new PaymentResponse(100L, 1L, "CARD", 24000, "SUCCESS", "MOCK-TID", null, null));

        PaymentFacade paymentFacade = new PaymentFacade(orderService, paymentService, pgClient);
        PaymentResponse response = paymentFacade.pay(1L, new PaymentRequest("CARD"));

        verify(paymentService).approve(100L, "MOCK-TID");
        verify(orderService).confirmPayment(1L);
        verify(paymentService, never()).fail(any(), anyString());
        assertEquals("SUCCESS", response.status());
    }

    @Test
    @DisplayName("PG 승인에 실패하면 결제만 실패 처리하고, 주문은 결제완료로 확정하지 않는다 (재시도 가능하게 남겨둠)")
    void pay_pgDeclined_failsPaymentWithoutConfirmingOrder() {
        given(orderService.getPayableAmount(1L)).willReturn(24000);
        given(paymentService.createReadyPayment(1L, "FAIL_TEST", 24000)).willReturn(101L);
        given(pgClient.approve(1L, 24000, "FAIL_TEST")).willReturn(PgApproveResult.failure("MOCK_PG_DECLINED"));
        given(paymentService.getPaymentResponse(101L)).willReturn(
                new PaymentResponse(101L, 1L, "FAIL_TEST", 24000, "FAILED", null, null, "MOCK_PG_DECLINED"));

        PaymentFacade paymentFacade = new PaymentFacade(orderService, paymentService, pgClient);
        paymentFacade.pay(1L, new PaymentRequest("FAIL_TEST"));

        verify(paymentService).fail(101L, "MOCK_PG_DECLINED");
        verify(orderService, never()).confirmPayment(any());
    }

    @Test
    @DisplayName("이미 결제됐거나 예약이 만료된 주문이면 결제 시도 자체를 만들지 않는다")
    void pay_orderNotPayable_throwsWithoutCreatingPayment() {
        given(orderService.getPayableAmount(1L)).willThrow(new IllegalStateException("주문 예약이 만료되었습니다."));

        PaymentFacade paymentFacade = new PaymentFacade(orderService, paymentService, pgClient);

        assertThrows(IllegalStateException.class, () -> paymentFacade.pay(1L, new PaymentRequest("CARD")));
        verify(paymentService, never()).createReadyPayment(any(), anyString(), anyInt());
        verify(pgClient, never()).approve(any(), anyInt(), anyString());
    }
}

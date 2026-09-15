package com.kang.ecommercedataplatform.payment.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.kang.ecommercedataplatform.payment.domain.Payment;
import com.kang.ecommercedataplatform.payment.domain.PaymentStatus;
import com.kang.ecommercedataplatform.payment.infrastructure.PaymentJpaRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceUnitTest {

    @Mock private PaymentJpaRepository paymentJpaRepository;
    @InjectMocks private PaymentService paymentService;

    @Test
    @DisplayName("결제를 생성하면 READY 상태로 주문 금액만큼 저장한다")
    void createReadyPayment_savesPaymentInReadyStatus() {
        paymentService.createReadyPayment(1L, "CARD", 24000);

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentJpaRepository).save(captor.capture());
        Payment saved = captor.getValue();
        assertEquals(1L, saved.getOrderId());
        assertEquals(24000, saved.getAmount());
        assertEquals(PaymentStatus.READY, saved.getStatus());
    }

    @Test
    @DisplayName("결제 승인에 성공하면 상태가 SUCCESS로 바뀌고 pgTid가 남는다")
    void approve_marksPaymentSuccessWithPgTid() {
        Payment payment = Payment.builder().orderId(1L).method("CARD").amount(10000).build();
        given(paymentJpaRepository.findById(1L)).willReturn(Optional.of(payment));

        paymentService.approve(1L, "MOCK-TID-1");

        assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
        assertEquals("MOCK-TID-1", payment.getPgTid());
    }

    @Test
    @DisplayName("결제가 실패하면 상태가 FAILED로 바뀌고 실패 사유가 남는다 (주문은 이 메서드가 손대지 않는다)")
    void fail_marksPaymentFailedWithReason() {
        Payment payment = Payment.builder().orderId(1L).method("CARD").amount(10000).build();
        given(paymentJpaRepository.findById(1L)).willReturn(Optional.of(payment));

        paymentService.fail(1L, "MOCK_PG_DECLINED");

        assertEquals(PaymentStatus.FAILED, payment.getStatus());
        assertEquals("MOCK_PG_DECLINED", payment.getFailReason());
    }

    @Test
    @DisplayName("존재하지 않는 결제를 승인/실패 처리하려 하면 예외가 발생한다")
    void approve_paymentNotFound_throws() {
        given(paymentJpaRepository.findById(1L)).willReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> paymentService.approve(1L, "TID"));
        verify(paymentJpaRepository, never()).save(any());
    }
}

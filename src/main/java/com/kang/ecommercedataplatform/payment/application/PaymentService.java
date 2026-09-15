package com.kang.ecommercedataplatform.payment.application;

import com.kang.ecommercedataplatform.payment.domain.Payment;
import com.kang.ecommercedataplatform.payment.dto.PaymentResponse;
import com.kang.ecommercedataplatform.payment.infrastructure.PaymentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentJpaRepository paymentJpaRepository;

    /**
     * payment.order_id는 1:N — 같은 주문에 실패한 결제 시도가 여러 건 쌓여도
     * 새로 READY 행을 하나 더 추가할 뿐, 기존 행을 덮어쓰지 않는다 (재시도 이력 보존).
     */
    @Transactional
    public Long createReadyPayment(Long orderId, String method, int amount) {
        Payment payment = Payment.builder()
                .orderId(orderId)
                .method(method)
                .amount(amount)
                .build();
        paymentJpaRepository.save(payment);
        return payment.getId();
    }

    @Transactional
    public void approve(Long paymentId, String pgTid) {
        getPaymentOrThrow(paymentId).approve(pgTid);
    }

    @Transactional
    public void fail(Long paymentId, String reason) {
        getPaymentOrThrow(paymentId).fail(reason);
    }

    public PaymentResponse getPaymentResponse(Long paymentId) {
        return PaymentResponse.from(getPaymentOrThrow(paymentId));
    }

    public List<PaymentResponse> getPaymentHistory(Long orderId) {
        return paymentJpaRepository.findByOrderId(orderId).stream()
                .map(PaymentResponse::from)
                .toList();
    }

    private Payment getPaymentOrThrow(Long paymentId) {
        return paymentJpaRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다. id=" + paymentId));
    }
}

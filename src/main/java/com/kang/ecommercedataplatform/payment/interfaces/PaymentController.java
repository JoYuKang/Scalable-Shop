package com.kang.ecommercedataplatform.payment.interfaces;

import com.kang.ecommercedataplatform.payment.application.PaymentFacade;
import com.kang.ecommercedataplatform.payment.application.PaymentService;
import com.kang.ecommercedataplatform.payment.dto.PaymentRequest;
import com.kang.ecommercedataplatform.payment.dto.PaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders/{orderId}/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentFacade paymentFacade;
    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> pay(@PathVariable Long orderId, @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentFacade.pay(orderId, request));
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getPaymentHistory(@PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.getPaymentHistory(orderId));
    }
}

package com.kang.ecommercedataplatform.payment.infrastructure;

import com.kang.ecommercedataplatform.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByOrderId(Long orderId);
}

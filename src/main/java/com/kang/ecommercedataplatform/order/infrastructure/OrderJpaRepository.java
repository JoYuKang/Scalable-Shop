package com.kang.ecommercedataplatform.order.infrastructure;

import com.kang.ecommercedataplatform.order.domain.Order;
import com.kang.ecommercedataplatform.order.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderJpaRepository extends JpaRepository<Order, Long> {
    List<Order> findByStatusAndReservationExpiresAtBefore(OrderStatus status, LocalDateTime time);
}

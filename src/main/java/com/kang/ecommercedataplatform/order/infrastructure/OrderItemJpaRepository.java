package com.kang.ecommercedataplatform.order.infrastructure;

import com.kang.ecommercedataplatform.order.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemJpaRepository extends JpaRepository<OrderItem, Long> {
}

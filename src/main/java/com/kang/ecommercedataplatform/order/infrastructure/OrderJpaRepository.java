package com.kang.ecommercedataplatform.order.infrastructure;

import com.kang.ecommercedataplatform.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderJpaRepository extends JpaRepository<Order, Long> {
}

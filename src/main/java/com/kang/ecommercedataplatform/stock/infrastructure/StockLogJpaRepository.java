package com.kang.ecommercedataplatform.stock.infrastructure;

import com.kang.ecommercedataplatform.stock.domain.StockLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockLogJpaRepository extends JpaRepository<StockLog, Long> {
}

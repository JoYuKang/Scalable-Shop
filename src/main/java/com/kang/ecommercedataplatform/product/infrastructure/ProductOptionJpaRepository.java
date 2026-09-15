package com.kang.ecommercedataplatform.product.infrastructure;

import com.kang.ecommercedataplatform.product.domain.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductOptionJpaRepository extends JpaRepository<ProductOption, Long> {

    /** 대량 조회 시 상품별로 한 건씩 lazy-load하는 N+1을 피하려고 상품 ID 목록으로 한 번에 가져옴. */
    List<ProductOption> findByProductIdIn(List<Long> productIds);
}

package com.kang.ecommercedataplatform.product.infrastructure;

import com.kang.ecommercedataplatform.product.domain.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductOptionJpaRepository extends JpaRepository<ProductOption, Long> {
}

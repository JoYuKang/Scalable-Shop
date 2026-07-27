package com.kang.ecommercedataplatform.product.infrastructure;

import com.kang.ecommercedataplatform.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductJpaRepository extends JpaRepository<Product, Long> {
}

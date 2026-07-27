package com.kang.ecommercedataplatform.product.infrastructure;

import com.kang.ecommercedataplatform.product.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryJpaRepository extends JpaRepository<Category, Long> {
}

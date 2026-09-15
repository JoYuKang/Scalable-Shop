package com.kang.ecommercedataplatform.product.infrastructure;

import com.kang.ecommercedataplatform.product.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductJpaRepository extends JpaRepository<Product, Long> {

    /** category는 *-to-one이라 페이징과 fetch join을 같이 써도 안전함 (options 같은 컬렉션은 안 됨). */
    @EntityGraph(attributePaths = "category")
    @Override
    Page<Product> findAll(Pageable pageable);
}

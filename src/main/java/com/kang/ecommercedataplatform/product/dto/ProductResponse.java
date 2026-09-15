package com.kang.ecommercedataplatform.product.dto;

import com.kang.ecommercedataplatform.product.domain.Product;
import com.kang.ecommercedataplatform.product.domain.ProductOption;

import java.util.List;

public record ProductResponse(
        Long id,
        Long sellerId,
        Long categoryId,
        String categoryName,
        String name,
        int basePrice,
        String status,
        List<ProductOptionResponse> options
) {
    public static ProductResponse from(Product product) {
        return from(product, product.getOptions());
    }

    /**
     * 대량 조회용 — product.getOptions()를 건드리면(위 from(Product))
     * 상품마다 lazy 컬렉션을 따로 불러오는 N+1이 생긴다. 옵션을 미리 배치로 조회해
     * 넘겨받아 그 lazy 접근 자체를 피한다.
     */
    public static ProductResponse from(Product product, List<ProductOption> options) {
        return new ProductResponse(
                product.getId(),
                product.getSeller().getId(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.getName(),
                product.getBasePrice(),
                product.getStatus().name(),
                options.stream().map(ProductOptionResponse::from).toList()
        );
    }
}

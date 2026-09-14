package com.kang.ecommercedataplatform.search.dto;

import com.kang.ecommercedataplatform.search.domain.SearchProduct;

public record SearchResponse(
        Long id,
        String name,
        Long categoryId,
        String categoryName,
        int basePrice,
        String status,
        int totalStock
) {
    public static SearchResponse from(SearchProduct product) {
        return new SearchResponse(
                product.getId(),
                product.getName(),
                product.getCategoryId(),
                product.getCategoryName(),
                product.getBasePrice(),
                product.getStatus(),
                product.getTotalStock()
        );
    }
}

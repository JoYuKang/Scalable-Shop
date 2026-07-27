package com.kang.ecommercedataplatform.product.dto;

import com.kang.ecommercedataplatform.product.domain.Product;

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
        return new ProductResponse(
                product.getId(),
                product.getSeller().getId(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.getName(),
                product.getBasePrice(),
                product.getStatus().name(),
                product.getOptions().stream().map(ProductOptionResponse::from).toList()
        );
    }
}

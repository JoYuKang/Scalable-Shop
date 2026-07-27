package com.kang.ecommercedataplatform.product.dto;

import com.kang.ecommercedataplatform.product.domain.ProductOption;

public record ProductOptionResponse(
        Long id,
        String optionName,
        int additionalPrice,
        int stockQuantity
) {
    public static ProductOptionResponse from(ProductOption option) {
        return new ProductOptionResponse(
                option.getId(),
                option.getOptionName(),
                option.getAdditionalPrice(),
                option.getStockQuantity()
        );
    }
}

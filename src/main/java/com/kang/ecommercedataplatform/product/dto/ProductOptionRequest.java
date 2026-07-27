package com.kang.ecommercedataplatform.product.dto;

public record ProductOptionRequest(
        String optionName,
        int additionalPrice,
        int stockQuantity

) {
}

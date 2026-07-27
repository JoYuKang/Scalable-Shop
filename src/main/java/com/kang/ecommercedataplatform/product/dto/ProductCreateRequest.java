package com.kang.ecommercedataplatform.product.dto;

import java.util.List;

public record ProductCreateRequest(
        Long sellerId,
        Long categoryId,
        String name,
        int basePrice,
        List<ProductOptionRequest> options
) {

}

package com.kang.ecommercedataplatform.product.dto;

/** CSV/Excel 한 행 = 상품 1개 + 옵션 1개. 옵션 여러 개짜리 상품은 이 포맷으로 표현 못 함(대량 테스트 데이터용 단순화). */
public record ProductBulkRow(
        Long sellerId,
        Long categoryId,
        String name,
        int basePrice,
        String optionName,
        int additionalPrice,
        int stockQuantity
) {
}

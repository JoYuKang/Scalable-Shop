package com.kang.ecommercedataplatform.search.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.kang.ecommercedataplatform.product.dto.ProductOptionResponse;
import com.kang.ecommercedataplatform.product.dto.ProductResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class SearchProductTest {

    @Test
    @DisplayName("from()은 ProductResponse의 옵션별 재고를 합산해 totalStock으로 채운다")
    void from_sumsOptionStockIntoTotalStock() {
        ProductResponse product = new ProductResponse(
                1L, 10L, 100L, "전자기기", "키보드", 50000, "ON_SALE",
                List.of(
                        new ProductOptionResponse(1L, "블랙", 0, 3),
                        new ProductOptionResponse(2L, "화이트", 2000, 4)
                ));

        SearchProduct searchProduct = SearchProduct.from(product);

        assertEquals(1L, searchProduct.getId());
        assertEquals("키보드", searchProduct.getName());
        assertEquals(100L, searchProduct.getCategoryId());
        assertEquals("전자기기", searchProduct.getCategoryName());
        assertEquals(50000, searchProduct.getBasePrice());
        assertEquals("ON_SALE", searchProduct.getStatus());
        assertEquals(10L, searchProduct.getSellerId());
        assertEquals(7, searchProduct.getTotalStock());
    }

    @Test
    @DisplayName("from()은 옵션이 없는 상품이면 totalStock을 0으로 채운다")
    void from_noOptions_totalStockIsZero() {
        ProductResponse product = new ProductResponse(
                2L, 10L, 100L, "전자기기", "품절상품", 10000, "SOLD_OUT", List.of());

        SearchProduct searchProduct = SearchProduct.from(product);

        assertEquals(0, searchProduct.getTotalStock());
    }
}

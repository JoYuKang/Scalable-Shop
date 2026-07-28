package com.kang.ecommercedataplatform.product.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ProductOptionTest {

    @Test
    @DisplayName("재고보다 적은 수량은 정상적으로 차감된다")
    void decreaseStock_success() {
        ProductOption option = ProductOption.builder()
                .optionName("테스트옵션")
                .additionalPrice(0)
                .stockQuantity(10)
                .build();

        option.decreaseStock(3);

        assertEquals(7, option.getStockQuantity());
    }

    @Test
    @DisplayName("재고보다 많은 수량을 차감하려 하면 예외가 발생하고 재고는 그대로다")
    void decreaseStock_insufficientStock_throwsAndDoesNotChange() {
        ProductOption option = ProductOption.builder()
                .optionName("테스트옵션")
                .additionalPrice(0)
                .stockQuantity(5)
                .build();

        assertThrows(IllegalStateException.class, () -> option.decreaseStock(6));
        assertEquals(5, option.getStockQuantity());
    }

    @Test
    @DisplayName("재고를 늘리면 수량이 증가한다")
    void increaseStock_success() {
        ProductOption option = ProductOption.builder()
                .optionName("테스트옵션")
                .additionalPrice(0)
                .stockQuantity(5)
                .build();

        option.increaseStock(3);

        assertEquals(8, option.getStockQuantity());
    }

}

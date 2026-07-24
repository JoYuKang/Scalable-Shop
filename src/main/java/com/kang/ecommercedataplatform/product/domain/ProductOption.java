package com.kang.ecommercedataplatform.product.domain;

import com.kang.ecommercedataplatform.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "product_option")
public class ProductOption extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private String optionName;
    private int additionalPrice;
    private int stockQuantity;

    /** 낙관적 락 비교 실험용 (2단계 동시성 제어에서 사용) */
    @Version
    private Long version;

    @Builder
    public ProductOption(Product product, String optionName, int additionalPrice, int stockQuantity) {
        this.product = product;
        this.optionName = optionName;
        this.additionalPrice = additionalPrice;
        this.stockQuantity = stockQuantity;
    }

    public void decreaseStock(int quantity) {
        if (stockQuantity < quantity) {
            throw new IllegalStateException("재고가 부족합니다. optionId=" + id);
        }
        this.stockQuantity -= quantity;
    }

    public void increaseStock(int quantity) {
        this.stockQuantity += quantity;
    }
}
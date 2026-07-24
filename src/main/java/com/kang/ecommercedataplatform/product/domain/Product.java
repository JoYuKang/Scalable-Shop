package com.kang.ecommercedataplatform.product.domain;

import com.kang.ecommercedataplatform.global.common.BaseEntity;
import com.kang.ecommercedataplatform.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "product")
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private Member seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    private String name;

    /** 옵션이 없을 때의 기본 가격. 실제 판매가 = basePrice + ProductOption.additionalPrice */
    private int basePrice;

    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductOption> options = new ArrayList<>();

    @Builder
    public Product(Member seller, Category category, String name, int basePrice) {
        this.seller = seller;
        this.category = category;
        this.name = name;
        this.basePrice = basePrice;
        this.status = ProductStatus.ON_SALE;
    }

    public void addOption(ProductOption option) {
        options.add(option);
    }
}
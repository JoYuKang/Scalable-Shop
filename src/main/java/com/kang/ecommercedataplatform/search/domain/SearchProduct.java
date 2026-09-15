package com.kang.ecommercedataplatform.search.domain;

import com.kang.ecommercedataplatform.product.dto.ProductOptionResponse;
import com.kang.ecommercedataplatform.product.dto.ProductResponse;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * MySQL Product를 원천으로 하는 읽기 전용 색인 문서.
 * product 도메인의 엔티티/enum을 직접 쓰지 않고 값만 복사해 담는다 — 그래야 product
 * 쪽 타입이 바뀌어도 이 문서 구조는 영향을 안 받는다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(indexName = "products")
public class SearchProduct {

    @Id
    private Long id;

    @Field(type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Long)
    private Long categoryId;

    @Field(type = FieldType.Keyword)
    private String categoryName;

    @Field(type = FieldType.Integer)
    private int basePrice;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Long)
    private Long sellerId;

    /** ProductOption별 stockQuantity 합 */
    @Field(type = FieldType.Integer)
    private int totalStock;

    private SearchProduct(Long id, String name, Long categoryId, String categoryName,
                           int basePrice, String status, Long sellerId, int totalStock) {
        this.id = id;
        this.name = name;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.basePrice = basePrice;
        this.status = status;
        this.sellerId = sellerId;
        this.totalStock = totalStock;
    }

    public static SearchProduct from(ProductResponse product) {
        int totalStock = product.options().stream()
                .mapToInt(ProductOptionResponse::stockQuantity)
                .sum();

        return new SearchProduct(
                product.id(),
                product.name(),
                product.categoryId(),
                product.categoryName(),
                product.basePrice(),
                product.status(),
                product.sellerId(),
                totalStock
        );
    }
}

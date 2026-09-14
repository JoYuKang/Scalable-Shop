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
 * product 도메인의 엔티티/enum을 참조하지 않고 값만 복사해 갖는다 - 읽기 쪽이 쓰기 쪽
 * 내부 타입에 의존하면 CQRS로 저장소를 분리한 의미가 없어지기 때문.
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

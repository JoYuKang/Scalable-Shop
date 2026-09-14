package com.kang.ecommercedataplatform.search.application;

import com.kang.ecommercedataplatform.product.application.ProductService;
import com.kang.ecommercedataplatform.product.dto.ProductResponse;
import com.kang.ecommercedataplatform.search.domain.SearchProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * product(쓰기)와 search(읽기) 도메인을 조율. product 리포지토리를 직접 참조하지 않고
 * ProductService의 공개 API(getProduct/listProducts)만 거쳐 데이터를 가져온다 - 읽기 쪽이
 * 쓰기 쪽 내부 구현에 의존하면 CQRS로 저장소를 분리한 의미가 없기 때문.
 */
@Component
@RequiredArgsConstructor
public class SearchIndexFacade {

    private final ProductService productService;
    private final SearchService searchService;

    public void indexProduct(Long productId) {
        ProductResponse product = productService.getProduct(productId);
        searchService.index(SearchProduct.from(product));
    }

    /** product/stock 변경 -> Kafka 자동 색인 파이프라인이 아직 없어 이 메서드가 유일한 동기화 경로다. */
    public int reindexAll() {
        List<ProductResponse> products = productService.listProducts();
        products.forEach(product -> searchService.index(SearchProduct.from(product)));
        return products.size();
    }
}

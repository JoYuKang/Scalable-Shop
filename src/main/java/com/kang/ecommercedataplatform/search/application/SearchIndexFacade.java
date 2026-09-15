package com.kang.ecommercedataplatform.search.application;

import com.kang.ecommercedataplatform.product.application.ProductService;
import com.kang.ecommercedataplatform.product.dto.ProductResponse;
import com.kang.ecommercedataplatform.search.domain.SearchProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * product(쓰기)와 search(읽기)를 잇는 유일한 연결점. product 리포지토리를 직접 쓰지 않고
 * ProductService의 조회 API(getProduct/listProducts)만 거친다 — 그래야 product 쪽 내부
 * 구조가 바뀌어도 이 클래스만 고치면 되고, search 쪽 코드는 안 건드려도 된다.
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

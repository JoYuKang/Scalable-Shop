package com.kang.ecommercedataplatform.search.application;

import com.kang.ecommercedataplatform.product.application.ProductService;
import com.kang.ecommercedataplatform.product.dto.ProductResponse;
import com.kang.ecommercedataplatform.search.domain.SearchProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

    private static final int REINDEX_PAGE_SIZE = 1000;

    /**
     * product/stock 변경 -> Kafka 자동 색인 파이프라인이 아직 없어 이 메서드가 유일한 동기화 경로다.
     * 페이지 단위로 읽어서 ES Bulk API(SearchService.indexAll)로 배치 색인한다 — 전체를 한 번에
     * 메모리에 올리고 한 건씩 save()하면 대량 데이터(수십만~백만 건)에서 감당이 안 되기 때문.
     */
    public int reindexAll() {
        int total = 0;
        Page<ProductResponse> page;
        int pageNumber = 0;
        do {
            page = productService.listProducts(PageRequest.of(pageNumber, REINDEX_PAGE_SIZE));
            List<SearchProduct> searchProducts = page.getContent().stream()
                    .map(SearchProduct::from)
                    .toList();
            searchService.indexAll(searchProducts);
            total += searchProducts.size();
            pageNumber++;
        } while (page.hasNext());
        return total;
    }
}

package com.kang.ecommercedataplatform.search.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.kang.ecommercedataplatform.product.application.ProductService;
import com.kang.ecommercedataplatform.product.dto.ProductOptionResponse;
import com.kang.ecommercedataplatform.product.dto.ProductResponse;
import com.kang.ecommercedataplatform.search.domain.SearchProduct;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

/**
 * SearchIndexFacade가 ProductService의 공개 API로만 상품 데이터를 가져와
 * SearchProduct로 바꿔 색인하는지 확인하는 오케스트레이션 테스트.
 * 변환 로직(SearchProduct.from) 자체의 필드 매핑 세부사항은 여기서 검증하지 않는다.
 */
@ExtendWith(MockitoExtension.class)
class SearchIndexFacadeUnitTest {

    @Mock private ProductService productService;
    @Mock private SearchService searchService;

    @Test
    @DisplayName("indexProduct는 ProductService로 조회한 상품 하나를 색인한다")
    void indexProduct_indexesSingleProduct() {
        ProductResponse product = new ProductResponse(
                1L, 10L, 100L, "전자기기", "키보드", 50000, "ON_SALE",
                List.of(new ProductOptionResponse(1L, "기본", 0, 5)));
        given(productService.getProduct(1L)).willReturn(product);

        SearchIndexFacade searchIndexFacade = new SearchIndexFacade(productService, searchService);
        searchIndexFacade.indexProduct(1L);

        ArgumentCaptor<SearchProduct> captor = ArgumentCaptor.forClass(SearchProduct.class);
        verify(searchService).index(captor.capture());
        assertEquals(1L, captor.getValue().getId());
        assertEquals("키보드", captor.getValue().getName());
    }

    @Test
    @DisplayName("reindexAll은 ProductService의 전체 목록을 전부 색인하고 처리한 개수를 반환한다")
    void reindexAll_indexesEveryProductAndReturnsCount() {
        ProductResponse product1 = new ProductResponse(
                1L, 10L, 100L, "전자기기", "키보드", 50000, "ON_SALE",
                List.of(new ProductOptionResponse(1L, "기본", 0, 5)));
        ProductResponse product2 = new ProductResponse(
                2L, 10L, 100L, "전자기기", "마우스", 20000, "SOLD_OUT",
                List.of(new ProductOptionResponse(2L, "기본", 0, 0)));
        given(productService.listProducts()).willReturn(List.of(product1, product2));

        SearchIndexFacade searchIndexFacade = new SearchIndexFacade(productService, searchService);
        int count = searchIndexFacade.reindexAll();

        assertEquals(2, count);
        verify(searchService).index(argThatHasId(1L));
        verify(searchService).index(argThatHasId(2L));
    }

    private SearchProduct argThatHasId(Long id) {
        return org.mockito.ArgumentMatchers.argThat(sp -> sp.getId().equals(id));
    }
}

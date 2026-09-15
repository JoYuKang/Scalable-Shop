package com.kang.ecommercedataplatform.search.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

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
    @DisplayName("reindexAll은 한 페이지 분량이면 그 페이지만 조회해 배치 색인하고 처리 건수를 반환한다")
    void reindexAll_singlePage_indexesAllAndReturnsCount() {
        ProductResponse product1 = new ProductResponse(
                1L, 10L, 100L, "전자기기", "키보드", 50000, "ON_SALE",
                List.of(new ProductOptionResponse(1L, "기본", 0, 5)));
        ProductResponse product2 = new ProductResponse(
                2L, 10L, 100L, "전자기기", "마우스", 20000, "SOLD_OUT",
                List.of(new ProductOptionResponse(2L, "기본", 0, 0)));
        Page<ProductResponse> page = new PageImpl<>(List.of(product1, product2), PageRequest.of(0, 1000), 2);
        given(productService.listProducts(PageRequest.of(0, 1000))).willReturn(page);

        SearchIndexFacade searchIndexFacade = new SearchIndexFacade(productService, searchService);
        int count = searchIndexFacade.reindexAll();

        assertEquals(2, count);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<SearchProduct>> captor = ArgumentCaptor.forClass(List.class);
        verify(searchService).indexAll(captor.capture());
        List<SearchProduct> indexed = captor.getValue();
        assertEquals(2, indexed.size());
        assertTrue(indexed.stream().anyMatch(sp -> sp.getId().equals(1L)));
        assertTrue(indexed.stream().anyMatch(sp -> sp.getId().equals(2L)));
    }

    @Test
    @DisplayName("reindexAll은 다음 페이지가 있으면 끝까지 페이지를 넘기며 배치 색인한다")
    void reindexAll_multiplePages_indexesEveryPage() {
        ProductResponse product1 = new ProductResponse(
                1L, 10L, 100L, "전자기기", "키보드", 50000, "ON_SALE", List.of());
        ProductResponse product2 = new ProductResponse(
                2L, 10L, 100L, "전자기기", "마우스", 20000, "ON_SALE", List.of());
        // totalElements를 페이지 크기보다 크게 줘서 hasNext()가 true가 되게 함
        Page<ProductResponse> firstPage = new PageImpl<>(List.of(product1), PageRequest.of(0, 1000), 1001);
        Page<ProductResponse> secondPage = new PageImpl<>(List.of(product2), PageRequest.of(1, 1000), 1001);
        given(productService.listProducts(PageRequest.of(0, 1000))).willReturn(firstPage);
        given(productService.listProducts(PageRequest.of(1, 1000))).willReturn(secondPage);

        SearchIndexFacade searchIndexFacade = new SearchIndexFacade(productService, searchService);
        int count = searchIndexFacade.reindexAll();

        assertEquals(2, count);
        verify(searchService).indexAll(argThatContainsId(1L));
        verify(searchService).indexAll(argThatContainsId(2L));
    }

    @SuppressWarnings("unchecked")
    private List<SearchProduct> argThatContainsId(Long id) {
        return org.mockito.ArgumentMatchers.argThat(list -> list.stream().anyMatch(sp -> sp.getId().equals(id)));
    }
}

package com.kang.ecommercedataplatform.search.application;

import static org.mockito.Mockito.verify;

import com.kang.ecommercedataplatform.product.dto.ProductResponse;
import com.kang.ecommercedataplatform.search.domain.SearchProduct;
import com.kang.ecommercedataplatform.search.infrastructure.SearchProductElasticsearchRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

import java.util.List;

/**
 * search()의 CriteriaQuery 조립은 mock으로 흉내내면 실 구현과 무관하게 항상 통과하므로
 * 여기서는 index/delete가 리포지토리로 제대로 위임되는지만 확인한다. CriteriaQuery 자체는
 * SearchIndexFacadeIntegrationTest에서 실제 Elasticsearch로 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class SearchServiceUnitTest {

    @Mock private SearchProductElasticsearchRepository searchProductElasticsearchRepository;
    @Mock private ElasticsearchOperations elasticsearchOperations;

    @Test
    @DisplayName("index()는 리포지토리의 save()로 위임한다")
    void index_delegatesToRepositorySave() {
        SearchService searchService = new SearchService(searchProductElasticsearchRepository, elasticsearchOperations);
        SearchProduct searchProduct = SearchProduct.from(
                new ProductResponse(1L, 10L, 100L, "전자기기", "키보드", 50000, "ON_SALE", List.of()));

        searchService.index(searchProduct);

        verify(searchProductElasticsearchRepository).save(searchProduct);
    }

    @Test
    @DisplayName("delete()는 리포지토리의 deleteById()로 위임한다")
    void delete_delegatesToRepositoryDeleteById() {
        SearchService searchService = new SearchService(searchProductElasticsearchRepository, elasticsearchOperations);

        searchService.delete(1L);

        verify(searchProductElasticsearchRepository).deleteById(1L);
    }
}

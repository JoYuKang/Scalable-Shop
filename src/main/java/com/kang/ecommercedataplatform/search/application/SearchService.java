package com.kang.ecommercedataplatform.search.application;

import com.kang.ecommercedataplatform.search.domain.SearchProduct;
import com.kang.ecommercedataplatform.search.dto.SearchRequest;
import com.kang.ecommercedataplatform.search.dto.SearchResponse;
import com.kang.ecommercedataplatform.search.infrastructure.SearchProductElasticsearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Elasticsearch 리포지토리/오퍼레이션만 다루는 검색(읽기) 전용 서비스.
 * product 데이터를 가져와 색인하는 조율은 SearchIndexFacade가 한다.
 */
@Service
@RequiredArgsConstructor
public class SearchService {

    private static final String ON_SALE_STATUS = "ON_SALE";

    private final SearchProductElasticsearchRepository searchProductElasticsearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    /** 파생 쿼리 메서드 대신 CriteriaQuery로 조립 — 선택적 조합 필터(카테고리/상태)로 인한 파생 쿼리 감당 불가. */
    public List<SearchResponse> search(SearchRequest request) {
        Criteria criteria = new Criteria("name").matches(request.keyword())
                .and(new Criteria("status").is(ON_SALE_STATUS));

        if (request.categoryId() != null) {
            criteria = criteria.and(new Criteria("categoryId").is(request.categoryId()));
        }

        Query query = new CriteriaQuery(criteria)
                .setPageable(PageRequest.of(request.page(), request.size()));

        SearchHits<SearchProduct> hits = elasticsearchOperations.search(query, SearchProduct.class);
        return hits.stream()
                .map(hit -> SearchResponse.from(hit.getContent()))
                .toList();
    }

    public void index(SearchProduct searchProduct) {
        searchProductElasticsearchRepository.save(searchProduct);
    }

    /** saveAll()은 건별 save()와 달리 ES Bulk API로 한 번에 보낸다 — 대량 색인은 이걸 써야 함. */
    public void indexAll(List<SearchProduct> searchProducts) {
        searchProductElasticsearchRepository.saveAll(searchProducts);
    }

    public void delete(Long productId) {
        searchProductElasticsearchRepository.deleteById(productId);
    }
}

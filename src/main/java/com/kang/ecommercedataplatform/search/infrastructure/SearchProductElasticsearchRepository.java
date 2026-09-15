package com.kang.ecommercedataplatform.search.infrastructure;

import com.kang.ecommercedataplatform.search.domain.SearchProduct;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface SearchProductElasticsearchRepository extends ElasticsearchRepository<SearchProduct, Long> {
}

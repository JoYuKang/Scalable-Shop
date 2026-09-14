package com.kang.ecommercedataplatform.search.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.kang.ecommercedataplatform.member.domain.Member;
import com.kang.ecommercedataplatform.member.domain.MemberRole;
import com.kang.ecommercedataplatform.member.infrastructure.MemberJpaRepository;
import com.kang.ecommercedataplatform.product.application.ProductService;
import com.kang.ecommercedataplatform.product.domain.Category;
import com.kang.ecommercedataplatform.product.dto.ProductCreateRequest;
import com.kang.ecommercedataplatform.product.dto.ProductOptionRequest;
import com.kang.ecommercedataplatform.product.infrastructure.CategoryJpaRepository;
import com.kang.ecommercedataplatform.search.domain.SearchProduct;
import com.kang.ecommercedataplatform.search.dto.SearchRequest;
import com.kang.ecommercedataplatform.search.dto.SearchResponse;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * SearchServiceUnitTest에 남겨둔 대로, mock으로는 의미가 없는 CriteriaQuery 조립 +
 * 색인 반영을 실제 Elasticsearch로 검증한다. nori 플러그인은 여기서 검증 대상이 아니라서
 * (그건 다음 작업 항목) docker/elasticsearch 커스텀 이미지 대신 공식 베이스 이미지를 그대로 쓴다.
 */
@Testcontainers
@SpringBootTest
@ActiveProfiles("local")
class SearchIndexFacadeIntegrationTest {

    @Container
    static GenericContainer<?> elasticsearch =
            new GenericContainer<>(DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:9.4.5"))
                    .withExposedPorts(9200)
                    .withEnv("discovery.type", "single-node")
                    .withEnv("xpack.security.enabled", "false")
                    .withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m")
                    .waitingFor(Wait.forHttp("/").forStatusCode(200).withStartupTimeout(java.time.Duration.ofMinutes(2)));

    @DynamicPropertySource
    static void elasticsearchProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.elasticsearch.uris",
                () -> "http://" + elasticsearch.getHost() + ":" + elasticsearch.getMappedPort(9200));
    }

    @Autowired private MemberJpaRepository memberJpaRepository;
    @Autowired private CategoryJpaRepository categoryJpaRepository;
    @Autowired private ProductService productService;
    @Autowired private SearchIndexFacade searchIndexFacade;
    @Autowired private SearchService searchService;
    @Autowired private ElasticsearchOperations elasticsearchOperations;

    @Test
    @DisplayName("indexProduct로 색인한 상품은 이름 키워드로 검색하면 조회된다")
    void indexProduct_thenSearchByKeyword_findsProduct() {
        Long productId = createOnSaleProduct("멀티기능 블루투스 키보드");

        searchIndexFacade.indexProduct(productId);
        elasticsearchOperations.indexOps(SearchProduct.class).refresh(); // 기본 refresh_interval(1s)을 기다리지 않고 즉시 조회 가능하게 함

        List<SearchResponse> results = searchService.search(new SearchRequest("키보드", null, 0, 10));

        assertTrue(results.stream().anyMatch(r -> r.id().equals(productId)));
    }

    @Test
    @DisplayName("reindexAll은 MySQL에 있는 상품 전체를 ES에 색인한다")
    void reindexAll_indexesAllProductsFromDatabase() {
        Long productId = createOnSaleProduct("무선 마우스");

        int indexedCount = searchIndexFacade.reindexAll();
        elasticsearchOperations.indexOps(SearchProduct.class).refresh();

        assertTrue(indexedCount >= 1);
        List<SearchResponse> results = searchService.search(new SearchRequest("마우스", null, 0, 10));
        assertTrue(results.stream().anyMatch(r -> r.id().equals(productId)));
    }

    @Test
    @DisplayName("카테고리 필터를 걸면 다른 카테고리의 동일 키워드 상품은 검색되지 않는다")
    void search_withCategoryFilter_excludesOtherCategories() {
        Long targetCategoryId = categoryJpaRepository.save(
                Category.builder().name("주변기기-" + UUID.randomUUID()).build()).getId();
        Long otherCategoryId = categoryJpaRepository.save(
                Category.builder().name("사무용품-" + UUID.randomUUID()).build()).getId();
        Long sellerId = createSeller();

        Long targetProductId = createOnSaleProduct(sellerId, targetCategoryId, "게이밍 키보드 A");
        Long otherProductId = createOnSaleProduct(sellerId, otherCategoryId, "게이밍 키보드 B");

        searchIndexFacade.indexProduct(targetProductId);
        searchIndexFacade.indexProduct(otherProductId);
        elasticsearchOperations.indexOps(SearchProduct.class).refresh();

        List<SearchResponse> results = searchService.search(new SearchRequest("키보드", targetCategoryId, 0, 10));

        assertEquals(1, results.size());
        assertEquals(targetProductId, results.get(0).id());
    }

    private Long createOnSaleProduct(String name) {
        Long sellerId = createSeller();
        Long categoryId = categoryJpaRepository.save(
                Category.builder().name("카테고리-" + UUID.randomUUID()).build()).getId();
        return createOnSaleProduct(sellerId, categoryId, name);
    }

    private Long createOnSaleProduct(Long sellerId, Long categoryId, String name) {
        ProductCreateRequest request = new ProductCreateRequest(
                sellerId, categoryId, name, 10000,
                List.of(new ProductOptionRequest("기본", 0, 5)));
        return productService.createProduct(request);
    }

    private Long createSeller() {
        return memberJpaRepository.save(Member.builder()
                .loginId("seller-" + UUID.randomUUID())
                .password("pw").name("판매자").email("seller@test.com")
                .role(MemberRole.SELLER).build()).getId();
    }
}

package com.kang.ecommercedataplatform.orders.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.kang.ecommercedataplatform.member.domain.Member;
import com.kang.ecommercedataplatform.member.domain.MemberRole;
import com.kang.ecommercedataplatform.member.infrastructure.MemberJpaRepository;
import com.kang.ecommercedataplatform.order.application.OrderFacade;
import com.kang.ecommercedataplatform.order.dto.OrderCreateRequest;
import com.kang.ecommercedataplatform.order.dto.OrderItemCreateRequest;
import com.kang.ecommercedataplatform.product.domain.Category;
import com.kang.ecommercedataplatform.product.domain.Product;
import com.kang.ecommercedataplatform.product.domain.ProductOption;
import com.kang.ecommercedataplatform.product.infrastructure.CategoryJpaRepository;
import com.kang.ecommercedataplatform.product.infrastructure.ProductJpaRepository;
import com.kang.ecommercedataplatform.product.infrastructure.ProductOptionJpaRepository;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * OrderFacadeUnitTest와의 차이: 저건 RedissonClient를 목으로 바꿔서 "락 획득 성공/실패 시
 * 코드가 올바른 경로를 타는가"만 확인함. 이 테스트는 진짜 Redis로 진짜 동시 요청을 흘려보내서
 * "여러 스레드가 실제로 경쟁했을 때 재고가 안 꼬이는가"까지 확인함 - 서로 대체 관계가 아님.
 * Testcontainers가 Redis를 알아서 띄우고 정리하므로 docker-compose를 미리 켜둘 필요는 없음
 * (Docker 자체는 설치·실행 중이어야 함).
 */
@Testcontainers
@SpringBootTest
@ActiveProfiles("local")
class OrderFacadeIntegrationTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private OrderFacade orderFacade;
    @Autowired
    private MemberJpaRepository memberJpaRepository;
    @Autowired
    private CategoryJpaRepository categoryJpaRepository;
    @Autowired
    private ProductJpaRepository productJpaRepository;
    @Autowired
    private ProductOptionJpaRepository productOptionJpaRepository;

    @Test
    @DisplayName("재고 10개인 상품에 20명이 동시에 1개씩 주문하면 10명만 성공하고 재고는 0으로 남는다")
    void concurrentOrders_doNotOversell() throws InterruptedException {
        // given
        Member buyer = memberJpaRepository.save(Member.builder()
                .loginId("buyer-" + UUID.randomUUID())
                .password("pw").name("구매자").email("buyer@test.com")
                .role(MemberRole.USER).build());
        Member seller = memberJpaRepository.save(Member.builder()
                .loginId("seller-" + UUID.randomUUID())
                .password("pw").name("판매자").email("seller@test.com")
                .role(MemberRole.SELLER).build());
        Category category = categoryJpaRepository.save(
                Category.builder().name("동시성테스트카테고리").build());
        Product product = productJpaRepository.save(Product.builder()
                .seller(seller).category(category)
                .name("한정판 상품").basePrice(10000).build());

        ProductOption option = productOptionJpaRepository.save(ProductOption.builder()
                .product(product).optionName("한정 옵션").additionalPrice(0)
                .stockQuantity(10)
                .build());
        Long optionId = option.getId();

        int threadCount = 20;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    OrderCreateRequest request = new OrderCreateRequest(
                            buyer.getId(),
                            List.of(new OrderItemCreateRequest(optionId, 1)));
                    orderFacade.placeOrder(request);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        // then
        ProductOption result = productOptionJpaRepository.findById(optionId).orElseThrow();
        assertEquals(10, successCount.get(), "재고 10개 한도까지만 성공해야 함");
        assertEquals(10, failCount.get(), "나머지 10건은 재고 부족으로 실패해야 함");
        assertEquals(0, result.getStockQuantity(), "재고가 음수로 내려가면(오버셀링) 안 됨");
    }
}
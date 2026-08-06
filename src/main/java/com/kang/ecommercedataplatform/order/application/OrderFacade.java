package com.kang.ecommercedataplatform.order.application;

import com.kang.ecommercedataplatform.order.dto.OrderItemPricing;
import com.kang.ecommercedataplatform.order.dto.OrderCreateRequest;
import com.kang.ecommercedataplatform.order.dto.OrderItemCreateRequest;
import com.kang.ecommercedataplatform.product.application.ProductService;
import com.kang.ecommercedataplatform.product.domain.ProductOption;
import org.redisson.api.RedissonClient;
import org.redisson.api.RLock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class OrderFacade {

    private static final long LOCK_WAIT_SECONDS = 5;
    private static final String LOCK_KEY_PREFIX = "stock:lock:";

    private final RedissonClient redissonClient;
    private final ProductService productService;
    private final OrderService orderService;

    public Long placeOrder(OrderCreateRequest request) {
        List<Long> optionIds = request.items().stream()
                .map(OrderItemCreateRequest::productOptionId)
                .distinct()
                .sorted() // 여러 락을 잡을 때 항상 같은 순서로 잡아야 데드락이 안 생김
                .toList();

        RLock[] locks = optionIds.stream()
                .map(id -> redissonClient.getLock(LOCK_KEY_PREFIX + id))
                .toArray(RLock[]::new);
        RLock multiLock = redissonClient.getMultiLock(locks);

        try {
            boolean acquired = multiLock.tryLock(LOCK_WAIT_SECONDS, TimeUnit.SECONDS);
            if (!acquired) {
                throw new IllegalStateException("지금 재고 처리 중인 요청이 많습니다. 잠시 후 다시 시도해주세요.");
            }

            Map<Long, Integer> unitPrices = productService.getUnitPrices(optionIds);
            List<OrderItemPricing> pricings = request.items().stream()
                    .map(item -> new OrderItemPricing(
                            item.productOptionId(),
                            item.quantity(),
                            unitPrices.get(item.productOptionId())))
                    .toList();

            return orderService.createOrder(request.memberId(), pricings);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("재고 락 획득 중 인터럽트가 발생했습니다.", e);
        } finally {
            if (multiLock.isHeldByCurrentThread()) {
                multiLock.unlock();
            }
        }
    }
}

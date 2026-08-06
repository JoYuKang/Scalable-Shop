package com.kang.ecommercedataplatform.orders.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.kang.ecommercedataplatform.order.application.OrderFacade;
import com.kang.ecommercedataplatform.order.application.OrderService;
import com.kang.ecommercedataplatform.order.dto.OrderCreateRequest;
import com.kang.ecommercedataplatform.order.dto.OrderItemCreateRequest;
import com.kang.ecommercedataplatform.product.application.ProductService;
import com.kang.ecommercedataplatform.product.domain.Product;
import com.kang.ecommercedataplatform.product.domain.ProductOption;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

@ExtendWith(MockitoExtension.class)
class OrderFacadeUnitTest {

    @Mock private RedissonClient redissonClient;
    @Mock private ProductService productService;
    @Mock private OrderService orderService;
    @Mock private RLock lock;
    @Mock private RLock multiLock;

    @Test
    @DisplayName("락 획득에 성공하면 단가를 조회해 OrderService에 위임하고, 끝나면 락을 해제한다")
    void placeOrder_lockAcquired_delegatesAndUnlocks() throws InterruptedException {
        given(redissonClient.getLock(anyString())).willReturn(lock);
        given(redissonClient.getMultiLock(any(RLock[].class))).willReturn(multiLock);
        given(multiLock.tryLock(anyLong(), any(TimeUnit.class))).willReturn(true);
        given(multiLock.isHeldByCurrentThread()).willReturn(true);

        given(productService.getUnitPrices(List.of(10L))).willReturn(Map.of(10L, 12000));
        given(orderService.createOrder(eq(1L), any())).willReturn(1L);

        OrderFacade orderFacade = new OrderFacade(redissonClient, productService, orderService);
        OrderCreateRequest request = new OrderCreateRequest(1L, List.of(new OrderItemCreateRequest(10L, 1)));

        Long orderId = orderFacade.placeOrder(request);

        assertEquals(1L, orderId);
        verify(orderService).createOrder(eq(1L), any());
        verify(multiLock).unlock();
    }

    @Test
    @DisplayName("락 획득에 실패하면 단가 조회도, OrderService 위임도 하지 않고 예외를 던진다")
    void placeOrder_lockNotAcquired_throwsWithoutDelegating() throws InterruptedException {
        given(redissonClient.getLock(anyString())).willReturn(lock);
        given(redissonClient.getMultiLock(any(RLock[].class))).willReturn(multiLock);
        given(multiLock.tryLock(anyLong(), any(TimeUnit.class))).willReturn(false);

        OrderFacade orderFacade = new OrderFacade(redissonClient, productService, orderService);
        OrderCreateRequest request = new OrderCreateRequest(1L, List.of(new OrderItemCreateRequest(10L, 1)));

        assertThrows(IllegalStateException.class, () -> orderFacade.placeOrder(request));
        verify(productService, never()).getUnitPrices(any());
        verify(orderService, never()).createOrder(any(), any());
    }
}

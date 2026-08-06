package com.kang.ecommercedataplatform.orders.application;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.kang.ecommercedataplatform.member.domain.Member;
import com.kang.ecommercedataplatform.member.domain.MemberRole;
import com.kang.ecommercedataplatform.member.infrastructure.MemberJpaRepository;
import com.kang.ecommercedataplatform.order.application.OrderService;
import com.kang.ecommercedataplatform.order.domain.Order;
import com.kang.ecommercedataplatform.order.dto.OrderItemPricing;
import com.kang.ecommercedataplatform.order.infrastructure.OrderItemJpaRepository;
import com.kang.ecommercedataplatform.order.infrastructure.OrderJpaRepository;
import com.kang.ecommercedataplatform.stock.application.StockService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceUnitTest {

    @Mock private OrderJpaRepository orderJpaRepository;
    @Mock private OrderItemJpaRepository orderItemJpaRepository;
    @Mock private MemberJpaRepository memberJpaRepository;
    @Mock private StockService stockService;
    @InjectMocks private OrderService orderService;

    @Test
    @DisplayName("전달받은 pricing으로 totalAmount를 계산하고, 옵션마다 reserve를 호출한다")
    void createOrder_calculatesTotalAmountAndReservesStock() {
        Member member = Member.builder().loginId("buyer").password("pw").role(MemberRole.USER).build();
        given(memberJpaRepository.findById(1L)).willReturn(Optional.of(member));

        List<OrderItemPricing> pricings = List.of(new OrderItemPricing(10L, 2, 12000));

        orderService.createOrder(1L, pricings);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderJpaRepository).save(orderCaptor.capture());
        assertEquals(24000, orderCaptor.getValue().getTotalAmount()); // 12000 * 2
        verify(stockService).reserve(eq(10L), eq(2), any());
    }

    @Test
    @DisplayName("존재하지 않는 회원으로 주문하면 예외가 발생하고 재고에는 손대지 않는다")
    void createOrder_memberNotFound_throwsWithoutTouchingStock() {
        given(memberJpaRepository.findById(1L)).willReturn(Optional.empty());
        List<OrderItemPricing> pricings = List.of(new OrderItemPricing(10L, 1, 10000));

        assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(1L, pricings));
    }
}

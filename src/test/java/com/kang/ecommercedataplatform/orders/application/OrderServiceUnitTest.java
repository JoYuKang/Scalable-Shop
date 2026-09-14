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
import com.kang.ecommercedataplatform.order.domain.OrderItem;
import com.kang.ecommercedataplatform.order.domain.OrderStatus;
import com.kang.ecommercedataplatform.order.dto.OrderItemPricing;
import com.kang.ecommercedataplatform.order.infrastructure.OrderItemJpaRepository;
import com.kang.ecommercedataplatform.order.infrastructure.OrderJpaRepository;
import com.kang.ecommercedataplatform.stock.application.StockService;
import java.time.LocalDateTime;
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

    @Test
    @DisplayName("결제 대기 중이고 예약 시간이 남은 주문이면 총액을 반환한다")
    void getPayableAmount_pendingAndNotExpired_returnsTotalAmount() {
        Order order = pendingOrder(24000, LocalDateTime.now().plusMinutes(5));
        given(orderJpaRepository.findById(1L)).willReturn(Optional.of(order));

        int amount = orderService.getPayableAmount(1L);

        assertEquals(24000, amount);
    }

    @Test
    @DisplayName("이미 결제 완료(PAID)된 주문은 다시 결제할 수 없다")
    void getPayableAmount_alreadyPaid_throws() {
        Order order = pendingOrder(24000, LocalDateTime.now().plusMinutes(5));
        order.confirmPayment();
        given(orderJpaRepository.findById(1L)).willReturn(Optional.of(order));

        assertThrows(IllegalStateException.class, () -> orderService.getPayableAmount(1L));
    }

    @Test
    @DisplayName("예약 시간이 지난 주문은 결제할 수 없다")
    void getPayableAmount_expired_throws() {
        Order order = pendingOrder(24000, LocalDateTime.now().minusMinutes(1));
        given(orderJpaRepository.findById(1L)).willReturn(Optional.of(order));

        assertThrows(IllegalStateException.class, () -> orderService.getPayableAmount(1L));
    }

    @Test
    @DisplayName("결제 승인 콜백을 받으면 주문 상태가 PAID로 바뀐다")
    void confirmPayment_marksOrderPaid() {
        Order order = pendingOrder(24000, LocalDateTime.now().plusMinutes(5));
        given(orderJpaRepository.findById(1L)).willReturn(Optional.of(order));

        orderService.confirmPayment(1L);

        assertEquals(OrderStatus.PAID, order.getStatus());
    }

    @Test
    @DisplayName("예약 시간이 지난 주문을 취소하고, 잡아뒀던 재고를 아이템별로 전부 반환한다")
    void expireOverdueOrders_cancelsOrderAndReleasesEachItemsStock() {
        Order order = pendingOrder(12000, LocalDateTime.now().minusMinutes(1));
        OrderItem item = OrderItem.builder().productOptionId(10L).quantity(2).orderPrice(12000).build();
        order.addItem(item);
        given(orderJpaRepository.findByStatusAndReservationExpiresAtBefore(eq(OrderStatus.PENDING_PAYMENT), any()))
                .willReturn(List.of(order));

        orderService.expireOverdueOrders();

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(stockService).release(eq(10L), eq(2), any());
    }

    private static Order pendingOrder(int totalAmount, LocalDateTime reservationExpiresAt) {
        Member member = Member.builder().loginId("buyer").password("pw").role(MemberRole.USER).build();
        return Order.builder()
                .member(member)
                .totalAmount(totalAmount)
                .reservationExpiresAt(reservationExpiresAt)
                .build();
    }
}

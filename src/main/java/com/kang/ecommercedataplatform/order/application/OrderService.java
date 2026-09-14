package com.kang.ecommercedataplatform.order.application;


import com.kang.ecommercedataplatform.member.domain.Member;
import com.kang.ecommercedataplatform.member.infrastructure.MemberJpaRepository;
import com.kang.ecommercedataplatform.order.domain.Order;
import com.kang.ecommercedataplatform.order.domain.OrderItem;
import com.kang.ecommercedataplatform.order.domain.OrderStatus;
import com.kang.ecommercedataplatform.order.dto.OrderItemPricing;
import com.kang.ecommercedataplatform.order.dto.OrderResponse;
import com.kang.ecommercedataplatform.order.infrastructure.OrderItemJpaRepository;
import com.kang.ecommercedataplatform.order.infrastructure.OrderJpaRepository;
import com.kang.ecommercedataplatform.stock.application.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional( readOnly = true)
public class OrderService {
    private static final long RESERVATION_MINUTES = 10;

    private final OrderJpaRepository orderJpaRepository;
    private final OrderItemJpaRepository orderItemJpaRepository;
    private final MemberJpaRepository memberJpaRepository;
    private final StockService stockService;

    /**
     * OrderFacade가 관련 상품 옵션에 대한 Redis 락을 잡고, 가격까지 계산한 뒤에만 호출된다는 전제로 동작함.
     */
    @Transactional
    public Long createOrder(Long memberId, List<OrderItemPricing> pricings) {
        Member member = memberJpaRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다. id=" + memberId));

        int totalAmount = pricings.stream()
                .mapToInt(p -> p.unitPrice() * p.quantity())
                .sum();

        Order order = Order.builder()
                .member(member)
                .totalAmount(totalAmount)
                .reservationExpiresAt(LocalDateTime.now().plusMinutes(RESERVATION_MINUTES))
                .build();
        orderJpaRepository.save(order);

        for (OrderItemPricing pricing : pricings) {
            OrderItem orderItem = OrderItem.builder()
                    .productOptionId(pricing.productOptionId())
                    .quantity(pricing.quantity())
                    .orderPrice(pricing.unitPrice() * pricing.quantity())
                    .build();
            order.addItem(orderItem);
            orderItemJpaRepository.save(orderItem); // StockLog.referenceId로 쓸 id를 바로 확보

            stockService.reserve(pricing.productOptionId(), pricing.quantity(), orderItem.getId());
        }

        return order.getId();
    }

    public OrderResponse getOrder(Long id) {
        return OrderResponse.from(getOrderOrThrow(id));
    }

    public List<OrderResponse> listOrders() {
        return orderJpaRepository.findAll().stream()
                .map(OrderResponse::from)
                .toList();
    }

    /**
     * PaymentFacade가 결제를 시도하기 전에 호출함. 주문 엔티티를 그대로 넘기지 않고
     * 트랜잭션 안에서 검증까지 끝낸 뒤 금액(int)만 반환하는 이유는 ProductService.getUnitPrices와
     * 같음 - 엔티티를 트랜잭션 밖으로 들고 나가지 않기 위해서. 쓰기는 없어서 클래스 기본값(readOnly)을 그대로 씀.
     */
    public int getPayableAmount(Long orderId) {
        Order order = getOrderOrThrow(orderId);
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("결제할 수 없는 주문 상태입니다. status=" + order.getStatus());
        }
        if (order.isExpired(LocalDateTime.now())) {
            throw new IllegalStateException("주문 예약이 만료되었습니다. orderId=" + orderId);
        }
        return order.getTotalAmount();
    }

    /** PG 승인 성공 콜백에서 PaymentFacade가 호출함. */
    @Transactional
    public void confirmPayment(Long orderId) {
        getOrderOrThrow(orderId).confirmPayment();
    }

    /**
     * OrderExpirationScheduler가 주기적으로 호출함.
     * 결제 안 된 채 예약 시간이 지난 주문을 취소하고, 잡아뒀던 재고를 전부 돌려준다.
     */
    @Transactional
    public void expireOverdueOrders() {
        LocalDateTime now = LocalDateTime.now();
        List<Order> overdue = orderJpaRepository.findByStatusAndReservationExpiresAtBefore(
                OrderStatus.PENDING_PAYMENT, now);

        for (Order order : overdue) {
            order.cancel();
            for (OrderItem item : order.getItems()) {
                stockService.release(item.getProductOptionId(), item.getQuantity(), item.getId());
            }
        }
    }

    private Order getOrderOrThrow(Long id) {
        return orderJpaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다. id=" + id));
    }
}

package com.kang.ecommercedataplatform.order.application;


import com.kang.ecommercedataplatform.member.domain.Member;
import com.kang.ecommercedataplatform.member.infrastructure.MemberJpaRepository;
import com.kang.ecommercedataplatform.order.domain.Order;
import com.kang.ecommercedataplatform.order.domain.OrderItem;
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
        Order order = orderJpaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다. id=" + id));
        return OrderResponse.from(order);
    }

    public List<OrderResponse> listOrders() {
        return orderJpaRepository.findAll().stream()
                .map(OrderResponse::from)
                .toList();
    }
}

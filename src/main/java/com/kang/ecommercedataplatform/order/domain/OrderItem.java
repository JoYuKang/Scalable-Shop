package com.kang.ecommercedataplatform.order.domain;


import com.kang.ecommercedataplatform.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "order_item")
public class OrderItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    /** product 모듈 엔티티를 직접 참조하지 않고 id만 들고 있음 (도메인 간 결합도를 낮추기 위함) */
    private Long productOptionId;
    private int quantity;

    /** 주문 시점 단가 스냅샷 (이후 상품 가격이 바뀌어도 영향 안 받게) */
    private int orderPrice;

    @Builder
    public OrderItem(Long productOptionId, int quantity, int orderPrice) {
        this.productOptionId = productOptionId;
        this.quantity = quantity;
        this.orderPrice = orderPrice;
    }

    void assignOrder(Order order) {
        this.order = order;
    }
}

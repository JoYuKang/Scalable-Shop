package com.kang.ecommercedataplatform.order.domain;

import com.kang.ecommercedataplatform.global.common.BaseEntity;
import com.kang.ecommercedataplatform.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "orders")
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private int totalAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    /** 시각이 지나도 결제가 안 끝나면 스케줄러가 CANCELLED 처리 */
    private LocalDateTime reservationExpiresAt;

    private LocalDateTime orderedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Builder
    public Order(Member member, int totalAmount, LocalDateTime reservationExpiresAt) {
        this.member = member;
        this.totalAmount = totalAmount;
        this.status = OrderStatus.PENDING_PAYMENT;
        this.reservationExpiresAt = reservationExpiresAt;
        this.orderedAt = LocalDateTime.now();
    }

    public void addItem(OrderItem item) {
        items.add(item);
        item.assignOrder(this);
    }

    /** 결제 성공 콜백에서 호출 */
    public void confirmPayment() {
        this.status = OrderStatus.PAID;
    }

    /** 결제 실패 또는 TTL 만료 스케줄러에서 호출 */
    public void cancel() {
        this.status = OrderStatus.CANCELLED;
    }

    public boolean isExpired(LocalDateTime now) {
        return status == OrderStatus.PENDING_PAYMENT && now.isAfter(reservationExpiresAt);
    }
}
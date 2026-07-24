package com.kang.ecommercedataplatform.stock.domain;


import com.kang.ecommercedataplatform.global.common.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "stock_log")
public class StockLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** product 모듈 엔티티를 직접 참조하지 않고 id만 참조 */
    private Long productOptionId;

    @Enumerated(EnumType.STRING)
    private StockLogType type;

    /** 증감 수량.
     * RESERVE/CONFIRM은 음수
     * RELEASE/RESTOCK은 양수 */
    private int quantity;

    /** 이 로그 시점의 stockQuantity 스냅샷 (감사·디버깅용) */
    private int remainStock;

    /** order_item.id 참조. */
    private Long referenceId;

    @Builder
    public StockLog(Long productOptionId, StockLogType type, int quantity, int remainStock, Long referenceId) {
        this.productOptionId = productOptionId;
        this.type = type;
        this.quantity = quantity;
        this.remainStock = remainStock;
        this.referenceId = referenceId;
    }
}
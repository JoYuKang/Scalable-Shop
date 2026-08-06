package com.kang.ecommercedataplatform.stock.application;

import com.kang.ecommercedataplatform.product.domain.ProductOption;
import com.kang.ecommercedataplatform.product.infrastructure.ProductOptionJpaRepository;
import com.kang.ecommercedataplatform.stock.domain.StockLog;
import com.kang.ecommercedataplatform.stock.domain.StockLogType;
import com.kang.ecommercedataplatform.stock.infrastructure.StockLogJpaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class StockService {

    private final ProductOptionJpaRepository productOptionJpaRepository;
    private final StockLogJpaRepository stockLogJpaRepository;

    public void reserve(Long productOptionId, int quantity, Long referenceId) {
        ProductOption option = getOption(productOptionId);
        option.decreaseStock(quantity);
        stockLogJpaRepository.save(StockLog.builder()
                .productOptionId(productOptionId)
                .type(StockLogType.RESERVE)
                .quantity(-quantity)
                .remainStock(option.getStockQuantity())
                .referenceId(referenceId)
                .build());
    }

    public void release(Long productOptionId, int quantity, Long referenceId) {
        ProductOption option = getOption(productOptionId);
        option.increaseStock(quantity);
        stockLogJpaRepository.save(StockLog.builder()
                .productOptionId(productOptionId)
                .type(StockLogType.RELEASE)
                .quantity(quantity)
                .remainStock(option.getStockQuantity())
                .referenceId(referenceId)
                .build());
    }

    private ProductOption getOption(Long productOptionId) {
        return productOptionJpaRepository.findById(productOptionId)
                .orElseThrow(() -> new IllegalArgumentException("상품 옵션을 찾을 수 없습니다. id=" + productOptionId));
    }
}

package com.kang.ecommercedataplatform.stock.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.kang.ecommercedataplatform.product.domain.ProductOption;
import com.kang.ecommercedataplatform.product.infrastructure.ProductOptionJpaRepository;
import com.kang.ecommercedataplatform.stock.domain.StockLog;
import com.kang.ecommercedataplatform.stock.domain.StockLogType;
import com.kang.ecommercedataplatform.stock.infrastructure.StockLogJpaRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StockUnitTest {

    @Mock
    private ProductOptionJpaRepository productOptionJpaRepository;
    @Mock
    private StockLogJpaRepository stockLogJpaRepository;
    @InjectMocks
    private StockService stockService;

    @Test
    @DisplayName("reserve 호출 시 재고를 줄이고 RESERVE 타입으로 로그를 남긴다")
    void reserve_decreasesStockAndSavesLog() {
        ProductOption option = ProductOption.builder()
                .optionName("옵션").additionalPrice(0).stockQuantity(10).build();
        given(productOptionJpaRepository.findById(1L)).willReturn(Optional.of(option));

        stockService.reserve(1L, 3, 100L);

        assertEquals(7, option.getStockQuantity());
        ArgumentCaptor<StockLog> captor = ArgumentCaptor.forClass(StockLog.class);
        verify(stockLogJpaRepository).save(captor.capture());
        assertEquals(StockLogType.RESERVE, captor.getValue().getType());
        assertEquals(-3, captor.getValue().getQuantity());
        assertEquals(7, captor.getValue().getRemainStock());
    }

    @Test
    @DisplayName("옵션을 찾을 수 없으면 예외가 발생한다")
    void reserve_optionNotFound_throws() {
        given(productOptionJpaRepository.findById(1L)).willReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> stockService.reserve(1L, 3, 100L));
    }

    @Test
    @DisplayName("release 호출 시 재고를 복원하고 RELEASE 타입으로 로그를 남긴다")
    void release_restoresStockAndSavesLog() {
        ProductOption option = ProductOption.builder()
                .optionName("옵션").additionalPrice(0).stockQuantity(5).build();
        given(productOptionJpaRepository.findById(1L)).willReturn(Optional.of(option));

        stockService.release(1L, 3, 100L);

        assertEquals(8, option.getStockQuantity());
        ArgumentCaptor<StockLog> captor = ArgumentCaptor.forClass(StockLog.class);
        verify(stockLogJpaRepository).save(captor.capture());
        assertEquals(StockLogType.RELEASE, captor.getValue().getType());
        assertEquals(3, captor.getValue().getQuantity());
    }
}
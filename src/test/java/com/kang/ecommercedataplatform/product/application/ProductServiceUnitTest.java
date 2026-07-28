package com.kang.ecommercedataplatform.product.application;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import com.kang.ecommercedataplatform.member.domain.Member;
import com.kang.ecommercedataplatform.member.domain.MemberRole;
import com.kang.ecommercedataplatform.member.infrastructure.MemberJpaRepository;
import com.kang.ecommercedataplatform.product.dto.ProductCreateRequest;
import com.kang.ecommercedataplatform.product.dto.ProductOptionRequest;
import com.kang.ecommercedataplatform.product.infrastructure.CategoryJpaRepository;
import com.kang.ecommercedataplatform.product.infrastructure.ProductJpaRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceUnitTest {

    @Mock private ProductJpaRepository productJpaRepository;
    @Mock private CategoryJpaRepository categoryJpaRepository;
    @Mock private MemberJpaRepository memberJpaRepository;
    @InjectMocks private ProductService productService;

    @Test
    @DisplayName("존재하지 않는 판매자로 등록하려 하면 예외가 발생한다")
    void createProduct_sellerNotFound_throws() {
        given(memberJpaRepository.findById(1L)).willReturn(Optional.empty());
        ProductCreateRequest request = new ProductCreateRequest(1L, 1L, "상품", 10000,
                List.of(new ProductOptionRequest("옵션", 0, 10)));

        assertThrows(IllegalArgumentException.class, () -> productService.createProduct(request));
    }

    @Test
    @DisplayName("존재하지 않는 카테고리로 등록하려 하면 예외가 발생한다")
    void createProduct_categoryNotFound_throws() {
        Member seller = Member.builder().loginId("seller").password("pw").role(MemberRole.SELLER).build();
        given(memberJpaRepository.findById(1L)).willReturn(Optional.of(seller));
        given(categoryJpaRepository.findById(1L)).willReturn(Optional.empty());
        ProductCreateRequest request = new ProductCreateRequest(1L, 1L, "상품", 10000,
                List.of(new ProductOptionRequest("옵션", 0, 10)));

        assertThrows(IllegalArgumentException.class, () -> productService.createProduct(request));
    }
}

package com.kang.ecommercedataplatform.product.application;

import com.kang.ecommercedataplatform.order.infrastructure.MemberJpaRepository;
import com.kang.ecommercedataplatform.product.infrastructure.ProductJpaRepository;
import com.kang.ecommercedataplatform.member.domain.Member;
import com.kang.ecommercedataplatform.product.domain.Category;
import com.kang.ecommercedataplatform.product.domain.Product;
import com.kang.ecommercedataplatform.product.domain.ProductOption;
import com.kang.ecommercedataplatform.product.dto.ProductCreateRequest;
import com.kang.ecommercedataplatform.product.dto.ProductOptionRequest;
import com.kang.ecommercedataplatform.product.dto.ProductResponse;
import com.kang.ecommercedataplatform.product.infrastructure.CategoryJpaRepository;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductJpaRepository productJpaRepository;
    private final CategoryJpaRepository categoryJpaRepository;
    private final MemberJpaRepository memberJpaRepository;

    @Transactional
    public Long createProduct(ProductCreateRequest request) {
        Member seller = memberJpaRepository.findById(request.sellerId())
                .orElseThrow(() -> new IllegalArgumentException("판매자를 찾을 수 없습니다. id=" + request.sellerId()));
        Category category = categoryJpaRepository.findById(request.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다. id=" + request.categoryId()));

        Product product = Product.builder()
                .seller(seller)
                .category(category)
                .name(request.name())
                .basePrice(request.basePrice())
                .build();

        for (ProductOptionRequest optionRequest : request.options()) {
            ProductOption option = ProductOption.builder()
                    .product(product)
                    .optionName(optionRequest.optionName())
                    .additionalPrice(optionRequest.additionalPrice())
                    .stockQuantity(optionRequest.stockQuantity())
                    .build();
            product.addOption(option);
        }

        return productJpaRepository.save(product).getId();
    }

    public ProductResponse getProduct(Long id) {
        Product product = productJpaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다. id=" + id));
        return ProductResponse.from(product);
    }

    public List<ProductResponse> listProducts() {
        return productJpaRepository.findAll().stream()
                .map(ProductResponse::from)
                .toList();
    }
}

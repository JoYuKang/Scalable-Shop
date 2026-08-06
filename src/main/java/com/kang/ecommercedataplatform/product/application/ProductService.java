package com.kang.ecommercedataplatform.product.application;

import com.kang.ecommercedataplatform.member.infrastructure.MemberJpaRepository;
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
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.kang.ecommercedataplatform.product.infrastructure.ProductOptionJpaRepository;
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
    private final ProductOptionJpaRepository productOptionJpaRepository;

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

    /**
     * OrderFacade가 주문 생성 전에 단가를 미리 계산하려고 호출함.
     * ProductOption 엔티티를 그대로 반환하지 않고 여기서 가격 계산까지 끝내는 이유:
     * ProductOption.product가 LAZY라서, 트랜잭션(=이 메서드) 밖으로 엔티티를 들고 나가면
     * option.getProduct() 호출 시점에 세션이 이미 닫혀 LazyInitializationException이 남.
     * 요청한 옵션 중 하나라도 존재하지 않으면 그 자리에서 막음.
     */
    public Map<Long, Integer> getUnitPrices(List<Long> optionIds) {
        List<ProductOption> options = productOptionJpaRepository.findAllById(optionIds);
        if (options.size() != optionIds.size()) {
            throw new IllegalArgumentException("존재하지 않는 상품 옵션이 포함되어 있습니다.");
        }
        return options.stream().collect(Collectors.toMap(
                ProductOption::getId,
                option -> option.getProduct().getBasePrice() + option.getAdditionalPrice()));
    }
}

package com.kang.ecommercedataplatform.product.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.kang.ecommercedataplatform.member.domain.Member;
import com.kang.ecommercedataplatform.member.domain.MemberRole;
import com.kang.ecommercedataplatform.member.infrastructure.MemberJpaRepository;
import com.kang.ecommercedataplatform.product.domain.Category;
import com.kang.ecommercedataplatform.product.domain.Product;
import com.kang.ecommercedataplatform.product.domain.ProductOption;
import com.kang.ecommercedataplatform.product.dto.ProductBulkUploadResponse;
import com.kang.ecommercedataplatform.product.infrastructure.CategoryJpaRepository;
import com.kang.ecommercedataplatform.product.infrastructure.ProductJpaRepository;
import com.kang.ecommercedataplatform.product.infrastructure.ProductOptionJpaRepository;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

/**
 * ProductBulkUploadService는 JPA가 아니라 원본 SQL(다중 VALUES INSERT + auto_increment
 * 연속 할당 가정)로 동작해서, mock으로는 "실제로 product-option이 올바른 product_id로
 * 연결되는가"를 검증할 수 없다. 그래서 실제 DB(H2, MySQL 호환 모드)에 대고 검증한다.
 */
@SpringBootTest
@ActiveProfiles("local")
class ProductBulkUploadServiceIntegrationTest {

    @Autowired private ProductBulkUploadService productBulkUploadService;
    @Autowired private MemberJpaRepository memberJpaRepository;
    @Autowired private CategoryJpaRepository categoryJpaRepository;
    @Autowired private ProductJpaRepository productJpaRepository;
    @Autowired private ProductOptionJpaRepository productOptionJpaRepository;

    @Test
    @DisplayName("CSV로 업로드한 상품 각각이 자기 옵션과 올바르게 연결된다")
    void uploadCsv_linksEachProductToItsOwnOption() throws IOException {
        Long sellerId = createSeller();
        Long categoryId = categoryJpaRepository.save(Category.builder().name("카테고리-" + UUID.randomUUID()).build()).getId();

        String csv = "sellerId,categoryId,name,basePrice,optionName,additionalPrice,stockQuantity\n"
                + sellerId + "," + categoryId + ",벌크키보드,50000,기본,0,10\n"
                + sellerId + "," + categoryId + ",벌크마우스,20000,기본,0,5\n"
                + sellerId + "," + categoryId + ",벌크모니터,300000,기본,0,3\n";
        MockMultipartFile file = new MockMultipartFile(
                "file", "products.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

        ProductBulkUploadResponse response = productBulkUploadService.upload(file);

        assertEquals(3, response.insertedCount());

        List<Product> saved = productJpaRepository.findAll().stream()
                .filter(p -> p.getCategory().getId().equals(categoryId))
                .toList();
        assertEquals(3, saved.size());

        for (Product product : saved) {
            List<ProductOption> options = productOptionJpaRepository.findAll().stream()
                    .filter(o -> o.getProduct().getId().equals(product.getId()))
                    .toList();
            assertEquals(1, options.size(), "product " + product.getId() + "는 옵션이 정확히 1개여야 함");
        }

        assertTrue(saved.stream().anyMatch(p -> p.getName().equals("벌크키보드")));
        assertTrue(saved.stream().anyMatch(p -> p.getName().equals("벌크마우스")));
        assertTrue(saved.stream().anyMatch(p -> p.getName().equals("벌크모니터")));
    }

    private Long createSeller() {
        return memberJpaRepository.save(Member.builder()
                .loginId("bulk-seller-" + UUID.randomUUID())
                .password("pw").name("판매자").email("bulk-seller@test.com")
                .role(MemberRole.SELLER).build()).getId();
    }
}

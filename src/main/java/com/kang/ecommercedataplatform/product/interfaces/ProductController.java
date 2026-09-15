package com.kang.ecommercedataplatform.product.interfaces;

import com.kang.ecommercedataplatform.product.application.ProductBulkUploadService;
import com.kang.ecommercedataplatform.product.application.ProductService;
import com.kang.ecommercedataplatform.product.dto.ProductBulkUploadResponse;
import com.kang.ecommercedataplatform.product.dto.ProductCreateRequest;
import com.kang.ecommercedataplatform.product.dto.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductBulkUploadService productBulkUploadService;

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@RequestBody ProductCreateRequest request) {
        Long productId = productService.createProduct(request);
        return ResponseEntity.created(URI.create("/api/products/" + productId))
                .body(productService.getProduct(productId));
    }

    /** CSV/Excel(.xlsx)로 상품을 대량 등록 — 대량 테스트 데이터 생성, 초기 카탈로그 이관 등 관리용. */
    @PostMapping(value = "/bulk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductBulkUploadResponse> bulkUpload(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(productBulkUploadService.upload(file));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProduct(id));
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> listProducts() {
        return ResponseEntity.ok(productService.listProducts());
    }
}

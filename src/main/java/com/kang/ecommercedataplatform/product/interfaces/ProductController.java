package com.kang.ecommercedataplatform.product.interfaces;

import com.kang.ecommercedataplatform.product.application.ProductService;
import com.kang.ecommercedataplatform.product.dto.ProductCreateRequest;
import com.kang.ecommercedataplatform.product.dto.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@RequestBody ProductCreateRequest request) {
        Long productId = productService.createProduct(request);
        return ResponseEntity.created(URI.create("/api/products/" + productId))
                .body(productService.getProduct(productId));
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

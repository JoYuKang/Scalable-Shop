package com.kang.ecommercedataplatform.order.interfaces;

import com.kang.ecommercedataplatform.order.application.OrderFacade;
import com.kang.ecommercedataplatform.order.application.OrderService;
import com.kang.ecommercedataplatform.order.dto.OrderCreateRequest;
import com.kang.ecommercedataplatform.order.dto.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderFacade orderFacade;
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Void> createOrder(@RequestBody OrderCreateRequest request) {
        Long orderId = orderFacade.placeOrder(request);
        return ResponseEntity.created(URI.create("/api/orders/" + orderId)).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrder(id));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> listOrders() {
        return ResponseEntity.ok(orderService.listOrders());
    }
}

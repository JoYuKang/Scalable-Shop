package com.kang.ecommercedataplatform.global.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("IllegalArgumentException(존재하지 않음)은 404로 변환된다")
    void handleNotFound_convertsTo404() {
        ResponseEntity<?> response = handler.handleNotFound(new IllegalArgumentException("상품을 찾을 수 없습니다. id=999"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("상품을 찾을 수 없습니다. id=999", ((java.util.Map<?, ?>) response.getBody()).get("message"));
    }

    @Test
    @DisplayName("IllegalStateException(잘못된 상태 전이)은 409로 변환된다")
    void handleInvalidState_convertsTo409() {
        ResponseEntity<?> response = handler.handleInvalidState(new IllegalStateException("이미 결제된 주문입니다."));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("이미 결제된 주문입니다.", ((java.util.Map<?, ?>) response.getBody()).get("message"));
    }
}

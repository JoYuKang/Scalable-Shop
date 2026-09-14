package com.kang.ecommercedataplatform.payment.application;

/**
 * 실제 PG사 연동을 추상화한 경계. 지금은 MockPgClient만 존재하지만,
 * PaymentFacade가 구체 구현이 아니라 이 인터페이스에만 의존하게 해서
 * 나중에 실제 PG SDK로 바꿔도 결제 흐름(PaymentFacade) 코드는 안 건드리게 함.
 */
public interface PgClient {
    PgApproveResult approve(Long orderId, int amount, String method);
}

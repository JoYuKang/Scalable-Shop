package com.kang.ecommercedataplatform.payment.application;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 실제 PG 연동 대신 결제 승인을 흉내내는 Mock 구현체.
 * method 값이 FORCE_FAIL_METHOD와 같으면 결정론적으로 실패를 재현한다 (데모/테스트용 훅).
 * 랜덤 실패율을 두지 않은 이유: 실패 케이스를 재현 가능하게 만들어야 재시도 흐름을
 * 신뢰성 있게 테스트/시연할 수 있기 때문 (플래키한 테스트는 오히려 신뢰를 깎음).
 */
@Component
public class MockPgClient implements PgClient {

    public static final String FORCE_FAIL_METHOD = "FAIL_TEST";

    @Override
    public PgApproveResult approve(Long orderId, int amount, String method) {
        if (FORCE_FAIL_METHOD.equals(method)) {
            return PgApproveResult.failure("MOCK_PG_DECLINED");
        }
        return PgApproveResult.success("MOCK-" + UUID.randomUUID());
    }
}

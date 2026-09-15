package com.kang.ecommercedataplatform.payment.application;

public record PgApproveResult(boolean success, String pgTid, String failReason) {

    public static PgApproveResult success(String pgTid) {
        return new PgApproveResult(true, pgTid, null);
    }

    public static PgApproveResult failure(String failReason) {
        return new PgApproveResult(false, null, failReason);
    }
}

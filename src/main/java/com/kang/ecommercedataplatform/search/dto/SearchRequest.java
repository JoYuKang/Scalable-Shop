package com.kang.ecommercedataplatform.search.dto;

public record SearchRequest(
        String keyword,
        Long categoryId,
        int page,
        int size
) {
    private static final int DEFAULT_SIZE = 20;

    public SearchRequest {
        if (page < 0) {
            page = 0;
        }
        if (size <= 0) {
            size = DEFAULT_SIZE;
        }
    }
}

package com.kang.ecommercedataplatform.search.interfaces;

import com.kang.ecommercedataplatform.search.application.SearchIndexFacade;
import com.kang.ecommercedataplatform.search.application.SearchService;
import com.kang.ecommercedataplatform.search.dto.SearchRequest;
import com.kang.ecommercedataplatform.search.dto.SearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;
    private final SearchIndexFacade searchIndexFacade;

    @GetMapping("/products")
    public ResponseEntity<List<SearchResponse>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        SearchRequest request = new SearchRequest(keyword, categoryId, page, size);
        return ResponseEntity.ok(searchService.search(request));
    }

    /** MySQL 전체를 재조회해 ES를 덮어쓰는 임시 관리용 엔드포인트. 이벤트 기반 갱신이 붙기 전까지의 동기화 수단. */
    @PostMapping("/reindex")
    public ResponseEntity<Void> reindexAll() {
        searchIndexFacade.reindexAll();
        return ResponseEntity.ok().build();
    }
}

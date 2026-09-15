package com.kang.ecommercedataplatform.product.application;

import com.kang.ecommercedataplatform.product.dto.ProductBulkRow;
import com.kang.ecommercedataplatform.product.dto.ProductBulkUploadResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * product 한 건씩 JPA save()로 넣으면 100만 건 규모에선 감당이 안 돼서, CSV/Excel을
 * 파싱해 JdbcTemplate 다중 VALUES INSERT로 직접 적재한다 — ProductService(JPA 경로)를
 * 거치지 않는 게 이 클래스만의 의도적 예외.
 */
@Service
@RequiredArgsConstructor
public class ProductBulkUploadService {

    private static final int CHUNK_SIZE = 1000;
    private static final String DEFAULT_STATUS = "ON_SALE";

    private final JdbcTemplate jdbcTemplate;

    public ProductBulkUploadResponse upload(MultipartFile file) throws IOException {
        long start = System.currentTimeMillis();
        String filename = file.getOriginalFilename();
        int inserted = (filename != null && filename.toLowerCase().endsWith(".xlsx"))
                ? uploadExcel(file)
                : uploadCsv(file);
        return new ProductBulkUploadResponse(inserted, System.currentTimeMillis() - start);
    }

    /** 행을 다 읽어 리스트로 쌓지 않고 CHUNK_SIZE마다 바로 적재 — 100만 행을 한꺼번에 메모리에 올리지 않기 위함. */
    private int uploadCsv(MultipartFile file) throws IOException {
        int total = 0;
        List<ProductBulkRow> chunk = new ArrayList<>(CHUNK_SIZE);
        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader().setSkipHeaderRecord(true).build().parse(reader)) {
            for (CSVRecord record : parser) {
                chunk.add(toRow(record));
                if (chunk.size() >= CHUNK_SIZE) {
                    total += insertChunk(chunk);
                    chunk.clear();
                }
            }
        }
        if (!chunk.isEmpty()) {
            total += insertChunk(chunk);
        }
        return total;
    }

    /** XSSFWorkbook은 파일 전체를 메모리에 올리는 구현이라 수만 건 규모까진 괜찮지만, 100만 건급 테스트는 CSV로 한다. */
    private int uploadExcel(MultipartFile file) throws IOException {
        int total = 0;
        List<ProductBulkRow> chunk = new ArrayList<>(CHUNK_SIZE);
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0 || row.getCell(0) == null) {
                    continue;
                }
                chunk.add(toRow(row));
                if (chunk.size() >= CHUNK_SIZE) {
                    total += insertChunk(chunk);
                    chunk.clear();
                }
            }
        }
        if (!chunk.isEmpty()) {
            total += insertChunk(chunk);
        }
        return total;
    }

    private ProductBulkRow toRow(CSVRecord r) {
        return new ProductBulkRow(
                Long.parseLong(r.get("sellerId")),
                Long.parseLong(r.get("categoryId")),
                r.get("name"),
                Integer.parseInt(r.get("basePrice")),
                r.get("optionName"),
                Integer.parseInt(r.get("additionalPrice")),
                Integer.parseInt(r.get("stockQuantity"))
        );
    }

    private ProductBulkRow toRow(Row row) {
        return new ProductBulkRow(
                (long) row.getCell(0).getNumericCellValue(),
                (long) row.getCell(1).getNumericCellValue(),
                row.getCell(2).getStringCellValue(),
                (int) row.getCell(3).getNumericCellValue(),
                row.getCell(4).getStringCellValue(),
                (int) row.getCell(5).getNumericCellValue(),
                (int) row.getCell(6).getNumericCellValue()
        );
    }

    /**
     * product/product_option을 각각 다중 VALUES INSERT 한 번으로 적재한다.
     * product_id는 개별 조회 없이 "같은 커넥션의 단일 INSERT 안에서 auto_increment가
     * 연속 할당된다"는 MySQL/InnoDB 보장에 기대어 첫 생성 id + offset으로 계산한다 —
     * 이 배치가 product 테이블의 유일한 writer일 때만 안전하다 (동시에 실사용자가
     * 상품을 등록하면 이 가정이 깨짐, 그래서 이 기능은 운영 트래픽과 같이 돌리면 안 됨).
     */
    private int insertChunk(List<ProductBulkRow> chunk) {
        long firstProductId = insertProducts(chunk);
        insertOptions(chunk, firstProductId);
        return chunk.size();
    }

    private long insertProducts(List<ProductBulkRow> chunk) {
        StringBuilder sql = new StringBuilder(
                "INSERT INTO product (seller_id, category_id, name, base_price, status, created_at, updated_at) VALUES ");
        List<Object> params = new ArrayList<>(chunk.size() * 5);
        for (int i = 0; i < chunk.size(); i++) {
            if (i > 0) {
                sql.append(',');
            }
            sql.append("(?,?,?,?,?,NOW(),NOW())");
            ProductBulkRow row = chunk.get(i);
            params.add(row.sellerId());
            params.add(row.categoryId());
            params.add(row.name());
            params.add(row.basePrice());
            params.add(DEFAULT_STATUS);
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            return ps;
        }, keyHolder);

        return ((Number) keyHolder.getKeyList().get(0).values().iterator().next()).longValue();
    }

    private void insertOptions(List<ProductBulkRow> chunk, long firstProductId) {
        StringBuilder sql = new StringBuilder(
                "INSERT INTO product_option (product_id, option_name, additional_price, stock_quantity, version, created_at, updated_at) VALUES ");
        List<Object> params = new ArrayList<>(chunk.size() * 4);
        for (int i = 0; i < chunk.size(); i++) {
            if (i > 0) {
                sql.append(',');
            }
            sql.append("(?,?,?,?,0,NOW(),NOW())");
            ProductBulkRow row = chunk.get(i);
            params.add(firstProductId + i);
            params.add(row.optionName());
            params.add(row.additionalPrice());
            params.add(row.stockQuantity());
        }
        jdbcTemplate.update(sql.toString(), params.toArray());
    }
}

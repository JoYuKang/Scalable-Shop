-- mysql 프로파일(API 호출 테스트용) 전용 기본 데이터. local(H2, 테스트용) 프로파일은
-- application.yml에서 spring.sql.init.mode=never로 꺼서 여기 안 섞이게 했다.
-- INSERT IGNORE + 고정 ID라 재기동해도 중복 없이 같은 ID로 호출할 수 있다.

INSERT IGNORE INTO member (id, login_id, password, name, email, role) VALUES
    (1, 'buyer1', 'password', '구매자1', 'buyer1@test.com', 'USER'),
    (2, 'seller1', 'password', '판매자1', 'seller1@test.com', 'SELLER');

INSERT IGNORE INTO category (id, name, parent_id, path, created_at, updated_at) VALUES
    (1, '전자기기', NULL, '/1', NOW(), NOW()),
    (2, '의류', NULL, '/2', NOW(), NOW());

INSERT IGNORE INTO product (id, seller_id, category_id, name, base_price, status, created_at, updated_at) VALUES
    (1, 2, 1, '기계식 키보드', 89000, 'ON_SALE', NOW(), NOW()),
    (2, 2, 1, '무선 마우스', 39000, 'ON_SALE', NOW(), NOW()),
    (3, 2, 2, '후드 티셔츠', 45000, 'ON_SALE', NOW(), NOW());

INSERT IGNORE INTO product_option (id, product_id, option_name, additional_price, stock_quantity, version, created_at, updated_at) VALUES
    (1, 1, '블랙', 0, 20, 0, NOW(), NOW()),
    (2, 1, '화이트', 3000, 10, 0, NOW(), NOW()),
    (3, 2, '기본', 0, 15, 0, NOW(), NOW()),
    (4, 3, 'M', 0, 8, 0, NOW(), NOW()),
    (5, 3, 'L', 0, 5, 0, NOW(), NOW());

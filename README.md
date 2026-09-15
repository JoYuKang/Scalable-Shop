# 🏬 E-commerce Data Platform

본 프로젝트는 수백만 건의 상품 데이터가 존재하는 대규모 이커머스 환경을 가정하고, 초저지연 검색 엔진과 고신뢰 비동기 주문 파이프라인 구축을 최우선 목표로 합니다. 대량의 트래픽 집중 시 발생할 수 있는 데이터 경합 및 시스템 병목 현상을 설계 단계에서 선제적으로 해결하는 데 집중할 계획입니다.

## 🛠 서비스 핵심 메커니즘
- **분산 환경에서의 정밀한 동시성 제어**: Redis 분산 락을 활용하여 상품 옵션 단위의 재고 차감을 관리함으로써, 선착순 특가 판매 시 발생하는 레이스 컨디션을 방지하고 데이터 무결성을 보장합니다.
- **Kafka 기반의 이벤트 드리븐 아키텍처**: 주문 생성과 후속 처리(재고 동기화, 통계 반영)를 비동기로 분리하여 시스템 결합도를 낮추고 사용자 응답 속도를 높힙니다.
- **Transactional Outbox 패턴을 통한 발행 보장**: DB 트랜잭션과 메시지 발행의 원자성을 확보하여, 인프라 장애 상황에서도 검색 엔진과 RDB 간의 최종 정합성을 보장합니다.
- **고성능 상품 검색 및 전문 분석**: Elasticsearch를 도입하여 초성 검색 및 형태소 분석을 지원하며, 랭킹 스코어 기반의 인기순/판매순 등 복합 정렬 기능을 제공합니다.
- **역할 기반 권한 제어**: User, Seller, Admin 권한을 분리하고 인터셉터를 통해 보안 및 접근 제어를 수행합니다.


## 🛠 Quick Start (Infra)

본 프로젝트는 Docker를 통해 개발 및 테스트에 필요한 인프라 환경을 자동으로 구성합니다.

### 1. 인프라 실행 및 중지
프로젝트 루트 디렉토리에서 아래 명령어를 사용하세요.

```bash
# 컨테이너 백그라운드 실행
docker compose up -d

# 실행 상태 확인
docker compose ps

# 컨테이너 정지 및 삭제
docker compose down

# 데이터(Volume)까지 완전히 삭제하고 초기화할 경우
docker compose down -v
```
## 🏗 아키텍처 설계
- **실용주의 클린 아키텍처**: 1인 개발 환경에 최적화된 레이어 구조를 채택하되, 각 도메인의 핵심 비즈니스 로직이 외부 인프라 기술에 종속되지 않도록 설계했습니다.
- **데이터 분리 설계**: 빈번하게 업데이트되는 통계 데이터(조회수, 판매량)를 별도 테이블로 분리하여 메인 상품 도메인의 락 경합을 최소화했습니다.
- **CQRS**: 데이터 원천인 MySQL과 고속 조회용 Elasticsearch의 책임을 물리적으로 분리하여 조회의 확장성과 쓰기의 안정성을 동시에 확보했습니다.
## 📂 패키지 구조

```text
src/
├── global/                        # 전역 공통 모듈
│   ├── common/                    # BaseEntity, 공통 Enum
│   ├── exception/                 # ErrorCode, GlobalExceptionHandler
│   ├── config/                    # DB, Redis, Kafka, ES, Scheduling 설정 클래스
│   └── util/                      # CommonResponse
│
├── member/
│   ├── domain/                    # Member Entity, MemberRole
│   ├── application/               # MemberService
│   ├── dto/                       # MemberRequest, MemberResponse
│   └── infrastructure/            # MemberJpaRepository
│
├── product/
│   ├── domain/                    # Product, ProductOption, Category, ProductStatus
│   ├── application/               # ProductService
│   ├── dto/                       # ProductCreateRequest, ProductOptionRequest, ProductResponse, ProductOptionResponse
│   ├── infrastructure/            # ProductJpaRepository, CategoryJpaRepository, ProductOptionJpaRepository
│   └── interfaces/                # ProductController
│
├── stock/                         # 재고 증감 이력 관리
│   ├── domain/                    # StockLog Entity, StockLogType
│   ├── application/               # StockService (reserve/release - 락은 OrderFacade가 감싸서 호출)
│   └── infrastructure/            # StockLogJpaRepository
│
├── search/                        # [Query - Elasticsearch 전용]
│   ├── domain/                    # SearchProduct
│   ├── application/               # SearchService(ES 리포지토리만 다룸), SearchIndexFacade(product 도메인과 조율)
│   ├── dto/                       # SearchRequest, SearchResponse
│   ├── infrastructure/            # SearchProductElasticsearchRepository
│   └── interfaces/                # SearchController
│
├── order/
│   ├── domain/                    # Order, OrderItem, OrderStatus
│   ├── application/               # OrderFacade(Redis 락+도메인 조율), OrderService, (추후) OrderExpirationScheduler
│   ├── dto/                       # OrderCreateRequest, OrderItemCreateRequest, OrderItemPricing, OrderResponse, OrderItemResponse
│   ├── infrastructure/            # OrderJpaRepository, OrderItemJpaRepository
│   └── interfaces/                # OrderController
│
├── payment/
│   ├── domain/                    # Payment Entity
│   ├── application/               # PaymentService, MockPgClient
│   ├── dto/                       # PaymentRequest, PaymentResponse
│   └── infrastructure/            # PaymentJpaRepository
│
└── messaging/
    ├── domain/                    # Outbox, ConsumedEvent
    ├── application/                # OutboxPublisher (폴링 퍼블리셔, 추후 Debezium CDC로 대체)
    ├── event/                     # ProductCreatedEvent, OrderPaidEvent, OrderCancelledEvent
    └── kafka/                     # Producer, Consumer
```

# 🧬 ERD

<!-- <img width="951" height="1117" alt="이커머스 (1)" src="https://github.com/user-attachments/assets/bb95ffb4-dbe0-4d2b-ae20-516ea5ca30b3" /> -->
<img width="1191" height="1014" alt="Untitled" src="https://github.com/user-attachments/assets/f64eb184-1e74-4f3c-b1f5-aa342b05b161" />

--- 
## 📦 재고 상태 정책

product_option.stock_quantity는 현재 구매 가능한 수량을 그대로 나타냅니다. 별도의 "예약중 수량" 카운터를 두지 않고, 모든 재고 변동을 stock_log에 append-only로 남기는 방식입니다.

## 💳 결제 도메인

실제 PG 연동 대신 Mock PG로 결제를 흉내냅니다. payment.order_id는 1:N으로 두어 재시도 흐름(실패 → 재시도 → 성공)을 모두 별도 행으로 남깁니다.


pg_tid: Mock PG가 반환하는 가상 거래 ID
status: READY / SUCCESS / FAILED / CANCELLED
approved_at, fail_reason: 각각 승인 시각, 실패 사유 (Mock PG 결과에 따라 기록)

## 🔍 검색

`GET /api/search/products?keyword=...&categoryId=...&page=...&size=...`로 상품명을 검색합니다. 판매중(`ON_SALE`) 상품만 노출되며, 옵션별 재고를 합산한 `totalStock`을 함께 반환합니다.

색인 갱신은 아직 이벤트 기반이 아니라 `POST /api/search/reindex`(MySQL 전체 재조회 후 덮어쓰기)로만 되어 있습니다. product/stock 변경을 Kafka로 흘려 색인을 자동 갱신하는 파이프라인은 다음 작업 항목이고, 그 전까지는 상품을 등록/변경해도 검색에 즉시 반영되지 않습니다 — 재색인을 수동으로 호출해야 합니다.

한국어 형태소 분석(nori)·초성 검색은 인프라(도커 이미지)만 준비된 상태고, 인덱스 매핑에 커스텀 analyzer 적용은 아직 안 되어 있어 현재는 기본(standard) analyzer로 동작합니다. 부분어/조사 변형에 약하다는 걸 인지하고 있고, 다음 작업 항목으로 남겨뒀습니다.

## 🔍 검색-구매 시점 정합성

Elasticsearch 조회 결과는 참고용입니다. MySQL의 재고 변경이 색인에 반영되기까지 지연(현재는 수동 재색인 전까지 아예 반영 안 됨)이 있을 수 있어, 검색 결과에는 재고가 있어 보여도 실제로는 이미 품절일 수 있습니다.


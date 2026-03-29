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
│   ├── config/                    # DB, Redis, Kafka, ES 설정 클래스
│   └── util/                      # CommonResponse
│
├── member/
│   ├── domain/                    # Member Entity
│   ├── application/               # MemberService
│   ├── dto/                       # MemberRequest, MemberResponse
│   └── infrastructure/            # MemberJpaRepository
│
├── product/                       # [Command]
│   ├── domain/                    # Product, ProductOption, Category 
│   ├── application/               # ProductService
│   ├── dto/                       # ProductRequest, ProductResponse, CategoryRequest...
│   └── infrastructure/            # ProductJpaRepository
│
├── search/                        # [Query - Elasticsearch 전용]
│   ├── domain/                    # SearchProduct 
│   ├── application/               # SearchService 
│   ├── dto/                       # SearchRequest, SearchResponse
│   └── infrastructure/            # ElasticsearchRepository 
│
├── order/
│   ├── domain/                    # Order, OrderItem, Outbox
│   ├── application/               # OrderService
│   ├── dto/                       # OrderRequest, OrderResponse
│   └── infrastructure/            # OrderJpaRepository
│
└── messaging/
    ├── event/                     # ProductCreatedEvent 
    └── kafka/                     # Producer, Consumer
```

# ERP

<img width="951" height="1117" alt="이커머스 (1)" src="https://github.com/user-attachments/assets/bb95ffb4-dbe0-4d2b-ae20-516ea5ca30b3" />






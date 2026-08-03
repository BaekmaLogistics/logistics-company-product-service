# Company-Product Service

스파르타 물류 시스템(Sparta Logistics System)의 업체(Company) / 상품(Product) 도메인을 담당하는 마이크로서비스입니다.

---

## 🛠 주요 기술 스택 & 포함된 설정

- **Java**: 17
- **Framework**: Spring Boot 3.5.14
- **Database**: PostgreSQL (Spring Data JPA)
- **Cache**: Redis (Cache-Aside, Sorted Set)
- **Messaging**: RabbitMQ (Hub 삭제 이벤트 구독 예정)
- **API Docs**: Springdoc OpenAPI (Swagger UI)
- **Testing**: JUnit 5, Testcontainers

---

## 📁 프로젝트 패키지 구조

```text
src/main/java/com/sparta/logistics
├── application/       # 비즈니스 유스케이스 / 서비스 로직
│   ├── company/       # 업체 도메인 서비스 로직
│   └── product/       # 상품 도메인 서비스 로직
├── domain/            # 도메인 엔티티, 리포지토리 인터페이스
│   ├── company/
│   └── product/
├── infrastructure/    # DB, 외부 API 연동 구현체
│   ├── config/        # RedisConfig, RabbitMQConfig 등 인프라 설정
│   └── client/        # Hub 서비스 등 외부 서비스 호출용 FeignClient
└── presentation/      # Controller, DTO 및 공통 예외/응답 처리
    ├── company/
    ├── product/
    └── common/
        ├── dto/       # 공통 응답 포맷 (GeneralResponse, ErrorResponse 등)
        └── exception/ # 공통 예외 핸들러 (GlobalExceptionHandler, ApiException)
```

---

## 🌐 담당 도메인

### Company (업체)
- CRUD + 검색(QueryDSL), 권한별 접근 제어(마스터/허브관리자/업체담당자)
- 업체 등록/수정 시 Hub 서비스에 `hubId` 존재 여부 검증 (FeignClient + Resilience4j Retry)
- 업체 목록 Redis 캐싱 (Cache-Aside)
- Hub 삭제 이벤트 구독 → 소속 업체 비활성화 + 캐시 무효화 (RabbitMQ)
- 업체 등록 시 분산락(Redisson) 기반 동시성 제어

### Product (상품)
- CRUD + 검색(QueryDSL), 권한별 접근 제어
- 상품 등록 시 Company 존재 여부 검증 (같은 서비스 내 로컬 조회)
- 상품 목록 Redis 캐싱 (Cache-Aside)
- 허브별 출고 빈도 TOP N 집계 (Redis Sorted Set, Order 생성 이벤트 구독 예정)
- 상품 대량등록 (CSV 업로드, Bulk Insert)

---

## ⚙️ 로컬 실행 환경 설정

### `application.yml` 필수 환경변수

| 변수 | 설명 | 기본값 |
| --- | --- | --- |
| `DB_HOST` / `DB_PORT` / `DB_NAME` / `DB_USER` / `DB_PASSWORD` | PostgreSQL 접속 정보 | - |
| `REDIS_HOST` / `REDIS_PORT` | Redis 접속 정보 | `localhost` / `6379` |
| `RABBITMQ_HOST` / `RABBITMQ_PORT` | RabbitMQ 접속 정보 | - |


### Swagger API 문서

애플리케이션 실행 후 접속 URL:

- **Swagger UI**: `http://localhost:8080/api/api-docs`
- **OpenAPI Spec**: `http://localhost:8080/api/api-spec`

---

## 📌 서비스 경계 관련 참고

Company와 Product는 상품이 업체 없이 존재할 수 없고 조회 시 업체 정보가 함께 필요한 결합도를 고려하여
하나의 서비스로 통합 운영합니다. 서비스 간 통신이 필요한 부분(Hub 존재 검증, Hub 삭제 이벤트 구독 등)은
FeignClient / RabbitMQ를 통해 처리합니다.
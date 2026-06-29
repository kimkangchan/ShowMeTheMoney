# ShowMeTheMoney Backend — 코드 컨벤션 & 설계 레퍼런스

Spring Boot 3.5.6 / Java 21 / MyBatis / MySQL 8.4 LTS

---

## 아키텍처 원칙 — Dependency Rule

이 프로젝트는 **Clean Architecture의 의존성 규칙 하나**를 일관되게 적용한다.

```
의존성은 항상 domain 방향(안쪽)으로만 흐른다

interfaces → application → infrastructure → domain
```

- 바깥 레이어가 안쪽을 참조하는 것만 허용
- 역방향 참조 금지 (예: `infrastructure`가 `interfaces`를 import하는 것 금지)
- `domain` 클래스는 Spring / MyBatis 등 프레임워크 어노테이션을 포함하지 않음

**도메인 간 호출 규칙:**

```
✅ TransactionService → CategoryService   (application → application)
❌ TransactionService → CategoryMapper    (application → 다른 도메인의 infrastructure)
❌ TransactionMapper  → TransactionListRequest  (infrastructure → interfaces)
```

다른 도메인을 호출할 때는 반드시 해당 도메인의 `application` 레이어(Service)만 사용한다.  
`domain/`, `infrastructure/`는 해당 도메인 내부에서만 접근 가능한 비공개 레이어로 취급한다.

---

## 패키지 구조 (DDD 레이어드)

루트 패키지: `com.showmethemoney`

```
com.showmethemoney
├── common              # BaseTimeEntity, ApiResponse<T>, 전역 예외처리, ErrorCode enum
├── auth                # 회원가입 / 로그인 / 로그아웃 / JWT 발급
├── user                # 사용자 정보 조회·수정·탈퇴
├── category            # 카테고리 목록 조회 (읽기 전용 — CUD 없음)
├── transaction         # 수입/지출 내역 CRUD
├── recurringitem       # 고정 수입/지출 항목 CRUD
├── budget              # 월별 예산 설정/조회/수정
├── dashboard           # 집계 조회 전용 (자체 Entity/Table 없음)
└── system              # /health 엔드포인트
```

각 도메인 내부 레이어:

```
{domain}/
├── domain/             # Entity, Repository 인터페이스, Domain Service
├── application/        # UseCase(Service), Command/Query 객체
├── infrastructure/     # MyBatis Mapper 구현체, XML mapper
└── interfaces/         # Controller, Request/Response DTO (record)
```

---

## 코딩 컨벤션

### DTO
- **Java record** 사용. 불변 객체, compact constructor로 검증.
  ```java
  public record CreateTransactionRequest(
      @NotNull Integer type,
      @NotBlank String categoryCode,
      @Positive BigDecimal amount,
      String memo,
      @NotNull LocalDate transactionAt
  ) {}
  ```

### MyBatis
- N+1 방지: JOIN을 명시적으로 작성해 필요한 컬럼을 한 번에 조회. `@OneToMany` 스타일의 lazy load 없음.
- Mapper XML 위치: `src/main/resources/mapper/{domain}/`
- ResultMap에 `typeHandler`로 `TINYINT(1)` ↔ enum/boolean 변환.

### 비동기 트랜잭션 이벤트
- 커밋 후 이벤트가 필요한 경우 `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` 사용.

### 보안
- 비밀번호: **반드시 BCrypt**로 해시 저장. 평문 저장/비교 금지.
- DB 접속 정보, JWT secret: **절대 코드/Git에 하드코딩 금지**. `application.yml`의 `${ENV_VAR}` 플레이스홀더로 참조, 실제 값은 `.env` 또는 K8s Secret.
- `grade`는 signup 시 항상 서버에서 `"user"`로 초기화 (클라이언트 지정 불가).

### 공통 응답
```java
public record ApiResponse<T>(boolean success, T data, ErrorDetail error) {
    public static <T> ApiResponse<T> ok(T data) { ... }
    public static ApiResponse<Void> fail(ErrorCode code) { ... }
}
```

---

## DB 컨벤션

- **엔진/문자셋**: `ENGINE=InnoDB CHARSET=utf8mb4`
- **PK/FK**: `BIGINT UNSIGNED AUTO_INCREMENT`
- **boolean 컬럼** (`type`, `is_active`): `TINYINT(1)` (0/1) — 애플리케이션에서 enum/상수 매핑
- **금액** (`amount`): `DECIMAL(12,0)` (원 단위, 소수점 없음)
- **소프트 삭제**: `deleted_at DATETIME NULL` — 삭제 시 `deleted_at = NOW()` 업데이트. 조회 쿼리에는 항상 `AND deleted_at IS NULL` 조건 포함.
- **`updated_at`**: `DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` (자동 갱신)
- **`yearMonth`**: DB 컬럼은 `CHAR(7)` 형태 `"2026-06"` 저장. API 파라미터 `"202606"` → 서버에서 변환.

---

## 테이블 스키마

### `users`
| 컬럼 | 타입 | 비고 |
|---|---|---|
| `id` | BIGINT UNSIGNED | PK |
| `username` | VARCHAR(50) | UNIQUE, NOT NULL — 로그인 ID |
| `email` | VARCHAR(255) | UNIQUE, NOT NULL |
| `password` | VARCHAR(255) | NOT NULL (BCrypt 해시) |
| `nickname` | VARCHAR(50) | NOT NULL — API의 `name` 필드가 이 컬럼에 매핑됨 |
| `created_at` | DATETIME | DEFAULT CURRENT_TIMESTAMP |
| `updated_at` | DATETIME | ON UPDATE CURRENT_TIMESTAMP |
| `deleted_at` | DATETIME | NULL — 소프트 삭제 |

### `categories`
| 컬럼 | 타입 | 비고 |
|---|---|---|
| `id` | BIGINT UNSIGNED | PK |
| `code` | VARCHAR(20) | UNIQUE (예: `FOOD`, `SALARY`) |
| `code_number` | VARCHAR(3) | UNIQUE (예: `001`, `101`) |
| `name` | VARCHAR(30) | NOT NULL |
| `type` | TINYINT(1) | 0=EXPENSE, 1=INCOME |

**카테고리 시드 데이터 (지출 type=0)**

| code | code_number | name |
|---|---|---|
| FOOD | 001 | 식비 |
| CAFE | 002 | 카페 |
| TRANSPORT | 003 | 교통 |
| SHOPPING | 004 | 쇼핑 |
| HOUSING | 005 | 주거 및 통신 |
| MEDICAL | 006 | 의료건강 |
| CULTURE | 007 | 문화생활 |
| EDUCATION | 008 | 교육 |
| ETC | 009 | 기타 |

**카테고리 시드 데이터 (수입 type=1)**

| code | code_number | name |
|---|---|---|
| SALARY | 101 | 급여 |
| INVESTMENT | 102 | 투자수익 |
| ETC | 103 | 기타 |
| PART_TIME | 104 | 아르바이트 |

### `transactions`
| 컬럼 | 타입 | 비고 |
|---|---|---|
| `id` | BIGINT UNSIGNED | PK |
| `user_id` | BIGINT UNSIGNED | FK → users.id |
| `category_id` | BIGINT UNSIGNED | FK → categories.id |
| `type` | TINYINT(1) | 0=EXPENSE, 1=INCOME |
| `amount` | DECIMAL(12,0) | CHECK > 0 |
| `memo` | VARCHAR(255) | NULL |
| `transaction_date` | DATE | NOT NULL |
| `created_at` / `updated_at` / `deleted_at` | DATETIME | — |

인덱스: `(user_id, transaction_date)`, `(user_id, category_id)`

### `recurring_items`
| 컬럼 | 타입 | 비고 |
|---|---|---|
| `id` | BIGINT UNSIGNED | PK |
| `user_id` | BIGINT UNSIGNED | FK → users.id |
| `category_id` | BIGINT UNSIGNED | FK → categories.id |
| `type` | TINYINT(1) | 0=EXPENSE, 1=INCOME |
| `name` | VARCHAR(100) | NOT NULL |
| `amount` | DECIMAL(12,0) | CHECK > 0 |
| `billing_day` | TINYINT UNSIGNED | CHECK 1~31 |
| `is_active` | TINYINT(1) | DEFAULT 1 |
| `created_at` / `updated_at` / `deleted_at` | DATETIME | — |

### `budgets`
| 컬럼 | 타입 | 비고 |
|---|---|---|
| `id` | BIGINT UNSIGNED | PK |
| `user_id` | BIGINT UNSIGNED | FK → users.id |
| `year_month` | CHAR(7) | 형식: `"2026-06"` |
| `amount` | DECIMAL(12,0) | CHECK > 0 |
| `created_at` / `updated_at` | DATETIME | — |

UNIQUE: `(user_id, year_month)` — 사용자당 월 1건

---

## API 엔드포인트 요약

Base path: `/api`. 인증 필요 항목은 `Authorization: Bearer {token}` 헤더.

### Auth (인증 불필요)
| Method | Path | Body |
|---|---|---|
| POST | `/auth/signup` | `username`, `name`, `email`, `password` |
| POST | `/auth/login` | `email`, `password` |
| POST | `/auth/logout` | — |

### Users
| Method | Path | Body/Query |
|---|---|---|
| GET | `/users/me` | — |
| PUT | `/users/me` | `name` |
| DELETE | `/users/me` | — (소프트 삭제) |

### Categories (읽기 전용)
| Method | Path | Query |
|---|---|---|
| GET | `/categories` | `type` (0\|1, optional) |

### Transactions
| Method | Path | Params |
|---|---|---|
| POST | `/transactions` | body: `type`, `categoryCode`, `amount`, `memo`?, `transactionAt` |
| GET | `/transactions` | query: `type`, `categoryCode`, `period`, `sort`, `page`, `size` |
| GET | `/transactions/{id}` | — |
| PUT | `/transactions/{id}` | body: 수정할 필드만 |
| DELETE | `/transactions/{id}` | — (소프트 삭제) |

### Recurring Items
| Method | Path | Params |
|---|---|---|
| POST | `/recurring-items` | body: `type`, `categoryCode`, `name`, `amount`, `billingDay` |
| GET | `/recurring-items` | query: `isActive`, `type` |
| PUT | `/recurring-items/{id}` | body: 수정할 필드만 (`isActive` 포함) |
| DELETE | `/recurring-items/{id}` | — (소프트 삭제) |

### Budgets
| Method | Path | Params |
|---|---|---|
| POST | `/budgets` | body: `yearMonth`, `amount`, `memo`? |
| GET | `/budgets` | query: `yearMonth` (필수) |
| PUT | `/budgets/{id}` | body: `amount`, `memo` |

### Dashboard (조회 전용)
| Method | Path | Query |
|---|---|---|
| GET | `/dashboard/summary` | `yearMonth` (필수) |
| GET | `/dashboard/categories` | `yearMonth` (필수), `type`? |

### System
| Method | Path | 인증 |
|---|---|---|
| GET | `/health` | X |

---

## 핵심 비즈니스 규칙 (검증 필수)

1. **type ↔ categoryCode 일치 검증**: `POST /transactions`, `POST /recurring-items`에서 요청의 `type`과 선택한 카테고리의 `type`이 반드시 일치해야 함 (INCOME 거래에 지출 카테고리 사용 불가).
2. **소프트 삭제 조회 필터**: 모든 목록/단건 조회 쿼리에 `AND deleted_at IS NULL` 필수.
3. **본인 소유 리소스 검증**: transaction, recurring_item, budget 수정/삭제 시 JWT의 `userId`와 리소스의 `user_id`가 일치하는지 확인. 불일치 시 403.
4. **예산 중복 방지**: `POST /budgets` — 동일 `(userId, yearMonth)` 이미 존재하면 409 Conflict.
5. **`period` 파라미터 파싱**: 문자열 길이로 포맷 판별 (4자=연간, 7자=월간, 10자=일간). 그 외 포맷은 400.
6. **`yearMonth` 변환**: API 파라미터 `"202606"` → DB 저장 `"2026-06"`. 역방향도 동일.

---

## JWT 설계

- AccessToken만 사용 (RefreshToken 없음, MVP)
- Claims: `sub`(userId), `email`, `grade`(`"user"`/`"admin"`), `iat`, `exp`
- 만료 권장: 1시간~수일 (`.env`의 `JWT_EXPIRATION`으로 제어)
- 서명 알고리즘: HS256, secret은 `JWT_SECRET` 환경변수

---

## DB 마이그레이션 (Flyway)

마이그레이션 파일 위치: `src/main/resources/db/migration/`

### 네이밍 규칙

```
V{순번}__{설명}.sql
예) V3__add_tag_column_to_transactions.sql
```

- 순번은 이전 파일보다 높아야 함 (건너뛰기 허용, 중복 불가)
- 설명은 스네이크케이스, 영문

### 새 마이그레이션 추가

1. `V{n+1}__....sql` 파일 생성
2. 앱 재실행 시 Flyway가 자동 적용

### 로컬 마이그레이션 실패 시 복구

MySQL은 DDL이 자동 커밋되어 롤백이 불가능하다. 실패한 마이그레이션은 아래 순서로 수동 복구한다.

```sql
-- 1. 부분 생성된 테이블 DROP (FK 역순으로)
DROP TABLE IF EXISTS recurring_items;
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS budgets;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS users;

-- 2. Flyway 히스토리 초기화
DROP TABLE IF EXISTS flyway_schema_history;
```

이후 앱을 재실행하면 V1부터 다시 적용된다.

---

## 프로파일 구성

`application.yml`의 기본 프로파일은 `local`이며, `SPRING_PROFILES_ACTIVE` 환경변수로 덮어쓴다.

- **local**: `application-local.yml`에 DB/JWT 값 직접 작성 (gitignored)
- **운영**: `application-prod.yml` 불필요 — `application.yml`이 `${DB_URL}`, `${JWT_SECRET}` 등 환경변수를 직접 읽으므로 환경변수만 주입하면 됨

---

## 로컬 실행

1. `src/main/resources/application-local.yml` 생성 (gitignored)
2. 아래 값 채우기:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/showmethemoney?useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8
    username: root
    password: your_password

jwt:
  secret: (openssl rand -hex 32 으로 생성)
  expiration: 86400
```

3. 실행:
```bash
./gradlew bootRun
```


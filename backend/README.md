# ShowMeTheMoney — Backend

Spring Boot 3.5.6 / Java 21 / MyBatis / MySQL 8.x

---

## 요구사항

| 항목 | 버전 |
|---|---|
| Java | 21 |
| MySQL | 8.x |
| Gradle | Wrapper 사용 (별도 설치 불필요) |

---

## 로컬 실행

### 1. 환경변수 설정

`.env.example`을 복사해 `.env`를 생성하고 값을 채웁니다.

```bash
cp .env.example .env
```

| 환경변수 | 설명 | 기본값 |
|---|---|---|
| `DB_URL` | JDBC URL | `jdbc:mysql://localhost:3306/showmethemoney?...` |
| `DB_USERNAME` | DB 유저명 | — (필수) |
| `DB_PASSWORD` | DB 비밀번호 | — (필수) |
| `JWT_SECRET` | JWT 서명 키 | — (필수, `openssl rand -hex 32`로 생성) |
| `JWT_EXPIRATION` | 토큰 만료 시간 (초) | `3600` |
| `CORS_ALLOWED_ORIGINS` | CORS 허용 Origin | `http://localhost:3000` |

> `spring-dotenv` 라이브러리가 `.env` 파일을 자동으로 로드합니다.

### 2. 실행

```bash
./gradlew bootRun
```

서버 시작 시 Flyway가 자동으로 아래 마이그레이션을 순서대로 적용합니다.

- `V1` 테이블 생성
- `V2` 카테고리 시드 데이터
- `V3` 로컬 개발용 더미 데이터 (testuser / alice / bob, 비밀번호: `password123`)

### 3. 동작 확인

```bash
curl http://localhost:8080/api/health
```

---

## 테스트 계정 (V3 더미 데이터)

| username | password | 데이터 |
|---|---|---|
| `testuser` | `password123` | 4개월 거래 내역, 고정항목 7개, 예산 4건 |
| `alice` | `password123` | 2개월 거래 내역, 고정항목 3개, 예산 2건 |
| `bob` | `password123` | 6월 거래 내역만 |

---

---

## 테스트

```bash
./gradlew test
```

---

## DB 초기화 / 마이그레이션 실패 시 복구

MySQL DDL은 자동 커밋되어 롤백이 불가합니다. 아래 순서로 수동 복구합니다.

**테이블만 초기화할 때** (DB는 유지)

```sql
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS recurring_items;
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS budgets;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS flyway_schema_history;
SET FOREIGN_KEY_CHECKS = 1;
```

**DB 자체를 날렸을 때**

```sql
CREATE DATABASE showmethemoney CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

이후 앱을 재실행하면 V1부터 다시 적용됩니다.

---

## API 문서

`docs/QA_CHECKLIST.md` — 전체 엔드포인트 및 QA 체크리스트

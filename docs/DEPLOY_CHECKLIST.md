# 배포 전 체크리스트

---

## DB 마이그레이션

- [ ] **더미 데이터(V3) prod 분리**
  - `db/migration/V3__seed_dummy_data.sql` → `db/dev/V3__seed_dummy_data.sql` 로 이동
  - `application.yml`: `locations: classpath:db/migration` (prod 기본값)
  - `application-local.yml`: `locations: classpath:db/migration,classpath:db/dev` 추가
  - prod에서는 V1(테이블), V2(카테고리)만 실행되어야 함

- [ ] `createDatabaseIfNotExist=true` 옵션 prod URL에서 제거 확인
  - 현재 기본 URL에 포함되어 있으나 `DB_URL` 환경변수로 덮어쓰면 자동 제외됨
  - prod `DB_URL` 환경변수에 해당 옵션 없는지 확인

---

## 보안

- [ ] `JWT_SECRET` 환경변수 충분한 길이(32바이트 이상)로 설정
- [ ] `DB_PASSWORD` 강도 확인
- [ ] `application-local.yml` git에 포함되지 않았는지 확인 (`git status`)
- [ ] HTTPS 적용 여부 확인

---

## 환경변수

- [ ] 아래 환경변수 모두 주입 확인

| 환경변수 | 확인 |
|---|---|
| `DB_URL` | |
| `DB_USERNAME` | |
| `DB_PASSWORD` | |
| `JWT_SECRET` | |
| `JWT_EXPIRATION` | |
| `SPRING_PROFILES_ACTIVE` | |

---

## 인프라

- [ ] MySQL 8.x 실행 중
- [ ] 방화벽 — 8080 포트 외부 노출 여부 결정
- [ ] 로그 저장 경로 설정

---

## 배포 후 확인

- [ ] `GET /api/health` → 200 응답
- [ ] 회원가입 / 로그인 정상 동작
- [ ] 거래 내역 등록 정상 동작
- [ ] Flyway 히스토리 확인 (`SELECT * FROM flyway_schema_history`)
  - V1, V2만 `success=1` 로 존재해야 함 (V3 없어야 함)

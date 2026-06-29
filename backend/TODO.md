# TODO

## 완료
- [x] JWT Secret 설정 (256-bit random)
- [x] 로컬 실행 환경 구성 (Spring Profile, application-local.yml)
- [x] Controller 테스트 작성 (8개 컨트롤러, 40개 테스트)
- [x] API 명세 문서 작성 (docs/api.md)
- [x] Flyway 마이그레이션 설정 (V1 테이블 생성, V2 카테고리 시드)
- [x] 서비스 로직 구현 (Auth, Category, User, Transaction, RecurringItem, Budget, Dashboard)

## 남은 작업
- [ ] Docker Compose 설정 (루트 프로젝트)
- [ ] 테스트 보강
  - [ ] Service 단위 테스트 (Mockito, 비즈니스 로직 검증)
  - [ ] Mapper 테스트 (MyBatis, 실제 DB 연결)
  - [ ] 통합 테스트 (API E2E)

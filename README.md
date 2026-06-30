# ShowMeTheMoney

금융 데이터 관리를 위한 개인 가계 대시보드의 클라우드 인프라 구축 및 컨테이너 실행 환경 구성

## 기술 스택

- **Frontend**: React 19 + Next.js 15 (App Router)
- **Backend**: Spring Boot 3.5 + Java 21 + Spring Security + MyBatis
- **Database**: MySQL 8
- **Infra**: Docker Compose + Kubernetes

## Docker Compose 실행

### 1. 환경변수 설정

루트와 backend 각각 `.env` 파일이 필요합니다.

**루트 `.env`** (docker-compose MySQL 비밀번호용)
```bash
DB_PASSWORD=your_db_password
```

**`backend/.env`** (backend 컨테이너 환경변수 전체)
```bash
cp backend/.env.example backend/.env
# 이후 backend/.env 값 채우기
```

### 2. 포트 충돌 주의

docker-compose는 **3306(MySQL), 8080(Backend), 3000(Frontend)** 포트를 사용합니다.
로컬에서 동일한 포트로 실행 중인 서비스가 있다면 먼저 종료해야 합니다.

> 종료 방법은 [docs/TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md)를 참고하세요.

### 3. 실행

```bash
# 처음 실행 (frontend 이미지 빌드 포함)
docker compose up --build

# 이후 실행
docker compose up

# 백그라운드 실행
docker compose up -d

# 종료
docker compose down

# 종료 + DB 데이터 초기화
docker compose down -v
```

실행 후 `http://localhost:3000`으로 접속합니다.

## 프로젝트 구조

```
showMeTheMoney/
├── docs/          # 문서
├── frontend/      # Next.js 앱
├── backend/       # Spring Boot 앱
├── infra/         # Docker Compose, K8s manifest
└── README.md
```

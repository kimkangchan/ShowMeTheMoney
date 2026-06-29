# ShowMeTheMoney

금융 데이터 관리를 위한 개인 가계 대시보드의 클라우드 인프라 구축 및 컨테이너 실행 환경 구성

## 기술 스택

- **Frontend**: React 19 + Next.js 15 (App Router)
- **Backend**: Spring Boot 3.5 + Java 21 + Spring Security + MyBatis
- **Database**: MySQL 8
- **Infra**: Docker Compose + Kubernetes

## 실행 방법

```bash
# Docker Compose로 전체 실행
docker compose -f infra/docker-compose.yml up -d
```

## 프로젝트 구조

```
showMeTheMoney/
├── docs/          # 문서
├── frontend/      # Next.js 앱
├── backend/       # Spring Boot 앱
├── infra/         # Docker Compose, K8s manifest
└── README.md
```

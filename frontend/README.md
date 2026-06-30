# ShowMeTheMoney — Frontend

개인 가계부 대시보드 프론트엔드 (Next.js 16 + React 19 + TypeScript)

---

## 기술 스택

| 분류 | 기술 |
|---|---|
| 프레임워크 | Next.js 16.2 (App Router) |
| UI 라이브러리 | React 19 |
| 언어 | TypeScript 5 |
| 스타일링 | Tailwind CSS 4 |
| HTTP 클라이언트 | Axios |
| 차트 | Recharts |
| 테마 | next-themes (라이트/다크 모드) |

---

## 프로젝트 구조

```
frontend/src/
├── app/
│   ├── auth/
│   │   ├── login/page.tsx       # 로그인 페이지
│   │   └── signup/page.tsx      # 회원가입 페이지
│   ├── dashboard/page.tsx       # 대시보드 (메인)
│   ├── transactions/page.tsx    # 거래 내역 관리
│   ├── recurring/page.tsx       # 고정 수입/지출
│   ├── budget/page.tsx          # 예산 설정
│   ├── globals.css              # 전역 스타일 (CSS 변수, 테마)
│   ├── layout.tsx               # 루트 레이아웃
│   └── page.tsx                 # 루트 → /dashboard 리다이렉트
├── components/
│   ├── layout/
│   │   ├── DashboardLayout.tsx  # 인증 체크 + 사이드바 래퍼
│   │   └── Sidebar.tsx          # 네비게이션 사이드바 + 테마 토글
│   └── ui/
│       └── TransactionModal.tsx # 거래 등록 모달
├── context/
│   └── AuthContext.tsx          # 인증 상태 전역 관리 (로그인/로그아웃/유저 정보)
├── lib/
│   ├── api.ts                   # Axios 인스턴스 (JWT 인터셉터, 401 처리)
│   ├── constants.ts             # 카테고리 목록, 차트 색상
│   └── format.ts                # 금액/날짜/연월 포맷 유틸
└── types/
    └── index.ts                 # 공통 타입 정의
```

---

## 주요 기능

### 인증
- 회원가입 / 로그인 / 로그아웃
- JWT AccessToken을 `localStorage`에 저장
- 모든 API 요청에 `Authorization: Bearer {token}` 자동 주입
- 401 응답 시 자동으로 로그인 페이지로 리다이렉트

### 대시보드
- 월별 총 수입 / 총 지출 / 잔액 / 예산 카드
- 예산 사용률 프로그레스 바 (초과 시 빨간색 경고)
- 카테고리별 지출 가로 막대 차트 (Recharts)
- 최근 거래 5건 목록

### 거래 내역
- 월별 거래 목록 (페이지네이션, 최신순/오래된순 정렬)
- 수입/지출/전체 타입 필터
- 거래 등록 모달 (유형, 카테고리, 금액, 날짜, 메모)
- 거래 삭제

### 고정 수입/지출
- 매월 반복되는 항목 등록/삭제
- 활성/비활성 토글

### 예산 설정
- 월별 예산 등록 및 수정

### 테마
- 라이트 / 다크 모드 전환 (사이드바 하단 토글)
- `next-themes` 기반, 시스템 설정 연동 가능

---

## 카테고리

### 지출
| 코드 | 이름 |
|---|---|
| FOOD | 식비 |
| CAFE | 카페 |
| TRANSPORT | 교통 |
| SHOPPING | 쇼핑 |
| HOUSING | 주거 및 통신 |
| MEDICAL | 의료건강 |
| CULTURE | 문화생활 |
| EDUCATION | 교육 |
| ETC | 기타 |

### 수입
| 코드 | 이름 |
|---|---|
| SALARY | 급여 |
| INVESTMENT | 투자수익 |
| PART_TIME | 아르바이트 |
| ETC | 기타 |

---

## 환경변수

| 변수명 | 설명 | 기본값 |
|---|---|---|
| `NEXT_PUBLIC_API_URL` | 백엔드 API 주소 | `http://localhost:8080` |

> `NEXT_PUBLIC_API_URL`은 **빌드 타임에 고정**됩니다. 환경이 바뀌면 반드시 재빌드해야 합니다.

### 환경별 설정

| 환경 | 설정 |
|---|---|
| 로컬 | 설정 불필요 (기본값 사용) |
| VMware | 루트 `.env`에 `NEXT_PUBLIC_API_URL=http://VM_IP:8080` 추가 |
| AWS | 루트 `.env`에 `NEXT_PUBLIC_API_URL=https://api.도메인.com` 추가 |

---

## 로컬 개발 실행

### 사전 요구사항
- Node.js 20+
- 백엔드 서버 실행 중 (`http://localhost:8080`)

### 실행

```bash
cd frontend
npm install
npm run dev
```

→ `http://localhost:3000` 접속

---

## Docker 빌드

```bash
# 로컬 환경 (기본값 사용)
docker build -t showmethemoney-frontend:v1.0.0 .

# VMware 등 외부 서버 환경
docker build \
  --build-arg NEXT_PUBLIC_API_URL=http://192.168.121.12:8080 \
  -t showmethemoney-frontend:v1.0.0 .
```

---

## API 연동

모든 API 요청은 `src/lib/api.ts`의 Axios 인스턴스를 통해 처리됩니다.

```
baseURL: process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080"
```

| 기능 | 메서드 | 엔드포인트 |
|---|---|---|
| 로그인 | POST | `/api/auth/login` |
| 회원가입 | POST | `/api/auth/signup` |
| 로그아웃 | POST | `/api/auth/logout` |
| 내 정보 조회 | GET | `/api/users/me` |
| 대시보드 요약 | GET | `/api/dashboard/summary` |
| 카테고리별 지출 | GET | `/api/dashboard/categories` |
| 거래 목록 | GET | `/api/transactions` |
| 거래 등록 | POST | `/api/transactions` |
| 거래 삭제 | DELETE | `/api/transactions/{id}` |
| 고정 항목 목록 | GET | `/api/recurring-items` |
| 고정 항목 등록 | POST | `/api/recurring-items` |
| 고정 항목 수정 | PUT | `/api/recurring-items/{id}` |
| 고정 항목 삭제 | DELETE | `/api/recurring-items/{id}` |
| 예산 조회 | GET | `/api/budgets` |
| 예산 등록 | POST | `/api/budgets` |
| 예산 수정 | PUT | `/api/budgets/{id}` |

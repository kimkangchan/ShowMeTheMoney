# 백엔드 트러블슈팅

## CORS 트러블슈팅

### 문제 상황

Spring Boot 백엔드에 Cross-Origin 리소스 공유(CORS) 설정이 없어, Next.js/React 프론트엔드에서 보낸 요청에 대한 브라우저의 CORS 정책 검증을 통과하지 못하고 **응답이 차단됨**.

프론트엔드 요청 자체는 서버까지 정상적으로 도달하고 응답도 오지만, 백엔드 응답 헤더에 `Access-Control-Allow-Origin`이 없어 브라우저가 해당 응답을 애플리케이션 코드(JS)에 전달하지 않고 차단하는 것이 정확한 현상. 프리플라이트(preflight, `OPTIONS`) 요청 단계에서 막히는 경우가 많으며, 개발자도구 콘솔에는 다음과 같은 형태의 에러가 출력됨.

```
Access to fetch at 'http://backend-host/api/...' from origin 'http://frontend-host'
has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present
on the requested resource.
```

### 배경 — 환경별로 허용해야 할 Origin이 계속 바뀜

실행 환경이 늘어나면서 CORS로 허용해줘야 하는 origin 값이 매번 달라지는 문제가 발생.

| 단계 | 실행 환경 | 상태 | 비고 |
|---|---|---|---|
| 1 | 로컬 맥북 (Next.js, Spring Boot 직접 실행) | 정상 통신 | `http://localhost:3000` 고정값 사용 |
| 2 | 로컬 맥북 + Docker Compose | 정상 통신 | 고정값(`localhost:3000`)으로도 여전히 통신 가능 |
| 3 | VMware (VM 환경) | **CORS 차단 발생** | 네트워크 구성 차이로 접근 origin이 `localhost`와 달라짐 |

### 해결 과정

#### 1차 — 하드코딩 제거, 환경변수로 분리

- CORS 허용 origin을 코드/설정 파일에 고정값으로 박아두지 않고 `CORS_ALLOWED_ORIGINS` 환경변수로 분리
- `.env` 파일에 값 추가
- `application.yml`에서 아래와 같이 환경변수를 읽고, 값이 없을 경우를 대비한 기본값(default value)도 함께 설정

  ```yaml
  cors:
    allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:3000}
  ```

- CORS 설정 클래스(`WebMvcConfigurer` 또는 `CorsConfigurationSource`)에서 해당 프로퍼티 값을 읽어 허용 origin으로 등록

#### 2차 — 배포 환경별 동적 처리

- 위 구조 덕분에 환경마다 코드 수정 없이 환경변수 값만 바꾸면 되는 구조로 전환 완료
- K8s 배포 시에는 `.env` 파일 대신 `secret.yaml`에 `CORS_ALLOWED_ORIGINS` 값을 등록하고, Deployment에서 해당 Secret을 환경변수로 주입받도록 수정

### 결론 / 배운 점

- CORS 허용 origin은 로컬 / Docker / VM / K8s 등 **실행 환경마다 달라질 수 있는 값**이므로 처음부터 하드코딩하지 않고 외부 설정(환경변수 / Secret)으로 분리하는 것이 맞음
- `localhost` 고정값이 통했던 것은 우연히 로컬·Docker 환경에서 브라우저가 접근하는 호스트가 동일했기 때문이며, VM처럼 네트워크 계층이 달라지는 환경에서는 항상 재검토가 필요
- 향후 배포 환경이 추가되더라도 코드 변경 없이 환경변수 / Secret 값만 추가하면 되는 구조를 확보

### 기술 스택

- **Backend**: Spring Boot
- **Frontend**: Next.js, React
- **DB**: MySQL

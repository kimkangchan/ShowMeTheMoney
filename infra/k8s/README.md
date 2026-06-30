## 1. 기술 스택

| 영역 | 구성 |
| --- | --- |
| Frontend | Next.js 15, React 19 |
| Backend | Spring Boot 3.5, Java 21, Spring Security, MyBatis |
| Database | MySQL 8 |
| Container | Docker |
| Orchestration | Kubernetes, kubeadm, cri-dockerd |
| VM | VMware Fusion, Ubuntu Server |
| CNI | Flannel |

## 2. 인프라 구성

현재 배포 환경은 MacBook의 VMware Fusion 위에 Ubuntu Server VM 3대를 구성해서 사용합니다.

| VM | 계정 | IP | 역할 |
| --- | --- | --- | --- |
| master | user | 192.168.121.10 | Kubernetes Control Plane |
| node01 | user | 192.168.121.11 | Worker Node, Frontend Pod 실행 |
| node02 | user | 192.168.121.12 | Worker Node, Backend Pod + MySQL Pod 실행 |
| storage | user | 192.168.121.13 | 2차 고도화 예정, 현재 미사용 |

VMware Fusion 네트워크는 `Share with my Mac` 기준이며, VM 네트워크 대역은 `192.168.121.0/24`입니다.

## 3. Kubernetes 배포 구조

```text
Kubernetes Cluster
├─ master
│  └─ Control Plane
├─ node01
│  └─ Frontend Pod
│     └─ Next.js application
└─ node02
   ├─ Backend Pod
   │  └─ Spring Boot API
   └─ MySQL Pod
      └─ MySQL 8 + Local PersistentVolume
```

## 4. 디렉토리 구조

```text
ShowMeTheMoney/
├─ backend/              # Spring Boot 백엔드
├─ frontend/             # Next.js 프론트엔드
├─ docs/                 # 프로젝트 문서
├─ infra/
│  └─ k8s/
│     ├─ namespace.yaml
│     ├─ configmap.yaml
│     ├─ secret.example.yaml
│     ├─ frontend/
│	   │	├─ deployment.yaml
│	   │	└─ service.yaml
│     ├─ backend/
│	   │	├─ deployment.yaml
│	   │	└─ service.yaml
│     ├─ mysql/
│	   │	├─ pv.yaml
│	   │	├─ pvc.yaml
│	   │	├─ service.yaml
│	   │	└─statefulset.yaml
│     └─ commands/		#kubectl 일괄 처리 명령어
├─ docker-compose.yml
└─ README.md
```

## 5. Kubernetes 리소스 설명

| 파일 | 역할 |
| --- | --- |
| `infra/k8s/namespace.yaml` | `showmethemoney` namespace 생성 |
| `infra/k8s/configmap.yaml` | Spring profile, DB URL, JWT 만료 시간 등 공개 가능한 환경값 정의 |
| `infra/k8s/secret.example.yaml` | Secret 샘플 파일. 실제 배포 시 `secret.yaml`로 복사 후 값 수정 |
| `infra/k8s/frontend/deployment.yaml` | Frontend Pod 실행. `workload=frontend` 라벨이 있는 node01에 배치 |
| `infra/k8s/frontend/service.yaml` | Frontend NodePort Service. 외부 접속 포트 `30080` 사용 |
| `infra/k8s/backend/deployment.yaml` | Backend Pod 실행. `workload=backend-db` 라벨이 있는 node02에 배치 |
| `infra/k8s/backend/service.yaml` | Backend ClusterIP Service. 클러스터 내부에서 `backend:8080`으로 접근 |
| `infra/k8s/mysql/pv.yaml` | node02의 `/srv/showmethemoney/mysql`을 MySQL 저장소로 사용하는 Local PV |
| `infra/k8s/mysql/pvc.yaml` | MySQL Pod가 사용할 저장소 요청 |
| `infra/k8s/mysql/statefulset.yaml` | MySQL Pod 실행. 재시작 후에도 동일한 저장소 유지 |
| `infra/k8s/mysql/service.yaml` | MySQL ClusterIP Service. Backend에서 `mysql:3306`으로 접근 |

## 6. Docker 이미지

Kubernetes YAML은 아래 이미지 이름을 사용합니다.

| 컴포넌트 | 이미지 |
| --- | --- |
| Frontend | `showmethemoney-frontend:v1.0.1` |
| Backend | `showmethemoney-backend:v1.0.2` |
| MySQL | `mysql:8.0` |

`imagePullPolicy: IfNotPresent`로 설정되어 있으므로, Pod가 실행되는 노드에 이미지가 있으면 로컬 이미지를 사용합니다.

예시:

```bash
docker build -t showmethemoney-frontend:v1.0.0 ./frontend
docker build -t showmethemoney-backend:v1.0.0 ./backend
```

Frontend Pod는 node01에서 실행되고, Backend Pod는 node02에서 실행되므로 각 이미지가 필요한 노드에 존재해야 합니다.

## 7. Kubernetes 배포

아래 명령어는 master VM에서 실행합니다.

```bash
kubectl apply -f infra/k8s/namespace.yaml
kubectl apply -f infra/k8s/configmap.yaml
kubectl apply -f infra/k8s/secret.yaml
kubectl apply -f infra/k8s/mysql/
kubectl apply -f infra/k8s/backend/
kubectl apply -f infra/k8s/frontend/
```

배포 상태 확인:

```bash
kubectl get nodes -o wide
kubectl get pods -n showmethemoney -o wide
kubectl get svc -n showmethemoney
kubectl get endpoints backend -n showmethemoney -o wide
kubectl get endpoints mysql -n showmethemoney -o wide
```

정상 상태 예시:

```text
backend   1/1   Running
frontend  1/1   Running
mysql-0   1/1   Running
```

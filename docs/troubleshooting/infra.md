# 인프라 트러블슈팅

ShowMeTheMoney Kubernetes 배포 과정에서 발생한 인프라 트러블슈팅 내용을 문제, 원인, 해결 방법, 결과 중심으로 정리한 문서입니다.

## 1. Docker Compose 실행 시 포트 충돌

### 문제

Docker Compose 실행 시 MySQL, Backend, Frontend가 사용하는 포트가 이미 로컬에서 사용 중이면 컨테이너가 정상적으로 실행되지 않습니다.

ShowMeTheMoney의 Docker Compose 환경은 아래 포트를 사용합니다.

| 서비스 | 포트 |
| --- | --- |
| MySQL | `3306` |
| Backend | `8080` |
| Frontend | `3000` |

대표적인 증상은 다음과 같습니다.

```text
port is already allocated
bind: address already in use
```

### 원인

로컬 PC에서 동일한 포트를 사용하는 서비스가 이미 실행 중이기 때문입니다.

예를 들어 다음과 같은 경우 포트 충돌이 발생할 수 있습니다.

- Homebrew 또는 공식 패키지로 설치한 MySQL이 `3306` 포트를 사용 중
- 로컬 Backend가 `./gradlew bootRun`으로 `8080` 포트에서 실행 중
- 로컬 Frontend가 `npm run dev`로 `3000` 포트에서 실행 중

### 해결 방법

Docker Compose 실행 전 동일 포트를 사용하는 로컬 서비스를 먼저 종료합니다.

#### MySQL `3306`

Homebrew로 설치한 MySQL인 경우:

```bash
brew services stop mysql
```

공식 패키지로 설치한 MySQL인 경우:

```bash
sudo launchctl stop com.oracle.oss.mysql.mysqld
```

실행 중인 프로세스를 직접 확인하려면:

```bash
sudo lsof -i :3306
sudo kill <PID>
```

#### Backend `8080`

로컬에서 `./gradlew bootRun`으로 실행 중이면 해당 터미널에서 종료합니다.

```text
Ctrl + C
```

#### Frontend `3000`

로컬에서 `npm run dev`로 실행 중이면 해당 터미널에서 종료합니다.

```text
Ctrl + C
```

포트 점검 명령어:

```bash
sudo lsof -i :3306
sudo lsof -i :8080
sudo lsof -i :3000
```

### 결과

포트 충돌을 제거한 뒤 Docker Compose로 Frontend, Backend, MySQL 컨테이너를 정상 실행할 수 있습니다.

```bash
docker compose up --build
```

## 2. MySQL Pod `ContainerCreating` 상태 지속

### 문제

MySQL Pod가 생성 이후 `ContainerCreating` 상태에서 정상적으로 실행되지 않았습니다.

```text
mysql-0   0/1   ContainerCreating
```

### 원인

MySQL 저장소로 사용할 Local PersistentVolume 경로가 node02에 존재하지 않았습니다.

```text
node02:/srv/showmethemoney/mysql
```

Kubernetes Local PV는 지정된 노드의 실제 디렉토리를 사용하므로, 해당 경로가 없으면 MySQL Pod가 볼륨을 마운트하지 못합니다.

### 해결 방법

node02에 접속하여 MySQL 데이터 저장용 디렉토리를 생성하고 권한을 설정한 뒤 MySQL Pod를 재생성했습니다.

```bash
ssh user@192.168.121.12
sudo mkdir -p /srv/showmethemoney/mysql
sudo chmod -R 0777 /srv/showmethemoney/mysql
exit

kubectl delete pod mysql-0 -n showmethemoney
```

### 결과

MySQL Pod가 정상적으로 볼륨을 마운트하고 `Running` 상태로 전환되었습니다.

```text
mysql-0   1/1   Running
```

## 3. Backend에서 `UnknownHostException: mysql` 발생

### 문제

Backend Pod가 MySQL에 연결하는 과정에서 `mysql` 호스트명을 찾지 못해 기동에 실패했습니다.

```text
java.net.UnknownHostException: mysql
Temporary failure in name resolution
```

### 원인

Kubernetes 내부 DNS가 `mysql` Service 이름을 정상적으로 해석하지 못했습니다.

Backend는 아래와 같은 DB URL을 사용합니다.

```text
jdbc:mysql://mysql:3306/showmethemoney
```

여기서 `mysql`은 Kubernetes Service 이름이며, CoreDNS가 이를 ClusterIP로 해석해야 합니다. DNS 또는 Service 라우팅에 문제가 있으면 Backend가 MySQL을 찾지 못합니다.

### 해결 방법

MySQL Service와 Endpoint가 정상인지 먼저 확인하고, 이후 CoreDNS와 kube-proxy 상태를 점검 및 재시작했습니다.

```bash
kubectl get svc mysql -n showmethemoney -o wide
kubectl get endpoints mysql -n showmethemoney -o wide
kubectl get pod mysql-0 -n showmethemoney --show-labels

kubectl get pods -n kube-system -l k8s-app=kube-dns -o wide
kubectl get pods -n kube-system -l k8s-app=kube-proxy -o wide

kubectl rollout restart deployment/coredns -n kube-system
kubectl rollout restart daemonset/kube-proxy -n kube-system
```

DNS 테스트도 함께 수행했습니다.

```bash
kubectl run dns-test -n showmethemoney \
  --image=busybox:1.36 \
  --restart=Never \
  --rm -it -- nslookup mysql
```

### 결과

Backend가 `mysql:3306` Service 주소를 정상적으로 인식하고 DB 연결 단계로 진행되었습니다.

```text
Name: mysql.showmethemoney.svc.cluster.local
Address: <mysql-service-cluster-ip>
```

## 4. CoreDNS `CrashLoopBackOff` 및 `Unauthorized` 로그 발생

### 문제

CoreDNS Pod가 `CrashLoopBackOff` 상태가 되었고, 로그에서 Kubernetes API 조회 권한 관련 오류가 발생했습니다.

```text
coredns   0/1   CrashLoopBackOff
```

주요 로그:

```text
failed to list *v1.Service: Unauthorized
failed to list *v1.Namespace: Unauthorized
failed to list *v1.EndpointSlice: Unauthorized
```

### 원인

CoreDNS가 Kubernetes API에서 Service, Namespace, EndpointSlice 정보를 조회하지 못했습니다.

CoreDNS는 클러스터 내부 DNS 처리를 위해 Kubernetes API의 Service와 Endpoint 정보를 읽어야 합니다. 해당 권한 또는 연결 상태에 문제가 있으면 Service DNS 해석이 실패합니다.

### 해결 방법

CoreDNS ServiceAccount와 RBAC 권한을 확인한 후 CoreDNS Deployment를 재시작했습니다.

```bash
kubectl get sa coredns -n kube-system
kubectl get deployment coredns -n kube-system -o yaml | grep serviceAccountName

kubectl auth can-i list services \
  --as=system:serviceaccount:kube-system:coredns \
  --all-namespaces

kubectl auth can-i list namespaces \
  --as=system:serviceaccount:kube-system:coredns

kubectl auth can-i list endpointslices.discovery.k8s.io \
  --as=system:serviceaccount:kube-system:coredns \
  --all-namespaces

kubectl rollout restart deployment/coredns -n kube-system
kubectl rollout status deployment/coredns -n kube-system
```

### 결과

CoreDNS Pod가 `Running` 상태로 복구되었고, Kubernetes 내부 DNS 조회가 정상화되었습니다.

```text
coredns   1/1   Running
```

## 5. kube-proxy `Unauthorized` 로그 발생

### 문제

kube-proxy 로그에서 `Unauthorized` 오류가 반복되었고, Service 라우팅과 DNS 접근에 문제가 발생했습니다.

주요 로그:

```text
failed to list *v1.Service: Unauthorized
failed to list *v1.Node: Unauthorized
failed to list *v1.EndpointSlice: Unauthorized
```

DNS 테스트에서는 다음과 같은 오류가 발생했습니다.

```text
nslookup: write to '10.96.0.10': Connection refused
connection timed out; no servers could be reached
```

### 원인

kube-proxy가 Kubernetes API에서 Service, Node, EndpointSlice 정보를 정상적으로 조회하지 못했습니다.

kube-proxy는 Service ClusterIP 라우팅 규칙을 각 노드에 반영하는 역할을 합니다. kube-proxy가 정상 동작하지 않으면 ClusterIP 기반 통신과 Service 라우팅이 실패할 수 있습니다.

### 해결 방법

kube-proxy ServiceAccount와 권한을 확인하고 DaemonSet을 재시작했습니다.

```bash
kubectl get daemonset kube-proxy -n kube-system -o yaml | grep serviceAccountName

kubectl auth can-i list services \
  --as=system:serviceaccount:kube-system:kube-proxy \
  --all-namespaces

kubectl auth can-i list nodes \
  --as=system:serviceaccount:kube-system:kube-proxy

kubectl auth can-i list endpointslices.discovery.k8s.io \
  --as=system:serviceaccount:kube-system:kube-proxy \
  --all-namespaces

kubectl rollout restart daemonset/kube-proxy -n kube-system
kubectl get pods -n kube-system -l k8s-app=kube-proxy -o wide
```

### 결과

kube-proxy가 정상화되면서 ClusterIP 및 Kubernetes Service 라우팅이 복구되었습니다.

```text
kube-proxy   1/1   Running
```

## 6. Backend DB 연결 시 `Public Key Retrieval is not allowed` 발생

### 문제

Backend가 MySQL Service 이름은 정상적으로 해석했지만, 실제 DB 연결 과정에서 인증 관련 오류가 발생했습니다.

```text
Public Key Retrieval is not allowed
Unable to obtain connection from database
```

### 원인

MySQL 8 인증 방식에서 JDBC 공개키 검색 옵션이 누락되었습니다.

MySQL 8 계열에서는 인증 플러그인과 JDBC 드라이버 조합에 따라 `allowPublicKeyRetrieval=true` 옵션이 필요할 수 있습니다. 해당 옵션이 없으면 Backend가 DB 커넥션을 생성하지 못합니다.

### 해결 방법

`infra/k8s/configmap.yaml`의 `DB_URL`에 아래 옵션을 추가했습니다.

```text
allowPublicKeyRetrieval=true
```

예시:

```yaml
DB_URL: "jdbc:mysql://mysql:3306/showmethemoney?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8&allowPublicKeyRetrieval=true"
```

ConfigMap 적용 후 Backend Deployment를 재시작했습니다.

```bash
kubectl apply -f infra/k8s/configmap.yaml
kubectl rollout restart deployment/backend -n showmethemoney
kubectl rollout status deployment/backend -n showmethemoney
```

### 결과

Backend가 MySQL에 정상적으로 연결되었고, Spring Boot 애플리케이션이 정상 기동되었습니다.

```text
Started ShowMeTheMoneyApplication
```

## 요약

| 문제 | 핵심 원인 | 해결 결과 |
| --- | --- | --- |
| Docker Compose 포트 충돌 | 로컬 MySQL/Backend/Frontend가 동일 포트 사용 | Compose 컨테이너 정상 실행 |
| MySQL `ContainerCreating` | node02 Local PV 경로 없음 | MySQL Pod `Running` |
| Backend `UnknownHostException: mysql` | 내부 DNS/Service 해석 실패 | `mysql:3306` 인식 |
| CoreDNS `CrashLoopBackOff` | API 조회 권한/상태 문제 | DNS Pod 복구 |
| kube-proxy `Unauthorized` | Service 라우팅 정보 조회 실패 | ClusterIP 라우팅 정상화 |
| `Public Key Retrieval is not allowed` | MySQL 8 JDBC 옵션 누락 | Backend DB 연결 성공 |


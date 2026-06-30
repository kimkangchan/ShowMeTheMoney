# ShowMeTheMoney K8s Command Files Summary

이 문서는 master VM에서 사용하는 `cmd_*` 명령어 파일들이 어떤 작업을 실행하는지 간단히 요약한다.

## `cmd_check_deploy_logs`

Frontend와 Backend Deployment의 최근 로그를 확인한다.

실행 내용:

```bash
kubectl logs -n showmethemoney deployment/frontend --tail=100
kubectl logs -n showmethemoney deployment/backend --tail=100
```

사용 시점:

- Pod는 Running인데 화면이나 API가 정상 동작하지 않을 때
- Spring Boot, Next.js 실행 로그를 확인할 때
- Backend DB 연결 오류, API 오류, Frontend 실행 오류를 볼 때

## `cmd_check_deploy_status`

Frontend와 Backend Deployment의 배포 진행 상태를 확인하고, Pod 상태를 함께 조회한다.

실행 내용:

```bash
kubectl rollout status deployment/frontend -n showmethemoney
kubectl rollout status deployment/backend -n showmethemoney
kubectl get pods -n showmethemoney -o wide
```

사용 시점:

- `kubectl apply` 이후 배포가 완료됐는지 확인할 때
- 이미지 변경 또는 Deployment 재시작 후 새 Pod가 정상 기동됐는지 볼 때
- Pod가 어느 노드에서 실행 중인지 확인할 때

## `cmd_check_pods`

`showmethemoney` namespace의 Pod 상태를 간단히 확인한다.

실행 내용:

```bash
kubectl get pods -n showmethemoney -o wide
```

사용 시점:

- Frontend, Backend, MySQL Pod가 모두 `Running`인지 확인할 때
- `READY`, `STATUS`, `RESTARTS`, `NODE`, `IP`를 빠르게 볼 때
- 장애 발생 시 가장 먼저 현재 상태를 확인할 때

## `cmd_check_resource_status`

Pod, Service, Endpoint 상태를 한 번에 확인한다.

실행 내용:

```bash
kubectl get pods -n showmethemoney -o wide
kubectl get svc -n showmethemoney
kubectl get endpoints backend -n showmethemoney -o wide
kubectl get endpoints mysql -n showmethemoney -o wide
```

사용 시점:

- Service가 Pod와 정상 연결됐는지 확인할 때
- Backend Service가 Backend Pod IP를 바라보는지 확인할 때
- MySQL Service가 `mysql-0` Pod IP와 `3306` 포트로 연결됐는지 확인할 때
- Service는 있는데 접속이 안 되는 문제를 점검할 때

## `cmd_kubectl_applys`

Kubernetes 리소스 yaml 파일들을 순서대로 적용한다.

실행 내용:

```bash
kubectl apply -f infra/k8s/namespace.yaml
kubectl apply -f infra/k8s/configmap.yaml
kubectl apply -f infra/k8s/secret.yaml
kubectl apply -f infra/k8s/mysql/
kubectl apply -f infra/k8s/backend/
kubectl apply -f infra/k8s/frontend/
```

사용 시점:

- 최초 배포할 때
- yaml 파일 수정 후 Kubernetes 클러스터에 반영할 때
- ConfigMap, Secret, MySQL, Backend, Frontend 리소스를 한 번에 적용할 때

주의:

- `infra/k8s/secret.yaml`은 GitHub에 올리지 않는 파일이다.
- Secret 파일은 각 VM 또는 로컬 환경에서 직접 생성해서 사용한다.
- ConfigMap이나 Secret 변경 후에는 기존 Pod가 자동으로 새 값을 읽지 않을 수 있으므로 필요한 Deployment를 재시작한다.

## `cmd_restart_backend-db`

Backend Deployment와 MySQL StatefulSet을 재시작한다.

실행 내용:

```bash
kubectl rollout restart deployment/backend -n showmethemoney
kubectl rollout restart statefulset/mysql -n showmethemoney
```

사용 시점:

- Backend ConfigMap 또는 Secret 변경사항을 반영할 때
- Backend가 DB 연결 문제로 재시작이 필요할 때
- MySQL StatefulSet을 다시 기동해야 할 때

참고:

- 실제 파일명이 `cmd_restart_backend_db`처럼 underscore를 쓸 수도 있으므로 master VM의 파일명을 확인해서 실행한다.
- MySQL 재시작은 데이터 경로와 PVC 상태를 확인한 뒤 수행하는 것이 좋다.

## 권장 실행 순서

배포 후에는 아래 순서로 확인한다.

```bash
./cmd_kubectl_applys
./cmd_check_deploy_status
./cmd_check_resource_status
./cmd_check_deploy_logs
```

문제가 생겼을 때는 먼저 Pod 상태를 확인한다.

```bash
./cmd_check_pods
```



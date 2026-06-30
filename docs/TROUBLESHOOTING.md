# Troubleshooting

## Docker Compose 실행 시 포트 충돌

docker-compose는 3306(MySQL), 8080(Backend), 3000(Frontend) 포트를 사용합니다.
로컬에서 동일한 포트로 서비스가 실행 중이면 아래 방법으로 먼저 종료합니다.

### MySQL (3306)

**Homebrew로 설치한 경우**
```bash
brew services stop mysql
```

**공식 패키지로 설치한 경우**
```bash
sudo launchctl stop com.oracle.oss.mysql.mysqld
```

실행 중인 프로세스를 직접 확인하려면:
```bash
sudo lsof -i :3306
# 출력된 PID로 종료
sudo kill <PID>
```

### Backend (8080)

로컬에서 `./gradlew bootRun`으로 실행 중이면 해당 터미널에서 `Ctrl + C`로 종료합니다.

### Frontend (3000)

로컬에서 `npm run dev`로 실행 중이면 해당 터미널에서 `Ctrl + C`로 종료합니다.

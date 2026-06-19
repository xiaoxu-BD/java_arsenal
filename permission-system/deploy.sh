#!/bin/bash

PROJECT_DIR="/home/projects/demo/permission-system"
IMAGE_NAME="permission-system"
CONTAINER_NAME="permission-system"
PORT="16399:13690"

# 宿主机日志目录（会挂载进容器，容器重建日志不丢）
HOST_LOG_DIR="${PROJECT_DIR}/logs"
# 容器内日志路径，必须和 logback-spring.xml 里的默认值一致
CONTAINER_LOG_DIR="/app/logs/permission-service"

# 敏感配置通过环境变量注入，不写进镜像/仓库
# 请把下面这些值改成你的真实凭据，或者用 --env-file / .env 文件管理
DB_HOST="124.222.192.3"
DB_NAME="2026mysql_ds"
DB_USERNAME="root"
DB_PASSWORD="8ik9ol000"
REDIS_HOST="124.222.192.3"
REDIS_PASSWORD=""
REDIS_DB="6"

cd "$PROJECT_DIR" || exit 1

echo ">>> 构建镜像..."
docker build -t "${IMAGE_NAME}:latest" .

echo ">>> 停止并移除旧容器..."
docker rm -f "$CONTAINER_NAME" 2>/dev/null

echo ">>> 准备宿主机日志目录..."
mkdir -p "$HOST_LOG_DIR"

echo ">>> 启动新容器..."
docker run -d \
  -p "$PORT" \
  --name "$CONTAINER_NAME" \
  -e DB_HOST="$DB_HOST" \
  -e DB_NAME="$DB_NAME" \
  -e DB_USERNAME="$DB_USERNAME" \
  -e DB_PASSWORD="$DB_PASSWORD" \
  -e REDIS_HOST="$REDIS_HOST" \
  -e REDIS_PASSWORD="$REDIS_PASSWORD" \
  -e REDIS_DB="$REDIS_DB" \
  -v "${HOST_LOG_DIR}:${CONTAINER_LOG_DIR}" \
  "${IMAGE_NAME}:latest"

echo ">>> 查看启动日志..."
sleep 2
docker logs --tail 30 "$CONTAINER_NAME"

echo ">>> 实时日志请看宿主机目录: ${HOST_LOG_DIR}"
echo ">>> tail -f ${HOST_LOG_DIR}/permission-service.log"
echo ">>> 部署完成！"

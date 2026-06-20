#!/bin/bash

PROJECT_DIR="/home/projects/demo/permission-system"
IMAGE_NAME="permission-system"
CONTAINER_NAME="permission-system"
PORT="16399:13690"
XXL_JOB_PORT="9998:9998"

# 宿主机日志目录（会挂载进容器，容器重建日志不丢）
HOST_LOG_DIR="${PROJECT_DIR}/logs"
# 容器内日志路径，必须和 logback-spring.xml 里的默认值一致
CONTAINER_LOG_DIR="/app/logs/permission-service"

# ============ 敏感配置通过环境变量注入，不写进镜像/仓库 ============

# ---- 数据库 ----
DB_HOST="124.222.192.3"
DB_PORT="3306"
DB_NAME="2026mysql_ds"
DB_USERNAME="root"
DB_PASSWORD="8ik9ol000"

# ---- Redis ----
REDIS_HOST="124.222.192.3"
REDIS_PORT="6379"
REDIS_PASSWORD=""
REDIS_DB="6"

# ---- 邮箱（163 SMTP）----
MAIL_HOST="smtp.163.com"
MAIL_USERNAME="tingqq7z@163.com"
MAIL_PASSWORD="QCciCnaKHLGiMePg"

# ---- XXL-Job ----
XXL_JOB_ADMIN_ADDRESS="http://124.222.192.3:8080/xxl-job-admin"
XXL_JOB_APP_NAME="permission-job-executor"
XXL_JOB_EXECUTOR_PORT="9998"
XXL_JOB_ACCESS_TOKEN="default_token"

# ---- 阿里云 OSS ----
OSS_ENDPOINT="oss-cn-shanghai.aliyuncs.com"
OSS_ACCESS_KEY_ID="LTAI5t7qLYjB6fUTzstewkP2"
OSS_ACCESS_KEY_SECRET="IvA8iA5mhJy5kCYbGoB2QmCU4YhnbK"
OSS_BUCKET_NAME="oss-inshanghai"
OSS_URL_PREFIX="https://oss-inshanghai.oss-cn-shanghai.aliyuncs.com"

# ================================================================

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
  -p "$XXL_JOB_PORT" \
  --name "$CONTAINER_NAME" \
  -e DB_HOST="$DB_HOST" \
  -e DB_PORT="$DB_PORT" \
  -e DB_NAME="$DB_NAME" \
  -e DB_USERNAME="$DB_USERNAME" \
  -e DB_PASSWORD="$DB_PASSWORD" \
  -e REDIS_HOST="$REDIS_HOST" \
  -e REDIS_PORT="$REDIS_PORT" \
  -e REDIS_PASSWORD="$REDIS_PASSWORD" \
  -e REDIS_DB="$REDIS_DB" \
  -e MAIL_HOST="$MAIL_HOST" \
  -e MAIL_USERNAME="$MAIL_USERNAME" \
  -e MAIL_PASSWORD="$MAIL_PASSWORD" \
  -e XXL_JOB_ADMIN_ADDRESS="$XXL_JOB_ADMIN_ADDRESS" \
  -e XXL_JOB_APP_NAME="$XXL_JOB_APP_NAME" \
  -e XXL_JOB_EXECUTOR_PORT="$XXL_JOB_EXECUTOR_PORT" \
  -e XXL_JOB_ACCESS_TOKEN="$XXL_JOB_ACCESS_TOKEN" \
  -e OSS_ENDPOINT="$OSS_ENDPOINT" \
  -e OSS_ACCESS_KEY_ID="$OSS_ACCESS_KEY_ID" \
  -e OSS_ACCESS_KEY_SECRET="$OSS_ACCESS_KEY_SECRET" \
  -e OSS_BUCKET_NAME="$OSS_BUCKET_NAME" \
  -e OSS_URL_PREFIX="$OSS_URL_PREFIX" \
  -v "${HOST_LOG_DIR}:${CONTAINER_LOG_DIR}" \
  "${IMAGE_NAME}:latest"

echo ">>> 查看启动日志..."
sleep 2
docker logs --tail 30 "$CONTAINER_NAME"

echo ">>> 实时日志请看宿主机目录: ${HOST_LOG_DIR}"
echo ">>> tail -f ${HOST_LOG_DIR}/permission-service.log"
echo ">>> 部署完成！"

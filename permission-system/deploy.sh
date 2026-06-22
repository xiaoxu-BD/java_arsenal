#!/bin/bash

PROJECT_DIR="/home/projects/demo/permission-system"
IMAGE_NAME="permission-system"
CONTAINER_NAME="permission-system"
PORT="16399:13690"
XXL_JOB_PORT="9998:9998"
DEBUG_PORT="5005:5005"

# 宿主机日志目录（会挂载进容器，容器重建日志不丢）
HOST_LOG_DIR="${PROJECT_DIR}/logs"
# 容器内日志路径，必须和 logback-spring.xml 里的默认值一致
CONTAINER_LOG_DIR="/app/logs/permission-service"

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
  -p "$DEBUG_PORT" \
  --name "$CONTAINER_NAME" \
  --env-file .env \
  -v "${HOST_LOG_DIR}:${CONTAINER_LOG_DIR}" \
  "${IMAGE_NAME}:latest"

echo ">>> 查看启动日志..."
sleep 2
docker logs --tail 30 "$CONTAINER_NAME"

echo ">>> 实时日志请看宿主机目录: ${HOST_LOG_DIR}"
echo ">>> tail -f ${HOST_LOG_DIR}/permission-service.log"
echo ">>> 部署完成！"

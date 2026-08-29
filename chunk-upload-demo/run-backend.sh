#!/usr/bin/env bash
# 启动 chunk-upload-demo 后端
# 用法: bash chunk-upload-demo/run-backend.sh
# 敏感配置写在 chunk-upload-demo/.env（模板见 .env.example），改完重启本脚本即生效
set -euo pipefail

MODULE_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(dirname "$MODULE_DIR")"

if [ ! -f "$MODULE_DIR/.env" ]; then
  echo "未找到 $MODULE_DIR/.env"
  echo "先复制模板并填入真实值: cp chunk-upload-demo/.env.example chunk-upload-demo/.env"
  exit 1
fi

# 把 .env 里的 KEY=VALUE 导出为环境变量（set -a 使 source 进来的变量自动 export）
set -a
# shellcheck disable=SC1091
source "$MODULE_DIR/.env"
set +a

# 本机默认 JAVA_HOME 指向 jdk8，编译 Java 17 前先覆盖（换机器时改这里）
export JAVA_HOME=/d/dev/jdks/jdk17/jdk-17.0.12/jdk-17.0.12

exec mvn -f "$ROOT_DIR/pom.xml" spring-boot:run -pl chunk-upload-demo "$@"

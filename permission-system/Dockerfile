FROM eclipse-temurin:17-jre

# 设置时区为东八区，避免容器内时间比国内少 8 小时
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

WORKDIR /app
COPY target/permission-system-1.0.jar app.jar
EXPOSE 13690
ENTRYPOINT ["java", "-jar", "app.jar"]

ARG BUILDER_IMAGE=maven:3.9.14-eclipse-temurin-8-noble
ARG RUNTIME_IMAGE=eclipse-temurin:8-jre

# 构建阶段
FROM ${BUILDER_IMAGE} AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B clean package -DskipTests

# 运行阶段
FROM ${RUNTIME_IMAGE}
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8123
ENTRYPOINT ["java", "-jar", "app.jar"]

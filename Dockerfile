ARG BUILDER_IMAGE=m.daocloud.io/docker.io/library/maven:3.9.6-eclipse-temurin-8
ARG RUNTIME_IMAGE=m.daocloud.io/docker.io/library/eclipse-temurin:8-jre

# 构建阶段
FROM ${BUILDER_IMAGE} AS builder
WORKDIR /app
COPY pom.xml ./
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# 运行阶段
FROM ${RUNTIME_IMAGE}
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8123
ENTRYPOINT ["java", "-jar", "app.jar"]

FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /build

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw mvnw
COPY mvnw.cmd mvnw.cmd
COPY src src

RUN mvn -Dmaven.test.skip=true clean package

FROM eclipse-temurin:21-jre
WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*

COPY --from=builder /build/target/review-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8087
ENTRYPOINT ["java", "-jar", "app.jar"]

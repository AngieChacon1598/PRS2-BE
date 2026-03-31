FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE ${SERVER_PORT}

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:+UseG1GC -Xms32m -Xmx128m -XX:MaxRAMPercentage=75 -XX:+DisableExplicitGC -XX:+OptimizeStringConcat -Djava.security.egd=file:/dev/./urandom -Dspring.jmx.enabled=false"

ENTRYPOINT sh -c "java $JAVA_OPTS -jar app.jar"
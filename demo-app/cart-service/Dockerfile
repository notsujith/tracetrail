# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# Agent download stage
FROM eclipse-temurin:21-jre AS agent
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar /otel/agent.jar

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /src/target/*.jar app.jar
COPY --from=agent /otel/agent.jar /otel/agent.jar
EXPOSE 8080
ENV JAVA_OPTS="-javaagent:/otel/agent.jar"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
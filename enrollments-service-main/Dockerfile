# =========================================================================
# Multi-stage Dockerfile for enrollments-service (Java 21 / Spring Boot 3)
# Systems Architecture - UPTC Distributed Systems Lab 3
# =========================================================================

# Stage 1: Build the application with Maven and Eclipse Temurin JDK 21
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Install Maven
RUN apk add --no-cache maven

# Cache dependencies layer
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copy project source
COPY src ./src

# Compile and package production JAR
RUN mvn clean package -DskipTests

# Stage 2: Create lightweight runtime container
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Non-root user for security best practices
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser:appgroup

# Copy executable jar from builder stage
COPY --from=builder /app/target/enrollments-service-1.0.0.jar app.jar

# Expose microservice HTTP port
EXPOSE 8083

# Healthcheck configuration using wget against actuator endpoint
HEALTHCHECK --interval=15s --timeout=5s --retries=3 --start-period=30s \
  CMD wget --quiet --tries=1 --spider http://localhost:8083/actuator/health || exit 1

# Production JVM container flags
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]

# syntax=docker/dockerfile:1

# ---------- Etapa 1: build ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copiamos primero solo lo necesario para resolver dependencias
# (mejora el cache de capas: si el pom no cambia, no se vuelve a descargar nada)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw && ./mvnw -q -B dependency:go-offline

# Ahora copiamos el código fuente y compilamos
COPY src src
RUN ./mvnw -q -B clean package -DskipTests

# ---------- Etapa 2: runtime ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Usuario no root (buena práctica de seguridad)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD wget -q --spider http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
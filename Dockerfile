# =============================================================
# Etapa 1: Build (compila con Maven oficial en JDK 21)
# =============================================================
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copia el POM y descarga dependencias (cache layer de Docker)
COPY pom.xml ./
RUN mvn -B dependency:go-offline

# Copia el código fuente y genera el jar de producción
COPY src ./src
RUN mvn -B clean package -DskipTests

# =============================================================
# Etapa 2: Runtime (imagen minimal ligera con JRE 21)
# =============================================================
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Usuario no root por buenas prácticas de seguridad
RUN addgroup -S app && adduser -S app -G app
USER app

# Expone el puerto por defecto de la aplicación
EXPOSE 8080

# Copia el artefacto compilado desde la etapa de build
COPY --from=build /app/target/*.jar app.jar

# Healthcheck continuo usando Spring Boot Actuator
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]

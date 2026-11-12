# =============================================================
# Etapa 1: Build (compila y ejecuta test + coverage con Maven)
# =============================================================
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copia el POM y descarga dependencias (cache layer)
COPY pom.xml ./
COPY .mvn .mvn
COPY mvnw ./
RUN mvn -B dependency:go-offline

# Copia el código fuente y compila el jar
COPY src ./src
RUN mvn -B clean package -DskipTests

# =============================================================
# Etapa 2: Runtime (imagen minimal con JRE)
# =============================================================
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Usuario no root por seguridad
RUN addgroup -S app && adduser -S app -G app
USER app

# Expone el puerto de la aplicación
EXPOSE 8080

# Copia el jar generado
COPY --from=build /app/target/*.jar app.jar

# Healthcheck usando el endpoint de Actuator
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]

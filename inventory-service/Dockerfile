# ============================================================================
# 🐋 Multi-stage Build: inventory-service (Spring Boot)
# Basado en las directrices de diseño SOA del docente Narro Andrade
# ============================================================================

# Stage 1: Compilar con Maven y Java 21
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
# Descarga dependencias de forma offline (aprovecha la cache de Docker)
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -DskipTests -q

# Stage 2: Imagen liviana de ejecución (JRE de Java 21 Alpine)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]

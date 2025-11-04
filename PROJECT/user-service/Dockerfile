# Multi-stage build pentru Spring Boot
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copiază pom.xml și descarcă dependențele (cache layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiază codul sursă și construiește aplicația
COPY src ./src
RUN mvn clean package -DskipTests

# Stage final - imagine runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Instalează netcat pentru health checks
RUN apk add --no-cache netcat-openbsd

# Copiază JAR-ul construït
COPY --from=build /app/target/*.jar app.jar

# Expune portul aplicației
EXPOSE 8081

# Rulează aplicația
ENTRYPOINT ["java", "-jar", "app.jar"]


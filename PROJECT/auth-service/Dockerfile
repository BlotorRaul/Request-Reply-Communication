# Multi-stage build pentru Spring Boot
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copiaza pom.xml si descarca dependentele (cache layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiaza codul sursa si construieste aplicatia
COPY src ./src
RUN mvn clean package -DskipTests

# Stage final - imagine runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Instaleaza wget pentru health checks
RUN apk add --no-cache wget

# Copiaza JAR-ul construit
COPY --from=build /app/target/*.jar app.jar

# Expune portul aplicatiei
EXPOSE 8083

# Ruleaza aplicatia
ENTRYPOINT ["java", "-jar", "app.jar"]


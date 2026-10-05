# Etapa 1: compilación del WAR
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

# Primero solo el pom: las dependencias quedan en una capa propia y no se
# vuelven a descargar mientras el pom no cambie.
COPY pom.xml ./
RUN mvn -B -q dependency:go-offline

COPY src ./src

# Los tests y el análisis de Sonar corren en el pipeline (`./mvnw clean verify
# sonar:sonar`), no al construir la imagen.
RUN mvn -B -q package -DskipTests

# Etapa 2: runtime en Tomcat externo
#
# Spring Boot 4 está sobre Jakarta EE 11 (Servlet 6.1): necesita Tomcat 11.
# Tomcat 10.x no carga la aplicación.
FROM tomcat:11.0-jdk21-temurin

# Se despliega como ROOT.war para servir en `/` y no en `/estudiante-0.0.1-SNAPSHOT`.
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /build/target/estudiante-*.war /usr/local/tomcat/webapps/ROOT.war

# La conexión a PostgreSQL llega por variables de entorno (DB_HOST, DB_PORT,
# DB_NAME, DB_USER, DB_PASSWORD); ver application.properties.
# DB_PASSWORD no tiene valor por defecto: hay que pasarla siempre.
ENV CATALINA_OPTS="-XX:MaxRAMPercentage=75 -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

CMD ["catalina.sh", "run"]

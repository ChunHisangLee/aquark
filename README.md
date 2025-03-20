# Aquark Project - Help and Setup Guide

This document provides instructions for setting up, configuring, and running the Aquark application. It covers
prerequisites, configuration details, Docker setup, and API documentation.

---

## 1. Overview

Aquark is a Spring Boot application built with Java 21 that leverages PostgreSQL for data persistence, Redis for
caching, Kafka for event streaming, and Flyway for database migrations. It also uses Springdoc OpenAPI for API
documentation. The application supports scheduled tasks and caching, and it is containerized with Docker.

---

## 2. Prerequisites

Before running the application, ensure you have the following installed:

- **Java JDK 21** (or later)
- **Apache Maven 3.9.8** (or later)
- **PostgreSQL 15+**
- **Redis**
- **Docker & Docker Compose** (optional, for containerized setup)
- **Kafka & Zookeeper** (or use Docker Compose to run them)

---

## 3. Configuration

### 3.1 application.yml

The application is configured via an `application.yml` file. Key sections include:

- **Server & Port:**
  ```yaml
  server:
    port: 8080
  ```

- **DataSource (PostgreSQL):** Uses environment variables with defaults for local and Docker profiles.
  ```yaml
  spring:
    datasource:
      url: jdbc:postgresql://localhost:5432/postgres
      username: postgres
      password: Ab123456
      driver-class-name: org.postgresql.Driver
      hikari:
        auto-commit: true
  ```

- **JPA & Hibernate:** Automatically update schema with ddl-auto: update and show SQL statements.
  ```yaml
  spring:
    jpa:
      hibernate:
        ddl-auto: update
      show-sql: true
      properties:
        hibernate:
          dialect: org.hibernate.dialect.PostgreSQLDialect
  ```
- **Redis:** Configured to connect to a Redis instance.
  ```yaml
  spring:
    data:
      redis:
        host: ${SPRING_REDIS_HOST:localhost}
        port: ${SPRING_REDIS_PORT:6379}
        password: ${SPRING_REDIS_PASSWORD:}
  ```
- **Kafka:** Uses the following setting to connect to your Kafka broker.
  ```yaml
  spring:
    kafka:
      bootstrap-servers: ${SPRING_KAFKA_BOOTSTRAP_SERVERS:kafka:9092}
  ```
- **Swagger/OpenAPI:** Custom paths for API documentation.
  ```yaml
  springdoc:
    api-docs:
      path: /api-docs
    swagger-ui:
      path: /swagger-ui-custom.html
  ```
- **Scheduling:**A cron expression is set for scheduled tasks.
  ```yaml
  scheduling:
    cron: "0 5/1 * * * ?"
  ```
- **API URLs:** The external API endpoints for fetching raw sensor data are defined:
  ```yaml
    api:
      urls:
        - "https://app.aquark.com.tw/api/raw/Angle2024/240627"
        - "https://app.aquark.com.tw/api/raw/Angle2024/240706"
        - "https://app.aquark.com.tw/api/raw/Angle2024/240708"
        - "https://app.aquark.com.tw/api/raw/Angle2024/240709"
        - "https://app.aquark.com.tw/api/raw/Angle2024/240710"
  ```

### 3.2 Maven (pom.xml)

Key dependencies in your pom.xml include:

- **Spring Boot Starters:**

  For web, data JPA, caching, validation, and actuator.
- **PostgreSQL Driver:**

  For connecting to PostgreSQL.
- **Spring Data Redis & Kafka:**

  To support caching with Redis and event streaming with Kafka.
- **Flyway:**

  For database migrations.
- **Springdoc OpenAPI Starter:**

  For API documentation.

Ensure your dependencies are up to date, and if using PostgreSQL 15, verify Flyway supports that version (consider
upgrading Flyway if needed).

---

## 4. Database Migration with Flyway

Flyway is auto-configured by Spring Boot if it’s on the classpath. Migration scripts should be placed in:

  ```css
src /main/ resources /db/ migration
  ```

Example migration script file:

- V1__Initial_setup.sql

  ```sql

CREATE TABLE alarm_threshold
(
id BIGSERIAL PRIMARY KEY,
station_id VARCHAR(50)    NOT NULL,
csq VARCHAR(20)    NOT NULL,
parameter VARCHAR(50)    NOT NULL,
threshold_value NUMERIC(19, 4) NOT NULL,
CONSTRAINT unique_threshold UNIQUE (station_id, csq, parameter)
);

  ```

---

## 5. Docker & Docker Compose Setup

### 5.1 Dockerfile

Your Dockerfile builds the application image:

  ```dockerfile
FROM openjdk:21-jdk-slim
LABEL maintainer="jack"
VOLUME /tmp
COPY target/aquark-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
  ```

### 5.2 docker-compose.yml

The Docker Compose file sets up containers for the application, PostgreSQL, Redis, Kafka, and Zookeeper:

  ```yaml
services:
  db:
    image: postgres:15
    ports:
      - "5432:5432"
    environment:
      POSTGRES_DB: postgres
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: Ab123456
    networks:
      - assignment-network
    healthcheck:
      test: [ "CMD", "pg_isready", "-U", "postgres" ]
      interval: 10s
      timeout: 5s
      retries: 5

  redis:
    image: redis:latest
    command: [ "redis-server", "--requirepass", "Ab123456" ]
    ports:
      - "6379:6379"
    networks:
      - assignment-network
    healthcheck:
      test: [ "CMD", "redis-cli", "-a", "Ab123456", "ping" ]
      interval: 10s
      timeout: 5s
      retries: 5

  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    ports:
      - "2181:2181"
    networks:
      - assignment-network

  kafka:
    image: confluentinc/cp-kafka:latest
    depends_on:
      - zookeeper
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    ports:
      - "9092:9092"
    networks:
      - assignment-network

  app:
    build:
      context: .
      dockerfile: Dockerfile
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/postgres
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: Ab123456
      SPRING_REDIS_HOST: redis
      SPRING_REDIS_PORT: 6379
      SPRING_REDIS_PASSWORD: Ab123456
      SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092
      SPRING_PROFILES_ACTIVE: docker
    depends_on:
      db:
        condition: service_healthy
      redis:
        condition: service_healthy
    networks:
      - assignment-network

networks:
  assignment-network:
    driver: bridge
  ```

Run the stack with:

 ``` bash
    docker-compose up --build
 ```

---

## 6. Running the Application

### 6.1 Local Development

Run the application locally using Maven:

 ``` bash
    mvn spring-boot:run
 ```

Or build the JAR and execute it:

 ``` bash
    mvn clean package
    java -jar target/aquark-0.0.1-SNAPSHOT.jar
 ```

### 6.2 Accessing the Application

- **API Endpoints:**

  The application listens on port 8080.

- **Swagger UI:**

  Access the API documentation at:

  http://localhost:8080/swagger-ui-custom.html

---

## 7. Additional Features

### 7.1 Kafka Integration

- Producer Configuration:
  Kafka is configured in KafkaConfig.java. Ensure Kafka is running (or use the Docker Compose setup) to enable event
  notifications.

### 7.2 Caching with Redis

- Redis Cache:
  Caching is enabled via @EnableCaching in the main application class. Redis properties are defined in the
  application.yml file and overridden by environment variables in Docker Compose.

### 7.3 Database Migrations

- Flyway:
  Flyway automatically runs migration scripts from src/main/resources/db/migration at startup. Ensure the Flyway version
  supports your PostgreSQL version.

---

## 8. Troubleshooting

- Bean Creation Issues:
  Ensure component scanning covers your services, repositories, and controllers.

- Database Connection:
  Verify PostgreSQL credentials and URL in your application.yml and Docker Compose environment.

- Redis & Kafka:
  Ensure Redis and Kafka are running and accessible by the application.

- Flyway Migrations:
  If migration errors occur, check your Flyway version compatibility with your PostgreSQL version and review migration
  scripts.

---

## 9. Further Reading

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)

- [Flyway Documentation](https://flywaydb.org/documentation/)

- [Spring Data JPA Documentation](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)

- [Spring Kafka Documentation](https://spring.io/projects/spring-kafka)

- [Spring Data Redis Documentation](https://docs.spring.io/spring-data/redis/docs/current/reference/html/)

This document should help you get started with the Aquark project and assist with troubleshooting and further
development.

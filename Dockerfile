# Dockerfile
FROM openjdk:21-jdk-slim

# Maintainer information
LABEL maintainer="jack"

VOLUME /tmp

# Copy the Maven build artifact from the target directory to the container
COPY target/aquark-0.0.1-SNAPSHOT.jar app.jar

# Expose the port the application will run on
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java","-jar","/app.jar"]

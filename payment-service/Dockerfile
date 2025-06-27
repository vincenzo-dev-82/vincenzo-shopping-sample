FROM openjdk:17-jdk-slim

WORKDIR /app

COPY build/libs/*.jar app.jar

EXPOSE 8084 9094

ENTRYPOINT ["java", "-jar", "app.jar"]

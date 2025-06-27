FROM openjdk:17-jdk-slim

WORKDIR /app

COPY build/libs/*.jar app.jar

EXPOSE 8082 9092

ENTRYPOINT ["java", "-jar", "app.jar"]

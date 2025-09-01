FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/userservice-1.0-SNAPSHOT.jar app.jar

EXPOSE 4322

CMD ["java", "-jar", "app.jar"]
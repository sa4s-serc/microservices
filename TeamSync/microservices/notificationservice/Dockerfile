FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/notificationservice-1.0-SNAPSHOT.jar app.jar

EXPOSE 4323

CMD ["java", "-jar", "app.jar"]
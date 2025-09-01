FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/taskservice-1.0-SNAPSHOT.jar app.jar

EXPOSE 4324

CMD ["java", "-jar", "app.jar"]
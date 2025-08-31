FROM openjdk:8-jre-alpine

RUN apk add --no-cache curl

RUN apk add --no-cache curl
MAINTAINER Alexander Lukyanchikov <sqshq@sqshq.com>

ADD ./target/notification-service.jar /app/
CMD ["java", "-Xmx200m", "-jar", "/app/notification-service.jar"]

EXPOSE 8000
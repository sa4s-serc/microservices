FROM openjdk:8-jre-alpine

RUN apk add --no-cache curl

RUN apk add --no-cache curl
MAINTAINER Alexander Lukyanchikov <sqshq@sqshq.com>

ADD ./target/auth-service.jar /app/
CMD ["java", "-Xmx200m", "-jar", "/app/auth-service.jar"]

EXPOSE 5000
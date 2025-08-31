FROM openjdk:8-jre-alpine

RUN apk add --no-cache curl

RUN apk add --no-cache curl
MAINTAINER Alexander Lukyanchikov <sqshq@sqshq.com>

ADD ./target/registry.jar /app/
CMD ["java", "-Xmx200m", "-jar", "/app/registry.jar"]

EXPOSE 8761
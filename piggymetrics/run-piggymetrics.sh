#!/bin/bash

# PiggyMetrics Management Script

# Set required environment variables
export JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-amd64

case "$1" in
  "build")
    echo "Building PiggyMetrics services..."
    mvn package -DskipTests
    ;;
  "start")
    echo "Starting PiggyMetrics microservices..."
    docker-compose up -d --build
    echo ""
    echo "PiggyMetrics is starting up. Please wait ~2 minutes for all services to be ready."
    echo ""
    echo "Available endpoints:"
    echo "  Main App:         http://localhost:8080"
    echo "  Eureka Registry:  http://localhost:8761"
    echo "  Hystrix Monitor:  http://localhost:9000/hystrix"
    echo "  RabbitMQ:         http://localhost:15672 (guest/guest)"
    ;;
  "stop")
    echo "Stopping PiggyMetrics system..."
    docker-compose down
    ;;
  "status")
    echo "PiggyMetrics system status:"
    docker-compose ps
    ;;
  "logs")
    if [ -z "$2" ]; then
      echo "Available services: config, registry, gateway, auth-service, account-service, statistics-service, notification-service, monitoring, turbine-stream-service"
      echo "Usage: $0 logs <service>"
    else
      echo "Showing logs for service: $2"
      docker-compose logs -f $2
    fi
    ;;
  "rebuild")
    echo "Rebuilding and restarting PiggyMetrics..."
    docker-compose down
    mvn package -DskipTests
    docker-compose up -d --build
    echo ""
    echo "PiggyMetrics is starting up. Please wait ~2 minutes for all services to be ready."
    echo ""
    echo "Available endpoints:"
    echo "  Main App:         http://localhost:8080"
    echo "  Eureka Registry:  http://localhost:8761"
    echo "  Hystrix Monitor:  http://localhost:9000/hystrix"
    echo "  RabbitMQ:         http://localhost:15672 (guest/guest)"
    ;;
  *)
    echo "PiggyMetrics Management Script"
    echo "Usage: $0 {build|start|stop|status|logs|rebuild}"
    echo ""
    echo "Commands:"
    echo "  build          - Build Java services with Maven"
    echo "  start          - Start all PiggyMetrics services"
    echo "  stop           - Stop all services"
    echo "  status         - Show service status"
    echo "  logs <service> - Show logs for a specific service"
    echo "  rebuild        - Full rebuild and restart"
    echo ""
    echo "Examples:"
    echo "  $0 start"
    echo "  $0 status"
    echo "  $0 logs gateway"
    ;;
esac
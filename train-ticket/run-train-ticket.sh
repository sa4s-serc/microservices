#!/bin/bash

# Train Ticket Management Script

# Set required environment variables
export JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-amd64
export IMG_REPO=codewisdom
export IMG_TAG=latest

case "$1" in
  "build")
    echo "Building Java services..."
    mvn clean package -Dmaven.test.skip=true
    ;;
  "start")
    echo "Starting Train Ticket system (core services)..."
    docker-compose -f docker-compose-simple.yml up -d --build
    ;;
  "start-full")
    echo "Starting full Train Ticket system (all ~40 services)..."
    docker-compose up -d --build
    ;;
  "stop")
    echo "Stopping Train Ticket system..."
    if [ "$2" = "full" ]; then
      docker-compose down
    else
      docker-compose -f docker-compose-simple.yml down
    fi
    ;;
  "status")
    echo "Train Ticket system status:"
    if [ "$2" = "full" ]; then
      docker-compose ps
    else
      docker-compose -f docker-compose-simple.yml ps
    fi
    ;;
  "logs")
    echo "Showing logs for service: $2"
    if [ "$3" = "full" ]; then
      docker-compose logs -f $2
    else
      docker-compose -f docker-compose-simple.yml logs -f $2
    fi
    ;;
  "rebuild")
    echo "Rebuilding and restarting..."
    if [ "$2" = "full" ]; then
      docker-compose down
      mvn clean package -Dmaven.test.skip=true
      docker-compose up -d --build
    else
      docker-compose -f docker-compose-simple.yml down
      mvn clean package -Dmaven.test.skip=true
      docker-compose -f docker-compose-simple.yml up -d --build
    fi
    ;;
  *)
    echo "Usage: $0 {build|start|start-full|stop|status|logs <service>|rebuild}"
    echo ""
    echo "Commands:"
    echo "  build      - Build Java services with Maven"
    echo "  start      - Start core Train Ticket services"
    echo "  start-full - Start all Train Ticket services"
    echo "  stop       - Stop all services"
    echo "  status     - Show service status"
    echo "  logs       - Show logs for a specific service"
    echo "  rebuild    - Full rebuild and restart"
    echo ""
    echo "Examples:"
    echo "  $0 start"
    echo "  $0 logs ts-ui-dashboard"
    echo "  $0 status"
    ;;
esac
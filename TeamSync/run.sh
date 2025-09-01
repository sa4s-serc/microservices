#!/bin/bash

# TeamSync Management Script

case "$1" in
  "build")
    echo "Building Java microservices..."
    for service in userservice courseservice taskservice notificationservice; do
      echo "Building $service..."
      cd microservices/$service
      mvn clean package -DskipTests
      cd ../..
    done
    ;;
  "start")
    echo "Starting TeamSync system..."
    docker-compose up -d --build
    ;;
  "stop")
    echo "Stopping TeamSync system..."
    docker-compose down
    ;;
  "status")
    echo "TeamSync system status:"
    docker-compose ps
    ;;
  "logs")
    echo "Showing logs for service: $2"
    docker-compose logs -f $2
    ;;
  "init")
    echo "Initializing database tables..."
    echo "Waiting for services to start..."
    sleep 30
    curl -X POST http://localhost:4321/api/courses/init
    curl -X POST http://localhost:4322/api/users/init
    curl -X POST http://localhost:4323/api/notifications/init
    curl -X POST http://localhost:4324/api/tasks/init
    ;;
  "rebuild")
    echo "Rebuilding and restarting..."
    docker-compose down
    ./run.sh build
    docker-compose up -d --build
    ;;
  *)
    echo "Usage: $0 {build|start|stop|status|logs <service>|init|rebuild}"
    echo ""
    echo "Commands:"
    echo "  build   - Build Java microservices with Maven"
    echo "  start   - Start all TeamSync services"
    echo "  stop    - Stop all services"
    echo "  status  - Show service status"
    echo "  logs    - Show logs for a specific service"
    echo "  init    - Initialize database tables"
    echo "  rebuild - Full rebuild and restart"
    echo ""
    echo "Examples:"
    echo "  $0 build"
    echo "  $0 start"
    echo "  $0 logs userservice"
    echo "  $0 init"
    ;;
esac
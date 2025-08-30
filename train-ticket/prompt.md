I need you to generate a complete microservice implementation in one go based on the following:

REQUIREMENTS DOCUMENT: VERY IMPORTANT: Look at wiki.md for the entire architecture and requirements.

TARGET MICROSERVICE TO GENERATE: MICROSERVICE: ts-cancel-service

  FUNCTIONAL SCOPE:
  - Handles ticket cancellation operations for train reservations
  - Calculates refund amounts based on cancellation timing and business rules
  - Processes refund transactions through internal payment system
  - Manages order status updates for cancelled tickets
  - Sends email notifications for successful cancellations (optional feature)

  KEY FEATURES IMPLEMENTED:
  - Ticket cancellation with automatic refund calculation
  - Support for both high-speed trains (G/D series) and other trains (Z/K/Other series)
  - Time-based refund calculation with 80% refund rate for valid cancellations
  - Integration with internal payment system for money drawback
  - Order status validation (only NOTPAID, PAID, and CHANGE status orders can be cancelled)
  - Email notification system for successful cancellations

  API RESPONSIBILITIES:
  - GET /api/v1/cancelservice/cancel/refound/{orderId} - Calculate refund amount for an order
  - GET /api/v1/cancelservice/cancel/{orderId}/{loginId} - Cancel a ticket and process refund
  - GET /api/v1/cancelservice/welcome - Health check endpoint

  DATA RESPONSIBILITIES:
  - Does not maintain its own database - operates as a stateless orchestration service
  - Validates order status before cancellation (NOTPAID, PAID, CHANGE are allowed)
  - Calculates refund amounts based on business rules:
    - NOTPAID orders: 0% refund
    - PAID orders cancelled before departure: 80% refund
    - PAID orders cancelled after departure: 0% refund
  - Updates order status to CANCEL in appropriate order service

  INTEGRATIONS:
  - ts-order-service: Retrieves and updates G/D series train orders
  - ts-order-other-service: Retrieves and updates non-G/D train orders
  - ts-inside-payment-service: Processes refund payments to user accounts
  - ts-user-service: Retrieves user account information for notifications
  - ts-notification-service: Sends email notifications (optional/commented out)

  TECHNICAL APPROACH:
  - Spring Boot microservice with REST API endpoints
  - Service discovery using Nacos
  - RestTemplate for synchronous inter-service communication
  - Stateless design - no database persistence
  - Authorization header propagation between service calls
  - Error handling with structured response objects
  - Comprehensive logging for audit and debugging

  IMPLEMENTATION NOTES:
  - Refund calculation uses 80% of original ticket price if cancelled before departure time
  - Supports dual order systems: separate handling for high-speed vs regular trains
  - Email notification feature is implemented but disabled (TODO comment indicates async messaging planned)
  - Uses deprecated Date constructor (flagged with NOSONAR comment)
  - All external service calls include proper authorization header forwarding
  - Graceful error handling with detailed logging for troubleshooting

INSTRUCTIONS:
FIRST: Explore the existing codebase - read through all relevant files to understand:
- What microservices already exist
- Current architecture patterns and tech stack being used
- Existing database schemas, API patterns, and code structure
- How services currently communicate with each other
- What's already implemented vs what's missing
- Analyze the entire requirements document to understand the system context
- Focus specifically on the target microservice description provided above
- Based on your exploration, determine what needs to be built/modified
- Generate a complete microservice that implements ONLY the functionality described in the target microservice
- Ensure the microservice integrates properly with existing services and follows established patterns
- CODE EVERYTHING IN ONE GO - After exploration, generate all files, components, and code at once
- Include all necessary components: API endpoints, data models, business logic, error handling, and security considerations

OUTPUT FORMAT: Generate the complete microservice code with all files:
- Main application file
- API endpoints/controllers
- Data models and schemas
- Business logic services
- Database configuration
- Error handling and validation
- Security implementation (authentication, authorization)
- Configuration files (requirements.txt, .env templates, etc.)
- Docker files if applicable
- Documentation/README

CONSTRAINTS:
- FIRST STEP: Explore and read the existing codebase thoroughly before coding
- Use the same tech stack, patterns, and conventions as existing services
- Generate only the specified microservice, not the entire system
- Ensure the service follows existing architecture patterns and integrates seamlessly
- Follow the established microservice patterns already in the codebase
- Include proper logging and monitoring hooks consistent with existing services
- Make the service production-ready and consistent with current deployment patterns
- Provide complete, runnable code that fits the existing ecosystem

IMPORTANT:
- Start by exploring the existing codebase - read files, understand patterns, see what's already built
- Then code the entire microservice implementation based on your findings
- Don't ask questions about tech stack - use what's already established in the project
- Follow existing patterns and integrate seamlessly with current architecture
- AFTER you have given code at the very end, try running the system: Run ./run-train-ticket.sh build to compile the Java services, then ./run-train-ticket.sh start-full to launch all 40+ microservices. You may use
  ./run-train-ticket.sh status full to check running services and ./run-train-ticket.sh logs <service-name> full to view specific service logs. but you can just try running `cd ts-cancel-service/ && JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-amd64 mvn test` to see if test cases pass. The system uses Docker Compose to orchestrate the complete microservices
  architecture. and see if any errors persist, fix them if they do.

Your extensive experience with enterprise microservices and transaction processing systems makes you uniquely qualified to build this critical service. Please leverage your expertise to deliver production-ready code that seamlessly integrates with the existing architecture.

I need you to generate a complete microservice implementation in one go based on the following:

REQUIREMENTS DOCUMENT: VERY IMPORTANT: Look at wiki.md for the entire architecture and requirements.

TARGET MICROSERVICE TO GENERATE: MICROSERVICE: ts-order-other-service

  FUNCTIONAL SCOPE:
  - Manages orders for trains with IDs that do NOT start with "G" or "D" (non-high-speed trains)
  - Provides comprehensive order lifecycle management from creation to completion
  - Handles ticket inventory tracking and seat allocation coordination
  - Supports both customer-facing and admin operations

  KEY FEATURES IMPLEMENTED:
  - Order creation and validation with duplicate prevention
  - Order querying with flexible filtering (by date ranges, status, account)
  - Order modification and status updates throughout lifecycle
  - Payment processing and order completion
  - Security checks for fraud prevention (time-based and volume-based)
  - Sold ticket calculation and inventory management
  - Admin order management capabilities

  API RESPONSIBILITIES:
  - GET sold tickets by date/train for inventory management
  - POST order creation with comprehensive validation
  - POST flexible order queries with multi-criteria filtering
  - GET order details, pricing, and payment processing
  - PUT order modifications and admin updates
  - DELETE order removal
  - GET security validation and fraud detection

  DATA RESPONSIBILITIES:
  - Order entity management in "orders_other" table with full lifecycle tracking
  - Business rule: Only handles trains where trainNumber does NOT start with "G" or "D"
  - Order status management (NOTPAID, PAID, COLLECTED, CANCEL, CHANGE, etc.)
  - Seat class categorization (BUSINESS, FIRSTCLASS, SECONDCLASS, HARDSEAT, SOFTSEAT, HARDBED, SOFTBED, HIGHSOFTBED)
  - Price calculation and payment tracking
  - Security metrics: order frequency monitoring and valid order counting

  INTEGRATIONS:
  - ts-station-service: Validates station names and retrieves station name lists
  - Shares order management pattern with ts-order-service (for G/D trains)
  - Database: MySQL with JPA/Hibernate for persistence
  - Service discovery: Nacos integration for microservice communication

  TECHNICAL APPROACH:
  - Spring Boot microservice with REST API
  - Repository pattern with CrudRepository for data access
  - Service layer with comprehensive business logic implementation
  - Controller layer with proper HTTP method mapping and cross-origin support
  - Entity-based data modeling with JPA annotations
  - Comprehensive logging and error handling throughout

  IMPLEMENTATION NOTES:
  - Critical business rule: Train number filtering (NOT starting with G or D)
  - Duplicate order prevention using account-based validation
  - Complex order querying with optional date range and status filtering
  - Security validation includes time-window analysis (1-hour lookback) and valid order counting
  - Order status transitions follow defined business workflow
  - Admin operations separated from customer operations with dedicated endpoints
  - Comprehensive seat class handling for various train types

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
  ./run-train-ticket.sh status full to check running services and ./run-train-ticket.sh logs <service-name> full to view specific service logs, run the tests by running `JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-amd64 mvn test` after `cd ts-order-other-service` after creating the files. The system uses Docker Compose to orchestrate the complete microservices
  architecture. and see if any errors persist, fix them if they do.

Your extensive experience with enterprise microservices and transaction processing systems makes you uniquely qualified to build this critical service. Please leverage your expertise to deliver production-ready code that seamlessly integrates with the existing architecture.
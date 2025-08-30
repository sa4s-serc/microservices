I need you to generate a complete microservice implementation in one go based on the following:

REQUIREMENTS DOCUMENT: VERY IMPORTANT: Look at wiki.md for the entire architecture and requirements.

TARGET MICROSERVICE TO GENERATE: MICROSERVICE: ts-verification-code-service

  FUNCTIONAL SCOPE:
  - Generates visual CAPTCHA verification codes to prevent automated attacks
  - Validates user-submitted verification codes against generated codes
  - Provides stateless verification mechanism for authentication workflows

  KEY FEATURES IMPLEMENTED:
  - Dynamic CAPTCHA image generation with configurable dimensions
  - Alphanumeric verification code creation (A-Z, 0-9)
  - Code validation with case-insensitive matching
  - Cookie-based code tracking with expiration
  - In-memory caching for verification codes with TTL
  - Security hardened with HttpOnly cookies

  API RESPONSIBILITIES:
  - GET /api/v1/verifycode/generate: Generate and return CAPTCHA image with embedded code
  - GET /api/v1/verifycode/verify/{verifyCode}: Validate submitted verification code

  DATA RESPONSIBILITIES:
  - Manages temporary verification code storage in local cache
  - Associates verification codes with unique cookie identifiers
  - Enforces code expiration (1000 seconds TTL)
  - Stores codes in uppercase format for consistency
  - No persistent data storage - fully stateless operation

  INTEGRATIONS:
  - Called BY ts-auth-service for user authentication verification
  - No outbound service dependencies - operates independently
  - Uses shared ts-common library for common utilities and JWT security
  - Integrates with Spring Security for endpoint protection

  TECHNICAL APPROACH:
  - Spring Boot microservice with REST endpoints
  - BufferedImage for dynamic CAPTCHA generation with noise lines
  - Google Guava Cache for in-memory code storage with automatic expiration
  - Cookie-based session correlation without server-side state
  - Security-first design with XSS protection via HttpOnly cookies
  - Service discovery integration via Nacos

  IMPLEMENTATION NOTES:
  - CAPTCHA images use random background colors and interference lines for bot prevention
  - Character set limited to uppercase letters and digits to avoid confusion
  - Fixed 4-character verification codes for consistent UX
  - Cache size limited to 1000 entries to prevent memory exhaustion
  - Verification endpoint always returns true (potential security issue in current implementation)
  - CORS enabled for cross-origin requests with credentials disabled
  - JWT filter applied but verification endpoints are publicly accessible

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
  ./run-train-ticket.sh status full to check running services and ./run-train-ticket.sh logs <service-name> full to view specific service logs. After giving the code verify by running the tests in `cd ts-verification-code-service && JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-amd64 mvn test` with  The system uses Docker Compose to orchestrate the complete microservices
  architecture. and see if any errors persist, fix them if they do.

Your extensive experience with enterprise microservices and transaction processing systems makes you uniquely qualified to build this critical service. Please leverage your expertise to deliver production-ready code that seamlessly integrates with the existing architecture.
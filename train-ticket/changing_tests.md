# Test Investigation and Fixes for Train Ticket Microservices

## Overview
This document details the investigation and fixes applied to mock tests in three Train Ticket microservices that were experiencing failures due to mocking mismatches and technical issues.

## Services Investigated
1. **ts-cancel-service**
2. **ts-order-other-service** 
3. **ts-verification-code-service**

## How to Run Tests

### Prerequisites
- Java 8 (required for compatibility with Spring Boot version used)
- Maven 3.6+

### Running Tests
```bash
# Navigate to service directory
cd /path/to/ts-{service-name}

# Run tests with Java 8
JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-amd64 mvn test

# Or for specific test class
JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-amd64 mvn test -Dtest=ClassName
```

## Test Results Summary

| Service | Initial Status | Final Status | Issues Fixed |
|---------|---------------|--------------|--------------|
| ts-cancel-service | NPEs + URL mismatches | ✅ 10/10 tests pass | 5 technical issues |
| ts-order-other-service | 22 errors + 3 failures | ✅ 51/51 tests pass | 11 technical issues |
| ts-verification-code-service | ✅ Already working | ✅ 4/4 tests pass | No issues |

## Detailed Analysis

### 1. ts-cancel-service

**Initial Issues:**
- **NullPointerException** in tests due to uninitialized Response objects
- **URL mismatch** between hardcoded test URLs and dynamic service discovery

**Root Causes:**
```java
// PROBLEM: Uninitialized Response objects
private Response response = new Response();  // NPE source

// PROBLEM: Hardcoded service URLs with ports in tests
when(restTemplate.exchange("http://ts-order-service:12031/...", ...))
// But actual implementation uses: "http://ts-order-service/..." (no port)
```

**Fixes Applied:**
```java
// FIX 1: Properly initialize Response objects
private Response response = new Response(1, "Success", "test_data");

// FIX 2: Remove hardcoded ports from URLs
when(restTemplate.exchange("http://ts-order-service/...", ...))
```

### 2. ts-order-other-service

**Initial Issues (22 errors + 3 failures):**

#### A. Optional Handling Errors
```java
// PROBLEM: Incorrect Optional mocking
when(orderOtherRepository.findById().get()).thenReturn(order);  // Tries to call .get() on mock
when(orderOtherRepository.findById()).thenReturn(null);         // Returns null instead of Optional

// FIX: Proper Optional mocking
when(orderOtherRepository.findById()).thenReturn(Optional.of(order));
when(orderOtherRepository.findById()).thenReturn(Optional.empty());
```

#### B. NullPointerException Errors
```java
// PROBLEM: Empty objects causing NPEs in business logic
Order order = new Order();  // accountId is null → NPE in service.findByAccountId()

// FIX: Initialize required fields
Order order = new Order();
order.setAccountId("test_account");
order.setBoughtDate("2025-01-01");
order.setTravelDate("2025-01-02");
```

#### C. Controller Test Null Responses
```java
// PROBLEM: Missing loginId in QueryInfo
QueryInfo qi = new QueryInfo();  // loginId is null → controller.getLoginId() fails

// FIX: Set required fields
QueryInfo qi = new QueryInfo();
qi.setLoginId("test_login_id");
```

#### D. Mock Return Value Issues
```java
// PROBLEM: Repository save() returning null when object needed
when(orderOtherRepository.save(any(Order.class))).thenReturn(null);

// FIX: Return the saved object
when(orderOtherRepository.save(any(Order.class))).thenReturn(order);
```

#### E. URL Pattern Mismatches
```java
// PROBLEM: Hardcoded service URLs with ports
"http://ts-station-service:12345/api/v1/stationservice/stations/namelist"

// FIX: Use service discovery pattern (no ports)
"http://ts-station-service/api/v1/stationservice/stations/namelist"
```

### 3. ts-verification-code-service

**Status:** No issues found - all tests pass cleanly.

## Key Patterns Identified

### 1. Optional Handling Anti-Pattern
```java
// ❌ Wrong: Calling .get() on mock
when(repository.findById().get()).thenReturn(entity);

// ✅ Correct: Mock Optional directly
when(repository.findById()).thenReturn(Optional.of(entity));
when(repository.findById()).thenReturn(Optional.empty());
```

### 2. Service URL Patterns
```java
// ❌ Wrong: Hardcoded ports in tests
"http://service-name:12345/endpoint"

// ✅ Correct: Service discovery pattern
"http://service-name/endpoint"
```

### 3. Response Object Initialization
```java
// ❌ Wrong: Uninitialized Response causing NPE
private Response response = new Response();

// ✅ Correct: Properly initialized
private Response response = new Response(1, "Success", "test_data");
```

### 4. Entity Initialization for Business Logic
```java
// ❌ Wrong: Empty entities causing NPEs
Order order = new Order();  // Missing required fields

// ✅ Correct: Initialize required fields
Order order = new Order();
order.setAccountId("test_account");
order.setId("test_id");
```

## Testing Methodology

### Approach Used:
1. **Preserve Business Logic**: Only fix technical mocking issues, not legitimate business validation failures
2. **Match Implementation**: Ensure mock expectations match actual service implementation
3. **Proper Initialization**: Initialize entity objects with required fields to prevent NPEs
4. **Consistent URL Patterns**: Align test URLs with service discovery patterns used in implementation

### Verification Strategy:
- Run tests individually to isolate specific failures
- Compare mock expectations with actual implementation code
- Verify that "legitimate failures" (business validation) still fail as intended
- Ensure all technical mocking issues are resolved

## Files Modified

### ts-cancel-service:
- `src/test/java/cancel/service/CancelServiceImplTest.java`
- `src/test/java/cancel/controller/CancelControllerTest.java`

### ts-order-other-service:
- `src/test/java/other/service/OrderOtherServiceImplTest.java`
- `src/test/java/other/controller/OrderOtherControllerTest.java`

### ts-verification-code-service:
- No modifications needed

## Research Notes

**Key Finding**: The primary issues were **technical mocking problems**, not business logic errors:

1. **Mocking Framework Issues**: Incorrect use of Mockito with Optional types and repository patterns
2. **Configuration Mismatches**: Test configurations not matching production service discovery patterns  
3. **Initialization Problems**: Missing required object properties causing NPEs in business logic paths
4. **Response Object Patterns**: Inconsistent Response object initialization across test suites

**Verification**: All fixes were validated by ensuring that:
- Business logic validation still works (e.g., "Order already exists" tests still fail appropriately)
- Only technical mocking issues were resolved
- Service implementations remain unchanged
- Test coverage is preserved while eliminating false failures
#!/bin/bash

echo "TeamSync API Integration Tests"
echo "=================================="

BASE_URL="http://localhost"
USER_SERVICE="$BASE_URL:4322"
COURSE_SERVICE="$BASE_URL:4321"
TASK_SERVICE="$BASE_URL:4324"
NOTIFICATION_SERVICE="$BASE_URL:4323"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

test_count=0
pass_count=0

# Function to run test
run_test() {
    local test_name="$1"
    local command="$2"
    local expected_pattern="$3"
    
    echo -n "Testing $test_name... "
    test_count=$((test_count + 1))
    
    response=$(eval "$command" 2>/dev/null)
    
    if echo "$response" | grep -q "$expected_pattern"; then
        echo -e "${GREEN}PASS${NC}"
        pass_count=$((pass_count + 1))
    else
        echo -e "${RED}FAIL${NC}"
        echo "  Expected: $expected_pattern"
        echo "  Got: $response"
    fi
}

echo -e "\n${YELLOW}1. User Service Tests${NC}"

# Test 1: User Registration
run_test "User Registration" \
    "curl -s -X PUT $USER_SERVICE/api/users -H 'Content-Type: application/json' -d '{\"username\":\"apitest\",\"password\":\"testpass\"}'" \
    "User registered successfully"

# Test 2: Username Check (should exist)
run_test "Username Check (exists)" \
    "curl -s -X GET $USER_SERVICE/api/users/check-username/apitest" \
    "\"exists\":true"

# Test 3: Username Check (should not exist)
run_test "Username Check (not exists)" \
    "curl -s -X GET $USER_SERVICE/api/users/check-username/nonexistent" \
    "\"exists\":false"

# Test 4: User Login
run_test "User Login" \
    "curl -s -X POST $USER_SERVICE/api/users/login -H 'Content-Type: application/json' -d '{\"username\":\"apitest\",\"password\":\"testpass\"}'" \
    "\"token\""

# Test 5: Invalid Login
run_test "Invalid Login" \
    "curl -s -X POST $USER_SERVICE/api/users/login -H 'Content-Type: application/json' -d '{\"username\":\"invalid\",\"password\":\"wrong\"}'" \
    "Invalid username or password"

echo -e "\n${YELLOW}2. Course Service Tests${NC}"

# Test 6: Get Courses
run_test "Get Courses" \
    "curl -s -X GET $COURSE_SERVICE/api/courses" \
    "\[\]"

echo -e "\n${YELLOW}3. Task Service Tests${NC}"

# Test 7: Add Task
run_test "Add Task" \
    "curl -s -X POST $TASK_SERVICE/api/tasks -H 'Content-Type: application/json' -d '{\"teamId\":1,\"name\":\"Integration Test Task\",\"description\":\"Test task for API\",\"status\":false,\"assignedToId\":1}'" \
    "Task added successfully"

echo -e "\n${YELLOW}4. Notification Service Tests${NC}"

# Test 8: Get Notifications
run_test "Get Notifications" \
    "curl -s -X GET $NOTIFICATION_SERVICE/api/notifications" \
    "\[\]"

# Summary
echo -e "\n${YELLOW}Test Summary${NC}"
echo "=================================="
echo "Tests passed: $pass_count/$test_count"

if [ $pass_count -eq $test_count ]; then
    echo -e "${GREEN}All tests passed!${NC}"
    exit 0
else
    echo -e "${RED}Some tests failed.${NC}"
    exit 1
fi
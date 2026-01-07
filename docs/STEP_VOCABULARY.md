# Step Vocabulary Reference

This document defines the standardized Gherkin step vocabulary for the OpenFGA SDK Conformance Test Suite. All steps are designed to be portable across SDK languages and platforms.

## Client Configuration Steps

### Basic Client Setup
```gherkin
Given I have a client configured with store "01H0H40Z9QDYNR71MRAG9FPE7M"
Given I have a client configured with:
  | apiUrl               | http://localhost:8080           |
  | storeId              | 01H0H40Z9QDYNR71MRAG9FPE7M     |
  | authorizationModelId | 01H0H40Z9QDYNR71MRAG9FPE7N     |
```

### Authentication Configuration
```gherkin
Given I configure authentication with bearer token "eyJhbGciOiJSUzI1NiIs..."
Given I configure client credentials authentication:
  | clientId     | test-client-id     |
  | clientSecret | test-client-secret |
  | audience     | https://api.fga.example |
  | issuer       | https://auth.example   |
```

### Header Configuration
```gherkin
Given I configure default headers:
  | X-Client-ID | test-client-123 |
  | X-Version   | v1.0           |
Given I set the request header "X-Correlation-ID" to "abc-123-def"
```

## API Method Steps

### Check Operations
```gherkin
When I call Check with user "user:alice" relation "viewer" object "document:readme"
When I call Check with:
  | user     | user:alice      |
  | relation | viewer          |
  | object   | document:readme |
When I call Check with authorization model "01H0H40Z9QDYNR71MRAG9FPE7N"
```

### Batch Check Operations
```gherkin
When I call BatchCheck with:
  | user        | relation | object            |
  | user:alice  | viewer   | document:readme   |
  | user:bob    | editor   | document:readme   |
  | user:carol  | admin    | folder:projects   |
```

### Read Operations
```gherkin
When I call Read with no parameters
When I call Read with:
  | user     | user:alice      |
  | relation | viewer          |
  | object   | document:readme |
When I call Read with page size 25
When I call Read with continuation token "eyJwayI6..."
```

### Write Operations
```gherkin
When I call Write with writes:
  | user        | relation | object            |
  | user:alice  | viewer   | document:readme   |
  | user:bob    | editor   | document:readme   |

When I call Write with deletes:
  | user        | relation | object            |
  | user:carol  | viewer   | document:readme   |

When I call Write with writes and deletes:
  | operation | user        | relation | object            |
  | write     | user:alice  | viewer   | document:readme   |
  | delete    | user:carol  | viewer   | document:readme   |
```

### Expand Operations
```gherkin
When I call Expand with relation "viewer" object "document:readme"
When I call Expand with:
  | relation | viewer          |
  | object   | document:readme |
```

### List Objects Operations
```gherkin
When I call ListObjects with user "user:alice" relation "viewer" type "document"
When I call ListObjects with:
  | user     | user:alice |
  | relation | viewer     |
  | type     | document   |
```

### Streaming Operations
```gherkin
When I call StreamedListObjects with user "user:alice" relation "viewer" type "document"
When I start streaming ListObjects with user "user:alice" relation "viewer" type "document"
When I read the next streaming result
When I cancel the streaming operation
```

## Store Management Steps

### Store Operations
```gherkin
When I call ListStores
When I call ListStores with page size 10
When I call ListStores with name filter "test-store"
When I call CreateStore with name "test-store"
When I call GetStore
When I call GetStore with store "01H0H40Z9QDYNR71MRAG9FPE7M"
When I call DeleteStore
```

## Authorization Model Steps

### Model Operations
```gherkin
When I call ReadAuthorizationModels
When I call ReadAuthorizationModels with page size 5
When I call ReadAuthorizationModel with model "01H0H40Z9QDYNR71MRAG9FPE7N"
When I call ReadLatestAuthorizationModel
When I call WriteAuthorizationModel with:
  | schema_version | 1.1      |
  | type           | document |
  | relation       | viewer   |
```

## Assertion Steps

### Response Status
```gherkin
Then the response should be successful
Then the response should fail
Then the response should have status code 200
Then the response should have status code 400
Then the response should complete within 5 seconds
```

### Response Content
```gherkin
Then the result should be "allowed: true"
Then the result should be "allowed: false"
Then the response should contain "continuation_token"
Then the response should not contain "continuation_token"
Then the response should have field "allowed" with value "true"
```

### Error Handling
```gherkin
Then the response should fail with a validation error
Then the response should fail with an authentication error
Then the response should fail with an authorization error
Then the error code should be "validation_error"
Then the error message should contain "invalid store ID"
```

### Collection Assertions
```gherkin
Then the response should contain 3 items
Then the response should contain at least 1 item
Then the response should be empty
Then the tuples should include:
  | user        | relation | object            |
  | user:alice  | viewer   | document:readme   |
```

### Header Verification
```gherkin
Then the request should include header "X-Client-ID" with value "test-client-123"
Then the request should include header "Authorization" matching "Bearer .+"
Then the response should include header "X-Request-ID"
```

## Streaming Assertions

### Stream State
```gherkin
Then the stream should be active
Then the stream should be closed
Then the stream should have 5 results available
Then the stream should be empty
```

### Stream Content
```gherkin
Then the next streaming result should be:
  | object            |
  | document:readme   |
  | document:guide    |
Then the streaming should complete successfully
Then the streaming should fail with error "context_cancelled"
```

## Timing and Performance Steps

### Response Timing
```gherkin
Given I set the request timeout to 30 seconds
When I measure the response time
Then the response should complete within 100 milliseconds
Then the response should take at least 50 milliseconds
```

### Retry Behavior
```gherkin
Given I configure retry policy with 3 attempts
Given I configure exponential backoff with base delay 100ms
When the server returns status 503 then 200
Then the client should retry the request
Then the client should make exactly 2 requests
```

## Data Setup Steps

### Test Data
```gherkin
Given I have test data from fixture "basic_tuples.json"
Given I have an authorization model with types:
  | user     |
  | document |
  | folder   |
Given the current time is "2024-01-15T14:30:25Z"
```

### WireMock Control
```gherkin
Given WireMock is configured with bundle "core_check_scenarios"
Given I reset WireMock state
Given I verify WireMock received exactly 1 request to "/stores/.*/check"
```

## Context and State Steps

### Test Context
```gherkin
Given I save the response as "check_result"
Given I use the saved "check_result" for comparison
When I repeat the previous request
Then the response should match the saved "check_result"
```

### Client State
```gherkin
Given I clear the client cache
Given I set the client store to "01H0H40Z9QDYNR71MRAG9FPE7M"
Given I set the client authorization model to "01H0H40Z9QDYNR71MRAG9FPE7N"
```

## Advanced Scenarios

### Concurrent Operations
```gherkin
When I call Check concurrently 10 times
When I start 5 concurrent streaming operations
Then all concurrent operations should complete successfully
Then no concurrent operations should interfere with each other
```

### Error Recovery
```gherkin
Given the server will return 401 then 200 on token refresh
When I call Check after token expiry
Then the client should automatically refresh the token
Then the Check operation should succeed
```

### Resource Cleanup
```gherkin
Given I track resource usage
When I perform 100 Check operations
Then memory usage should remain stable
Then no resources should leak
```

## Step Implementation Guidelines

### Parameter Handling
- **Store IDs**: Always use valid 26-character ULID format
- **Model IDs**: Always use valid 26-character ULID format  
- **User/Object IDs**: Use descriptive, readable formats
- **Timestamps**: Use ISO 8601 format (2024-01-15T14:30:25Z)

### Error Conditions
- **Validation errors**: Invalid input parameters
- **Authentication errors**: Missing or invalid credentials
- **Authorization errors**: Insufficient permissions
- **Network errors**: Connection failures, timeouts

### Response Formats
- **Boolean results**: "allowed: true" or "allowed: false"
- **Collections**: Use table format for multiple items
- **JSON objects**: Reference fixture files for complex data

### Portability Notes
- All steps must work across Go, JavaScript, .NET, Python, and Java SDKs
- Avoid language-specific terminology or concepts
- Focus on behavior, not implementation details
- Use consistent data formats and naming conventions

# Portability Guidelines

This document outlines guidelines for ensuring conformance test scenarios work consistently across all OpenFGA SDK languages and platforms.

## Core Principles

### Language Agnostic Design
- Test scenarios focus on **behavior**, not implementation details
- Step definitions abstract away language-specific syntax
- WireMock provides consistent API responses regardless of SDK language

### Capability-Based Testing
- Use tags to mark scenarios that may not be supported by all SDKs
- Graceful degradation for platform-specific limitations
- Clear documentation of SDK-specific behaviors

## Step Vocabulary Design

### Portable Patterns

#### ✅ Good - Behavior Focused
```gherkin
Given I have a client configured with store "01H0H40Z9QDYNR71MRAG9FPE7M"
When I call Check with user "user:alice" relation "viewer" object "document:readme"
Then the response should be successful
And the result should be "allowed: true"
```

#### ❌ Avoid - Implementation Specific
```gherkin
Given I create a new OpenFgaClient(config)
When I call client.check(CheckRequest.builder().user("user:alice").build())
Then the CompletableFuture<CheckResponse> should complete successfully
```

### Data Type Handling

#### Strings and Primitives
```gherkin
# Portable across all languages
Given I have a store ID "01H0H40Z9QDYNR71MRAG9FPE7M"
When I set the page size to 25
Then the continuation token should be "eyJwayI6..."
```

#### Complex Objects
```gherkin
# Use table format for structured data
When I call Write with writes:
  | user        | relation | object            |
  | user:alice  | viewer   | document:readme   |
  | user:bob    | editor   | document:readme   |
```

#### Arrays and Lists
```gherkin
# Specify collections clearly
Given I have authorization model types:
  | user     |
  | document |
  | folder   |
```

## Platform Considerations

### Async/Sync Patterns

Different SDKs handle asynchronous operations differently:

**Python**: Explicit async/sync variants
```python
# Async
async def test_check_async():
    result = await client.check(...)

# Sync  
def test_check_sync():
    result = client.check(...)
```

**JavaScript**: Promise-based
```javascript
const result = await client.check(...);
```

**Go**: Builder pattern with context
```go
result, err := client.Check(ctx, ...)
```

**Java**: CompletableFuture
```java
CompletableFuture<CheckResponse> future = client.check(...);
```

**.NET**: Task-based
```csharp
var result = await client.CheckAsync(...);
```

#### Portable Step Design
```gherkin
# Works for all async patterns
When I call Check with user "user:alice" relation "viewer" object "document:readme"
Then the response should be successful within 5 seconds
```

### Error Handling Variations

#### HTTP Status Codes
```gherkin
# Portable - focuses on HTTP semantics
Then the response should have status code 400
And the error code should be "validation_error"
```

#### Exception Types
```gherkin
# Avoid language-specific exception names
Then the request should fail with a validation error
# Instead of: Then it should throw ValidationException
```

### Authentication Patterns

#### Bearer Token
```gherkin
Given I configure authentication with bearer token "eyJhbGciOiJSUzI1NiIs..."
```

#### Client Credentials
```gherkin
Given I configure client credentials authentication:
  | clientId     | test-client-id     |
  | clientSecret | test-client-secret |
  | audience     | https://api.fga.example |
  | issuer       | https://auth.example   |
```

## Capability Tags

### Core Capabilities
All SDKs must support these scenarios:
```gherkin
@core @required
Feature: Basic Check Operation
```

### Optional Capabilities
Mark scenarios that may not be universally supported:
```gherkin
@streaming @optional
Feature: Streaming List Objects

@telemetry @optional  
Feature: Metrics Collection

@grpc @optional
Feature: gRPC Transport
```

### Platform-Specific Capabilities
```gherkin
@dotnet-only
Scenario: .NET Framework 4.8 compatibility

@python-async
Scenario: Async context manager usage

@go-context
Scenario: Context cancellation handling
```

## Data Format Standards

### Identifiers
- **Store IDs**: 26-character ULID format
- **Model IDs**: 26-character ULID format  
- **User IDs**: Descriptive format (`user:alice`, `team:engineering`)
- **Object IDs**: Descriptive format (`document:readme`, `folder:projects`)

### Timestamps
```gherkin
# Use ISO 8601 format
Given the current time is "2024-01-15T14:30:25Z"
When I read changes since "2024-01-15T14:00:00Z"
```

### JSON Payloads
```gherkin
# Use fixtures for complex JSON
When I call Write with the payload from fixture "write_request_basic.json"
```

## WireMock Portability

### Request Matching
```json
{
  "request": {
    "method": "POST",
    "urlPathPattern": "/stores/[0-9A-Z]{26}/check",
    "headers": {
      "Content-Type": {"equalTo": "application/json"},
      "User-Agent": {"matches": ".*"}
    },
    "bodyPatterns": [
      {"matchesJsonPath": "$.tuple_key.user"},
      {"matchesJsonPath": "$.tuple_key.relation"},
      {"matchesJsonPath": "$.tuple_key.object"}
    ]
  }
}
```

### Response Templates
```json
{
  "response": {
    "status": 200,
    "headers": {
      "Content-Type": "application/json",
      "X-Request-ID": "{{randomValue type='UUID'}}"
    },
    "bodyFileName": "check_success.json"
  }
}
```

## Testing Strategies

### Cross-Platform Validation

#### Matrix Testing
Run scenarios across all SDK/platform combinations:
- Go on Linux/macOS/Windows
- JavaScript on Node.js versions 16, 18, 20
- .NET on Framework 4.8, .NET 6, .NET 8
- Python on versions 3.8, 3.9, 3.10, 3.11, 3.12
- Java on JDK 11, 17, 21

#### Behavior Verification
```gherkin
@cross-platform
Scenario Outline: Check operation across SDKs
  Given I have a client configured for <sdk>
  When I call Check with user "user:alice" relation "viewer" object "document:readme"
  Then the response should be successful
  And the result should be "allowed: true"
  
  Examples:
    | sdk        |
    | go         |
    | javascript |
    | dotnet     |
    | python     |
    | java       |
```

### Performance Considerations
```gherkin
@performance
Scenario: Response time consistency
  When I call Check with user "user:alice" relation "viewer" object "document:readme"
  Then the response should complete within 100 milliseconds
  And the memory usage should be reasonable
```

## Common Pitfalls

### Timing Dependencies
```gherkin
# ❌ Avoid - Flaky timing
Then the response should arrive in exactly 50 milliseconds

# ✅ Better - Reasonable bounds
Then the response should complete within 5 seconds
```

### Platform-Specific Paths
```gherkin
# ❌ Avoid - OS-specific
Given I load configuration from "/usr/local/etc/config.json"

# ✅ Better - Relative or configurable
Given I load configuration from fixture "config.json"
```

### Language-Specific Types
```gherkin
# ❌ Avoid - Java-specific
Then the result should be an Optional<Boolean>

# ✅ Better - Behavior-focused
Then the result should indicate whether access is allowed
```

## Validation Tools

### Linting Rules
- Step definitions must not contain language-specific terms
- Scenario names should be descriptive and language-agnostic
- Tags should follow the established capability taxonomy

### Cross-SDK Testing
```bash
# Run scenarios across all SDKs
./scripts/test-all-sdks.sh features/core/

# Validate portability
./scripts/validate-portability.sh features/
```

### Documentation Generation
```bash
# Generate step vocabulary documentation
./scripts/generate-step-docs.sh > docs/STEP_VOCABULARY.md
```

## Migration Guidelines

### Existing Test Migration
When migrating existing SDK tests:

1. **Extract Behavior**: Focus on what the test validates, not how
2. **Generalize Steps**: Remove language-specific implementation details
3. **Standardize Data**: Use consistent test data formats
4. **Add Tags**: Mark scenarios with appropriate capability tags

### Example Migration
```gherkin
# Before (Go-specific)
Scenario: Test client configuration validation
  Given I create a new client with invalid store ID "invalid"
  When I call client.Check()
  Then it should return ErrInvalidStoreId

# After (Portable)
@core
Scenario: Client rejects invalid store ID
  Given I have a client configured with store "invalid"
  When I call Check with user "user:alice" relation "viewer" object "document:readme"
  Then the response should fail with a validation error
  And the error message should contain "invalid store ID"
```

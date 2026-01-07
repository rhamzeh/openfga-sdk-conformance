# Contributor Workflow

This document outlines the development workflow for contributing to the OpenFGA SDK Conformance Test Suite.

## Development Setup

### Prerequisites

- **Docker**: For running WireMock server
- **Git**: For version control and submodule management
- **Language-specific tools**: Depending on which SDK runner you're working with

### Local Environment Setup

1. **Clone the repository**:
   ```bash
   git clone https://github.com/openfga/sdk-conformance.git
   cd sdk-conformance
   ```

2. **Start WireMock server**:
   ```bash
   docker run -d --name wiremock-conformance \
     -p 8080:8080 \
     -v $(pwd)/wiremock:/home/wiremock \
     wiremock/wiremock:latest
   ```

3. **Verify WireMock is running**:
   ```bash
   curl http://localhost:8080/__admin/health
   ```

## Contributing New Scenarios

### 1. Write Gherkin Feature

Create or modify feature files in the appropriate category:

```gherkin
# features/core/basic_check.feature
@core
Feature: Basic Check Operation
  As an SDK user
  I want to perform authorization checks
  So that I can verify user permissions

  Scenario: Successful check with valid tuple
    Given I have a client configured with store "01H0H40Z9QDYNR71MRAG9FPE7M"
    And I have an authorization model "01H0H40Z9QDYNR71MRAG9FPE7N"
    When I call Check with:
      | user     | user:alice        |
      | relation | viewer            |
      | object   | document:readme   |
    Then the response should be successful
    And the result should be "allowed: true"
```

### 2. Create WireMock Mappings

Create corresponding WireMock mappings in `wiremock/bundles/`:

```json
{
  "mappings": [
    {
      "id": "check-success-basic",
      "name": "Basic successful check",
      "request": {
        "method": "POST",
        "urlPath": "/stores/01H0H40Z9QDYNR71MRAG9FPE7M/check",
        "headers": {
          "Content-Type": {
            "equalTo": "application/json"
          }
        },
        "bodyPatterns": [
          {
            "equalToJson": {
              "tuple_key": {
                "user": "user:alice",
                "relation": "viewer",
                "object": "document:readme"
              },
              "authorization_model_id": "01H0H40Z9QDYNR71MRAG9FPE7N"
            }
          }
        ]
      },
      "response": {
        "status": 200,
        "headers": {
          "Content-Type": "application/json"
        },
        "bodyFileName": "check_success_basic.json"
      }
    }
  ]
}
```

### 3. Create Response Fixtures

Add response fixtures in `wiremock/fixtures/`:

```json
{
  "allowed": true,
  "resolution": ""
}
```

### 4. Test Across SDK Runners

Test your scenarios with at least one SDK runner:

```bash
# Example with Go runner
cd runners/go
go test -tags=conformance -run="TestBasicCheck"
```

## Step Vocabulary Guidelines

### Portable Steps

Use steps that work across all SDK languages:

**✅ Good - Portable**:
```gherkin
Given I have a client configured with store "01H0H40Z9QDYNR71MRAG9FPE7M"
When I call Check with user "user:alice" relation "viewer" object "document:readme"
Then the response should be successful
```

**❌ Bad - Language-specific**:
```gherkin
Given I have a GoClient with storeId "01H0H40Z9QDYNR71MRAG9FPE7M"
When I call client.Check(CheckRequest{...})
Then err should be nil
```

### Standard Step Patterns

#### Client Configuration
```gherkin
Given I have a client configured with:
  | apiUrl               | http://localhost:8080 |
  | storeId              | 01H0H40Z9QDYNR71MRAG9FPE7M |
  | authorizationModelId | 01H0H40Z9QDYNR71MRAG9FPE7N |
```

#### API Method Calls
```gherkin
When I call Check with:
  | user     | user:alice      |
  | relation | viewer          |
  | object   | document:readme |

When I call Write with writes:
  | user     | relation | object          |
  | user:bob | editor   | document:readme |
```

#### Response Assertions
```gherkin
Then the response should be successful
Then the response should have status code 400
Then the response should contain error "invalid_request"
Then the result should be "allowed: true"
```

#### Header Verification
```gherkin
Given I configure default headers:
  | X-Client-ID | test-client-123 |
  | X-Version   | v1.0           |
Then the request should include header "X-Client-ID" with value "test-client-123"
```

## WireMock Conventions

### Bundle Organization

- **One bundle per feature file**: `features/core/basic_check.feature` → `wiremock/bundles/core_basic_check.json`
- **Descriptive mapping IDs**: Use kebab-case with scenario context
- **Fixture references**: Use `bodyFileName` for complex responses

### Request Matching

```json
{
  "request": {
    "method": "POST",
    "urlPath": "/stores/01H0H40Z9QDYNR71MRAG9FPE7M/check",
    "headers": {
      "Content-Type": {"equalTo": "application/json"},
      "Authorization": {"matches": "Bearer .+"}
    },
    "bodyPatterns": [
      {"equalToJson": "..."}
    ]
  }
}
```

### Response Templates

```json
{
  "response": {
    "status": 200,
    "headers": {"Content-Type": "application/json"},
    "bodyFileName": "check_success.json",
    "fixedDelayMilliseconds": 100
  }
}
```

## Testing Guidelines

### Scenario Categories

- **@core**: Must pass in PR CI, basic functionality
- **@auth**: Authentication and authorization flows
- **@headers**: HTTP header handling
- **@client**: Client convenience methods
- **@streaming**: NDJSON streaming scenarios
- **@retry**: Retry logic and fault tolerance
- **@integration**: End-to-end scenarios

### Test Data Standards

- **Store IDs**: Use valid ULID format (26 characters)
- **Model IDs**: Use valid ULID format
- **User/Object IDs**: Use descriptive names (`user:alice`, `document:readme`)
- **Relations**: Use common relation names (`viewer`, `editor`, `admin`)

## Pull Request Process

1. **Create feature branch**: `git checkout -b feature/new-scenario`
2. **Add scenarios and mappings**: Follow the guidelines above
3. **Test locally**: Verify with at least one SDK runner
4. **Update documentation**: Add any new step vocabulary to docs
5. **Submit PR**: Include description of scenarios added/modified
6. **CI validation**: All `@core` scenarios must pass across all SDKs

## Versioning Strategy

- **Patch (x.x.X)**: Bug fixes, documentation updates, fixture corrections
- **Minor (x.X.x)**: New scenarios, non-breaking step additions
- **Major (X.x.x)**: Breaking changes to step vocabulary or WireMock conventions

## Troubleshooting

### WireMock Issues

**Check WireMock logs**:
```bash
docker logs wiremock-conformance
```

**Verify mappings loaded**:
```bash
curl http://localhost:8080/__admin/mappings
```

**Reset WireMock state**:
```bash
curl -X POST http://localhost:8080/__admin/reset
```

### Runner Issues

**Go Runner**:
```bash
cd runners/go
go mod tidy
go test -v -tags=conformance
```

**JavaScript Runner**:
```bash
cd runners/js
npm install
npm test
```

## Getting Help

- **Issues**: [GitHub Issues](https://github.com/openfga/sdk-conformance/issues)
- **Discussions**: [GitHub Discussions](https://github.com/openfga/sdk-conformance/discussions)
- **Community**: [https://openfga.dev/community](https://openfga.dev/community)

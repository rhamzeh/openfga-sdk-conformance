# Contributing to OpenFGA SDK Conformance Suite

This guide explains how to add new tests to the OpenFGA SDK conformance suite. The conformance suite uses a BDD (Behavior-Driven Development) approach with Gherkin feature files, WireMock for API mocking, and language-specific test runners.

## Architecture Overview

```
sdk-conformance/
├── features/                    # Gherkin feature files
│   ├── apis/                   # Core API tests
│   │   ├── queries/           # Query APIs (Check, BatchCheck, Expand, etc.)
│   │   └── tuples/            # Tuple APIs (Read, Write, ReadChanges, etc.)
│   └── client_wrappers/       # Client convenience method tests
├── wiremock/bundles/          # WireMock response mappings
│   ├── apis/                  # API endpoint mappings
│   └── client_wrappers/       # Client wrapper mappings
└── runners/                   # Language-specific test runners
    ├── go/                    # Go SDK runner
    ├── js/                    # JavaScript SDK runner
    ├── dotnet/                # .NET SDK runner
    ├── python/                # Python SDK runner
    └── java/                  # Java SDK runner
```

## Adding a New Test: Step-by-Step Guide

### Step 1: Create Feature File

Create a new `.feature` file in the appropriate directory under `features/`:

```gherkin
@api-name
Feature: API Name
  Description of what this API does

  Background:
    Given I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"

  @scenario-tag
  Scenario: Scenario description
    Given some precondition
    When I call the API with parameters
    Then the response should be successful
    And the response should contain expected data
```

**Feature File Conventions:**
- Use `@api-name` tags for grouping related scenarios
- Use descriptive scenario tags like `@batch-check`, `@time-filtering`, etc.
- Always include a Background section with store configuration
- Use consistent store ID: `01ARZ3NDEKTSV4RRFFQ69G5FAV`
- Follow Given-When-Then structure strictly

### Step 2: Create WireMock Mappings

Create corresponding WireMock mappings in `wiremock/bundles/` following the same folder structure:

```json
{
  "mappings": [
    {
      "id": "unique-mapping-id",
      "name": "Descriptive mapping name",
      "request": {
        "method": "POST",
        "url": "/stores/01ARZ3NDEKTSV4RRFFQ69G5FAV/api-endpoint",
        "bodyPatterns": [
          {
            "matchesJsonPath": "$.required_field"
          }
        ]
      },
      "response": {
        "status": 200,
        "headers": {
          "Content-Type": "application/json"
        },
        "jsonBody": {
          "expected": "response"
        }
      }
    }
  ]
}
```

**WireMock Conventions:**
- Use descriptive `id` and `name` fields
- Use consistent store ID in URLs
- Use `bodyPatterns` for request matching
- Include proper HTTP status codes
- Add `Content-Type` headers
- Use `jsonBody` for structured responses

### Step 3: Add Step Definitions

Add step definitions to each runner following the established patterns:

#### Go Runner (`runners/go/api.go`)

```go
func (ctx *TestContext) iCallNewApiWith(table *godog.Table) error {
    if ctx.client == nil {
        return fmt.Errorf("client not configured")
    }

    // Process table data
    for _, row := range table.Rows {
        // Handle row data
    }

    // Make API call (simulated for Go runner)
    response := map[string]interface{}{
        "result": "simulated_response",
    }
    
    ctx.lastResponse = response
    ctx.lastError = nil
    return nil
}
```

#### JavaScript Runner (`runners/js/features/step_definitions/api.ts`)

```typescript
When('I call NewAPI with:', async function(this: TestWorld, dataTable: any) {
  if (!this.client) {
    throw new Error('Client not configured');
  }

  const requestData = {};
  
  for (const row of dataTable.hashes()) {
    // Process table data
  }

  await this.executeApiCall(async () => {
    return await this.client!.newApi(requestData);
  });
});
```

#### .NET Runner (`runners/dotnet/StepDefinitions/Api.cs`)

```csharp
[When(@"I call NewAPI with:")]
public async Task WhenICallNewAPIWith(Table table)
{
    if (_context.Client == null)
    {
        throw new InvalidOperationException("Client not configured");
    }

    await _context.ExecuteApiCall(async () =>
    {
        var request = new NewApiRequest();
        
        foreach (var row in table.Rows)
        {
            // Process table data
        }

        return await _context.Client.NewApiAsync(request);
    });
}
```

#### Python Runner (`runners/python/steps/api.py`)

```python
@when('I call NewAPI with:')
def step_call_new_api_with(context):
    if not context.test_context.client:
        raise Exception("Client not configured")

    async def new_api_call():
        request_data = {}
        
        for row in context.table:
            # Process table data
            pass

        return await context.test_context.client.new_api(request_data)

    asyncio.run(context.test_context.execute_api_call(new_api_call))
```

#### Java Runner (`runners/java/src/test/java/dev/openfga/sdk/conformance/steps/Api.java`)

```java
@When("I call NewAPI with:")
public void iCallNewAPIWith(DataTable dataTable) throws Exception {
    if (testContext.getClient() == null) {
        throw new IllegalStateException("Client not configured");
    }

    testContext.executeApiCall(() -> {
        NewApiRequest request = new NewApiRequest();
        
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            // Process table data
        }

        return testContext.getClient().newApi(request);
    });
}
```

### Step 4: Add Response Validation Steps

Add response validation steps to each runner's response validation files:

#### Go Runner (`runners/go/response.go`)

```go
func (ctx *TestContext) theResponseShouldContainNewApiData() error {
    if ctx.lastError != nil {
        return ctx.lastError
    }
    
    if ctx.lastResponse == nil {
        return fmt.Errorf("no response received")
    }
    
    // Validate response structure
    return nil
}
```

#### Other Runners
Follow similar patterns in:
- JavaScript: `runners/js/features/step_definitions/response.ts`
- .NET: `runners/dotnet/StepDefinitions/Response.cs`
- Python: `runners/python/steps/response.py`
- Java: `runners/java/src/test/java/dev/openfga/sdk/conformance/steps/Response.java`

## Testing Your Implementation

### 1. Validate Feature Files
```bash
# Check Gherkin syntax
cucumber-js --dry-run features/your-new-feature.feature
```

### 2. Test WireMock Mappings
```bash
# Start WireMock with your mappings
java -jar wiremock-standalone.jar --port 8080 --root-dir wiremock/
```

### 3. Run Conformance Tests
```bash
# Test specific runner
cd runners/go && go test
cd runners/js && npm test
cd runners/dotnet && dotnet test
cd runners/python && python -m pytest
cd runners/java && mvn test
```

## Best Practices

### Feature Files
- **Use consistent terminology** across all scenarios
- **Include edge cases** and error scenarios
- **Test both success and failure paths**
- **Use realistic test data** that matches production patterns
- **Group related scenarios** with appropriate tags

### WireMock Mappings
- **Use specific request matching** to avoid conflicts
- **Include correlation IDs** for traceability
- **Test error responses** with appropriate HTTP status codes
- **Use response templates** for dynamic data when needed
- **Keep mappings focused** - one mapping per scenario when possible

### Step Definitions
- **Follow established naming patterns** (`api.*` for main API steps)
- **Use proper error handling** in all runners
- **Maintain consistency** across language implementations
- **Include proper type checking** and validation
- **Store test data** in context for validation steps

### Response Validation
- **Validate response structure** and required fields
- **Check data types** and value ranges
- **Test correlation ID matching** when applicable
- **Verify error messages** and status codes
- **Include performance assertions** when relevant

## Common Patterns

### Table-Driven Tests
```gherkin
When I call API with:
  | field1 | field2 | field3 |
  | value1 | value2 | value3 |
  | value4 | value5 | value6 |
```

### Parameterized Scenarios
```gherkin
Scenario Outline: Test with different values
  When I call API with <parameter>
  Then the response should be <expected>
  
  Examples:
    | parameter | expected |
    | value1    | result1  |
    | value2    | result2  |
```

### Error Testing
```gherkin
Scenario: API returns error for invalid input
  When I call API with invalid data
  Then the response should be an error
  And the error should indicate invalid input
```

## File Naming Conventions

- **Feature files**: `snake_case.feature` (e.g., `batch_check.feature`)
- **WireMock mappings**: Match feature file names (e.g., `batch_check.json`)
- **Step definition files**: `api.*` for main APIs, `streaming_steps.*` for streaming APIs
- **Response validation**: `response.*` in each runner

## SDK-Specific Considerations

### Go Runner
- Uses **simulated responses** due to SDK limitations
- Stores configuration in `savedData` map
- Uses `godog.Table` for table data processing

### JavaScript Runner
- Uses **actual SDK calls** with proper TypeScript types
- Implements async/await patterns consistently
- Uses `TestWorld` context for state management

### .NET Runner
- Uses **proper SDK types** and async methods
- Implements SpecFlow table processing
- Uses dependency injection for test context

### Python Runner
- Uses **async/await** with `asyncio.run`
- Implements Behave table processing
- Uses proper exception handling

### Java Runner
- Uses **fluent API patterns** with method chaining
- Implements Cucumber DataTable processing
- Uses CompletableFuture for async operations

## Troubleshooting

### Common Issues
1. **Step definition not found**: Check method signatures match Gherkin exactly
2. **WireMock mapping not matched**: Verify request patterns and URL paths
3. **Type errors**: Ensure proper imports and type annotations
4. **Async issues**: Use proper async/await patterns in each language

### Debugging Tips
- **Use descriptive error messages** in step definitions
- **Log request/response data** during development
- **Test WireMock mappings** independently first
- **Validate JSON schemas** for complex request/response data

## Review Checklist

Before submitting your new test:

- [ ] Feature file follows Gherkin best practices
- [ ] WireMock mappings cover all scenarios
- [ ] Step definitions implemented in all 5 runners
- [ ] Response validation steps added
- [ ] Error scenarios included
- [ ] Tests pass in all runners
- [ ] Documentation updated if needed
- [ ] Follows established naming conventions
- [ ] Includes appropriate tags for test organization
- [ ] Uses consistent test data and store IDs

## Getting Help

- Review existing feature files for patterns and examples
- Check the `features/apis/queries/` directory for comprehensive examples
- Look at recent commits for implementation patterns
- Consult SDK documentation for API-specific details
- Ask questions in team channels or create GitHub issues

Remember: The goal is comprehensive, consistent, and maintainable test coverage across all OpenFGA SDKs!

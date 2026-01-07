# .NET SDK Conformance Runner

This directory contains the .NET implementation of the OpenFGA SDK Conformance Test Suite using SpecFlow.

## Prerequisites

- .NET 6.0 or later
- Docker/Podman (for WireMock server)

## Setup

1. **Restore dependencies**:
   ```bash
   dotnet restore
   ```

2. **Start WireMock server** (from repository root):
   ```bash
   make start CONTAINER_CMD=podman COMPOSE_CMD=podman-compose  # or docker
   make load-mappings
   ```

## Running Tests

### Run all conformance tests
```bash
dotnet test
```

### Run specific scenarios by tags
```bash
dotnet test --filter "TestCategory=core"
dotnet test --filter "TestCategory=headers"
dotnet test --filter "TestCategory=smoke"
```

### Run with different output formats
```bash
dotnet test --logger "console;verbosity=detailed"
dotnet test --logger "trx;LogFileName=test-results.trx"
dotnet test --logger "html;LogFileName=test-results.html"
```

### Run specific feature files
```bash
dotnet test --filter "FullyQualifiedName~Check"
dotnet test --filter "FullyQualifiedName~Headers"
```

## Development

### Project Structure
```
runners/dotnet/
├── OpenFga.Sdk.Conformance.csproj  # Project file with dependencies
├── specflow.json                    # SpecFlow configuration
├── Support/
│   └── TestContext.cs              # Test context and utilities
├── Hooks/
│   └── Hooks.cs                    # Before/After scenario hooks
├── StepDefinitions/
│   ├── ClientSteps.cs              # Client configuration steps
│   ├── ApiSteps.cs                 # API method steps
│   └── AssertionSteps.cs           # Response assertions
└── README.md                       # This file
```

### Adding New Step Definitions

1. Add the step method to the appropriate file in `StepDefinitions/`
2. Use the `[Given]`, `[When]`, or `[Then]` attributes with regex patterns
3. Inject `TestContext` via constructor dependency injection
4. Test with a simple scenario first

### Test Context

The `TestContext` class maintains state between steps in a scenario:
- `Client`: OpenFGA client instance
- `Config`: Client configuration
- `LastResponse`: Last API response
- `LastError`: Last error encountered
- `SavedData`: Data saved between steps (Dictionary)
- `Headers`: Request headers (Dictionary)

### Debugging

Enable verbose test output:
```bash
dotnet test --logger "console;verbosity=detailed"
```

View WireMock logs:
```bash
make logs
```

Check WireMock status:
```bash
make status
```

## Implementation Status

### ✅ Completed
- Basic project structure with SpecFlow
- NuGet package configuration with dependencies
- Test context and dependency injection setup
- Client configuration steps
- Complete API method implementations (Check, Read, Write)
- **Enhanced authentication flows** (bearer token, client credentials)
- **Advanced HTTP status code extraction** from exceptions
- **Response header inspection** and validation
- **Enhanced error response parsing** with categorization
- Comprehensive response assertion framework
- Collection and tuple validation
- Error handling and validation
- Context and state management

### 🚧 In Progress
- Streaming operations support
- Performance testing capabilities
- Integration with CI/CD pipeline

### 📋 TODO
- Advanced streaming scenarios
- Request timing and performance metrics
- Custom assertion helpers

## Architecture

The .NET runner follows the established conformance suite patterns:

1. **SpecFlow Configuration**: `specflow.json` sets up test execution
2. **Dependency Injection**: `TestContext` injected into step definition classes
3. **Step Definitions**: Modular step implementations across multiple classes
4. **OpenFGA SDK Integration**: Direct usage of `OpenFga.Sdk` NuGet package
5. **Assertion Framework**: FluentAssertions with custom helpers

## Environment Variables

- `WIREMOCK_URL`: WireMock server URL (default: http://localhost:8080)
- `DOTNET_ENVIRONMENT`: Environment (Development, Test, Production)

## Building

```bash
dotnet build                    # Build project
dotnet build --configuration Release  # Release build
```

## Package Management

```bash
dotnet add package <PackageName>     # Add NuGet package
dotnet remove package <PackageName>  # Remove NuGet package
dotnet list package                  # List installed packages
```

# Go SDK Conformance Runner

This directory contains the Go implementation of the OpenFGA SDK Conformance Test Suite using Godog (Cucumber for Go).

## Prerequisites

- Go 1.21 or later
- Docker (for WireMock server)

## Setup

1. **Install dependencies**:
   ```bash
   go mod tidy
   ```

2. **Start WireMock server** (from repository root):
   ```bash
   make start
   make load-mappings
   ```

## Running Tests

### Run all conformance tests
```bash
go test -v
```

### Run specific scenarios
```bash
go test -v --godog.tags="@core"
go test -v --godog.tags="@headers"
```

### Run with different output formats
```bash
go test -v --godog.format=pretty
go test -v --godog.format=progress
go test -v --godog.format=junit
```

### Run specific feature files
```bash
go test -v ../../features/apis/queries/check.feature
go test -v ../../features/request_configuration/headers.feature
```

## Development

### Adding New Step Definitions

1. Add the step pattern to `main_test.go` in the `InitializeScenario` function
2. Implement the step function in `context.go`
3. Test with a simple scenario first

### Test Context

The `TestContext` struct maintains state between steps in a scenario:
- `client`: OpenFGA client instance
- `config`: Client configuration
- `lastResponse`: Last API response
- `lastError`: Last error encountered
- `savedData`: Data saved between steps
- `headers`: Request headers

### Debugging

Enable verbose output:
```bash
go test -v --godog.format=pretty --godog.strict
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
- Basic project structure
- Go module setup with dependencies
- Test runner configuration with Godog
- Basic client configuration steps
- Simple Check operation implementation
- Response assertion framework

### 🚧 In Progress
- Complete API method implementations (Read, Write)
- Error handling and validation
- Header verification
- Collection assertions

### 📋 TODO
- Authentication flows (bearer token, client credentials)
- Streaming operations
- Advanced assertions
- Performance testing
- Integration with CI/CD pipeline

## Architecture

```
runners/go/
├── main_test.go    # Godog test runner and step definitions
├── context.go      # Test context and step implementations
├── go.mod          # Go module dependencies
└── README.md       # This file
```

The runner follows the standard Godog pattern:
1. `TestMain` sets up the Godog test suite
2. `InitializeScenario` registers step definitions
3. `TestContext` maintains state between steps
4. Step functions implement the actual test logic

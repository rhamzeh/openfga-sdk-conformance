# JavaScript SDK Conformance Runner

This directory contains the JavaScript implementation of the OpenFGA SDK Conformance Test Suite using Cucumber.js.

## Prerequisites

- Node.js 16.0.0 or later
- npm or yarn
- Docker/Podman (for WireMock server)

## Setup

1. **Install dependencies**:
   ```bash
   npm install
   ```

2. **Start WireMock server** (from repository root):
   ```bash
   make start CONTAINER_CMD=podman COMPOSE_CMD=podman-compose  # or docker
   make load-mappings
   ```

## Running Tests

### Run all conformance tests
```bash
npm test
```

### Run specific scenarios
```bash
npm run test:core      # @core scenarios
npm run test:headers   # @headers scenarios  
npm run test:smoke     # @smoke scenarios
```

### Run with different output formats
```bash
npm run test:pretty    # Pretty console output
npm run test:json      # JSON output for CI
```

### Run specific feature files
```bash
npx cucumber-js ../../features/apis/queries/check.feature
npx cucumber-js ../../features/request_configuration/headers.feature
```

## Development

### Project Structure
```
runners/js/
├── package.json              # Dependencies and scripts
├── cucumber.js               # Cucumber configuration
├── features/
│   └── step_definitions/
│       ├── world.js          # Test context and world setup
│       ├── client_steps.js   # Client configuration steps
│       ├── api_steps.js      # API method steps
│       └── assertion_steps.js # Response assertions
└── README.md                 # This file
```

### Adding New Step Definitions

1. Add the step pattern to the appropriate file in `features/step_definitions/`
2. Implement the step function using the world context
3. Test with a simple scenario first

### Test Context (World)

The `TestWorld` class maintains state between steps in a scenario:
- `client`: OpenFGA client instance
- `config`: Client configuration
- `lastResponse`: Last API response
- `lastError`: Last error encountered
- `savedData`: Data saved between steps (Map)
- `headers`: Request headers (Map)

### Debugging

Enable verbose output:
```bash
npx cucumber-js --format=pretty --verbose
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
- Basic project structure with Cucumber.js
- npm package configuration with dependencies
- Test world and context management
- Client configuration steps
- Complete API method implementations (Check, Read, Write)
- **Enhanced authentication flows** (bearer token, client credentials)
- **Advanced HTTP status code extraction** from exceptions
- **Response header inspection** and validation
- **Enhanced error response parsing** with categorization
- **Streaming operations support** (ReadChanges, ListObjects)
- Comprehensive response assertion framework
- Collection and tuple validation
- Error handling and validation
- Context and state management

### 🚧 In Progress
- Performance testing capabilities
- Integration with CI/CD pipeline

### 📋 TODO
- Advanced streaming scenarios
- Request timing and performance metrics
- Custom assertion helpers

## Architecture

The JavaScript runner follows the established conformance suite patterns:

1. **Cucumber.js Configuration**: `cucumber.js` sets up test execution
2. **World Setup**: `world.js` provides test context and utilities
3. **Step Definitions**: Modular step implementations across multiple files
4. **OpenFGA SDK Integration**: Direct usage of `@openfga/sdk` package
5. **Assertion Framework**: Chai-based assertions with custom helpers

## Environment Variables

- `WIREMOCK_URL`: WireMock server URL (default: http://localhost:8080)
- `NODE_ENV`: Environment (development, test, production)

## Linting

```bash
npm run lint        # Check code style
npm run lint:fix    # Auto-fix issues
```

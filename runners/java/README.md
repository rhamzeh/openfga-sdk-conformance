# Java SDK Conformance Runner

This directory contains the Java implementation of the OpenFGA SDK Conformance Test Suite using Cucumber JVM.

## Prerequisites

- Java 11 or later
- Maven 3.6 or later
- Docker/Podman (for WireMock server)

## Setup

1. **Install dependencies**:
   ```bash
   mvn clean install
   ```

2. **Start WireMock server** (from repository root):
   ```bash
   make start CONTAINER_CMD=podman COMPOSE_CMD=podman-compose  # or docker
   make load-mappings
   ```

## Running Tests

### Run all conformance tests
```bash
mvn test
```

### Run specific scenarios by tags
```bash
mvn test -Dcucumber.filter.tags="@core"
mvn test -Dcucumber.filter.tags="@headers"
mvn test -Dcucumber.filter.tags="@smoke"
```

### Run with different output formats
```bash
mvn test -Dcucumber.plugin="pretty,html:target/cucumber-reports"
mvn test -Dcucumber.plugin="json:target/cucumber-reports/Cucumber.json"
mvn test -Dcucumber.plugin="junit:target/cucumber-reports/Cucumber.xml"
```

### Run specific feature files
```bash
mvn test -Dcucumber.features="../../features/apis/queries/check.feature"
mvn test -Dcucumber.features="../../features/request_configuration/headers.feature"
```

## Development

### Project Structure
```
runners/java/
├── pom.xml                          # Maven project configuration
├── src/test/resources/
│   └── junit-platform.properties   # Cucumber configuration
├── src/test/java/dev/openfga/sdk/conformance/
│   ├── CucumberTestRunner.java      # Test runner
│   ├── support/
│   │   └── TestContext.java         # Test context and utilities
│   ├── hooks/
│   │   └── Hooks.java               # Before/After scenario hooks
│   └── steps/
│       ├── ClientSteps.java         # Client configuration steps
│       ├── ApiSteps.java            # API method steps
│       └── AssertionSteps.java      # Response assertions
└── README.md                        # This file
```

### Adding New Step Definitions

1. Add the step method to the appropriate file in `steps/`
2. Use the `@Given`, `@When`, or `@Then` annotations with regex patterns
3. Inject `TestContext` via constructor dependency injection
4. Test with a simple scenario first

### Test Context

The `TestContext` class maintains state between steps in a scenario:
- `client`: OpenFGA client instance
- `config`: Client configuration
- `lastResponse`: Last API response
- `lastError`: Last error encountered
- `savedData`: Data saved between steps (Map)
- `headers`: Request headers (Map)

### Debugging

Enable verbose test output:
```bash
mvn test -Dcucumber.plugin="pretty" -Dtest.verbose=true
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
- Basic project structure with Cucumber JVM
- Maven configuration with dependencies
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

The Java runner follows the established conformance suite patterns:

1. **Maven Configuration**: `pom.xml` sets up dependencies and build
2. **Cucumber Configuration**: `junit-platform.properties` configures test execution
3. **Dependency Injection**: `TestContext` injected into step definition classes
4. **Step Definitions**: Modular step implementations across multiple classes
5. **OpenFGA SDK Integration**: Direct usage of `openfga-sdk` Maven dependency
6. **Assertion Framework**: AssertJ with custom helpers

## Environment Variables

- `WIREMOCK_URL`: WireMock server URL (default: http://localhost:8080)
- `JAVA_OPTS`: JVM options for test execution

## Building

```bash
mvn clean compile                    # Compile sources
mvn clean test-compile              # Compile test sources
mvn clean package                   # Build JAR
mvn clean install                   # Install to local repository
```

## Maven Commands

```bash
mvn dependency:tree                  # Show dependency tree
mvn dependency:analyze              # Analyze dependencies
mvn clean                          # Clean build artifacts
mvn compile                        # Compile main sources
mvn test-compile                   # Compile test sources
mvn test                          # Run tests
mvn package                       # Create JAR
mvn install                       # Install to local repo
```

## IDE Integration

### IntelliJ IDEA
1. Import as Maven project
2. Install Cucumber for Java plugin
3. Run tests from IDE test runner

### Eclipse
1. Import as Maven project
2. Install Cucumber Eclipse plugin
3. Run tests from JUnit runner

### VS Code
1. Install Java Extension Pack
2. Install Cucumber (Gherkin) Full Support
3. Run tests from integrated terminal

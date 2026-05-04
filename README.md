# OpenFGA SDK Conformance Test Suite

A shared, versioned conformance test suite for OpenFGA SDKs using Behavior-Driven Development (BDD) with Gherkin features and WireMock for deterministic testing.

## Overview

This repository provides a unified testing framework to ensure consistent behavior across all OpenFGA SDKs (Go, JavaScript, .NET, Python, Java). It eliminates test duplication, prevents behavioral drift, and enables deterministic testing of complex scenarios like retries, authentication flows, and streaming.

> [!Note] PoC Status
> This is a PoC repo to address https://github.com/openfga/rfcs/pull/34 - it is unusuable in its current form and it will ultimately live in https://github.com/openfga/sdk-conformance in its final iteration.

> [!WARNING] AI Disclosure
> The code in this repo has been written with extensive help from AI Agents

## Architecture

### Core Components

- **Gherkin Features**: Human-readable test scenarios in `features/`
- **WireMock Mappings**: Deterministic API response mocking in `wiremock/`
- **Language Runners**: SDK-specific test runners in `runners/`
- **Fixtures**: Canonical request/response payloads for testing

### Directory Structure

```
sdk-conformance/
├── features/                    # Gherkin feature files
│   ├── core/                   # @core scenarios (must pass in PR CI)
│   ├── auth/                   # @auth authentication flows
│   ├── retry/                  # @retry retry logic scenarios
│   ├── headers/                # @headers header handling
│   ├── client/                 # @client convenience methods
│   ├── streaming/              # @streaming NDJSON scenarios
│   └── integration/            # @integration end-to-end tests
├── wiremock/
│   ├── bundles/                # WireMock mapping bundles by scenario
│   └── fixtures/               # JSON request/response fixtures
├── runners/                    # Language-specific test runners
│   ├── go/                     # Godog (Go)
│   ├── js/                     # Cucumber.js (JavaScript)
│   ├── dotnet/                 # Reqnroll/SpecFlow (.NET)
│   ├── python/                 # Behave (Python)
│   └── java/                   # Cucumber-JVM (Java)
└── docs/                       # Documentation
```

## Supported SDKs

- **Go SDK** - `openfga/go-sdk`
- **JavaScript SDK** - `openfga/js-sdk` (npm: @openfga/sdk)
- **.NET SDK** - `openfga/dotnet-sdk` (NuGet: OpenFga.Sdk)
- **Python SDK** - `openfga/python-sdk` (PyPI: openfga_sdk)
- **Java SDK** - `openfga/java-sdk` (Maven: dev.openfga/openfga-sdk)

## Test Categories

### @core Scenarios
Essential behaviors that must pass in PR CI:
- Basic API operations (Check, Read, Write)
- Client configuration and validation
- Error handling and status codes
- Request/response serialization

### @auth Scenarios
Authentication and authorization flows:
- Bearer token authentication
- Client credentials flow
- Token refresh on 401
- Issuer URL normalization

### @headers Scenarios
HTTP header handling:
- Default headers from client configuration
- Per-request header overrides
- Header precedence across API methods

### @client Scenarios
Client convenience methods:
- ListRelations with pagination
- BatchCheck request splitting
- ReadAssertions/WriteAssertions helpers

### @streaming Scenarios
NDJSON streaming behavior:
- StreamedListObjects handling
- Empty line processing
- Error object handling
- Invalid JSON recovery
- Context cancellation

### @retry Scenarios
Retry logic and fault tolerance:
- Configurable retry policies
- Exponential backoff
- Jitter and timing
- Failure sequences (503→200, 429→200)

## Getting Started

### Prerequisites

- Container runtime: Docker or Podman
- Compose tool: docker-compose or podman-compose
- Language-specific requirements per SDK runner

### Running Tests

#### With Docker (default)
```bash
make dev-setup     # Starts WireMock and loads all scenarios
make test          # Validates the complete setup
```

#### With Podman
```bash
make dev-setup CONTAINER_CMD=podman COMPOSE_CMD=podman-compose
make test CONTAINER_CMD=podman COMPOSE_CMD=podman-compose
```

#### Environment Variables
You can also set these permanently:
```bash
export CONTAINER_CMD=podman
export COMPOSE_CMD=podman-compose
make dev-setup
```

#### Manual Setup
1. **Start WireMock**:
   ```bash
   docker run -it --rm -p 8080:8080 -v $(pwd)/wiremock:/home/wiremock wiremock/wiremock:latest
   # OR with podman:
   podman run -it --rm -p 8080:8080 -v $(pwd)/wiremock:/home/wiremock wiremock/wiremock:latest
   ```

2. **Run conformance tests** (example for Go):
   ```bash
   cd runners/go
   go test -tags=conformance ./...
   ```

### Local Development

See [WORKFLOW.md](docs/WORKFLOW.md) for detailed contributor guidelines.

## Versioning

This conformance suite follows [Semantic Versioning](https://semver.org/):

- **MAJOR**: Breaking changes to step vocabulary or WireMock conventions
- **MINOR**: New scenarios or non-breaking step additions
- **PATCH**: Bug fixes, documentation updates, fixture corrections

SDKs pin conformance suite versions via git submodules for stability.

## CI Integration

### PR Workflow
- All `@core` scenarios must pass
- WireMock runs in Docker container
- Tests complete in <5 minutes per SDK

### Nightly Builds
- Extended scenario execution (`@auth`, `@retry`, `@streaming`)
- Performance benchmarking
- Integration smoke tests against live OpenFGA

## Contributing

1. Fork the repository
2. Create a feature branch
3. Add/modify Gherkin scenarios and WireMock mappings
4. Test across all SDK runners
5. Submit a pull request

See [CONTRIBUTING.md](CONTRIBUTING.md) for detailed guidelines.

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Support

- **Issues**: [GitHub Issues](https://github.com/openfga/sdk-conformance/issues)
- **Discussions**: [GitHub Discussions](https://github.com/openfga/sdk-conformance/discussions)
- **Documentation**: [docs/](docs/)

## Related Projects

- [OpenFGA](https://github.com/openfga/openfga) - Authorization server
- [SDK Generator](https://github.com/openfga/sdk-generator) - SDK generation tooling
- [Community SDKs](https://openfga.dev/docs/getting-started/setup-sdk) - Additional language support

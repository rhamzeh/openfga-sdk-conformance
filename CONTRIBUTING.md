# Contributing to OpenFGA SDK Conformance Test Suite

Thank you for your interest in contributing to the OpenFGA SDK Conformance Test Suite! This document provides guidelines for contributing new scenarios, improving existing tests, and maintaining the codebase.

## Code of Conduct

This project follows the [OpenFGA Code of Conduct](https://github.com/openfga/.github/blob/main/CODE_OF_CONDUCT.md). By participating, you agree to uphold this code.

## Getting Started

### Prerequisites

- **Git**: For version control
- **Docker**: For running WireMock server
- **Language-specific tools**: Depending on which SDK runner you're working with

### Development Setup

1. **Fork and clone**:
   ```bash
   git fork https://github.com/openfga/sdk-conformance.git
   git clone https://github.com/YOUR_USERNAME/sdk-conformance.git
   cd sdk-conformance
   ```

2. **Set up development environment**:
   ```bash
   # Start WireMock server
   docker run -d --name wiremock-dev \
     -p 8080:8080 \
     -v $(pwd)/wiremock:/home/wiremock \
     wiremock/wiremock:latest
   ```

3. **Verify setup**:
   ```bash
   curl http://localhost:8080/__admin/health
   ```

## Types of Contributions

### 1. New Test Scenarios

Add new Gherkin scenarios to cover additional SDK behaviors:

- **Core functionality**: Basic API operations that all SDKs must support
- **Authentication flows**: OAuth2, bearer tokens, client credentials
- **Error handling**: Various error conditions and edge cases
- **Performance scenarios**: Response time and resource usage tests

### 2. WireMock Mappings

Create or improve WireMock mappings for deterministic testing:

- **Request matching**: Precise request pattern matching
- **Response templates**: Realistic API responses
- **Error simulation**: Various failure modes
- **Timing control**: Delays and timeouts

### 3. SDK Runners

Implement or improve language-specific test runners:

- **Step definitions**: Gherkin step implementations
- **Client setup**: SDK configuration and initialization
- **Assertion helpers**: Response validation utilities
- **CI integration**: Automated test execution

### 4. Documentation

Improve documentation and examples:

- **Step vocabulary**: Document new step patterns
- **Migration guides**: Help migrate existing tests
- **Best practices**: Testing patterns and conventions
- **Troubleshooting**: Common issues and solutions

## Contribution Workflow

### 1. Create an Issue

Before starting work, create an issue to discuss:

- **Bug reports**: Describe the problem and expected behavior
- **Feature requests**: Explain the new functionality needed
- **Questions**: Ask for clarification or guidance

### 2. Create a Branch

Create a descriptive branch name:

```bash
git checkout -b feature/streaming-scenarios
git checkout -b fix/wiremock-mapping-headers
git checkout -b docs/step-vocabulary-update
```

### 3. Make Changes

Follow the established patterns and conventions:

#### Adding New Scenarios

1. **Write Gherkin feature**:
   ```gherkin
   @core
   Feature: Basic Authorization Check
     As an SDK user
     I want to check user permissions
     So that I can authorize access to resources

     Scenario: Successful authorization check
       Given I have a client configured with store "01H0H40Z9QDYNR71MRAG9FPE7M"
       When I call Check with user "user:alice" relation "viewer" object "document:readme"
       Then the response should be successful
       And the result should be "allowed: true"
   ```

2. **Create WireMock mapping**:
   ```json
   {
     "mappings": [
       {
         "id": "check-success-basic",
         "request": {
           "method": "POST",
           "urlPath": "/stores/01H0H40Z9QDYNR71MRAG9FPE7M/check",
           "bodyPatterns": [{"equalToJson": "..."}]
         },
         "response": {
           "status": 200,
           "bodyFileName": "check_success.json"
         }
       }
     ]
   }
   ```

3. **Add response fixture**:
   ```json
   {
     "allowed": true,
     "resolution": ""
   }
   ```

#### Updating SDK Runners

1. **Implement step definitions**:
   ```go
   func (s *StepContext) iHaveAClientConfiguredWithStore(storeID string) error {
       config := openfga.ClientConfiguration{
           ApiUrl:  s.baseURL,
           StoreId: &storeID,
       }
       client, err := openfga.NewSdkClient(&config)
       s.client = client
       return err
   }
   ```

2. **Add assertion helpers**:
   ```go
   func (s *StepContext) theResponseShouldBeSuccessful() error {
       if s.lastError != nil {
           return fmt.Errorf("expected successful response, got error: %v", s.lastError)
       }
       return nil
   }
   ```

### 4. Test Your Changes

#### Local Testing

```bash
# Test with specific SDK runner
cd runners/go
go test -tags=conformance -run="TestBasicCheck"

# Test WireMock mappings
curl -X POST http://localhost:8080/__admin/mappings/reset
curl -X POST http://localhost:8080/__admin/mappings/import \
  -H "Content-Type: application/json" \
  -d @wiremock/bundles/core_basic_check.json
```

#### Cross-Platform Testing

```bash
# Run across multiple SDK runners (if available)
./scripts/test-all-runners.sh features/core/basic_check.feature
```

### 5. Update Documentation

- **Step vocabulary**: Document new steps in `docs/STEP_VOCABULARY.md`
- **Changelog**: Add entry to `CHANGELOG.md`
- **README**: Update if adding new features or changing setup

### 6. Submit Pull Request

#### PR Title Format
- `feat: add streaming scenarios for NDJSON handling`
- `fix: correct WireMock mapping for auth token refresh`
- `docs: update step vocabulary documentation`

#### PR Description Template
```markdown
## Description
Brief description of the changes made.

## Type of Change
- [ ] Bug fix (non-breaking change which fixes an issue)
- [ ] New feature (non-breaking change which adds functionality)
- [ ] Breaking change (fix or feature that would cause existing functionality to not work as expected)
- [ ] Documentation update

## Testing
- [ ] Added new test scenarios
- [ ] Updated existing test scenarios
- [ ] Tested with Go runner
- [ ] Tested with JavaScript runner
- [ ] Tested with .NET runner
- [ ] Tested with Python runner
- [ ] Tested with Java runner

## Checklist
- [ ] My code follows the style guidelines of this project
- [ ] I have performed a self-review of my own code
- [ ] I have commented my code, particularly in hard-to-understand areas
- [ ] I have made corresponding changes to the documentation
- [ ] My changes generate no new warnings
- [ ] I have added tests that prove my fix is effective or that my feature works
- [ ] New and existing unit tests pass locally with my changes
```

## Style Guidelines

### Gherkin Style

- **Feature names**: Use descriptive, user-focused names
- **Scenario names**: Be specific about the behavior being tested
- **Step language**: Use consistent, portable vocabulary
- **Tags**: Apply appropriate scenario tags (@core, @auth, etc.)

### WireMock Style

- **Mapping IDs**: Use kebab-case with descriptive names
- **Request matching**: Be as specific as necessary, no more
- **Response files**: Use descriptive fixture filenames
- **Organization**: Group related mappings in bundles

### Code Style

Follow language-specific conventions for SDK runners:

- **Go**: Follow `go fmt` and standard Go conventions
- **JavaScript**: Use ESLint configuration
- **Python**: Follow PEP 8
- **.NET**: Follow Microsoft C# conventions
- **Java**: Follow Google Java Style Guide

## Review Process

### Automated Checks

All PRs must pass:

- **Linting**: Code style and format checks
- **Tests**: All existing tests must continue to pass
- **Cross-platform**: Core scenarios must work across all SDK runners

### Manual Review

Maintainers will review:

- **Correctness**: Do the changes work as intended?
- **Completeness**: Are all necessary components included?
- **Consistency**: Do the changes follow established patterns?
- **Documentation**: Are changes properly documented?

### Approval Process

- **One approval**: Required from a maintainer
- **Two approvals**: Required for breaking changes
- **All checks passing**: Automated tests must pass

## Release Process

### Version Bumping

Contributions affect versioning:

- **PATCH**: Bug fixes, documentation updates
- **MINOR**: New scenarios, non-breaking additions
- **MAJOR**: Breaking changes to step vocabulary or conventions

### SDK Integration

After release, SDK repositories update their conformance suite submodules:

```bash
# In SDK repository
git submodule update --remote conformance
cd conformance
git checkout v1.2.3
cd ..
git add conformance
git commit -m "Update conformance suite to v1.2.3"
```

## Getting Help

### Communication Channels

- **GitHub Issues**: Bug reports and feature requests
- **GitHub Discussions**: Questions and general discussion
- **Slack**: #sdk-conformance channel (for OpenFGA team members)

### Documentation

- **Workflow**: [docs/WORKFLOW.md](docs/WORKFLOW.md)
- **Portability**: [docs/PORTABILITY.md](docs/PORTABILITY.md)
- **Versioning**: [docs/VERSIONING.md](docs/VERSIONING.md)

### Common Questions

**Q: How do I test my changes locally?**
A: Start WireMock with Docker, then run the appropriate SDK runner tests.

**Q: What if my scenario doesn't work on all SDKs?**
A: Use capability tags like `@optional` or `@sdk-specific` to mark platform limitations.

**Q: How do I handle SDK-specific behavior?**
A: Focus on the behavior being tested, not the implementation. Use portable step vocabulary.

**Q: Can I add new step definitions?**
A: Yes, but ensure they're portable across all SDK languages and follow established patterns.

## Recognition

Contributors are recognized in:

- **CHANGELOG.md**: Credit for significant contributions
- **GitHub releases**: Contributor mentions in release notes
- **README.md**: Contributor acknowledgments

Thank you for contributing to the OpenFGA SDK Conformance Test Suite!

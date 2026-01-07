# Python SDK Conformance Runner

This directory contains the Python implementation of the OpenFGA SDK Conformance Test Suite using behave.

## Prerequisites

- Python 3.8 or later
- pip or pipenv
- Docker/Podman (for WireMock server)

## Setup

1. **Install dependencies**:
   ```bash
   pip install -r requirements.txt
   ```

   Or using virtual environment:
   ```bash
   python -m venv venv
   source venv/bin/activate  # On Windows: venv\Scripts\activate
   pip install -r requirements.txt
   ```

2. **Start WireMock server** (from repository root):
   ```bash
   make start CONTAINER_CMD=podman COMPOSE_CMD=podman-compose  # or docker
   make load-mappings
   ```

## Running Tests

### Run all conformance tests
```bash
behave
```

### Run specific scenarios by tags
```bash
behave --tags=core      # @core scenarios
behave --tags=headers   # @headers scenarios  
behave --tags=smoke     # @smoke scenarios
```

### Run with different output formats
```bash
behave --format=pretty           # Pretty console output
behave --format=json             # JSON output
behave --format=junit            # JUnit XML output
behave --format=html             # HTML report
```

### Run specific feature files
```bash
behave ../../features/apis/queries/check.feature
behave ../../features/request_configuration/headers.feature
```

## Development

### Project Structure
```
runners/python/
├── requirements.txt        # Python dependencies
├── behave.ini             # Behave configuration
├── environment.py         # Test context and hooks
├── steps/
│   ├── client_steps.py    # Client configuration steps
│   ├── api_steps.py       # API method steps
│   └── assertion_steps.py # Response assertions
└── README.md              # This file
```

### Adding New Step Definitions

1. Add the step function to the appropriate file in `steps/`
2. Use the `@given`, `@when`, or `@then` decorators with regex patterns
3. Access test context via `context.test_context`
4. Test with a simple scenario first

### Test Context

The `TestContext` class maintains state between steps in a scenario:
- `client`: OpenFGA client instance
- `config`: Client configuration
- `last_response`: Last API response
- `last_error`: Last error encountered
- `saved_data`: Data saved between steps (dict)
- `headers`: Request headers (dict)

### Debugging

Enable verbose output:
```bash
behave --verbose
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
- Basic project structure with behave
- pip requirements with dependencies
- Test context and environment setup
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

The Python runner follows the established conformance suite patterns:

1. **Behave Configuration**: `behave.ini` sets up test execution
2. **Environment Setup**: `environment.py` provides hooks and test context
3. **Step Definitions**: Modular step implementations across multiple files
4. **OpenFGA SDK Integration**: Direct usage of `openfga-sdk` package
5. **Assertion Framework**: assertpy with custom helpers

## Environment Variables

- `WIREMOCK_URL`: WireMock server URL (default: http://localhost:8080)
- `PYTHONPATH`: Include current directory for imports

## Virtual Environment

### Create and activate virtual environment
```bash
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate
pip install -r requirements.txt
```

### Deactivate virtual environment
```bash
deactivate
```

## Testing

### Run with coverage
```bash
pip install coverage
coverage run -m behave
coverage report
coverage html  # Generate HTML report
```

### Run with pytest (alternative)
```bash
pip install pytest-bdd
pytest --bdd-features=../../features/
```

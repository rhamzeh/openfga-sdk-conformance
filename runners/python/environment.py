import os
from openfga_sdk import OpenFgaClient, ClientConfiguration
from openfga_sdk.client.models import CheckRequest, ReadRequest, WriteRequest
from assertpy import assert_that


class TestContext:
    def __init__(self):
        self.client = None
        self.config = None
        self.last_response = None
        self.last_error = None
        self.saved_data = {}
        self.headers = {}
        self.response_headers = {}
        self.auth_config = {}
        self.status_code = None
        self.wiremock_url = os.getenv('WIREMOCK_URL', 'http://localhost:8080')
        self.retry_config = {}
        self.retry_attempts = []
        self.retry_delays = []
        self.circuit_breaker_triggered = False
        self.circuit_breaker_open = False
        self.circuit_breaker_failure_count = 0

    def reset(self):
        """Reset test context for new scenario"""
        self.client = None
        self.config = None
        self.last_response = None
        self.last_error = None
        self.saved_data = {}
        self.headers = {}
        self.response_headers = {}
        self.auth_config = {}
        self.status_code = None
        self.retry_config = {}
        self.retry_attempts = []
        self.retry_delays = []
        self.circuit_breaker_triggered = False
        self.circuit_breaker_open = False
        self.circuit_breaker_failure_count = 0

    async def create_client(self, config_dict):
        """Create OpenFGA client with configuration"""
        try:
            self.config = ClientConfiguration(
                api_url=self.wiremock_url,
                store_id=config_dict.get('store_id'),
                authorization_model_id=config_dict.get('authorization_model_id'),
                credentials=config_dict.get('credentials')
            )
            
            self.client = OpenFgaClient(self.config)
        except Exception as e:
            raise Exception(f"Failed to create OpenFGA client: {str(e)}")

    async def execute_api_call(self, api_call):
        """Execute API call and capture response/error"""
        try:
            self.last_response = await api_call()
            self.last_error = None
        except Exception as e:
            self.last_error = e
            self.last_response = None
            
            # Enhanced error handling with status code extraction
            self._extract_status_code_from_exception(e)

    def _extract_status_code_from_exception(self, exception):
        """Extract HTTP status code from various exception types"""
        # Handle different types of HTTP exceptions
        if hasattr(exception, 'status_code'):
            self.status_code = exception.status_code
        elif hasattr(exception, 'response') and hasattr(exception.response, 'status_code'):
            self.status_code = exception.response.status_code
            # Also capture response headers if available
            if hasattr(exception.response, 'headers'):
                self.response_headers = dict(exception.response.headers)
        elif hasattr(exception, 'code'):
            self.status_code = exception.code
        else:
            # Try to extract from error message
            error_msg = str(exception).lower()
            if '401' in error_msg or 'unauthorized' in error_msg:
                self.status_code = 401
            elif '403' in error_msg or 'forbidden' in error_msg:
                self.status_code = 403
            elif '404' in error_msg or 'not found' in error_msg:
                self.status_code = 404
            elif '400' in error_msg or 'bad request' in error_msg:
                self.status_code = 400

    def configure_authentication(self, method, config):
        """Enhanced authentication configuration"""
        if method == 'bearer':
            self.headers['Authorization'] = f"Bearer {config['token']}"
        elif method == 'client_credentials':
            # Store credentials for token exchange
            self.auth_config = {
                'method': 'client_credentials',
                'client_id': config.get('client_id', ''),
                'client_secret': config.get('client_secret', ''),
                'api_token_issuer': config.get('api_token_issuer', ''),
                'api_audience': config.get('api_audience', '')
            }
        else:
            raise ValueError(f"Unsupported authentication method: {method}")

    def get_response_header(self, header_name):
        """Response header inspection"""
        if hasattr(self.last_response, 'headers') and self.last_response.headers:
            return self.last_response.headers.get(header_name.lower())
        elif self.response_headers:
            return self.response_headers.get(header_name.lower())
        return None

    def get_status_code(self):
        """Enhanced status code checking"""
        if hasattr(self, 'status_code') and self.status_code:
            return self.status_code
        # Assume 200 for successful responses
        return 200 if self.last_response else None

    # Retry tracking methods
    def get_retry_count(self):
        """Get the number of retry attempts"""
        return len(self.retry_attempts)

    def get_retry_delays(self):
        """Get the list of retry delays"""
        return self.retry_delays.copy()

    def track_retry_attempt(self, delay=0):
        """Track a retry attempt with optional delay"""
        import time
        self.retry_attempts.append(time.time())
        if delay > 0:
            self.retry_delays.append(delay)

    def reset_retry_tracking(self):
        """Reset retry tracking for new test"""
        self.retry_attempts = []
        self.retry_delays = []
        self.circuit_breaker_triggered = False
        self.circuit_breaker_open = False
        self.circuit_breaker_failure_count = 0

    # Assertion helpers
    def assert_response_success(self):
        """Assert that the last response was successful"""
        if self.last_error:
            raise AssertionError(f"Expected successful response, got error: {self.last_error}")
        assert_that(self.last_response).is_not_none()

    def assert_response_failure(self):
        """Assert that the last response was an error"""
        if not self.last_error:
            raise AssertionError("Expected error response, but got success")

    def assert_response_contains_items(self, count):
        """Assert response contains specific number of items"""
        self.assert_response_success()
        
        if hasattr(self.last_response, 'tuples'):
            actual_count = len(self.last_response.tuples or [])
            assert_that(actual_count).is_equal_to(count)
        else:
            raise AssertionError("Response does not contain tuples")

    def assert_tuples_include(self, expected_tuples):
        """Assert response includes expected tuples"""
        self.assert_response_success()
        
        if hasattr(self.last_response, 'tuples'):
            actual_tuples = self.last_response.tuples or []
            
            for expected in expected_tuples:
                found = any(
                    tuple_obj.key.user == expected['user'] and
                    tuple_obj.key.relation == expected['relation'] and
                    tuple_obj.key.object == expected['object']
                    for tuple_obj in actual_tuples
                )
                
                if not found:
                    raise AssertionError(
                        f"Expected tuple not found: user={expected['user']}, "
                        f"relation={expected['relation']}, object={expected['object']}"
                    )
        else:
            raise AssertionError("Response does not contain tuples")

    # Save and compare responses
    def save_response(self, key):
        """Save current response for later comparison"""
        if not self.last_response:
            raise Exception("No response to save")
        self.saved_data[key] = self.last_response

    def compare_with_saved(self, key, should_match=True):
        """Compare current response with saved response"""
        if key not in self.saved_data:
            raise Exception(f"No saved data found for key: {key}")
        
        if not self.last_response:
            raise Exception("No current response to compare")

        # Simple comparison using string representation
        current_str = str(self.last_response)
        saved_str = str(self.saved_data[key])
        matches = current_str == saved_str

        if should_match and not matches:
            raise AssertionError(f"Current response does not match saved response for key: {key}")
        elif not should_match and matches:
            raise AssertionError(f"Current response matches saved response for key: {key} (expected different)")


def before_all(context):
    """Set up test environment before all scenarios"""
    context.test_context = TestContext()


def before_scenario(context, scenario):
    """Reset context before each scenario"""
    context.test_context.reset()


def after_scenario(context, scenario):
    """Clean up after each scenario"""
    if scenario.status == "failed":
        print(f"Scenario failed: {scenario.name}")
        if context.test_context.last_error:
            print(f"Last error: {context.test_context.last_error}")

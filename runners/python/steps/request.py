from behave import given, when, then
from assertpy import assert_that

# Header validation step definitions for Python SDK

@when('I set the request header "{header_name}" to "{header_value}"')
def step_set_request_header(context, header_name, header_value):
    if not hasattr(context.test_context, 'request_headers'):
        context.test_context.request_headers = {}
    context.test_context.request_headers[header_name] = header_value

@when('I clear the request header "{header_name}"')
def step_clear_request_header(context, header_name):
    if hasattr(context.test_context, 'request_headers') and context.test_context.request_headers:
        context.test_context.request_headers.pop(header_name, None)

@then('the request should have included header "{header_name}" with value "{expected_value}"')
def step_request_should_have_header_with_value(context, header_name, expected_value):
    if not hasattr(context.test_context, 'captured_request_headers') or not context.test_context.captured_request_headers:
        raise AssertionError('No request headers were captured')
    
    actual_value = find_header_value(context.test_context.captured_request_headers, header_name)
    if actual_value is None:
        raise AssertionError(f"Request header '{header_name}' was not found")
    
    if actual_value != expected_value:
        raise AssertionError(f"Request header '{header_name}' expected value '{expected_value}', got '{actual_value}'")

@then('the request should have included header "{header_name}"')
def step_request_should_have_header(context, header_name):
    if not hasattr(context.test_context, 'captured_request_headers') or not context.test_context.captured_request_headers:
        raise AssertionError('No request headers were captured')
    
    actual_value = find_header_value(context.test_context.captured_request_headers, header_name)
    if actual_value is None:
        raise AssertionError(f"Request header '{header_name}' was not found")

@then('the request should not have included header "{header_name}"')
def step_request_should_not_have_header(context, header_name):
    if not hasattr(context.test_context, 'captured_request_headers') or not context.test_context.captured_request_headers:
        return  # No headers captured means header wasn't included
    
    actual_value = find_header_value(context.test_context.captured_request_headers, header_name)
    if actual_value is not None:
        raise AssertionError(f"Request header '{header_name}' was found but should not have been included")

@then('the response should include header "{header_name}" with value "{expected_value}"')
def step_response_should_include_header_with_value(context, header_name, expected_value):
    if not context.test_context.last_response:
        raise AssertionError('No response received')
    
    response_headers = extract_response_headers(context.test_context)
    if not response_headers:
        raise AssertionError('No response headers available')
    
    actual_value = find_header_value(response_headers, header_name)
    if actual_value is None:
        raise AssertionError(f"Response header '{header_name}' was not found")
    
    if actual_value != expected_value:
        raise AssertionError(f"Response header '{header_name}' expected value '{expected_value}', got '{actual_value}'")

@then('the response should include header "{header_name}"')
def step_response_should_include_header(context, header_name):
    if not context.test_context.last_response:
        raise AssertionError('No response received')
    
    response_headers = extract_response_headers(context.test_context)
    if not response_headers:
        raise AssertionError('No response headers available')
    
    actual_value = find_header_value(response_headers, header_name)
    if actual_value is None:
        raise AssertionError(f"Response header '{header_name}' was not found")

@then('the authorization header should contain "{expected_substring}"')
def step_authorization_header_should_contain(context, expected_substring):
    if not hasattr(context.test_context, 'captured_request_headers') or not context.test_context.captured_request_headers:
        raise AssertionError('No request headers were captured')
    
    auth_value = find_header_value(context.test_context.captured_request_headers, 'Authorization')
    if auth_value is None:
        raise AssertionError('Authorization header was not found')
    
    if expected_substring not in auth_value:
        raise AssertionError(f"Authorization header '{auth_value}' does not contain '{expected_substring}'")

@then('the content length should be greater than {min_length:d}')
def step_content_length_should_be_greater_than(context, min_length):
    if not hasattr(context.test_context, 'captured_request_headers') or not context.test_context.captured_request_headers:
        raise AssertionError('No request headers were captured')
    
    content_length_str = find_header_value(context.test_context.captured_request_headers, 'Content-Length')
    if content_length_str is None:
        raise AssertionError('Content-Length header was not found')
    
    try:
        content_length = int(content_length_str)
    except ValueError:
        raise AssertionError(f"Invalid Content-Length value: {content_length_str}")
    
    if content_length <= min_length:
        raise AssertionError(f"Content-Length {content_length} is not greater than {min_length}")

@then('both responses should be successful')
def step_both_responses_should_be_successful(context):
    if not hasattr(context.test_context, 'response_history') or len(context.test_context.response_history) < 2:
        response_count = len(context.test_context.response_history) if hasattr(context.test_context, 'response_history') else 0
        raise AssertionError(f"Expected at least 2 responses, got {response_count}")
    
    # Check last two responses
    last_two = context.test_context.response_history[-2:]
    for i, response_record in enumerate(last_two):
        if response_record.get('error'):
            raise AssertionError(f"Response {i + 1} failed: {response_record['error']}")

@then('both requests should have included header "{header_name}" with value "{expected_value}"')
def step_both_requests_should_have_header_with_value(context, header_name, expected_value):
    if not hasattr(context.test_context, 'request_header_history') or len(context.test_context.request_header_history) < 2:
        history_count = len(context.test_context.request_header_history) if hasattr(context.test_context, 'request_header_history') else 0
        raise AssertionError(f"Expected at least 2 request header sets, got {history_count}")
    
    # Check last two request header sets
    last_two = context.test_context.request_header_history[-2:]
    for i, headers in enumerate(last_two):
        actual_value = find_header_value(headers, header_name)
        if actual_value is None:
            raise AssertionError(f"Request {i + 1} header '{header_name}' was not found")
        if actual_value != expected_value:
            raise AssertionError(f"Request {i + 1} header '{header_name}' expected value '{expected_value}', got '{actual_value}'")

@then('the first request should have included header "{header_name}" with value "{expected_value}"')
def step_first_request_should_have_header_with_value(context, header_name, expected_value):
    if not hasattr(context.test_context, 'request_header_history') or len(context.test_context.request_header_history) < 1:
        raise AssertionError('No request headers in history')
    
    first_headers = context.test_context.request_header_history[0]
    actual_value = find_header_value(first_headers, header_name)
    if actual_value is None:
        raise AssertionError(f"First request header '{header_name}' was not found")
    if actual_value != expected_value:
        raise AssertionError(f"First request header '{header_name}' expected value '{expected_value}', got '{actual_value}'")

@then('the second request should not have included header "{header_name}"')
def step_second_request_should_not_have_header(context, header_name):
    if not hasattr(context.test_context, 'request_header_history') or len(context.test_context.request_header_history) < 2:
        history_count = len(context.test_context.request_header_history) if hasattr(context.test_context, 'request_header_history') else 0
        raise AssertionError(f"Expected at least 2 request header sets, got {history_count}")
    
    second_headers = context.test_context.request_header_history[1]
    actual_value = find_header_value(second_headers, header_name)
    if actual_value is not None:
        raise AssertionError(f"Second request header '{header_name}' was found but should not have been included")

@when('I make a preflight request to Check endpoint')
def step_make_preflight_request(context):
    # Placeholder for CORS preflight request
    raise NotImplementedError('Preflight requests not yet implemented in Python SDK')

@then('the preflight response should be successful')
def step_preflight_response_should_be_successful(context):
    # Placeholder for preflight response validation
    raise NotImplementedError('Preflight response validation not yet implemented')

# Helper functions

def find_header_value(headers, header_name):
    """Find header value with case-insensitive matching"""
    if not headers:
        return None
    
    # Check for exact match first
    if header_name in headers:
        return headers[header_name]
    
    # Case-insensitive search
    lower_header_name = header_name.lower()
    for name, value in headers.items():
        if name.lower() == lower_header_name:
            return value
    
    return None

def extract_response_headers(test_context):
    """Extract headers from response object"""
    if not test_context.last_response:
        return None
    
    # Check if response has headers attribute
    if hasattr(test_context.last_response, 'headers'):
        return test_context.last_response.headers
    
    # Check if response has _headers attribute
    if hasattr(test_context.last_response, '_headers'):
        return test_context.last_response._headers
    
    # Return stored response headers if available
    if hasattr(test_context, 'last_response_headers'):
        return test_context.last_response_headers
    
    return {}

def capture_request_headers(test_context, request_config=None):
    """Capture request headers for validation"""
    if not hasattr(test_context, 'captured_request_headers'):
        test_context.captured_request_headers = {}
    
    # Capture headers from request configuration
    if request_config and hasattr(request_config, 'headers'):
        test_context.captured_request_headers.update(request_config.headers)
    
    # Add custom request headers
    if hasattr(test_context, 'request_headers') and test_context.request_headers:
        test_context.captured_request_headers.update(test_context.request_headers)
    
    # Store in history
    if not hasattr(test_context, 'request_header_history'):
        test_context.request_header_history = []
    test_context.request_header_history.append(dict(test_context.captured_request_headers))

def store_response_in_history(test_context, response, error=None):
    """Store response in history for multi-response validation"""
    if not hasattr(test_context, 'response_history'):
        test_context.response_history = []
    test_context.response_history.append({'response': response, 'error': error})

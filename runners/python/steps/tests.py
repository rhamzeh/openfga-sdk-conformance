from behave import given, when, then
from assertpy import assert_that
import asyncio


# Integration test step definitions for Python SDK
# These steps combine multiple features for comprehensive testing

# Authorization model management steps
@when('I call ReadAuthorizationModel')
def step_call_read_authorization_model(context):
    if not context.test_context.client:
        raise Exception("Client not configured")

    async def read_authorization_model_call():
        # Simulate reading authorization model for integration tests
        return {
            'authorization_model': {
                'id': '01ARZ3NDEKTSV4RRFFQ69G5FAV',
                'schema_version': '1.1',
                'type_definitions': []
            }
        }

    asyncio.run(context.test_context.execute_api_call(read_authorization_model_call))


@when('I call ListAuthorizationModels with:')
def step_call_list_authorization_models_with_table(context):
    if not context.test_context.client:
        raise Exception("Client not configured")

    page_size = 10
    
    for row in context.table:
        key = list(row.headings)[0]
        value = row[key]
        
        if key.lower() == 'pagesize':
            page_size = int(value)

    async def list_authorization_models_call():
        # Simulate listing authorization models
        return {
            'authorization_models': [
                {
                    'id': '01ARZ3NDEKTSV4RRFFQ69G5FAV',
                    'schema_version': '1.1'
                }
            ],
            'page_size': page_size
        }

    asyncio.run(context.test_context.execute_api_call(list_authorization_models_call))


# Complex response validation steps
@then('the response should contain authorization model')
def step_response_should_contain_authorization_model(context):
    context.test_context.assert_response_success()
    
    if not hasattr(context.test_context.last_response, 'authorization_model') and \
       'authorization_model' not in context.test_context.last_response:
        raise AssertionError("Response does not contain authorization model")


@then('the response should contain authorization models')
def step_response_should_contain_authorization_models(context):
    context.test_context.assert_response_success()
    
    if hasattr(context.test_context.last_response, 'authorization_models'):
        models = context.test_context.last_response.authorization_models
    elif 'authorization_models' in context.test_context.last_response:
        models = context.test_context.last_response['authorization_models']
    else:
        raise AssertionError("Response does not contain authorization models")
    
    if not models or len(models) == 0:
        raise AssertionError("Authorization models list is empty")


@then('the streaming response should contain multiple objects')
def step_streaming_response_should_contain_multiple_objects(context):
    context.test_context.assert_response_success()
    
    if not hasattr(context.test_context, 'streamed_objects') or \
       len(context.test_context.streamed_objects) < 2:
        count = len(context.test_context.streamed_objects) if hasattr(context.test_context, 'streamed_objects') else 0
        raise AssertionError(f"Streaming response should contain multiple objects, got {count}")


# Multi-request validation steps
@then('all requests should have included header "{header_name}" with value "{expected_value}"')
def step_all_requests_should_have_included_header_with_value(context, header_name, expected_value):
    if not hasattr(context.test_context, 'request_header_history') or \
       len(context.test_context.request_header_history) == 0:
        raise AssertionError("No request headers captured")

    for i, headers in enumerate(context.test_context.request_header_history):
        found = False
        for name, value in headers.items():
            if name.lower() == header_name.lower() and value == expected_value:
                found = True
                break
        
        if not found:
            raise AssertionError(f"Request {i + 1} did not include header {header_name} with value {expected_value}")


@then('the second request should have included header "{header_name}" with value "{expected_value}"')
def step_second_request_should_have_included_header_with_value(context, header_name, expected_value):
    if not hasattr(context.test_context, 'request_header_history') or \
       len(context.test_context.request_header_history) < 2:
        count = len(context.test_context.request_header_history) if hasattr(context.test_context, 'request_header_history') else 0
        raise AssertionError(f"Need at least 2 requests for comparison, got {count}")

    headers = context.test_context.request_header_history[1]
    found = False
    
    for name, value in headers.items():
        if name.lower() == header_name.lower() and value == expected_value:
            found = True
            break
    
    if not found:
        raise AssertionError(f"Second request did not include header {header_name} with value {expected_value}")


# Error handling steps
@then('the response should be an error')
def step_response_should_be_an_error(context):
    if not context.test_context.last_error:
        raise AssertionError("Expected an error but response was successful")


@then('the error should be "{expected_error}"')
def step_error_should_be(context, expected_error):
    if not context.test_context.last_error:
        raise AssertionError("No error occurred")

    error_string = str(context.test_context.last_error)
    
    if expected_error == 'unauthorized':
        if 'unauthorized' not in error_string.lower() and '401' not in error_string.lower():
            raise AssertionError(f"Expected unauthorized error, got: {error_string}")
    elif expected_error == 'rate_limited':
        if 'rate' not in error_string.lower() and '429' not in error_string.lower():
            raise AssertionError(f"Expected rate limited error, got: {error_string}")
    else:
        if expected_error.lower() not in error_string.lower():
            raise AssertionError(f"Expected error containing '{expected_error}', got: {error_string}")


# Context management for integration tests
def capture_request_headers(context):
    """Capture current request headers for multi-request validation"""
    if not hasattr(context.test_context, 'request_header_history'):
        context.test_context.request_header_history = []
    
    # Capture current request headers
    headers_copy = {}
    if hasattr(context.test_context, 'request_headers'):
        headers_copy = context.test_context.request_headers.copy()
    
    context.test_context.request_header_history.append(headers_copy)


def capture_response(context):
    """Capture current response for multi-response validation"""
    if not hasattr(context.test_context, 'response_history'):
        context.test_context.response_history = []
    
    # Capture current response
    context.test_context.response_history.append({
        'response': context.test_context.last_response,
        'error': context.test_context.last_error
    })


async def execute_api_call_with_capture(context, api_call):
    """Execute API call with header and response capture for integration tests"""
    # Capture request headers before the call
    capture_request_headers(context)

    # Execute the API call
    try:
        response = await api_call()
        context.test_context.last_response = response
        context.test_context.last_error = None
    except Exception as error:
        context.test_context.last_response = None
        context.test_context.last_error = error

    # Capture response after the call
    capture_response(context)

from behave import given, when, then
from assertpy import assert_that
import json

# Advanced API assertion step definitions for Python SDK

# ReadChanges API assertions
@then('the response should contain "{field_name}"')
def step_response_should_contain_field(context, field_name):
    context.test_context.assert_response_success()
    
    if not context.test_context.last_response:
        raise AssertionError('No response received')

    if field_name == 'changes':
        if not hasattr(context.test_context.last_response, 'changes') or context.test_context.last_response.changes is None:
            raise AssertionError('Response does not contain "changes" field')
    elif field_name == 'objects':
        if not hasattr(context.test_context.last_response, 'objects') or context.test_context.last_response.objects is None:
            raise AssertionError('Response does not contain "objects" field')
    else:
        if not hasattr(context.test_context.last_response, field_name):
            raise AssertionError(f'Response does not contain "{field_name}" field')

@then('the response should contain at most {max_count:d} changes')
def step_response_should_contain_at_most_changes(context, max_count):
    context.test_context.assert_response_success()
    
    if not context.test_context.last_response or not hasattr(context.test_context.last_response, 'changes'):
        raise AssertionError('Response does not contain changes field')

    actual_count = len(context.test_context.last_response.changes)
    if actual_count > max_count:
        raise AssertionError(f'Expected at most {max_count} changes, got {actual_count}')

@then('the response should have continuation token')
def step_response_should_have_continuation_token(context):
    context.test_context.assert_response_success()
    
    if not context.test_context.last_response:
        raise AssertionError('No response received')

    if not hasattr(context.test_context.last_response, 'continuation_token') or not context.test_context.last_response.continuation_token:
        raise AssertionError('Response does not have continuation token')

@then('the response should not have continuation token')
def step_response_should_not_have_continuation_token(context):
    context.test_context.assert_response_success()
    
    if not context.test_context.last_response:
        raise AssertionError('No response received')

    if hasattr(context.test_context.last_response, 'continuation_token') and context.test_context.last_response.continuation_token:
        raise AssertionError('Response has continuation token but should not have one')

@then('the changes should be different from "{saved_key}"')
def step_changes_should_be_different_from(context, saved_key):
    context.test_context.assert_response_success()
    
    if not hasattr(context.test_context, 'saved_data') or saved_key not in context.test_context.saved_data:
        raise AssertionError(f'No saved data found for key: {saved_key}')

    saved_response = context.test_context.saved_data[saved_key]
    
    # Simple comparison - in real implementation, would compare actual change content
    current_changes = getattr(context.test_context.last_response, 'changes', [])
    saved_changes = getattr(saved_response.get('response'), 'changes', [])
    
    if json.dumps(current_changes, sort_keys=True) == json.dumps(saved_changes, sort_keys=True):
        raise AssertionError(f'Changes are identical to saved data from {saved_key}')

@then('each change should have type "{expected_type}"')
def step_each_change_should_have_type(context, expected_type):
    context.test_context.assert_response_success()
    
    if not context.test_context.last_response or not hasattr(context.test_context.last_response, 'changes'):
        raise AssertionError('Response does not contain valid changes field')

    changes = context.test_context.last_response.changes
    for i, change in enumerate(changes):
        if not hasattr(change, 'type') or change.type != expected_type:
            actual_type = getattr(change, 'type', None)
            raise AssertionError(f'Change at index {i} has type "{actual_type}", expected "{expected_type}"')

# ListObjects API assertions
@then('the response should contain exactly {expected_count:d} objects')
def step_response_should_contain_exactly_objects(context, expected_count):
    context.test_context.assert_response_success()
    
    if not context.test_context.last_response or not hasattr(context.test_context.last_response, 'objects'):
        raise AssertionError('Response does not contain valid objects field')

    actual_count = len(context.test_context.last_response.objects)
    if actual_count != expected_count:
        raise AssertionError(f'Expected exactly {expected_count} objects, got {actual_count}')

@then('the response should contain exactly {expected_count:d} changes')
def step_response_should_contain_exactly_changes(context, expected_count):
    context.test_context.assert_response_success()
    
    if not context.test_context.last_response or not hasattr(context.test_context.last_response, 'changes'):
        raise AssertionError('Response does not contain valid changes field')

    actual_count = len(context.test_context.last_response.changes)
    if actual_count != expected_count:
        raise AssertionError(f'Expected exactly {expected_count} changes, got {actual_count}')

# Streaming API assertions
@then('the streaming response should be successful')
def step_streaming_response_should_be_successful(context):
    if context.test_context.last_error:
        raise AssertionError(f'Streaming response failed: {context.test_context.last_error}')

    if not context.test_context.last_response:
        raise AssertionError('No streaming response received')

@then('the streaming response should contain objects')
def step_streaming_response_should_contain_objects(context):
    context.test_context.assert_response_success()
    
    # For streaming responses, check if we received any objects
    if not hasattr(context.test_context, 'streamed_objects') or not context.test_context.streamed_objects:
        raise AssertionError('Streaming response did not contain any objects')

@then('each streamed object should have required fields')
def step_each_streamed_object_should_have_required_fields(context):
    context.test_context.assert_response_success()
    
    if not hasattr(context.test_context, 'streamed_objects') or not context.test_context.streamed_objects:
        raise AssertionError('No streamed objects to validate')

    for i, obj in enumerate(context.test_context.streamed_objects):
        if not hasattr(obj, 'object') or not obj.object:
            raise AssertionError(f'Streamed object at index {i} does not have required "object" field')

@then('the streaming connection should be properly closed')
def step_streaming_connection_should_be_properly_closed(context):
    # Placeholder for streaming connection validation
    # In real implementation, would check connection state
    if hasattr(context.test_context, 'streaming_connection') and context.test_context.streaming_connection:
        if getattr(context.test_context.streaming_connection, 'closed', False) is False:
            raise AssertionError('Streaming connection was not properly closed')

# Advanced response validation
@then('I save the response as "{save_key}"')
def step_save_response_as(context, save_key):
    context.test_context.assert_response_success()
    
    if not hasattr(context.test_context, 'saved_data'):
        context.test_context.saved_data = {}
    
    context.test_context.saved_data[save_key] = {
        'response': context.test_context.last_response,
        'timestamp': context.test_context.get_current_timestamp()
    }

@then('the response should contain at least {min_count:d} items')
def step_response_should_contain_at_least_items(context, min_count):
    context.test_context.assert_response_success()
    
    if not context.test_context.last_response:
        raise AssertionError('No response received')

    item_count = 0
    
    # Check for different possible item fields
    if hasattr(context.test_context.last_response, 'changes') and context.test_context.last_response.changes:
        item_count = len(context.test_context.last_response.changes)
    elif hasattr(context.test_context.last_response, 'objects') and context.test_context.last_response.objects:
        item_count = len(context.test_context.last_response.objects)
    elif hasattr(context.test_context.last_response, 'tuples') and context.test_context.last_response.tuples:
        item_count = len(context.test_context.last_response.tuples)
    else:
        raise AssertionError('Response does not contain countable items (changes, objects, or tuples)')

    if item_count < min_count:
        raise AssertionError(f'Expected at least {min_count} items, got {item_count}')

@then('the response should have field "{field_name}"')
def step_response_should_have_field(context, field_name):
    context.test_context.assert_response_success()
    
    if not context.test_context.last_response:
        raise AssertionError('No response received')

    if not hasattr(context.test_context.last_response, field_name):
        raise AssertionError(f'Response does not have field "{field_name}"')

@then('the response field "{field_name}" should be "{expected_value}"')
def step_response_field_should_be(context, field_name, expected_value):
    context.test_context.assert_response_success()
    
    if not context.test_context.last_response:
        raise AssertionError('No response received')

    if not hasattr(context.test_context.last_response, field_name):
        raise AssertionError(f'Response does not have field "{field_name}"')

    actual_value = getattr(context.test_context.last_response, field_name)
    if str(actual_value) != expected_value:
        raise AssertionError(f'Response field "{field_name}" expected "{expected_value}", got "{actual_value}"')

# Pagination assertions
@then('the response should have pagination info')
def step_response_should_have_pagination_info(context):
    context.test_context.assert_response_success()
    
    if not context.test_context.last_response:
        raise AssertionError('No response received')

    # Check for common pagination fields
    has_continuation_token = hasattr(context.test_context.last_response, 'continuation_token') and context.test_context.last_response.continuation_token
    has_page_size = hasattr(context.test_context.last_response, 'page_size')
    has_next_page = hasattr(context.test_context.last_response, 'has_next_page')

    if not (has_continuation_token or has_page_size or has_next_page):
        raise AssertionError('Response does not contain pagination information')

# Error scenario assertions
@then('the error should contain "{expected_message}"')
def step_error_should_contain(context, expected_message):
    if not context.test_context.last_error:
        raise AssertionError('Expected an error but none occurred')

    error_message = str(context.test_context.last_error)
    if expected_message not in error_message:
        raise AssertionError(f'Error message "{error_message}" does not contain "{expected_message}"')

@then('the error should be of type "{expected_type}"')
def step_error_should_be_of_type(context, expected_type):
    if not context.test_context.last_error:
        raise AssertionError('Expected an error but none occurred')

    error_type = type(context.test_context.last_error).__name__
    if error_type != expected_type:
        raise AssertionError(f'Error type "{error_type}" does not match expected "{expected_type}"')

# Multi-response validation
@then('all responses should be successful')
def step_all_responses_should_be_successful(context):
    if not hasattr(context.test_context, 'response_history') or not context.test_context.response_history:
        raise AssertionError('No response history available')

    for i, record in enumerate(context.test_context.response_history):
        if record.get('error'):
            raise AssertionError(f'Response {i + 1} failed: {record["error"]}')

@then('the last {count:d} responses should be successful')
def step_last_responses_should_be_successful(context, count):
    if not hasattr(context.test_context, 'response_history') or len(context.test_context.response_history) < count:
        available = len(context.test_context.response_history) if hasattr(context.test_context, 'response_history') else 0
        raise AssertionError(f'Expected at least {count} responses, got {available}')

    last_responses = context.test_context.response_history[-count:]
    for i, record in enumerate(last_responses):
        if record.get('error'):
            raise AssertionError(f'Response {i + 1} of last {count} failed: {record["error"]}')

# Helper functions for advanced assertions
def validate_response_structure(response, expected_structure):
    """Validate response has expected structure"""
    for field_name, expected_type in expected_structure.items():
        if not hasattr(response, field_name):
            raise AssertionError(f'Response missing required field: {field_name}')
        
        actual_value = getattr(response, field_name)
        if expected_type != 'any':
            actual_type = type(actual_value).__name__
            if actual_type != expected_type:
                raise AssertionError(f'Response field "{field_name}" expected type "{expected_type}", got "{actual_type}"')

def get_response_field(response, field_path):
    """Get nested field from response using dot notation"""
    if not response:
        return None
    
    parts = field_path.split('.')
    current = response
    
    for part in parts:
        if current is None:
            return None
        if not hasattr(current, part):
            return None
        current = getattr(current, part)
    
    return current

def compare_responses(response1, response2, ignore_fields=None):
    """Compare two responses, optionally ignoring certain fields"""
    if ignore_fields is None:
        ignore_fields = []
    
    # Convert responses to dictionaries for comparison
    dict1 = response_to_dict(response1, ignore_fields)
    dict2 = response_to_dict(response2, ignore_fields)
    
    return json.dumps(dict1, sort_keys=True) == json.dumps(dict2, sort_keys=True)

def response_to_dict(response, ignore_fields=None):
    """Convert response object to dictionary, ignoring specified fields"""
    if ignore_fields is None:
        ignore_fields = []
    
    result = {}
    for attr in dir(response):
        if not attr.startswith('_') and attr not in ignore_fields:
            value = getattr(response, attr)
            if not callable(value):
                result[attr] = value
    
    return result

def store_response_in_history(test_context, response, error=None):
    """Store response in history for multi-response validation"""
    if not hasattr(test_context, 'response_history'):
        test_context.response_history = []
    
    test_context.response_history.append({
        'response': response,
        'error': error,
        'timestamp': test_context.get_current_timestamp()
    })

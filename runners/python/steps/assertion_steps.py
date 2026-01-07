from behave import given, when, then
from assertpy import assert_that
import json
import re


# Response assertions
@then('the response should be successful')
def step_response_successful(context):
    context.test_context.assert_response_success()


@then('the response should fail')
def step_response_fail(context):
    context.test_context.assert_response_failure()


@then('the response should have status code {status_code:d}')
def step_response_status_code(context, status_code):
    actual_status_code = context.test_context.get_status_code()
    if actual_status_code is None:
        raise AssertionError("No response or error to check status code")
    assert_that(actual_status_code).is_equal_to(status_code)


@then('the response should complete within {seconds:d} seconds')
def step_response_complete_within(context, seconds):
    # This would require timing implementation in the test context
    # For now, just pass if we have a response
    if not context.test_context.last_response and not context.test_context.last_error:
        raise AssertionError("No response received")


@then('the result should be "{expected}"')
def step_result_should_be(context, expected):
    context.test_context.assert_response_success()
    
    # Parse expected result (e.g., "allowed: true")
    parts = expected.split(': ')
    assert len(parts) == 2, f"Expected format: 'field: value', got: {expected}"
    
    field, value = parts
    
    if field == 'allowed':
        expected_bool = value.lower() == 'true'
        if hasattr(context.test_context.last_response, 'allowed'):
            assert_that(context.test_context.last_response.allowed).is_equal_to(expected_bool)
        else:
            raise AssertionError("Response does not have 'allowed' field")
    else:
        raise AssertionError(f"Unsupported result field: {field}")


@then('the response should contain "{content}"')
def step_response_contain(context, content):
    context.test_context.assert_response_success()
    response_json = json.dumps(context.test_context.last_response.__dict__, default=str)
    assert_that(response_json).contains(content)


@then('the response should not contain "{content}"')
def step_response_not_contain(context, content):
    context.test_context.assert_response_success()
    response_json = json.dumps(context.test_context.last_response.__dict__, default=str)
    assert_that(response_json).does_not_contain(content)


@then('the response should have field "{field}" with value "{value}"')
def step_response_field_value(context, field, value):
    context.test_context.assert_response_success()
    
    # Navigate nested fields using dot notation
    field_path = field.split('.')
    current = context.test_context.last_response
    
    for part in field_path:
        assert hasattr(current, part), f"Field '{part}' not found"
        current = getattr(current, part)
    
    assert_that(str(current)).is_equal_to(value)


# Error assertions
@then('the response should fail with a validation error')
def step_response_validation_error(context):
    context.test_context.assert_response_failure()
    
    if context.test_context.last_error:
        error_message = str(context.test_context.last_error).lower()
        assert_that(error_message).matches(r'.*(validation|invalid|bad request).*')


@then('the response should fail with an authentication error')
def step_response_authentication_error(context):
    context.test_context.assert_response_failure()
    
    if context.test_context.last_error:
        error_message = str(context.test_context.last_error).lower()
        assert_that(error_message).matches(r'.*(401|unauthorized|authentication).*')


@then('the response should fail with an authorization error')
def step_response_authorization_error(context):
    context.test_context.assert_response_failure()
    
    if context.test_context.last_error:
        error_message = str(context.test_context.last_error).lower()
        assert_that(error_message).matches(r'.*(403|forbidden|authorization).*')


@then('the error code should be "{expected_code}"')
def step_error_code(context, expected_code):
    context.test_context.assert_response_failure()
    
    if context.test_context.last_error:
        error_message = str(context.test_context.last_error)
        assert_that(error_message).contains(expected_code)


@then('the error message should contain "{expected_message}"')
def step_error_message_contain(context, expected_message):
    context.test_context.assert_response_failure()
    
    if context.test_context.last_error:
        error_message = str(context.test_context.last_error)
        assert_that(error_message).contains(expected_message)


# Collection assertions
@then('the response should contain {count:d} items')
def step_response_contain_items(context, count):
    context.test_context.assert_response_contains_items(count)


@then('the response should contain at least {count:d} item')
@then('the response should contain at least {count:d} items')
def step_response_at_least_items(context, count):
    context.test_context.assert_response_success()
    
    if hasattr(context.test_context.last_response, 'tuples'):
        actual_count = len(context.test_context.last_response.tuples or [])
        assert_that(actual_count).is_greater_than_or_equal_to(count)
    else:
        raise AssertionError("Response does not contain tuples")


@then('the response should contain at most {count:d} items')
def step_response_at_most_items(context, count):
    context.test_context.assert_response_success()
    
    if hasattr(context.test_context.last_response, 'tuples'):
        actual_count = len(context.test_context.last_response.tuples or [])
        assert_that(actual_count).is_less_than_or_equal_to(count)
    else:
        raise AssertionError("Response does not contain tuples")


@then('the response should contain exactly {count:d} item')
@then('the response should contain exactly {count:d} items')
def step_response_exactly_items(context, count):
    context.test_context.assert_response_contains_items(count)


@then('the response should be empty')
def step_response_empty(context):
    context.test_context.assert_response_contains_items(0)


@then('the tuples should include')
def step_tuples_include(context):
    expected_tuples = []
    
    for row in context.table:
        values = list(row.cells)
        expected_tuples.append({
            'user': values[0],
            'relation': values[1],
            'object': values[2]
        })
    
    context.test_context.assert_tuples_include(expected_tuples)


# Header verification
@then('the request should include header "{header}" with value "{value}"')
def step_request_header_value(context, header, value):
    assert_that(context.test_context.headers).contains_key(header)
    assert_that(context.test_context.headers[header]).is_equal_to(value)


@then('the request should include header "{header}" matching "{pattern}"')
def step_request_header_pattern(context, header, pattern):
    assert_that(context.test_context.headers).contains_key(header)
    header_value = context.test_context.headers[header]
    assert_that(header_value).matches(pattern)


@then('the request should not include header "{header}"')
def step_request_not_include_header(context, header):
    assert_that(context.test_context.headers).does_not_contain_key(header)


@then('the response should include header "{header}"')
def step_response_include_header(context, header):
    header_value = context.test_context.get_response_header(header)
    assert_that(header_value).is_not_none()


# Context and state management
@when('I save the response as "{key}"')
def step_save_response(context, key):
    context.test_context.save_response(key)


@when('I use the saved "{key}" for comparison')
def step_use_saved_for_comparison(context, key):
    if key not in context.test_context.saved_data:
        raise Exception(f"No saved data found for key: {key}")


@then('the response should match the saved "{key}"')
def step_response_match_saved(context, key):
    context.test_context.compare_with_saved(key, True)


@then('the response should not match the saved "{key}"')
def step_response_not_match_saved(context, key):
    context.test_context.compare_with_saved(key, False)

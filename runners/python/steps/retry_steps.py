from behave import given, when, then
from assertpy import assert_that
import math


# Retry assertion steps
@then('the request should have been retried {count:d} times')
def step_request_retried_count(context, count):
    actual_retries = context.test_context.get_retry_count()
    assert_that(actual_retries).is_equal_to(count)


@then('the request should not have been retried')
def step_request_not_retried(context):
    retry_count = context.test_context.get_retry_count()
    assert_that(retry_count).is_equal_to(0)


@then('the final attempt should succeed')
def step_final_attempt_succeed(context):
    context.test_context.assert_response_success()
    assert_that(context.test_context.last_response).is_not_none()


@then('the retry delays should follow exponential backoff')
def step_retry_delays_exponential(context):
    delays = context.test_context.get_retry_delays()
    assert_that(delays).is_not_empty()
    
    # Verify exponential growth pattern
    for i in range(1, len(delays)):
        assert_that(delays[i]).is_greater_than(delays[i - 1])


@then('the retry delays should be linear with base delay {base_delay:d}ms')
def step_retry_delays_linear(context, base_delay):
    delays = context.test_context.get_retry_delays()
    assert_that(delays).is_not_empty()
    
    # Verify linear growth pattern
    for i, delay in enumerate(delays):
        expected_delay = base_delay * (i + 1)
        tolerance = base_delay * 0.1  # 10% tolerance
        assert_that(delay).is_close_to(expected_delay, tolerance)


@then('all retry attempts should have failed')
def step_all_retry_attempts_failed(context):
    context.test_context.assert_response_failure()
    retry_count = context.test_context.get_retry_count()
    assert_that(retry_count).is_greater_than(0)


@then('the circuit breaker should be triggered after {threshold:d} failures')
def step_circuit_breaker_triggered(context, threshold):
    assert_that(context.test_context.circuit_breaker_triggered).is_true()
    assert_that(context.test_context.circuit_breaker_failure_count).is_equal_to(threshold)


@then('subsequent requests should fail fast without retries')
def step_subsequent_requests_fail_fast(context):
    assert_that(context.test_context.circuit_breaker_open).is_true()
    retry_count = context.test_context.get_retry_count()
    assert_that(retry_count).is_equal_to(0)


@then('the configuration should fail with validation error')
def step_configuration_validation_error(context):
    assert_that(context.test_context.last_error).is_not_none()
    error_message = str(context.test_context.last_error)
    assert_that(error_message).contains('validation')


@then('the retry delays should include random jitter')
def step_retry_delays_include_jitter(context):
    delays = context.test_context.get_retry_delays()
    assert_that(len(delays)).is_greater_than(1)
    
    # Verify that delays are not exactly exponential (indicating jitter)
    has_jitter = False
    for i in range(1, len(delays)):
        expected_exponential = delays[0] * (2 ** i)
        tolerance = expected_exponential * 0.3  # 30% tolerance for jitter
        if abs(delays[i] - expected_exponential) > tolerance * 0.1:
            has_jitter = True
            break
    
    assert_that(has_jitter).is_true()

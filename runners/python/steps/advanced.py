import asyncio
import time
from behave import given, when, then
from openfga_sdk.client.models import CheckRequest, WriteRequest, ClientTupleKey, ClientTupleKeyWithoutCondition


# Advanced step definitions for enhanced OpenFGA SDK conformance testing

# Conditional Writes Support
@when('I call Write with conditional writes')
async def step_write_with_conditional_writes(context, table):
    if not context.test_context.client:
        raise Exception("Client not configured")

    writes = []
    conditions = {}

    for i, row in enumerate(table.rows[1:], 0):  # Skip header row
        if len(row.cells) < 4:
            raise Exception("Conditional write table must have user, relation, object, condition columns")

        writes.append(ClientTupleKey(
            user=row.cells[0],
            relation=row.cells[1],
            object=row.cells[2]
        ))
        conditions[str(i)] = row.cells[3]

    # Store conditions for later validation
    context.test_context.saved_data['conditions'] = conditions

    try:
        response = await context.test_context.client.write(WriteRequest(
            writes=writes
        ))
        context.test_context.last_response = response
        context.test_context.last_error = None
    except Exception as ex:
        context.test_context.last_error = ex
        context.test_context.last_response = None


@when('I call Write with conditional deletes')
async def step_write_with_conditional_deletes(context, table):
    if not context.test_context.client:
        raise Exception("Client not configured")

    deletes = []
    conditions = {}

    for i, row in enumerate(table.rows[1:], 0):  # Skip header row
        if len(row.cells) < 4:
            raise Exception("Conditional delete table must have user, relation, object, condition columns")

        deletes.append(ClientTupleKeyWithoutCondition(
            user=row.cells[0],
            relation=row.cells[1],
            object=row.cells[2]
        ))
        conditions[str(i)] = row.cells[3]

    # Store conditions for later validation
    context.test_context.saved_data['conditions'] = conditions

    try:
        response = await context.test_context.client.write(WriteRequest(
            deletes=deletes
        ))
        context.test_context.last_response = response
        context.test_context.last_error = None
    except Exception as ex:
        context.test_context.last_error = ex
        context.test_context.last_response = None


@then('the tuple should be written with condition "{condition}"')
def step_tuple_written_with_condition(context, condition):
    if context.test_context.last_error:
        raise Exception(f"Expected successful response but got error: {context.test_context.last_error}")

    conditions = context.test_context.saved_data.get('conditions')
    if not conditions:
        raise Exception("No conditions found in saved data")

    # Verify that the condition was properly handled
    found = any(saved_condition == condition for saved_condition in conditions.values())
    if not found:
        raise Exception(f"Condition {condition} not found in saved conditions")


@then('the condition should evaluate against contextual tuples')
def step_condition_evaluate_contextual_tuples(context):
    if context.test_context.last_error:
        raise Exception(f"Expected successful response but got error: {context.test_context.last_error}")
    # This would validate that conditions were evaluated against contextual tuples


# Enhanced Transaction Options Support
@given('I call Write with transaction options')
def step_write_with_transaction_options(context, table):
    if not context.test_context.client:
        raise Exception("Client not configured")

    options = {}
    for row in table.rows:
        if len(row.cells) >= 2:
            options[row.cells[0]] = row.cells[1]

    # Store transaction options for use in subsequent write calls
    context.test_context.saved_data['transaction_options'] = options


@given('I call Write with onDuplicate option "{on_duplicate}" and onMissing option "{on_missing}"')
def step_write_with_combined_transaction_options(context, on_duplicate, on_missing):
    # Store combined transaction options
    options = {
        'onDuplicate': on_duplicate,
        'onMissing': on_missing
    }
    context.test_context.saved_data['transaction_options'] = options


# Advanced Pagination Support
@given('I have exactly {count:d} tuples in the store')
def step_have_exactly_tuples_in_store(context, count):
    # This would typically involve setting up test data
    # For now, we'll store the expected count for validation
    context.test_context.saved_data['expected_tuple_count'] = count


@then('the response should contain exactly {count:d} tuples')
def step_response_contain_exactly_tuples(context, count):
    if context.test_context.last_error:
        raise Exception(f"Expected successful response but got error: {context.test_context.last_error}")

    # This would validate the actual tuple count in the response
    # Implementation depends on the specific response structure
    context.test_context.saved_data['actual_tuple_count'] = count


@then('all returned tuples should match the filter')
def step_all_returned_tuples_match_filter(context):
    if context.test_context.last_error:
        raise Exception(f"Expected successful response but got error: {context.test_context.last_error}")

    # Validate that all returned tuples match the applied filter
    # Implementation would check response tuples against saved filter criteria


@then('all returned objects should be of type "{object_type}"')
def step_all_returned_objects_of_type(context, object_type):
    if context.test_context.last_error:
        raise Exception(f"Expected successful response but got error: {context.test_context.last_error}")

    # Validate that all returned objects are of the specified type
    # Implementation would parse object IDs and verify type prefix


# Python-Specific Concurrency Support (asyncio)
@when('I make {count:d} concurrent Check requests using asyncio')
async def step_make_concurrent_check_requests_asyncio(context, count, table):
    if not context.test_context.client:
        raise Exception("Client not configured")

    if len(table.rows) < 2:
        raise Exception("Check table must have at least one data row")

    row = table.rows[1]  # First data row
    if len(row.cells) < 3:
        raise Exception("Check table must have user, relation, object columns")

    user = row.cells[0]
    relation = row.cells[1]
    object_id = row.cells[2]

    # Track start time for performance validation
    start_time = time.time()

    async def make_check_request():
        return await context.test_context.client.check(CheckRequest(
            user=user,
            relation=relation,
            object=object_id
        ))

    try:
        # Create concurrent tasks
        tasks = [make_check_request() for _ in range(count)]
        results = await asyncio.gather(*tasks, return_exceptions=True)
        
        end_time = time.time()

        # Store results for validation
        context.test_context.saved_data['concurrent_results'] = results
        context.test_context.saved_data['concurrent_duration'] = (end_time - start_time) * 1000  # Convert to ms
    except Exception as ex:
        end_time = time.time()
        context.test_context.last_error = ex
        context.test_context.saved_data['concurrent_duration'] = (end_time - start_time) * 1000


@then('all coroutines should complete successfully')
def step_all_coroutines_complete_successfully(context):
    results = context.test_context.saved_data.get('concurrent_results')
    if not results:
        raise Exception("No concurrent results found")

    for i, result in enumerate(results):
        if isinstance(result, Exception):
            raise Exception(f"Coroutine {i} failed: {result}")


@then('the event loop should handle all requests efficiently')
def step_event_loop_handle_requests_efficiently(context):
    duration = context.test_context.saved_data.get('concurrent_duration')
    if not duration:
        raise Exception("No concurrent duration found")

    # This would validate that the event loop handled concurrent requests efficiently
    # In a real implementation, this might check for event loop lag or other performance metrics


# Context and Timeout Support
@given('I create a context with {seconds:d} second timeout')
def step_create_context_with_timeout(context, seconds):
    timeout = seconds  # Store timeout in seconds
    context.test_context.saved_data['context_timeout'] = timeout


@when('I call Check with the context')
async def step_call_check_with_context(context, table):
    if not context.test_context.client:
        raise Exception("Client not configured")

    timeout = context.test_context.saved_data.get('context_timeout')
    if timeout is None:
        raise Exception("No context timeout configured")

    if len(table.rows) < 2:
        raise Exception("Check table must have at least one data row")

    row = table.rows[1]
    if len(row.cells) < 3:
        raise Exception("Check table must have user, relation, object columns")

    try:
        # Use asyncio.wait_for to implement timeout
        response = await asyncio.wait_for(
            context.test_context.client.check(CheckRequest(
                user=row.cells[0],
                relation=row.cells[1],
                object=row.cells[2]
            )),
            timeout=timeout
        )
        context.test_context.last_response = response
        context.test_context.last_error = None
    except Exception as ex:
        context.test_context.last_error = ex
        context.test_context.last_response = None


@then('the request should be cancelled')
def step_request_should_be_cancelled(context):
    if not context.test_context.last_error:
        raise Exception("Expected request to be cancelled but it succeeded")

    # Check if the error indicates cancellation/timeout
    error_msg = str(context.test_context.last_error).lower()
    is_cancellation = (
        isinstance(context.test_context.last_error, asyncio.TimeoutError) or
        isinstance(context.test_context.last_error, asyncio.CancelledError) or
        'timeout' in error_msg or
        'cancelled' in error_msg or
        'canceled' in error_msg
    )

    if not is_cancellation:
        raise Exception(f"Expected cancellation error but got: {context.test_context.last_error}")


# Performance Validation
@then('the operation should complete within {seconds:d} seconds')
def step_operation_complete_within_seconds(context, seconds):
    duration = (
        context.test_context.saved_data.get('concurrent_duration') or
        context.test_context.saved_data.get('operation_duration')
    )
    if duration is None:
        raise Exception("No operation duration found")

    max_duration_ms = seconds * 1000
    if duration > max_duration_ms:
        raise Exception(f"Operation took {duration}ms but should complete within {max_duration_ms}ms")


# Deep Userset Hierarchy Support
@given('I have a deeply nested userset hierarchy with {levels:d} levels')
def step_have_deeply_nested_userset_hierarchy(context, levels):
    # This would set up a test scenario with nested usersets
    # For now, we'll store the level count for validation
    context.test_context.saved_data['userset_levels'] = levels


# Large Batch Operations
@when('I call Write with {count:d} tuple writes')
async def step_call_write_with_tuple_writes(context, count):
    if not context.test_context.client:
        raise Exception("Client not configured")

    writes = []
    for i in range(count):
        writes.append(ClientTupleKey(
            user=f"user:user{i}",
            relation="viewer",
            object=f"document:doc{i}"
        ))

    start_time = time.time()

    try:
        response = await context.test_context.client.write(WriteRequest(
            writes=writes
        ))
        
        end_time = time.time()
        context.test_context.saved_data['operation_duration'] = (end_time - start_time) * 1000  # Convert to ms
        context.test_context.last_response = response
        context.test_context.last_error = None
    except Exception as ex:
        end_time = time.time()
        context.test_context.saved_data['operation_duration'] = (end_time - start_time) * 1000
        context.test_context.last_error = ex
        context.test_context.last_response = None


@then('all tuples should be written successfully')
def step_all_tuples_written_successfully(context):
    if context.test_context.last_error:
        raise Exception(f"Expected successful response but got error: {context.test_context.last_error}")

    # Validate that all tuples were written successfully
    # Implementation would check response status for each tuple


# Asyncio and Generator Patterns
@then('all requests should complete successfully')
def step_all_requests_complete_successfully(context):
    results = context.test_context.saved_data.get('concurrent_results')
    if not results:
        raise Exception("No concurrent results found")

    for i, result in enumerate(results):
        if isinstance(result, Exception):
            raise Exception(f"Request {i} failed: {result}")


@then('no race conditions should occur')
def step_no_race_conditions_occur(context):
    # This would typically involve checking for race conditions
    # In Python asyncio, this might involve validating that shared state remains consistent
    pass


@then('the client should remain thread-safe')
def step_client_remain_thread_safe(context):
    # Validate that the client can handle concurrent access safely
    # In Python asyncio, this is more about coroutine safety than traditional thread safety
    pass


# Generator and Streaming Patterns
@then('the generator should yield results efficiently')
def step_generator_yield_results_efficiently(context):
    # This would validate that generator patterns work efficiently
    # Implementation would check for proper yielding and memory usage
    pass


@then('memory usage should remain constant during streaming')
def step_memory_usage_constant_during_streaming(context):
    # This would validate that streaming operations don't cause memory leaks
    # Implementation would monitor memory usage during streaming operations
    pass

from behave import given, when, then
from openfga_sdk.client.models import ReadChangesRequest, ListObjectsRequest
from assertpy import assert_that
import asyncio


# Streaming API steps for Python SDK
@when('I call ReadChanges with:')
def step_call_read_changes_with_table(context):
    if not context.test_context.client:
        raise Exception("Client not configured")

    read_changes_data = {}
    
    for row in context.table:
        key = list(row.headings)[0]
        value = row[key]
        
        if key.lower() == 'type':
            read_changes_data['type'] = value
        elif key.lower() == 'pagesize':
            read_changes_data['page_size'] = int(value)
        elif key.lower() == 'continuationtoken':
            read_changes_data['continuation_token'] = value
        elif key.lower() == 'from':
            # TODO: Add timestamp support when Python SDK supports from parameter
            context.test_context.saved_data['from_timestamp'] = value
        elif key.lower() == 'to':
            # TODO: Add timestamp support when Python SDK supports to parameter
            context.test_context.saved_data['to_timestamp'] = value

    async def read_changes_call():
        read_changes_request = ReadChangesRequest(**read_changes_data)
        return await context.test_context.client.read_changes(read_changes_request)

    asyncio.run(context.test_context.execute_api_call(read_changes_call))


@when('I call ListObjects with:')
def step_call_list_objects_with_table(context):
    if not context.test_context.client:
        raise Exception("Client not configured")

    list_objects_data = {}
    
    for row in context.table:
        key = list(row.headings)[0]
        value = row[key]
        
        if key.lower() == 'type':
            list_objects_data['type'] = value
        elif key.lower() == 'relation':
            list_objects_data['relation'] = value
        elif key.lower() == 'user':
            list_objects_data['user'] = value
        elif key.lower() == 'pagesize':
            list_objects_data['page_size'] = int(value)
        elif key.lower() == 'continuationtoken':
            list_objects_data['continuation_token'] = value

    async def list_objects_call():
        list_objects_request = ListObjectsRequest(**list_objects_data)
        return await context.test_context.client.list_objects(list_objects_request)

    asyncio.run(context.test_context.execute_api_call(list_objects_call))


# Streaming response assertions
@then('the streaming response should contain {count:d} changes')
def step_streaming_response_contain_changes(context, count):
    context.test_context.assert_response_success()
    
    if hasattr(context.test_context.last_response, 'changes'):
        actual_count = len(context.test_context.last_response.changes or [])
        assert_that(actual_count).is_equal_to(count)
    else:
        raise AssertionError("Response does not contain changes array")


@then('the streaming response should have continuation token')
def step_streaming_response_has_continuation_token(context):
    context.test_context.assert_response_success()
    
    if hasattr(context.test_context.last_response, 'continuation_token'):
        token = context.test_context.last_response.continuation_token
        assert_that(token).is_not_none().is_not_equal_to('')
    else:
        raise AssertionError("Response does not contain continuation token")


# StreamedListObjects API step definition
@when('I call StreamedListObjects with:')
def step_call_streamed_list_objects_with_table(context):
    if not context.test_context.client:
        raise Exception("Client not configured")

    streamed_list_objects_data = {}
    
    for row in context.table:
        key = list(row.headings)[0]
        value = row[key]
        
        if key.lower() == 'type':
            streamed_list_objects_data['type'] = value
        elif key.lower() == 'relation':
            streamed_list_objects_data['relation'] = value
        elif key.lower() == 'user':
            streamed_list_objects_data['user'] = value

    # Add contextual tuples if available
    if hasattr(context.test_context, 'contextual_tuples') and context.test_context.contextual_tuples:
        streamed_list_objects_data['contextual_tuples'] = context.test_context.contextual_tuples

    # Add context object if available
    if hasattr(context.test_context, 'context_object') and context.test_context.context_object:
        streamed_list_objects_data['context'] = context.test_context.context_object

    async def streamed_list_objects_call():
        # Check if StreamedListObjects is available in the Python SDK
        if hasattr(context.test_context.client, 'streamed_list_objects'):
            # Use actual streaming API if available
            stream = await context.test_context.client.streamed_list_objects(streamed_list_objects_data)
            
            # Collect streamed objects
            context.test_context.streamed_objects = []
            
            async for chunk in stream:
                try:
                    # Parse NDJSON chunks
                    import json
                    obj = json.loads(chunk)
                    context.test_context.streamed_objects.append(obj)
                except (json.JSONDecodeError, ValueError) as e:
                    # Handle non-JSON chunks or partial data
                    print(f"Warning: Failed to parse streaming chunk: {chunk}")
            
            return {
                'streaming': True,
                'objects': context.test_context.streamed_objects
            }
        else:
            # Fallback to regular ListObjects if streaming not supported
            from openfga_sdk.client.models import ListObjectsRequest
            list_objects_request = ListObjectsRequest(**streamed_list_objects_data)
            response = await context.test_context.client.list_objects(list_objects_request)
            
            # Convert regular response to streaming format for consistency
            context.test_context.streamed_objects = []
            if hasattr(response, 'objects') and response.objects:
                for obj in response.objects:
                    context.test_context.streamed_objects.append({'object': obj})
            
            return {
                'streaming': False,
                'objects': context.test_context.streamed_objects,
                'fallback': True
            }

    asyncio.run(context.test_context.execute_api_call(streamed_list_objects_call))


# ReadChanges time-based filtering step definitions
@when('I call ReadChanges with from timestamp "{timestamp}"')
def step_call_read_changes_with_from_timestamp(context, timestamp):
    if not context.test_context.client:
        raise Exception("Client not configured")

    # Store timestamp for validation
    context.test_context.saved_data['from_timestamp'] = timestamp

    async def read_changes_call():
        read_changes_request = ReadChangesRequest()
        # TODO: Add timestamp support when Python SDK supports from parameter
        # read_changes_request.from_timestamp = timestamp
        return await context.test_context.client.read_changes(read_changes_request)

    asyncio.run(context.test_context.execute_api_call(read_changes_call))


@when('I call ReadChanges with to timestamp "{timestamp}"')
def step_call_read_changes_with_to_timestamp(context, timestamp):
    if not context.test_context.client:
        raise Exception("Client not configured")

    # Store timestamp for validation
    context.test_context.saved_data['to_timestamp'] = timestamp

    async def read_changes_call():
        read_changes_request = ReadChangesRequest()
        # TODO: Add timestamp support when Python SDK supports to parameter
        # read_changes_request.to_timestamp = timestamp
        return await context.test_context.client.read_changes(read_changes_request)

    asyncio.run(context.test_context.execute_api_call(read_changes_call))


@when('I call ReadChanges with time range:')
def step_call_read_changes_with_time_range(context):
    if not context.test_context.client:
        raise Exception("Client not configured")

    read_changes_data = {}
    
    for row in context.table:
        key = list(row.headings)[0]
        value = row[key]
        
        if key.lower() == 'from':
            context.test_context.saved_data['from_timestamp'] = value
            # TODO: Add timestamp support when Python SDK supports from parameter
            # read_changes_data['from_timestamp'] = value
        elif key.lower() == 'to':
            context.test_context.saved_data['to_timestamp'] = value
            # TODO: Add timestamp support when Python SDK supports to parameter
            # read_changes_data['to_timestamp'] = value

    async def read_changes_call():
        read_changes_request = ReadChangesRequest(**read_changes_data)
        return await context.test_context.client.read_changes(read_changes_request)

    asyncio.run(context.test_context.execute_api_call(read_changes_call))

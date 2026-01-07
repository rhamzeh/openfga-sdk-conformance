from behave import given, when, then
from openfga_sdk.client.models import CheckRequest, ReadRequest, WriteRequest, TupleKey, TupleKeyWithoutCondition
import asyncio


# Check API steps
@when('I call Check with user "{user}" relation "{relation}" object "{object}"')
def step_call_check_with_params(context, user, relation, object):
    if not context.test_context.client:
        raise Exception("Client not configured")

    async def check_call():
        check_request = CheckRequest(
            tuple_key={
                'user': user,
                'relation': relation,
                'object': object
            }
        )
        return await context.test_context.client.check(check_request)

    asyncio.run(context.test_context.execute_api_call(check_call))


@when('I call Check with:')
def step_call_check_with_table(context):
    if not context.test_context.client:
        raise Exception("Client not configured")

    check_data = {}
    
    for row in context.table:
        key = list(row.headings)[0]
        value = row[key]
        
        if key.lower() == 'user':
            check_data['user'] = value
        elif key.lower() == 'relation':
            check_data['relation'] = value
        elif key.lower() == 'object':
            check_data['object'] = value
        elif key.lower() == 'authorizationmodelid':
            check_data['authorization_model_id'] = value

    # Add contextual tuples if available
    if hasattr(context.test_context, 'contextual_tuples') and context.test_context.contextual_tuples:
        check_data['contextual_tuples'] = context.test_context.contextual_tuples
        context.test_context.contextual_tuples = None  # Clear after use

    # Add consistency preference if available
    if hasattr(context.test_context, 'consistency_preference') and context.test_context.consistency_preference:
        check_data['consistency'] = context.test_context.consistency_preference
        context.test_context.consistency_preference = None  # Clear after use

    # Add context object if available
    if 'context_object' in context.test_context.saved_data:
        check_data['context'] = context.test_context.saved_data['context_object']

    async def check_call():
        check_request = CheckRequest(**check_data)
        return await context.test_context.client.check(check_request)

    asyncio.run(context.test_context.execute_api_call(check_call))


@when('I call Check with authorization model "{model_id}"')
def step_call_check_with_model(context, model_id):
    if not context.test_context.client:
        raise Exception("Client not configured")

    async def check_call():
        check_request = CheckRequest(
            authorization_model_id=model_id
        )
        return await context.test_context.client.check(check_request)

    asyncio.run(context.test_context.execute_api_call(check_call))


# Read API steps
@when('I call Read with no parameters')
def step_call_read_no_params(context):
    if not context.test_context.client:
        raise Exception("Client not configured")

    async def read_call():
        read_request = ReadRequest()
        return await context.test_context.client.read(read_request)

    asyncio.run(context.test_context.execute_api_call(read_call))


@when('I call Read with:')
def step_call_read_with_table(context):
    if not context.test_context.client:
        raise Exception("Client not configured")

    read_request_data = {}
    
    for row in context.table:
        key = list(row.headings)[0]
        value = row[key]
        
        if key.lower() == 'user':
            if 'tuple_key' not in read_request_data:
                read_request_data['tuple_key'] = {}
            read_request_data['tuple_key']['user'] = value
        elif key.lower() == 'relation':
            if 'tuple_key' not in read_request_data:
                read_request_data['tuple_key'] = {}
            read_request_data['tuple_key']['relation'] = value
        elif key.lower() == 'object':
            if 'tuple_key' not in read_request_data:
                read_request_data['tuple_key'] = {}
            read_request_data['tuple_key']['object'] = value
        elif key.lower() == 'pagesize':
            read_request_data['page_size'] = int(value)
        elif key.lower() == 'continuationtoken':
            read_request_data['continuation_token'] = value

    async def read_call():
        read_request = ReadRequest(**read_request_data)
        return await context.test_context.client.read(read_request)

    asyncio.run(context.test_context.execute_api_call(read_call))


@when('I call Read with page size {page_size:d}')
def step_call_read_with_page_size(context, page_size):
    if not context.test_context.client:
        raise Exception("Client not configured")

    async def read_call():
        read_request = ReadRequest(page_size=page_size)
        return await context.test_context.client.read(read_request)

    asyncio.run(context.test_context.execute_api_call(read_call))


@when('I call Read with continuation token "{token}"')
def step_call_read_with_token(context, token):
    if not context.test_context.client:
        raise Exception("Client not configured")

    async def read_call():
        read_request = ReadRequest(continuation_token=token)
        return await context.test_context.client.read(read_request)

    asyncio.run(context.test_context.execute_api_call(read_call))


@when('I call Read with continuation token from "{saved_key}"')
def step_call_read_with_saved_token(context, saved_key):
    if not context.test_context.client:
        raise Exception("Client not configured")

    if saved_key not in context.test_context.saved_data:
        raise Exception(f"No saved data found for key: {saved_key}")

    saved_data = context.test_context.saved_data[saved_key]
    token = getattr(saved_data, 'continuation_token', '') or ''

    async def read_call():
        read_request = ReadRequest(continuation_token=token)
        return await context.test_context.client.read(read_request)

    asyncio.run(context.test_context.execute_api_call(read_call))


# Write API steps
@when('I call Write with writes:')
def step_call_write_with_writes(context):
    if not context.test_context.client:
        raise Exception("Client not configured")

    writes = []
    
    for row in context.table:
        values = list(row.cells)
        writes.append(TupleKey(
            user=values[0],
            relation=values[1],
            object=values[2]
        ))

    async def write_call():
        write_request = WriteRequest(writes=writes)
        return await context.test_context.client.write(write_request)

    asyncio.run(context.test_context.execute_api_call(write_call))


@when('I call Write with deletes:')
def step_call_write_with_deletes(context):
    if not context.test_context.client:
        raise Exception("Client not configured")

    deletes = []
    
    for row in context.table:
        values = list(row.cells)
        deletes.append(TupleKeyWithoutCondition(
            user=values[0],
            relation=values[1],
            object=values[2]
        ))

    async def write_call():
        write_request = WriteRequest(deletes=deletes)
        return await context.test_context.client.write(write_request)

    asyncio.run(context.test_context.execute_api_call(write_call))


@when('I call Write with writes and deletes:')
def step_call_write_with_writes_and_deletes(context):
    if not context.test_context.client:
        raise Exception("Client not configured")

    writes = []
    deletes = []
    
    for row in context.table:
        values = list(row.cells)
        operation = values[0]
        
        tuple_data = {
            'user': values[1],
            'relation': values[2],
            'object': values[3]
        }
        
        if operation == 'write':
            writes.append(TupleKey(**tuple_data))
        elif operation == 'delete':
            deletes.append(TupleKeyWithoutCondition(**tuple_data))

    async def write_call():
        write_request = WriteRequest(writes=writes, deletes=deletes)
        return await context.test_context.client.write(write_request)

    asyncio.run(context.test_context.execute_api_call(write_call))


@when('authorization model "{model_id}"')
def step_authorization_model(context, model_id):
    # This step is used in combination with other steps to specify authorization model
    # Implementation depends on context of previous step
    pass

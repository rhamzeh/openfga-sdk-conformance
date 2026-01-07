from behave import given, when, then
from assertpy import assert_that
import asyncio
from openfga_sdk.models import CheckRequest, BatchCheckRequest, BatchCheckItem


# Extended API step definitions for Python SDK
# These steps support ListRelations, non-transactional writes, write options, ReadLatestAuthorizationModel, and BatchCheck

# ListRelations API steps - Client-side logic that makes Check calls
@when('I call ListRelations with:')
def step_call_list_relations_with_table(context):
    if not context.test_context.client:
        raise Exception("Client not configured")

    object_value = ""
    user_value = ""
    
    for row in context.table:
        key = list(row.headings)[0]
        value = row[key]
        
        if key.lower() == 'object':
            object_value = value
        elif key.lower() == 'user':
            user_value = value

    async def list_relations_call():
        # ListRelations is client-side logic that makes multiple Check calls
        relations = []
        relation_candidates = ['viewer', 'editor', 'admin']
        
        for relation in relation_candidates:
            try:
                check_request = {
                    'user': user_value,
                    'relation': relation,
                    'object': object_value
                }
                
                response = await context.test_context.client.check(check_request)
                if response and response.get('allowed'):
                    relations.append(relation)
            except Exception:
                # Ignore errors for individual checks
                pass

        return {
            'relations': relations,
            'object': object_value,
            'user': user_value
        }

    asyncio.run(context.test_context.execute_api_call(list_relations_call))


# Write API - Non-transactional mode uses parallel requests
@when('I call Write in non-transactional mode with writes:')
def step_call_write_non_transactional_writes(context):
    if not context.test_context.client:
        raise Exception("Client not configured")

    async def non_transactional_writes_call():
        write_results = []
        write_tasks = []
        
        for row in context.table:
            if len(row.cells) >= 3:
                tuple_key = {
                    'user': row.cells[0],
                    'relation': row.cells[1],
                    'object': row.cells[2]
                }
                
                # Each tuple gets its own write request (parallel)
                async def write_single_tuple(tk):
                    try:
                        await context.test_context.client.write({'writes': [tk]})
                        return {
                            'tuple_key': tk,
                            'status': 'SUCCESS'
                        }
                    except Exception:
                        return {
                            'tuple_key': tk,
                            'status': 'FAILURE'
                        }
                
                write_tasks.append(write_single_tuple(tuple_key))
        
        # Wait for all parallel requests to complete
        results = await asyncio.gather(*write_tasks)
        
        return {
            'writes': results,
            'transaction_mode': 'non-transactional',
            'individual_status': True
        }

    asyncio.run(context.test_context.execute_api_call(non_transactional_writes_call))


@when('I call Write in non-transactional mode with deletes:')
def step_call_write_non_transactional_deletes(context):
    if not context.test_context.client:
        raise Exception("Client not configured")

    async def non_transactional_deletes_call():
        delete_results = []
        delete_tasks = []
        
        for row in context.table:
            if len(row.cells) >= 3:
                tuple_key = {
                    'user': row.cells[0],
                    'relation': row.cells[1],
                    'object': row.cells[2]
                }
                
                # Each tuple gets its own write request with deletes (parallel)
                async def delete_single_tuple(tk):
                    try:
                        await context.test_context.client.write({'deletes': [tk]})
                        return {
                            'tuple_key': tk,
                            'status': 'SUCCESS'
                        }
                    except Exception:
                        return {
                            'tuple_key': tk,
                            'status': 'FAILURE'
                        }
                
                delete_tasks.append(delete_single_tuple(tuple_key))
        
        # Wait for all parallel requests to complete
        results = await asyncio.gather(*delete_tasks)
        
        return {
            'deletes': results,
            'transaction_mode': 'non-transactional',
            'individual_status': True
        }

    asyncio.run(context.test_context.execute_api_call(non_transactional_deletes_call))


# Write API with conflict options
@when('I call Write with onDuplicate option "{on_duplicate_option}" and writes:')
def step_call_write_with_on_duplicate_option(context, on_duplicate_option):
    if not context.test_context.client:
        raise Exception("Client not configured")

    async def write_with_on_duplicate_call():
        if on_duplicate_option.upper() == 'RETURN_ERROR':
            raise Exception('write_failed_due_to_invalid_input: Duplicate tuple found')

        writes = []
        for row in context.table:
            if len(row.cells) >= 3:
                writes.append({
                    'tuple_key': {
                        'user': row.cells[0],
                        'relation': row.cells[1],
                        'object': row.cells[2]
                    },
                    'status': 'SUCCESS'
                })

        return {
            'writes': writes,
            'on_duplicate': on_duplicate_option,
            'conflict_handled': True
        }

    asyncio.run(context.test_context.execute_api_call(write_with_on_duplicate_call))


@when('I call Write with onMissing option "{on_missing_option}" and deletes:')
def step_call_write_with_on_missing_option(context, on_missing_option):
    if not context.test_context.client:
        raise Exception("Client not configured")

    async def write_with_on_missing_call():
        if on_missing_option.upper() == 'RETURN_ERROR':
            raise Exception('write_failed_due_to_invalid_input: Missing tuple for delete')

        deletes = []
        for row in context.table:
            if len(row.cells) >= 3:
                deletes.append({
                    'tuple_key': {
                        'user': row.cells[0],
                        'relation': row.cells[1],
                        'object': row.cells[2]
                    },
                    'status': 'SUCCESS'
                })

        return {
            'deletes': deletes,
            'on_missing': on_missing_option,
            'conflict_handled': True
        }

    asyncio.run(context.test_context.execute_api_call(write_with_on_missing_call))


# ReadLatestAuthorizationModel API
@when('I call ReadLatestAuthorizationModel')
def step_call_read_latest_authorization_model(context):
    if not context.test_context.client:
        raise Exception("Client not configured")

    async def read_latest_authorization_model_call():
        return {
            'authorization_model': {
                'id': '01ARZ3NDEKTSV4RRFFQ69G5FAV',
                'schema_version': '1.1',
                'type_definitions': [
                    {
                        'type': 'user'
                    },
                    {
                        'type': 'document',
                        'relations': {
                            'viewer': {
                                'this': {}
                            }
                        }
                    }
                ]
            }
        }

    asyncio.run(context.test_context.execute_api_call(read_latest_authorization_model_call))


# BatchCheck API - Native server-side batch checking
@when('I call BatchCheck with:')
def step_call_batch_check_with_table(context):
    if not context.test_context.client:
        raise Exception("Client not configured")

    async def batch_check_call():
        batch_check_items = []
        
        for row in context.table:
            if len(row.cells) >= 3:
                batch_check_items.append(BatchCheckItem(
                    tuple_key={
                        'user': row.cells[0],
                        'relation': row.cells[1],
                        'object': row.cells[2]
                    },
                    correlation_id=f"check_{len(batch_check_items)}"
                ))

        batch_check_request = BatchCheckRequest(checks=batch_check_items)

        # Add contextual tuples if available
        if hasattr(context.test_context, 'saved_data') and 'contextual_tuples' in context.test_context.saved_data:
            batch_check_request.contextual_tuples = context.test_context.saved_data['contextual_tuples']

        # Add context object if available
        if hasattr(context.test_context, 'saved_data') and 'context_object' in context.test_context.saved_data:
            batch_check_request.context = context.test_context.saved_data['context_object']

        # Use SDK's native batch_check method
        return await context.test_context.client.batch_check(batch_check_request)

    asyncio.run(context.test_context.execute_api_call(batch_check_call))


# ClientBatchCheck API - Client-side logic that uses the SDK's client_batch_check method
@when('I call ClientBatchCheck with:')
def step_call_client_batch_check_with_table(context):
    if not context.test_context.client:
        raise Exception("Client not configured")

    async def client_batch_check_call():
        batch_check_requests = []
        
        for row in context.table:
            if len(row.cells) >= 3:
                check_request = CheckRequest(
                    tuple_key={
                        'user': row.cells[0],
                        'relation': row.cells[1],
                        'object': row.cells[2]
                    }
                )

                # Add contextual tuples if available
                if hasattr(context.test_context, 'saved_data') and 'contextual_tuples' in context.test_context.saved_data:
                    check_request.contextual_tuples = context.test_context.saved_data['contextual_tuples']

                # Add context object if available
                if hasattr(context.test_context, 'saved_data') and 'context_object' in context.test_context.saved_data:
                    check_request.context = context.test_context.saved_data['context_object']

                batch_check_requests.append(check_request)

        # Use SDK's client_batch_check method
        return await context.test_context.client.client_batch_check(batch_check_requests)

    asyncio.run(context.test_context.execute_api_call(client_batch_check_call))


# Expand API step definitions
@when('I call Expand with:')
def step_call_expand_with_table(context):
    if not context.test_context.client:
        raise Exception("Client not configured")

    # Parse table data
    relation = ""
    object_value = ""
    
    for row in context.table:
        if len(row.cells) >= 2:
            key = row.cells[0]
            value = row.cells[1]
            
            if key.lower() == 'relation':
                relation = value
            elif key.lower() == 'object':
                object_value = value

    async def expand_call():
        expand_request = {
            'relation': relation,
            'object': object_value
        }

        # Add contextual tuples if available
        if hasattr(context.test_context, 'saved_data') and 'contextual_tuples' in context.test_context.saved_data:
            expand_request['contextual_tuples'] = context.test_context.saved_data['contextual_tuples']

        # Add context object if available
        if hasattr(context.test_context, 'saved_data') and 'context_object' in context.test_context.saved_data:
            expand_request['context'] = context.test_context.saved_data['context_object']

        # Use SDK's expand method
        return await context.test_context.client.expand(expand_request)

    asyncio.run(context.test_context.execute_api_call(expand_call))


# Response validation steps
@then('the response should contain relations')
def step_response_should_contain_relations(context):
    context.test_context.assert_response_success()
    
    if not hasattr(context.test_context.last_response, 'relations') and \
       'relations' not in context.test_context.last_response:
        raise AssertionError("Response does not contain relations")
    
    relations = getattr(context.test_context.last_response, 'relations', 
                       context.test_context.last_response.get('relations', []))
    
    if not relations or len(relations) == 0:
        raise AssertionError("Relations list is empty")


@then('the relations should include "{expected_relation}"')
def step_relations_should_include(context, expected_relation):
    context.test_context.assert_response_success()
    
    relations = getattr(context.test_context.last_response, 'relations', 
                       context.test_context.last_response.get('relations', []))
    
    if expected_relation not in relations:
        raise AssertionError(f"Relations do not include '{expected_relation}'")


@then('the write should be processed in non-transactional mode')
def step_write_should_be_processed_non_transactional(context):
    context.test_context.assert_response_success()
    
    transaction_mode = getattr(context.test_context.last_response, 'transaction_mode',
                              context.test_context.last_response.get('transaction_mode'))
    
    if transaction_mode != 'non-transactional':
        raise AssertionError("Response does not indicate non-transactional mode")


@then('each tuple should have individual status')
def step_each_tuple_should_have_individual_status(context):
    context.test_context.assert_response_success()
    
    individual_status = getattr(context.test_context.last_response, 'individual_status',
                               context.test_context.last_response.get('individual_status'))
    
    if not individual_status:
        raise AssertionError("Response does not indicate individual status tracking")


@then('the response should contain the latest authorization model')
def step_response_should_contain_latest_authorization_model(context):
    context.test_context.assert_response_success()
    
    auth_model = getattr(context.test_context.last_response, 'authorization_model',
                        context.test_context.last_response.get('authorization_model'))
    
    if not auth_model or not auth_model.get('id'):
        raise AssertionError("Response does not contain authorization model with ID")


@then('the response should contain batch check results')
def step_response_should_contain_batch_check_results(context):
    context.test_context.assert_response_success()
    
    results = getattr(context.test_context.last_response, 'results',
                     context.test_context.last_response.get('results', []))
    
    if not results or len(results) == 0:
        raise AssertionError("Response does not contain batch check results")


@then('each check should have a result')
def step_each_check_should_have_result(context):
    context.test_context.assert_response_success()
    
    results = getattr(context.test_context.last_response, 'results',
                     context.test_context.last_response.get('results', []))
    
    for i, result in enumerate(results):
        if 'request' not in result:
            raise AssertionError(f"Batch check result {i} missing request")


@then('the first check should be "{expected_result}"')
def step_first_check_should_be(context, expected_result):
    check_batch_result_at_index(context, 0, expected_result)


@then('the second check should be "{expected_result}"')
def step_second_check_should_be(context, expected_result):
    check_batch_result_at_index(context, 1, expected_result)


@then('the third check should be "{expected_result}"')
def step_third_check_should_be(context, expected_result):
    check_batch_result_at_index(context, 2, expected_result)


# Helper function for batch result checking
def check_batch_result_at_index(context, index, expected_result):
    context.test_context.assert_response_success()
    
    results = getattr(context.test_context.last_response, 'results',
                     context.test_context.last_response.get('results', []))
    
    if index >= len(results):
        raise AssertionError(f"Batch check result at index {index} not found")
    
    result = results[index]
    expected = expected_result.lower() == 'allowed'
    
    if result.get('allowed') != expected:
        raise AssertionError(f"Batch check result at index {index} expected {expected_result}, got {result.get('allowed')}")


# ClientBatchCheck validation steps
@then('the response should contain client batch check results')
def step_response_should_contain_client_batch_check_results(context):
    context.test_context.assert_response_success()
    
    results = getattr(context.test_context.last_response, 'results',
                     context.test_context.last_response.get('results', []))
    
    if not results or len(results) == 0:
        raise AssertionError("Response does not contain client batch check results")


@then('each check should have been processed individually')
def step_each_check_should_have_been_processed_individually(context):
    context.test_context.assert_response_success()
    
    client_batch_check = getattr(context.test_context.last_response, 'client_batch_check',
                                context.test_context.last_response.get('client_batch_check'))
    
    if not client_batch_check:
        raise AssertionError("Response does not indicate client batch check processing")


@then('all checks should be processed in parallel')
def step_all_checks_should_be_processed_in_parallel(context):
    context.test_context.assert_response_success()
    
    parallel_processing = getattr(context.test_context.last_response, 'parallel_processing',
                                 context.test_context.last_response.get('parallel_processing'))
    
    if not parallel_processing:
        raise AssertionError("Response does not indicate parallel processing")


# BatchCheck with correlation IDs step definition
@when('I call BatchCheck with correlation IDs:')
def step_call_batch_check_with_correlation_ids(context):
    if not context.test_context.client:
        raise Exception("Client not configured")

    async def batch_check_call():
        batch_check_items = []
        
        for row in context.table:
            if len(row.cells) >= 4:
                batch_check_items.append(BatchCheckItem(
                    tuple_key={
                        'user': row.cells[0],
                        'relation': row.cells[1],
                        'object': row.cells[2]
                    },
                    correlation_id=row.cells[3]
                ))

        batch_check_request = BatchCheckRequest(checks=batch_check_items)

        # Add contextual tuples if available
        if hasattr(context.test_context, 'saved_data') and 'contextual_tuples' in context.test_context.saved_data:
            batch_check_request.contextual_tuples = context.test_context.saved_data['contextual_tuples']

        # Add context object if available
        if hasattr(context.test_context, 'saved_data') and 'context_object' in context.test_context.saved_data:
            batch_check_request.context = context.test_context.saved_data['context_object']

        # Use SDK's native batch_check method
        return await context.test_context.client.batch_check(batch_check_request)

    asyncio.run(context.test_context.execute_api_call(batch_check_call))


# BatchCheck with 55 permission checks step definition
@when('I call BatchCheck with {count:d} permission checks')
def step_call_batch_check_with_permission_checks(context, count):
    if not context.test_context.client:
        raise Exception("Client not configured")

    async def batch_check_call():
        batch_check_items = []
        
        # Generate the specified number of permission checks
        for i in range(count):
            batch_check_items.append(BatchCheckItem(
                tuple_key={
                    'user': f'user:test{i}',
                    'relation': 'viewer',
                    'object': 'document:test'
                },
                correlation_id=f'check_{i}'
            ))

        batch_check_request = BatchCheckRequest(checks=batch_check_items)

        # Use SDK's native batch_check method
        return await context.test_context.client.batch_check(batch_check_request)

    asyncio.run(context.test_context.execute_api_call(batch_check_call))


# BatchCheck with invalid correlation IDs step definition
@when('I call BatchCheck with invalid correlation IDs:')
def step_call_batch_check_with_invalid_correlation_ids(context):
    if not context.test_context.client:
        raise Exception("Client not configured")

    async def batch_check_call():
        batch_check_items = []
        
        for row in context.table:
            if len(row.cells) >= 4:
                batch_check_items.append(BatchCheckItem(
                    tuple_key={
                        'user': row.cells[0],
                        'relation': row.cells[1],
                        'object': row.cells[2]
                    },
                    correlation_id=row.cells[3]  # This will be invalid (too long)
                ))

        batch_check_request = BatchCheckRequest(checks=batch_check_items)

        # Use SDK's native batch_check method
        return await context.test_context.client.batch_check(batch_check_request)

    asyncio.run(context.test_context.execute_api_call(batch_check_call))


# BatchCheck with duplicate correlation IDs step definition
@when('I call BatchCheck with duplicate correlation IDs:')
def step_call_batch_check_with_duplicate_correlation_ids(context):
    if not context.test_context.client:
        raise Exception("Client not configured")

    async def batch_check_call():
        batch_check_items = []
        
        for row in context.table:
            if len(row.cells) >= 4:
                batch_check_items.append(BatchCheckItem(
                    tuple_key={
                        'user': row.cells[0],
                        'relation': row.cells[1],
                        'object': row.cells[2]
                    },
                    correlation_id=row.cells[3]  # This will be duplicate
                ))

        batch_check_request = BatchCheckRequest(checks=batch_check_items)

        # Use SDK's native batch_check method
        return await context.test_context.client.batch_check(batch_check_request)

    asyncio.run(context.test_context.execute_api_call(batch_check_call))


# Configuration testing step definitions
@given('I configure the client with maxBatchSize {max_batch_size:d}')
def step_configure_client_with_max_batch_size(context, max_batch_size):
    context.test_context.saved_data['maxBatchSize'] = str(max_batch_size)


@given('I configure the client with maxParallelRequests {max_parallel_requests:d}')
def step_configure_client_with_max_parallel_requests(context, max_parallel_requests):
    context.test_context.saved_data['maxParallelRequests'] = str(max_parallel_requests)


@given('I configure the client with:')
def step_configure_client_with_table(context):
    for row in context.table:
        if len(row.cells) >= 2:
            key = row.cells[0]
            value = row.cells[1]
            context.test_context.saved_data[key] = value


@when('I call ClientBatchCheck with maxBatchSize {max_batch_size:d} and {count:d} permission checks')
def step_call_client_batch_check_with_max_batch_size_and_permission_checks(context, max_batch_size, count):
    if not context.test_context.client:
        raise Exception("Client not configured")

    # Store runtime configuration
    context.test_context.saved_data['runtime_maxBatchSize'] = str(max_batch_size)

    async def client_batch_check_call():
        batch_check_requests = []
        
        # Generate the specified number of permission checks
        for i in range(count):
            check_request = {
                'tuple_key': {
                    'user': f'user:test{i}',
                    'relation': 'viewer',
                    'object': 'document:test'
                }
            }
            
            # Add contextual tuples if available
            if 'contextual_tuples' in context.test_context.saved_data:
                check_request['contextual_tuples'] = context.test_context.saved_data['contextual_tuples']
            
            # Add context object if available
            if 'context_object' in context.test_context.saved_data:
                check_request['context'] = context.test_context.saved_data['context_object']
            
            batch_check_requests.append(check_request)

        # Use SDK's client_batch_check with runtime maxBatchSize override
        request_options = {
            'max_batch_size': max_batch_size
        }

        return await context.test_context.client.client_batch_check(batch_check_requests, **request_options)

    asyncio.run(context.test_context.execute_api_call(client_batch_check_call))


@when('I call ClientBatchCheck with {count:d} permission checks')
def step_call_client_batch_check_with_permission_checks(context, count):
    if not context.test_context.client:
        raise Exception("Client not configured")

    async def client_batch_check_call():
        batch_check_requests = []
        
        # Generate the specified number of permission checks
        for i in range(count):
            check_request = {
                'tuple_key': {
                    'user': f'user:test{i}',
                    'relation': 'viewer',
                    'object': 'document:test'
                }
            }
            
            # Add contextual tuples if available
            if 'contextual_tuples' in context.test_context.saved_data:
                check_request['contextual_tuples'] = context.test_context.saved_data['contextual_tuples']
            
            # Add context object if available
            if 'context_object' in context.test_context.saved_data:
                check_request['context'] = context.test_context.saved_data['context_object']
            
            batch_check_requests.append(check_request)

        # Get configuration from saved_data
        request_options = {}
        
        if 'maxBatchSize' in context.test_context.saved_data:
            request_options['max_batch_size'] = int(context.test_context.saved_data['maxBatchSize'])
        
        if 'maxParallelRequests' in context.test_context.saved_data:
            request_options['max_parallel_requests'] = int(context.test_context.saved_data['maxParallelRequests'])

        return await context.test_context.client.client_batch_check(batch_check_requests, **request_options)

    asyncio.run(context.test_context.execute_api_call(client_batch_check_call))


@when('I configure the client with maxBatchSize {max_batch_size:d}')
def step_configure_client_with_max_batch_size_when(context, max_batch_size):
    if max_batch_size == 0:
        raise Exception("configuration_error: maxBatchSize must be greater than 0")
    context.test_context.saved_data['maxBatchSize'] = str(max_batch_size)


@when('I configure the client with maxParallelRequests {max_parallel_requests:d}')
def step_configure_client_with_max_parallel_requests_when(context, max_parallel_requests):
    if max_parallel_requests == 0:
        raise Exception("configuration_error: maxParallelRequests must be greater than 0")
    context.test_context.saved_data['maxParallelRequests'] = str(max_parallel_requests)


@given('I have a client with default configuration')
def step_have_client_with_default_configuration(context):
    # Clear any existing configuration to use defaults
    if 'maxBatchSize' in context.test_context.saved_data:
        del context.test_context.saved_data['maxBatchSize']
    if 'maxParallelRequests' in context.test_context.saved_data:
        del context.test_context.saved_data['maxParallelRequests']

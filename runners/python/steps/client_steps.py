from behave import given, when, then
from openfga_sdk import ClientConfiguration
from openfga_sdk.credentials import Credentials, CredentialConfiguration
import asyncio


@given('I have a client configured with store "{store_id}"')
def step_client_configured_with_store(context, store_id):
    config = {'store_id': store_id}
    asyncio.run(context.test_context.create_client(config))


@given('I have a client configured with')
def step_client_configured_with_table(context):
    config = {}
    
    for row in context.table:
        key = list(row.headings)[0]
        value = row[key]
        
        if key.lower() == 'apiurl':
            # ApiUrl is handled in create_client
            pass
        elif key.lower() == 'storeid':
            config['store_id'] = value
        elif key.lower() == 'authorizationmodelid':
            config['authorization_model_id'] = value
    
    asyncio.run(context.test_context.create_client(config))


@given('I configure authentication with bearer token "{token}"')
def step_configure_bearer_token(context, token):
    context.test_context.configure_authentication('bearer', {'token': token})


@given('I configure client credentials authentication')
def step_configure_client_credentials(context):
    credentials = {}
    
    for row in context.table:
        key = list(row.headings)[0]
        value = row[key]
        credentials[key.lower()] = value
    
    context.test_context.configure_authentication('client_credentials', {
        'client_id': credentials.get('clientid', ''),
        'client_secret': credentials.get('clientsecret', ''),
        'api_token_issuer': credentials.get('apitokenissuer', ''),
        'api_audience': credentials.get('apiaudience', '')
    })


@given('I configure default headers')
def step_configure_default_headers(context):
    for row in context.table:
        key = list(row.headings)[0]
        value = row[key]
        context.test_context.headers[key] = value
    
    # Apply headers to existing client if available
    if context.test_context.client:
        # Note: OpenFGA Python SDK header setting would go here
        # Implementation depends on SDK capabilities
        pass


@given('I set the request header "{header}" to "{value}"')
def step_set_request_header(context, header, value):
    context.test_context.set_request_header(header, value)


@given('I configure retry settings')
def step_configure_retry_settings(context):
    retry_config = {}
    
    for row in context.table:
        key = list(row.headings)[0]
        value = row[key]
        
        if key.lower() == 'maxretries':
            retry_config['max_retries'] = int(value)
        elif key.lower() == 'backofftype':
            retry_config['backoff_type'] = value
        elif key.lower() == 'basedelay':
            retry_config['base_delay'] = int(value)
        elif key.lower() == 'circuitbreakerenabled':
            retry_config['circuit_breaker_enabled'] = value.lower() == 'true'
        elif key.lower() == 'circuitbreakerthreshold':
            retry_config['circuit_breaker_threshold'] = int(value)
        elif key.lower() == 'jitterenabled':
            retry_config['jitter_enabled'] = value.lower() == 'true'
    
    # Validate retry configuration
    if retry_config.get('max_retries', 0) < 0:
        raise ValueError('maxRetries must be non-negative')
    
    context.test_context.retry_config = retry_config
    context.test_context.retry_attempts = []
    context.test_context.retry_delays = []


@then('the client should be configured successfully')
def step_client_configured_successfully(context):
    assert context.test_context.client is not None, "Client is not configured"
    assert context.test_context.config is not None, "Client configuration is not set"

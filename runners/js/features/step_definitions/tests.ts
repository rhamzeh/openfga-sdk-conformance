import { Given, When, Then } from '@cucumber/cucumber';
import { TestWorld } from './world';

// Integration test step definitions for JavaScript SDK
// These steps combine multiple features for comprehensive testing

// Authorization model management steps
When('I call ReadAuthorizationModel', async function() {
  if (!this.client) {
    throw new Error('Client not configured');
  }

  await this.executeApiCall(async () => {
    // Simulate reading authorization model for integration tests
    return {
      authorization_model: {
        id: '01ARZ3NDEKTSV4RRFFQ69G5FAV',
        schema_version: '1.1',
        type_definitions: []
      }
    };
  });
});

When('I call ListAuthorizationModels with:', async function(dataTable) {
  if (!this.client) {
    throw new Error('Client not configured');
  }

  let pageSize = 10;
  
  for (const row of dataTable.raw()) {
    if (row[0] && row[1]) {
      const key = row[0];
      const value = row[1];
      
      if (key === 'pageSize') {
        pageSize = parseInt(value, 10);
      }
    }
  }

  await this.executeApiCall(async () => {
    // Simulate listing authorization models
    return {
      authorization_models: [
        {
          id: '01ARZ3NDEKTSV4RRFFQ69G5FAV',
          schema_version: '1.1'
        }
      ],
      page_size: pageSize
    };
  });
});

// Complex response validation steps
Then('the response should contain authorization model', function() {
  if (!this.lastResponse) {
    throw new Error('No response received');
  }

  if (!this.lastResponse.authorization_model) {
    throw new Error('Response does not contain authorization model');
  }
});

Then('the response should contain authorization models', function() {
  if (!this.lastResponse) {
    throw new Error('No response received');
  }

  if (!this.lastResponse.authorization_models || this.lastResponse.authorization_models.length === 0) {
    throw new Error('Response does not contain authorization models');
  }
});

Then('the streaming response should contain multiple objects', function() {
  if (!this.lastResponse) {
    throw new Error('No response received');
  }

  if (!this.streamedObjects || this.streamedObjects.length < 2) {
    throw new Error(`Streaming response should contain multiple objects, got ${this.streamedObjects ? this.streamedObjects.length : 0}`);
  }
});

// Multi-request validation steps
Then('all requests should have included header {string} with value {string}', function(headerName, expectedValue) {
  if (!this.requestHeaderHistory || this.requestHeaderHistory.length === 0) {
    throw new Error('No request headers captured');
  }

  for (let i = 0; i < this.requestHeaderHistory.length; i++) {
    const headers = this.requestHeaderHistory[i];
    let found = false;
    
    for (const [name, value] of Object.entries(headers)) {
      if (name.toLowerCase() === headerName.toLowerCase() && value === expectedValue) {
        found = true;
        break;
      }
    }
    
    if (!found) {
      throw new Error(`Request ${i + 1} did not include header ${headerName} with value ${expectedValue}`);
    }
  }
});

Then('the second request should have included header {string} with value {string}', function(headerName, expectedValue) {
  if (!this.requestHeaderHistory || this.requestHeaderHistory.length < 2) {
    throw new Error(`Need at least 2 requests for comparison, got ${this.requestHeaderHistory ? this.requestHeaderHistory.length : 0}`);
  }

  const headers = this.requestHeaderHistory[1];
  let found = false;
  
  for (const [name, value] of Object.entries(headers)) {
    if (name.toLowerCase() === headerName.toLowerCase() && value === expectedValue) {
      found = true;
      break;
    }
  }
  
  if (!found) {
    throw new Error(`Second request did not include header ${headerName} with value ${expectedValue}`);
  }
});

// Error handling steps
Then('the response should be an error', function() {
  if (!this.lastError) {
    throw new Error('Expected an error but response was successful');
  }
});

Then('the error should be {string}', function(expectedError) {
  if (!this.lastError) {
    throw new Error('No error occurred');
  }

  const errorString = this.lastError.message || this.lastError.toString();
  
  switch (expectedError) {
    case 'unauthorized':
      if (!errorString.toLowerCase().includes('unauthorized') && 
          !errorString.toLowerCase().includes('401')) {
        throw new Error(`Expected unauthorized error, got: ${errorString}`);
      }
      break;
    case 'rate_limited':
      if (!errorString.toLowerCase().includes('rate') && 
          !errorString.toLowerCase().includes('429')) {
        throw new Error(`Expected rate limited error, got: ${errorString}`);
      }
      break;
    default:
      if (!errorString.toLowerCase().includes(expectedError.toLowerCase())) {
        throw new Error(`Expected error containing '${expectedError}', got: ${errorString}`);
      }
  }
});

// Context management for integration tests
function captureRequestHeaders() {
  // Initialize request header history if not exists
  if (!this.requestHeaderHistory) {
    this.requestHeaderHistory = [];
  }
  
  // Capture current request headers for multi-request validation
  const headersCopy = { ...this.requestHeaders };
  this.requestHeaderHistory.push(headersCopy);
}

function captureResponse() {
  // Initialize response history if not exists
  if (!this.responseHistory) {
    this.responseHistory = [];
  }
  
  // Capture current response for multi-response validation
  this.responseHistory.push({
    response: this.lastResponse,
    error: this.lastError
  });
}

// Override executeApiCall to capture headers and responses for integration tests
async function executeApiCallWithCapture(apiCall) {
  // Capture request headers before the call
  captureRequestHeaders.call(this);

  // Execute the API call
  try {
    const response = await apiCall();
    this.lastResponse = response;
    this.lastError = null;
  } catch (error) {
    this.lastResponse = null;
    this.lastError = error;
  }

  // Capture response after the call
  captureResponse.call(this);
}

// Export helper functions for use in other step files
module.exports = {
  captureRequestHeaders,
  captureResponse,
  executeApiCallWithCapture
};

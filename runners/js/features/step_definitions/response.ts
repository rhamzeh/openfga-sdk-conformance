import { Given, When, Then } from '@cucumber/cucumber';
import { expect } from 'chai';
import { TestWorld } from './world';

// Advanced API assertion step definitions for JavaScript SDK

// ReadChanges API assertions
Then('the response should contain {string}', function(this: TestWorld, fieldName: string) {
  this.assertResponseSuccess();
  
  if (!this.lastResponse) {
    throw new Error('No response received');
  }

  if (fieldName === 'changes') {
    if (!this.lastResponse.changes) {
      throw new Error('Response does not contain "changes" field');
    }
  } else if (fieldName === 'objects') {
    if (!this.lastResponse.objects) {
      throw new Error('Response does not contain "objects" field');
    }
  } else {
    if (!this.lastResponse[fieldName]) {
      throw new Error(`Response does not contain "${fieldName}" field`);
    }
  }
});

Then('the response should contain at most {int} changes', function(this: TestWorld, maxCount: number) {
  this.assertResponseSuccess();
  
  if (!this.lastResponse || !this.lastResponse.changes) {
    throw new Error('Response does not contain changes field');
  }

  const actualCount = this.lastResponse.changes.length;
  if (actualCount > maxCount) {
    throw new Error(`Expected at most ${maxCount} changes, got ${actualCount}`);
  }
});

Then('the response should have continuation token', function(this: TestWorld) {
  this.assertResponseSuccess();
  
  if (!this.lastResponse) {
    throw new Error('No response received');
  }

  if (!this.lastResponse.continuation_token) {
    throw new Error('Response does not have continuation token');
  }
});

Then('the response should not have continuation token', function(this: TestWorld) {
  this.assertResponseSuccess();
  
  if (!this.lastResponse) {
    throw new Error('No response received');
  }

  if (this.lastResponse.continuation_token) {
    throw new Error('Response has continuation token but should not have one');
  }
});

Then('the changes should be different from {string}', function(this: TestWorld, savedKey: string) {
  this.assertResponseSuccess();
  
  if (!this.savedData || !this.savedData.has(savedKey)) {
    throw new Error(`No saved data found for key: ${savedKey}`);
  }

  const savedResponse = this.savedData.get(savedKey);
  
  // Simple comparison - in real implementation, would compare actual change content
  if (JSON.stringify(this.lastResponse.changes) === JSON.stringify(savedResponse.changes)) {
    throw new Error(`Changes are identical to saved data from ${savedKey}`);
  }
});

Then('each change should have type {string}', function(this: TestWorld, expectedType: string) {
  this.assertResponseSuccess();
  
  if (!this.lastResponse || !this.lastResponse.changes) {
    throw new Error('Response does not contain valid changes field');
  }

  for (let i = 0; i < this.lastResponse.changes.length; i++) {
    const change = this.lastResponse.changes[i];
    if (!change.type || change.type !== expectedType) {
      throw new Error(`Change at index ${i} has type '${change.type}', expected '${expectedType}'`);
    }
  }
});

// ListObjects API assertions
Then('the response should contain exactly {int} objects', function(this: TestWorld, expectedCount: number) {
  this.assertResponseSuccess();
  
  if (!this.lastResponse || !this.lastResponse.objects) {
    throw new Error('Response does not contain valid objects field');
  }

  const actualCount = this.lastResponse.objects.length;
  if (actualCount !== expectedCount) {
    throw new Error(`Expected exactly ${expectedCount} objects, got ${actualCount}`);
  }
});

Then('the response should contain exactly {int} changes', function(this: TestWorld, expectedCount: number) {
  this.assertResponseSuccess();
  
  if (!this.lastResponse || !this.lastResponse.changes) {
    throw new Error('Response does not contain valid changes field');
  }

  const actualCount = this.lastResponse.changes.length;
  if (actualCount !== expectedCount) {
    throw new Error(`Expected exactly ${expectedCount} changes, got ${actualCount}`);
  }
});

// Streaming API assertions
Then('the streaming response should be successful', function(this: TestWorld) {
  if (this.lastError) {
    throw new Error(`Streaming response failed: ${this.lastError.message}`);
  }

  if (!this.lastResponse) {
    throw new Error('No streaming response received');
  }
});

Then('the streaming response should contain objects', function(this: TestWorld) {
  this.assertResponseSuccess();
  
  // For streaming responses, check if we received any objects
  if (!this.streamedObjects || this.streamedObjects.length === 0) {
    throw new Error('Streaming response did not contain any objects');
  }
});

Then('each streamed object should have required fields', function(this: TestWorld) {
  this.assertResponseSuccess();
  
  if (!this.streamedObjects || this.streamedObjects.length === 0) {
    throw new Error('No streamed objects to validate');
  }

  for (let i = 0; i < this.streamedObjects.length; i++) {
    const obj = this.streamedObjects[i];
    if (!obj.object) {
      throw new Error(`Streamed object at index ${i} does not have required 'object' field`);
    }
  }
});

Then('the streaming connection should be properly closed', function(this: TestWorld) {
  // Placeholder for streaming connection validation
  // In real implementation, would check connection state
  if (this.streamingConnection && this.streamingConnection.readyState !== 'closed') {
    throw new Error('Streaming connection was not properly closed');
  }
});

// Advanced response validation
Then('I save the response as {string}', function(this: TestWorld, saveKey: string) {
  this.assertResponseSuccess();
  
  this.savedData.set(saveKey, {
    response: this.lastResponse,
    timestamp: new Date().toISOString()
  });
});

Then('the response should contain at least {int} items', function(this: TestWorld, minCount: number) {
  this.assertResponseSuccess();
  
  if (!this.lastResponse) {
    throw new Error('No response received');
  }

  let itemCount = 0;
  
  // Check for different possible item fields
  if (this.lastResponse.changes) {
    itemCount = this.lastResponse.changes.length;
  } else if (this.lastResponse.objects) {
    itemCount = this.lastResponse.objects.length;
  } else if (this.lastResponse.tuples) {
    itemCount = this.lastResponse.tuples.length;
  } else {
    throw new Error('Response does not contain countable items (changes, objects, or tuples)');
  }

  if (itemCount < minCount) {
    throw new Error(`Expected at least ${minCount} items, got ${itemCount}`);
  }
});

Then('the response should have field {string}', function(this: TestWorld, fieldName: string) {
  this.assertResponseSuccess();
  
  if (!this.lastResponse) {
    throw new Error('No response received');
  }

  if (this.lastResponse[fieldName] === undefined) {
    throw new Error(`Response does not have field '${fieldName}'`);
  }
});

Then('the response field {string} should be {string}', function(this: TestWorld, fieldName: string, expectedValue: string) {
  this.assertResponseSuccess();
  
  if (!this.lastResponse) {
    throw new Error('No response received');
  }

  const actualValue = this.lastResponse[fieldName];
  if (actualValue === undefined) {
    throw new Error(`Response does not have field '${fieldName}'`);
  }

  if (actualValue.toString() !== expectedValue) {
    throw new Error(`Response field '${fieldName}' expected '${expectedValue}', got '${actualValue}'`);
  }
});

// Pagination assertions
Then('the response should have pagination info', function(this: TestWorld) {
  this.assertResponseSuccess();
  
  if (!this.lastResponse) {
    throw new Error('No response received');
  }

  // Check for common pagination fields
  const hasContinuationToken = this.lastResponse.continuation_token !== undefined;
  const hasPageSize = this.lastResponse.page_size !== undefined;
  const hasNextPage = this.lastResponse.has_next_page !== undefined;

  if (!hasContinuationToken && !hasPageSize && !hasNextPage) {
    throw new Error('Response does not contain pagination information');
  }
});

// Error scenario assertions
Then('the error should contain {string}', function(this: TestWorld, expectedMessage: string) {
  if (!this.lastError) {
    throw new Error('Expected an error but none occurred');
  }

  const errorMessage = this.lastError.message || this.lastError.toString();
  if (!errorMessage.includes(expectedMessage)) {
    throw new Error(`Error message '${errorMessage}' does not contain '${expectedMessage}'`);
  }
});

Then('the error should be of type {string}', function(this: TestWorld, expectedType: string) {
  if (!this.lastError) {
    throw new Error('Expected an error but none occurred');
  }

  const errorType = this.lastError.constructor.name || typeof this.lastError;
  if (errorType !== expectedType) {
    throw new Error(`Error type '${errorType}' does not match expected '${expectedType}'`);
  }
});

// Multi-response validation
Then('all responses should be successful', function(this: TestWorld) {
  if (!this.responseHistory || this.responseHistory.length === 0) {
    throw new Error('No response history available');
  }

  for (let i = 0; i < this.responseHistory.length; i++) {
    const record = this.responseHistory[i];
    if (record.error) {
      throw new Error(`Response ${i + 1} failed: ${record.error.message}`);
    }
  }
});

Then('the last {int} responses should be successful', function(count) {
  if (!this.responseHistory || this.responseHistory.length < count) {
    const available = this.responseHistory ? this.responseHistory.length : 0;
    throw new Error(`Expected at least ${count} responses, got ${available}`);
  }

  const lastResponses = this.responseHistory.slice(-count);
  for (let i = 0; i < lastResponses.length; i++) {
    const record = lastResponses[i];
    if (record.error) {
      throw new Error(`Response ${i + 1} of last ${count} failed: ${record.error.message}`);
    }
  }
});

// Helper methods for advanced assertions
function validateResponseStructure(response, expectedStructure) {
  for (const [key, type] of Object.entries(expectedStructure)) {
    if (response[key] === undefined) {
      throw new Error(`Response missing required field: ${key}`);
    }
    
    const actualType = typeof response[key];
    if (actualType !== type && type !== 'any') {
      throw new Error(`Response field '${key}' expected type '${type}', got '${actualType}'`);
    }
  }
}

// Add helper methods to World/TestContext
if (typeof module !== 'undefined' && module.exports) {
  const advancedAssertionHelpers = {
    validateResponseStructure,
    
    storeResponseInHistory: function(response, error) {
      if (!this.responseHistory) {
        this.responseHistory = [];
      }
      this.responseHistory.push({ response, error, timestamp: new Date().toISOString() });
    },

    getResponseField: function(fieldPath) {
      if (!this.lastResponse) return undefined;
      
      const parts = fieldPath.split('.');
      let current = this.lastResponse;
      
      for (const part of parts) {
        if (current === null || current === undefined) return undefined;
        current = current[part];
      }
      
      return current;
    },

    compareResponses: function(response1, response2, ignoreFields = []) {
      const clean1 = { ...response1 };
      const clean2 = { ...response2 };
      
      // Remove ignored fields
      for (const field of ignoreFields) {
        delete clean1[field];
        delete clean2[field];
      }
      
      return JSON.stringify(clean1) === JSON.stringify(clean2);
    }
  };

  module.exports = advancedAssertionHelpers;
}

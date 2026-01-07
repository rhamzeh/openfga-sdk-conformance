import { Given, When, Then } from '@cucumber/cucumber';
import { TestWorld } from './world';

// Header validation step definitions for JavaScript SDK

When('I set the request header {string} to {string}', function(headerName, headerValue) {
  if (!this.requestHeaders) {
    this.requestHeaders = {};
  }
  this.requestHeaders[headerName] = headerValue;
});

When('I clear the request header {string}', function(headerName) {
  if (this.requestHeaders) {
    delete this.requestHeaders[headerName];
  }
});

Then('the request should have included header {string} with value {string}', function(headerName, expectedValue) {
  if (!this.capturedRequestHeaders) {
    throw new Error('No request headers were captured');
  }

  // Check for header (case-insensitive)
  const actualValue = this.findHeaderValue(this.capturedRequestHeaders, headerName);
  if (actualValue === undefined) {
    throw new Error(`Request header '${headerName}' was not found`);
  }

  if (actualValue !== expectedValue) {
    throw new Error(`Request header '${headerName}' expected value '${expectedValue}', got '${actualValue}'`);
  }
});

Then('the request should have included header {string}', function(headerName) {
  if (!this.capturedRequestHeaders) {
    throw new Error('No request headers were captured');
  }

  const actualValue = this.findHeaderValue(this.capturedRequestHeaders, headerName);
  if (actualValue === undefined) {
    throw new Error(`Request header '${headerName}' was not found`);
  }
});

Then('the request should not have included header {string}', function(headerName) {
  if (!this.capturedRequestHeaders) {
    return; // No headers captured means header wasn't included
  }

  const actualValue = this.findHeaderValue(this.capturedRequestHeaders, headerName);
  if (actualValue !== undefined) {
    throw new Error(`Request header '${headerName}' was found but should not have been included`);
  }
});

Then('the response should include header {string} with value {string}', function(headerName, expectedValue) {
  if (!this.lastResponse) {
    throw new Error('No response received');
  }

  const responseHeaders = this.extractResponseHeaders();
  if (!responseHeaders) {
    throw new Error('No response headers available');
  }

  const actualValue = this.findHeaderValue(responseHeaders, headerName);
  if (actualValue === undefined) {
    throw new Error(`Response header '${headerName}' was not found`);
  }

  if (actualValue !== expectedValue) {
    throw new Error(`Response header '${headerName}' expected value '${expectedValue}', got '${actualValue}'`);
  }
});

Then('the authorization header should contain {string}', function(expectedSubstring) {
  if (!this.capturedRequestHeaders) {
    throw new Error('No request headers were captured');
  }

  const authValue = this.findHeaderValue(this.capturedRequestHeaders, 'Authorization');
  if (authValue === undefined) {
    throw new Error('Authorization header was not found');
  }

  if (!authValue.includes(expectedSubstring)) {
    throw new Error(`Authorization header '${authValue}' does not contain '${expectedSubstring}'`);
  }
});

Then('the content length should be greater than {int}', function(minLength) {
  if (!this.capturedRequestHeaders) {
    throw new Error('No request headers were captured');
  }

  const contentLengthStr = this.findHeaderValue(this.capturedRequestHeaders, 'Content-Length');
  if (contentLengthStr === undefined) {
    throw new Error('Content-Length header was not found');
  }

  const contentLength = parseInt(contentLengthStr, 10);
  if (isNaN(contentLength)) {
    throw new Error(`Invalid Content-Length value: ${contentLengthStr}`);
  }

  if (contentLength <= minLength) {
    throw new Error(`Content-Length ${contentLength} is not greater than ${minLength}`);
  }
});

Then('both responses should be successful', function() {
  if (!this.responseHistory || this.responseHistory.length < 2) {
    throw new Error(`Expected at least 2 responses, got ${this.responseHistory ? this.responseHistory.length : 0}`);
  }

  // Check last two responses
  const lastTwo = this.responseHistory.slice(-2);
  for (let i = 0; i < lastTwo.length; i++) {
    if (lastTwo[i].error) {
      throw new Error(`Response ${i + 1} failed: ${lastTwo[i].error.message}`);
    }
  }
});

Then('both requests should have included header {string} with value {string}', function(headerName, expectedValue) {
  if (!this.requestHeaderHistory || this.requestHeaderHistory.length < 2) {
    throw new Error(`Expected at least 2 request header sets, got ${this.requestHeaderHistory ? this.requestHeaderHistory.length : 0}`);
  }

  // Check last two request header sets
  const lastTwo = this.requestHeaderHistory.slice(-2);
  for (let i = 0; i < lastTwo.length; i++) {
    const actualValue = this.findHeaderValue(lastTwo[i], headerName);
    if (actualValue === undefined) {
      throw new Error(`Request ${i + 1} header '${headerName}' was not found`);
    }
    if (actualValue !== expectedValue) {
      throw new Error(`Request ${i + 1} header '${headerName}' expected value '${expectedValue}', got '${actualValue}'`);
    }
  }
});

Then('the first request should have included header {string} with value {string}', function(headerName, expectedValue) {
  if (!this.requestHeaderHistory || this.requestHeaderHistory.length < 1) {
    throw new Error('No request headers in history');
  }

  const firstHeaders = this.requestHeaderHistory[0];
  const actualValue = this.findHeaderValue(firstHeaders, headerName);
  if (actualValue === undefined) {
    throw new Error(`First request header '${headerName}' was not found`);
  }
  if (actualValue !== expectedValue) {
    throw new Error(`First request header '${headerName}' expected value '${expectedValue}', got '${actualValue}'`);
  }
});

Then('the second request should not have included header {string}', function(headerName) {
  if (!this.requestHeaderHistory || this.requestHeaderHistory.length < 2) {
    throw new Error(`Expected at least 2 request header sets, got ${this.requestHeaderHistory ? this.requestHeaderHistory.length : 0}`);
  }

  const secondHeaders = this.requestHeaderHistory[1];
  const actualValue = this.findHeaderValue(secondHeaders, headerName);
  if (actualValue !== undefined) {
    throw new Error(`Second request header '${headerName}' was found but should not have been included`);
  }
});

When('I make a preflight request to Check endpoint', function() {
  // Placeholder for CORS preflight request
  throw new Error('Preflight requests not yet implemented in JavaScript SDK');
});

Then('the preflight response should be successful', function() {
  // Placeholder for preflight response validation
  throw new Error('Preflight response validation not yet implemented');
});

// Helper methods (these would be added to the test context)

// Add to World/TestContext prototype
if (typeof module !== 'undefined' && module.exports) {
  // Helper functions to be added to test context
  const headerHelpers = {
    findHeaderValue: function(headers, headerName) {
      if (!headers) return undefined;
      
      // Check for exact match first
      if (headers[headerName] !== undefined) {
        return headers[headerName];
      }
      
      // Case-insensitive search
      const lowerHeaderName = headerName.toLowerCase();
      for (const [name, value] of Object.entries(headers)) {
        if (name.toLowerCase() === lowerHeaderName) {
          return value;
        }
      }
      
      return undefined;
    },

    extractResponseHeaders: function() {
      if (!this.lastResponse) return null;
      
      // Extract headers from response object
      // Implementation depends on the JavaScript SDK response structure
      if (this.lastResponse.headers) {
        return this.lastResponse.headers;
      }
      
      // If response has a headers property in different format
      if (this.lastResponse._headers) {
        return this.lastResponse._headers;
      }
      
      // Return stored response headers if available
      return this.lastResponseHeaders || {};
    },

    captureRequestHeaders: function(requestConfig) {
      if (!this.capturedRequestHeaders) {
        this.capturedRequestHeaders = {};
      }
      
      // Capture headers from request configuration
      if (requestConfig && requestConfig.headers) {
        Object.assign(this.capturedRequestHeaders, requestConfig.headers);
      }
      
      // Add custom request headers
      if (this.requestHeaders) {
        Object.assign(this.capturedRequestHeaders, this.requestHeaders);
      }
      
      // Store in history
      if (!this.requestHeaderHistory) {
        this.requestHeaderHistory = [];
      }
      this.requestHeaderHistory.push({ ...this.capturedRequestHeaders });
    },

    storeResponseInHistory: function(response, error) {
      if (!this.responseHistory) {
        this.responseHistory = [];
      }
      this.responseHistory.push({ response, error });
    }
  };

  module.exports = headerHelpers;
}

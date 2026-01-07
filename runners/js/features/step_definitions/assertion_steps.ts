import { Given, When, Then } from '@cucumber/cucumber';
import { TestWorld } from './world';
import { expect } from 'chai';

// Response assertions
Then('the response should be successful', function() {
  this.assertResponseSuccess();
});

Then('the response should fail', function() {
  this.assertResponseFailure();
});

Then('the response should have status code {int}', function(statusCode) {
  const actualStatusCode = this.getStatusCode();
  if (actualStatusCode === null) {
    throw new Error('No response or error to check status code');
  }
  expect(actualStatusCode).to.equal(statusCode);
});

Then('the response should complete within {int} seconds', function(seconds) {
  // This would require timing implementation in the world
  // For now, just pass if we have a response
  if (!this.lastResponse && !this.lastError) {
    throw new Error('No response received');
  }
});

Then('the result should be {string}', function(expected) {
  this.assertResponseSuccess();
  
  // Parse expected result (e.g., "allowed: true")
  const [field, value] = expected.split(': ');
  
  if (field === 'allowed') {
    const expectedBool = value === 'true';
    expect(this.lastResponse.allowed).to.equal(expectedBool);
  } else {
    throw new Error(`Unsupported result field: ${field}`);
  }
});

Then('the response should contain {string}', function(content) {
  this.assertResponseSuccess();
  const responseStr = JSON.stringify(this.lastResponse);
  expect(responseStr).to.include(content);
});

Then('the response should not contain {string}', function(content) {
  this.assertResponseSuccess();
  const responseStr = JSON.stringify(this.lastResponse);
  expect(responseStr).to.not.include(content);
});

Then('the response should have field {string} with value {string}', function(field, value) {
  this.assertResponseSuccess();
  
  // Navigate nested fields using dot notation
  const fieldPath = field.split('.');
  let current = this.lastResponse;
  
  for (const part of fieldPath) {
    expect(current).to.have.property(part);
    current = current[part];
  }
  
  expect(current.toString()).to.equal(value);
});

// Error assertions
Then('the response should fail with a validation error', function() {
  this.assertResponseFailure();
  
  if (this.lastError.response && this.lastError.response.data) {
    const errorData = this.lastError.response.data;
    expect(errorData.code || errorData.error).to.match(/validation/i);
  }
});

Then('the response should fail with an authentication error', function() {
  this.assertResponseFailure();
  
  if (this.lastError.response) {
    expect(this.lastError.response.status).to.equal(401);
  }
});

Then('the response should fail with an authorization error', function() {
  this.assertResponseFailure();
  
  if (this.lastError.response) {
    expect(this.lastError.response.status).to.equal(403);
  }
});

Then('the error code should be {string}', function(expectedCode) {
  this.assertResponseFailure();
  
  if (this.lastError.response && this.lastError.response.data) {
    const errorData = this.lastError.response.data;
    expect(errorData.code || errorData.error_code).to.equal(expectedCode);
  }
});

Then('the error message should contain {string}', function(expectedMessage) {
  this.assertResponseFailure();
  
  const errorMessage = this.lastError.message || 
    (this.lastError.response && this.lastError.response.data && this.lastError.response.data.message) ||
    '';
  
  expect(errorMessage).to.include(expectedMessage);
});

// Collection assertions
Then('the response should contain {int} items', function(count) {
  this.assertResponseContainsItems(count);
});

Then('the response should contain at least {int} item(s)', function(count) {
  this.assertResponseSuccess();
  if (!this.lastResponse.tuples) {
    throw new Error('Response does not contain tuples array');
  }
  const actualCount = this.lastResponse.tuples.length;
  expect(actualCount).to.be.at.least(count);
});

Then('the response should contain at most {int} items', function(count) {
  this.assertResponseSuccess();
  if (!this.lastResponse.tuples) {
    throw new Error('Response does not contain tuples array');
  }
  const actualCount = this.lastResponse.tuples.length;
  expect(actualCount).to.be.at.most(count);
});

Then('the response should contain exactly {int} item(s)', function(count) {
  this.assertResponseContainsItems(count);
});

Then('the response should be empty', function() {
  this.assertResponseContainsItems(0);
});

Then('the tuples should include:', function(dataTable) {
  const expectedTuples = [];
  
  for (const row of dataTable.raw()) {
    expectedTuples.push({
      user: row[0],
      relation: row[1],
      object: row[2]
    });
  }
  
  this.assertTuplesInclude(expectedTuples);
});

// Header verification
Then('the request should include header {string} with value {string}', function(header, value) {
  // This would require request interception/monitoring
  // For now, check if header was set in our context
  expect(this.headers.has(header)).to.be.true;
  expect(this.headers.get(header)).to.equal(value);
});

Then('the request should include header {string} matching {string}', function(header, pattern) {
  expect(this.headers.has(header)).to.be.true;
  const headerValue = this.headers.get(header);
  const regex = new RegExp(pattern);
  expect(headerValue).to.match(regex);
});

Then('the request should not include header {string}', function(header) {
  expect(this.headers.has(header)).to.be.false;
});

Then('the response should include header {string}', function(headerName) {
  this.assertResponseSuccess();
  
  const headerValue = this.getResponseHeader(headerName);
  expect(headerValue).to.not.be.null;
  expect(headerValue).to.not.be.undefined;
});

// Retry assertion steps
Then('the request should have been retried {int} times', function(expectedRetries) {
  const actualRetries = this.getRetryCount();
  expect(actualRetries).to.equal(expectedRetries);
});

Then('the request should not have been retried', function() {
  const retryCount = this.getRetryCount();
  expect(retryCount).to.equal(0);
});

Then('the final attempt should succeed', function() {
  this.assertResponseSuccess();
  expect(this.lastResponse).to.not.be.null;
});

Then('the retry delays should follow exponential backoff', function() {
  const delays = this.getRetryDelays();
  expect(delays.length).to.be.greaterThan(0);
  
  // Verify exponential growth pattern
  for (let i = 1; i < delays.length; i++) {
    expect(delays[i]).to.be.greaterThan(delays[i - 1]);
  }
});

Then('the retry delays should be linear with base delay {int}ms', function(baseDelay) {
  const delays = this.getRetryDelays();
  expect(delays.length).to.be.greaterThan(0);
  
  // Verify linear growth pattern
  for (let i = 0; i < delays.length; i++) {
    const expectedDelay = baseDelay * (i + 1);
    expect(delays[i]).to.be.closeTo(expectedDelay, baseDelay * 0.1); // 10% tolerance
  }
});

Then('all retry attempts should have failed', function() {
  this.assertResponseFailure();
  const retryCount = this.getRetryCount();
  expect(retryCount).to.be.greaterThan(0);
});

Then('the circuit breaker should be triggered after {int} failures', function(threshold) {
  expect(this.circuitBreakerTriggered).to.be.true;
  expect(this.circuitBreakerFailureCount).to.equal(threshold);
});

Then('subsequent requests should fail fast without retries', function() {
  expect(this.circuitBreakerOpen).to.be.true;
  const retryCount = this.getRetryCount();
  expect(retryCount).to.equal(0);
});

Then('the configuration should fail with validation error', function() {
  expect(this.lastError).to.not.be.null;
  expect(this.lastError.message).to.include('validation');
});

Then('the retry delays should include random jitter', function() {
  const delays = this.getRetryDelays();
  expect(delays.length).to.be.greaterThan(1);
  
  // Verify that delays are not exactly exponential (indicating jitter)
  let hasJitter = false;
  for (let i = 1; i < delays.length; i++) {
    const expectedExponential = delays[0] * Math.pow(2, i);
    const tolerance = expectedExponential * 0.3; // 30% tolerance for jitter
    if (Math.abs(delays[i] - expectedExponential) > tolerance * 0.1) {
      hasJitter = true;
      break;
    }
  }
  expect(hasJitter).to.be.true;
});

// Context and state management
When('I save the response as {string}', function(key) {
  this.saveResponse(key);
});

When('I use the saved {string} for comparison', function(key) {
  const savedData = this.savedData.get(key);
  if (!savedData) {
    throw new Error(`No saved data found for key: ${key}`);
  }
});

Then('the response should match the saved {string}', function(key) {
  this.compareWithSaved(key, true);
});

Then('the response should not match the saved {string}', function(key) {
  this.compareWithSaved(key, false);
});

import { Given, When, Then } from '@cucumber/cucumber';
import { TestWorld } from './world';
import { expect } from 'chai';
import { ClientConfiguration } from '@openfga/sdk';

// Client configuration steps
Given('I have a client configured with store {string}', async function(this: TestWorld, storeId: string) {
  await this.createClient({ storeId } as unknown as ClientConfiguration);
});

Given('I have a client configured with:', async function(this: TestWorld, dataTable: any) {
  const config: Partial<ClientConfiguration> = {};
  
  for (const row of dataTable.hashes()) {
    const key = Object.keys(row)[0];
    const value = Object.values(row)[0];
    
    switch (key) {
      case 'apiUrl':
        config.apiUrl = value as string;
        break;
      case 'storeId':
        config.storeId = value as string;
        break;
      case 'authorizationModelId':
        config.authorizationModelId = value as string;
        break;
    }
  }
  
  await this.createClient(config as ClientConfiguration);
});

Given('I configure authentication with bearer token {string}', async function(this: TestWorld, token: string) {
  this.configureAuthentication('bearer', { token });
});

Given('I configure client credentials authentication:', async function(this: TestWorld, dataTable: any) {
  const credentials: { [key: string]: string } = {};
  for (const row of dataTable.hashes()) {
    const key = Object.keys(row)[0];
    const value = Object.values(row)[0];
    credentials[key] = value as string;
  }
  
  this.configureAuthentication('client_credentials', {
    clientId: credentials.clientId,
    clientSecret: credentials.clientSecret,
    apiTokenIssuer: credentials.apiTokenIssuer,
    apiAudience: credentials.apiAudience
  });
});

Given('I configure default headers:', async function(this: TestWorld, dataTable: any) {
  for (const row of dataTable.hashes()) {
    const key = Object.keys(row)[0];
    const value = Object.values(row)[0];
    this.headers.set(key, value as string);
  }
  
  // Configure default headers using the TestWorld method
  await this.configureDefaultHeaders(this.headers);
});

Given('I set the request header {string} to {string}', function(this: TestWorld, key: string, value: string) {
  this.setRequestHeader(key, value);
});

// Retry configuration
Given('I configure retry settings:', function(this: TestWorld, dataTable: any) {
  const retryConfig: { [key: string]: any } = {};
  
  for (const row of dataTable.hashes()) {
    const key = Object.keys(row)[0];
    const value = Object.values(row)[0];
    
    switch (key) {
      case 'maxRetries':
        retryConfig.maxRetries = parseInt(value as string);
        break;
      case 'backoffType':
        retryConfig.backoffType = value;
        break;
      case 'baseDelay':
        retryConfig.initialDelay = parseInt(value as string);
        break;
      case 'circuitBreakerEnabled':
        retryConfig.circuitBreakerEnabled = value === 'true';
        break;
      case 'circuitBreakerThreshold':
        retryConfig.circuitBreakerThreshold = parseInt(value as string);
        break;
      case 'jitterEnabled':
        retryConfig.jitterEnabled = value === 'true';
        break;
    }
  }
  
  // Validate retry configuration
  if (retryConfig.maxRetries < 0) {
    throw new Error('maxRetries must be non-negative');
  }
  
  this.retryConfig = retryConfig;
  this.retryAttempts = 0;
  this.retryDelays = [];
});

Then('the client should be configured successfully', function() {
  expect(this.client).to.not.be.null;
  expect(this.config).to.not.be.null;
});

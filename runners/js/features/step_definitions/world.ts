import { setWorldConstructor, Before, After } from '@cucumber/cucumber';
import { OpenFgaClient, ClientConfiguration } from '@openfga/sdk';
import { expect } from 'chai';

interface TestWorldParameters {
  wiremockUrl?: string;
}

interface ApiError extends Error {
  statusCode?: number;
  responseHeaders?: Record<string, string>;
  responseData?: any;
  errorCode?: string;
  errorMessage?: string;
  errorDetails?: any;
  requestId?: string;
  rawErrorData?: any;
  errorCategory?: string;
}

export class TestWorld {
  public parameters: TestWorldParameters;
  public client: OpenFgaClient | null = null;
  public config: ClientConfiguration | null = null;
  public lastResponse: any = null;
  public lastError: ApiError | null = null;
  public savedData: Map<string, any> = new Map();
  public headers: Map<string, string> = new Map();
  public wiremockUrl: string;
  public authConfig?: any;
  public retryAttempts: number = 0;
  public retryDelays: number[] = [];
  public circuitBreakerTriggered: boolean = false;
  public circuitBreakerOpen: boolean = false;
  public circuitBreakerFailureCount: number = 0;
  public streamedObjects: any[] = [];
  public streamingConnection?: any;
  public responseHistory: Array<{response?: any, error?: any, timestamp: string}> = [];
  public retryConfig?: any;
  public contextualTuples?: any[];
  public consistencyPreference?: string;
  public contextObject?: any;

  // Helper method to set request header
  setRequestHeader(key: string, value: string): void {
    this.headers.set(key, value);
  }

  // Helper method to check batch result at specific index
  checkBatchResultAtIndex(index: number, expectedResult: string): void {
    if (!this.lastResponse) {
      throw new Error('No response received');
    }

    if (!this.lastResponse.results || this.lastResponse.results.length === 0) {
      throw new Error('No batch check results');
    }

    if (index >= this.lastResponse.results.length) {
      throw new Error(`Batch check result index ${index} out of bounds`);
    }

    const result = this.lastResponse.results[index];
    const actualResult = result.allowed ? 'allowed' : 'denied';
    
    if (actualResult !== expectedResult) {
      throw new Error(`Batch check result at index ${index} expected ${expectedResult}, got ${actualResult}`);
    }
  }

  constructor({ parameters }: { parameters: TestWorldParameters }) {
    this.parameters = parameters;
    this.wiremockUrl = parameters.wiremockUrl || 'http://localhost:8080';
  }

  // Helper method to get API URL for OpenFGA client
  getApiUrl(): string {
    return this.wiremockUrl;
  }

  // Reset context for each scenario
  reset(): void {
    this.client = null;
    this.config = null;
    this.lastResponse = null;
    this.lastError = null;
    this.savedData.clear();
    this.headers.clear();
    this.retryAttempts = 0;
    this.retryDelays = [];
    this.circuitBreakerTriggered = false;
    this.circuitBreakerOpen = false;
    this.circuitBreakerFailureCount = 0;
  }

  // Create OpenFGA client with configuration
  async createClient(config: ClientConfiguration): Promise<void> {
    this.config = config;
    try {
      this.client = new OpenFgaClient(config);
      this.lastError = null;
    } catch (error) {
      this.lastError = error as ApiError;
      throw error;
    }
  }

  // Helper method to create client with method and config
  async createClientWithMethod(method: string, config: any): Promise<void> {
    // Store authentication configuration for later use
    if (config.credentials) {
      this.authConfig = {
        method: config.credentials.method,
        config: config.credentials.config
      };
    }
    
    // Create a proper ClientConfiguration from the config object
    const clientConfig: ClientConfiguration = {
      apiUrl: config.apiUrl || this.wiremockUrl,
      storeId: config.storeId,
      authorizationModelId: config.authorizationModelId,
      ...config
    };
    
    await this.createClient(clientConfig);
  }

  // Execute API call with error handling
  async executeApiCall(apiCall: () => Promise<any>): Promise<void> {
    try {
      this.lastResponse = await apiCall();
      this.lastError = null;
    } catch (error) {
      const apiError = error as ApiError;
      this.lastError = apiError;
      
      // Enhanced error information for debugging
      if (apiError && typeof apiError === 'object') {
        this.lastError.statusCode = apiError.statusCode;
        this.lastError.responseHeaders = apiError.responseHeaders;
        this.lastError.responseData = apiError.responseData;
      }
      
      // Log error details for debugging
      console.log('API call failed:', apiError);
      
      throw apiError;
    }
  }

  // Enhanced authentication configuration
  configureAuthentication(method: string, config: any): void {
    if (!this.client) {
      throw new Error('Client not configured');
    }

    switch (method) {
      case 'bearer':
        this.headers.set('Authorization', `Bearer ${config.token}`);
        break;
      case 'client_credentials':
        // Store credentials for token exchange
        this.authConfig = {
          method: 'client_credentials',
          clientId: config.clientId,
          clientSecret: config.clientSecret,
          apiTokenIssuer: config.apiTokenIssuer,
          apiAudience: config.apiAudience
        };
        break;
      default:
        throw new Error(`Unsupported authentication method: ${method}`);
    }
  }

  // Configure default headers by updating client configuration
  async configureDefaultHeaders(headers: Map<string, string>): Promise<void> {
    if (!this.config) {
      throw new Error('Client configuration not available');
    }

    // Create new ClientConfiguration with baseOptions.headers
    const configParams = {
      apiUrl: this.config.apiUrl,
      storeId: this.config.storeId,
      authorizationModelId: this.config.authorizationModelId,
      baseOptions: {
        headers: Object.fromEntries(headers)
      }
    };

    // Create new configuration instance
    const updatedConfig = new ClientConfiguration(configParams);

    // Recreate the client with updated configuration
    await this.createClient(updatedConfig);
  }

  // Helper to get response header value
  getResponseHeader(headerName: string): string | null {
    if (!this.lastResponse) {
      return null;
    }
    
    // Check if response has headers
    if (this.lastResponse.headers) {
      return this.lastResponse.headers[headerName] || this.lastResponse.headers[headerName.toLowerCase()];
    }
    
    // Check error response headers
    if (this.lastError && this.lastError.statusCode) {
      const statusCode = this.lastError.statusCode;
      return statusCode.toString();
    }
    
    return null;
  }

  // Enhanced status code checking
  getStatusCode(): number | null {
    if (this.lastError && this.lastError.statusCode) {
      return this.lastError.statusCode;
    }
    // Assume 200 for successful responses
    return this.lastResponse ? 200 : null;
  }

  // Track retry attempts and delays
  trackRetryAttempt(): void {
    this.retryAttempts = this.retryAttempts + 1;
  }
  
  trackRetryDelay(delay: number): void {
    this.retryDelays.push(delay);
  }
  
  resetRetryTracking(): void {
    this.retryAttempts = 0;
    this.retryDelays = [];
  }
  
  getRetryDelays(): number[] {
    return this.retryDelays;
  }
  
  // Circuit breaker simulation
  simulateCircuitBreaker(): void {
    this.retryAttempts = 0;
    this.retryDelays = [];
    this.circuitBreakerTriggered = true;
    this.circuitBreakerOpen = true;
    this.circuitBreakerFailureCount = 5; // Simulate threshold reached
  }

  // Enhanced error response parsing
  parseErrorResponse(errorData: any): void {
    if (!errorData || !this.lastError) return;
    
    try {
      if (typeof errorData === 'string') {
        errorData = JSON.parse(errorData);
      }
      
      this.lastError.errorCode = errorData.code;
      this.lastError.errorMessage = errorData.message;
      this.lastError.errorDetails = errorData.details;
      this.lastError.requestId = errorData.request_id;
    } catch (e) {
      // If parsing fails, store raw error data
      if (this.lastError) {
        this.lastError.rawErrorData = errorData;
      }
    }
  }

  // Enhanced error handling and categorization
  categorizeError(errorData: any): string {
    if (!this.lastError) {
      return 'unknown';
    }
    
    // Set error properties for validation
    if (errorData) {
      this.lastError.errorCode = errorData.code;
      this.lastError.errorMessage = errorData.message;
      this.lastError.errorDetails = errorData.details;
      this.lastError.requestId = errorData.request_id;
    }
    
    // Store raw error data
    this.lastError.rawErrorData = errorData;
    return 'categorized';
  }
  
  // Validate error message content
  validateErrorMessage(message: string): boolean {
    if (!this.lastError) {
      throw new Error('No error occurred');
    }
    
    const errorCategories: Record<string, RegExp> = {
      validation: /invalid|required|missing|malformed/i,
      authentication: /unauthorized|unauthenticated|invalid.*token|expired.*token/i,
      authorization: /forbidden|permission.*denied|access.*denied/i,
      notFound: /not.*found|does.*not.*exist/i,
      timeout: /timeout|timed.*out/i,
      rateLimit: /rate.*limit|too.*many.*requests/i
    };
    
    this.lastError.errorCategory = 'unknown';
    const pattern = errorCategories[message];
    if (pattern && pattern.test(this.lastError.message || '')) {
      this.lastError.errorCategory = message;
      return true;
    }
    
    return false;
  }

  // Assertion helpers
  assertResponseSuccess(): void {
    if (this.lastError) {
      throw new Error(`Expected successful response, got error: ${this.lastError.message}`);
    }
    expect(this.lastResponse).to.not.be.null;
  }

  assertResponseFailure(): void {
    if (!this.lastError) {
      throw new Error('Expected error response, but got success');
    }
  }

  // Response validation helpers
  validateResponseTupleCount(count: number): void {
    if (!this.lastResponse || !this.lastResponse.tuples) {
      throw new Error('No tuples in response');
    }
    
    const actualCount = this.lastResponse.tuples.length;
    expect(actualCount).to.equal(count);
  }
  
  validateResponseTuples(expectedTuples: any[]): void {
    if (!this.lastResponse || !this.lastResponse.tuples) {
      throw new Error('No tuples in response');
    }
    
    const actualTuples = this.lastResponse.tuples;
    expectedTuples.forEach((expectedTuple: any, index: number) => {
      const actualTuple = actualTuples[index];
      expect(actualTuple.key.user).to.equal(expectedTuple.user);
      expect(actualTuple.key.relation).to.equal(expectedTuple.relation);
      expect(actualTuple.key.object).to.equal(expectedTuple.object);
    });
  }
  
  // Data management helpers
  saveResponseData(key: string): void {
    if (!this.lastResponse) {
      throw new Error('No response to save');
    }
    this.savedData.set(key, this.lastResponse);
  }
  
  getSavedData(key: string): any {
    if (!this.savedData.has(key)) {
      throw new Error(`No saved data found for key: ${key}`);
    }
    return this.savedData.get(key);
  }

  compareWithSaved(key: string, shouldMatch: boolean = true): void {
    const savedData = this.savedData.get(key);
    if (!savedData) {
      throw new Error(`No saved data found for key: ${key}`);
    }
    
    if (!this.lastResponse) {
      throw new Error('No current response to compare');
    }

    const currentStr = JSON.stringify(this.lastResponse);
    const savedStr = JSON.stringify(savedData);
    const matches = currentStr === savedStr;

    if (shouldMatch && !matches) {
      throw new Error(`Current response does not match saved response for key: ${key}`);
    } else if (!shouldMatch && matches) {
      throw new Error(`Current response matches saved response for key: ${key} (expected different)`);
    }
  }
}

setWorldConstructor(TestWorld);

// Cucumber hooks
Before(function() {
  this.reset();
});

After(function(scenario: any) {
  // Log scenario results for debugging
  if (scenario.result && scenario.result.status === 'failed') {
    console.log(`Scenario failed: ${scenario.pickle.name}`);
    if (this.lastError) {
      console.log('Last error:', this.lastError);
    }
  }
});


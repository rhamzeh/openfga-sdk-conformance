import { Given, When, Then } from '@cucumber/cucumber';
import { TestWorld } from './world';
import { 
  CheckRequest, 
  CheckRequestTupleKey, 
  BatchCheckRequest, 
  BatchCheckItem,
  ListObjectsRequest,
  ReadRequest,
  ReadRequestTupleKey,
  WriteRequest
} from '@openfga/sdk';

// Check API steps
When('I call Check with user {string} relation {string} object {string}', async function(user, relation, object) {
  if (!this.client) {
    throw new Error('Client not configured');
  }

  await this.executeApiCall(async () => {
    return await this.client.check({
      user,
      relation,
      object
    });
  });
});

When('I call Check with:', async function(dataTable) {
  if (!this.client) {
    throw new Error('Client not configured');
  }

  const tupleKey: Partial<CheckRequestTupleKey> = {};
  let authorizationModelId: string | undefined;
  
  for (const row of dataTable.hashes()) {
    const key = Object.keys(row)[0];
    const value = Object.values(row)[0] as string;
    
    switch (key) {
      case 'user':
        tupleKey.user = value;
        break;
      case 'relation':
        tupleKey.relation = value;
        break;
      case 'object':
        tupleKey.object = value;
        break;
      case 'authorizationModelId':
        authorizationModelId = value;
        break;
    }
  }

  const checkRequest: Partial<CheckRequest> = {
    tuple_key: tupleKey as CheckRequestTupleKey
  };
  
  if (authorizationModelId) {
    checkRequest.authorization_model_id = authorizationModelId;
  }

  // Add contextual tuples if available
  if (this.contextualTuples) {
    checkRequest.contextual_tuples = this.contextualTuples;
    this.contextualTuples = null; // Clear after use
  }

  // Add consistency preference if available
  if (this.consistencyPreference) {
    checkRequest.consistency = this.consistencyPreference;
    this.consistencyPreference = null; // Clear after use
  }

  // Add context object if available
  if (this.contextObject) {
    checkRequest.context = this.contextObject;
    this.contextObject = null; // Clear after use
  }

  await this.executeApiCall(async () => {
    return await this.client.check(checkRequest);
  });
});

When('I call Check with authorization model {string}', async function(modelId) {
  if (!this.client) {
    throw new Error('Client not configured');
  }

  await this.executeApiCall(async () => {
    return await this.client.check({
      authorization_model_id: modelId
    });
  });
});

// Read API steps
When('I call Read with no parameters', async function() {
  if (!this.client) {
    throw new Error('Client not configured');
  }

  await this.executeApiCall(async () => {
    return await this.client.read();
  });
});

When('I call Read with:', async function(dataTable) {
  if (!this.client) {
    throw new Error('Client not configured');
  }

  const tupleKey: Partial<ReadRequestTupleKey> = {};
  const readRequest: Partial<ReadRequest> = {};
  
  for (const row of dataTable.hashes()) {
    const key = Object.keys(row)[0];
    const value = Object.values(row)[0] as string;
    
    switch (key) {
      case 'user':
        tupleKey.user = value;
        break;
      case 'relation':
        tupleKey.relation = value;
        break;
      case 'object':
        tupleKey.object = value;
        break;
      case 'pageSize':
        readRequest.page_size = parseInt(value);
        break;
      case 'continuationToken':
        readRequest.continuation_token = value;
        break;
    }
  }

  if (Object.keys(tupleKey).length > 0) {
    readRequest.tuple_key = tupleKey as ReadRequestTupleKey;
  }

  await this.executeApiCall(async () => {
    return await this.client.read(readRequest);
  });
});

When('I call Read with page size {int}', async function(pageSize) {
  if (!this.client) {
    throw new Error('Client not configured');
  }

  await this.executeApiCall(async () => {
    return await this.client.read({}, { page_size: pageSize });
  });
});

When('I call Read with continuation token {string}', async function(token) {
  if (!this.client) {
    throw new Error('Client not configured');
  }

  await this.executeApiCall(async () => {
    return await this.client.read({}, { continuation_token: token });
  });
});

When('I call Read with continuation token from {string}', async function(savedKey) {
  if (!this.client) {
    throw new Error('Client not configured');
  }

  const savedData = this.savedData.get(savedKey);
  if (!savedData) {
    throw new Error(`No saved data found for key: ${savedKey}`);
  }

  const token = savedData.continuation_token || '';

  await this.executeApiCall(async () => {
    return await this.client.read({}, { continuation_token: token });
  });
});

// Write API steps
When('I call Write with writes:', async function(dataTable) {
  if (!this.client) {
    throw new Error('Client not configured');
  }

  const writes = [];
  
  for (const row of dataTable.raw()) {
    writes.push({
      user: row[0],
      relation: row[1],
      object: row[2]
    });
  }

  await this.executeApiCall(async () => {
    return await this.client.write({ writes });
  });
});

When('I call Write with deletes:', async function(dataTable) {
  if (!this.client) {
    throw new Error('Client not configured');
  }

  const deletes = [];
  
  for (const row of dataTable.raw()) {
    deletes.push({
      user: row[0],
      relation: row[1],
      object: row[2]
    });
  }

  await this.executeApiCall(async () => {
    return await this.client.write({ deletes });
  });
});

When('I call Write with writes and deletes:', async function(dataTable) {
  if (!this.client) {
    throw new Error('Client not configured');
  }

  const writes = [];
  const deletes = [];
  
  for (const row of dataTable.raw()) {
    const operation = row[0];
    const tuple = {
      user: row[1],
      relation: row[2],
      object: row[3]
    };
    
    if (operation === 'write') {
      writes.push(tuple);
    } else if (operation === 'delete') {
      deletes.push(tuple);
    }
  }

  await this.executeApiCall(async () => {
    return await this.client.write({ writes, deletes });
  });
});

When('authorization model {string}', function(modelId) {
  // This step is used in combination with other steps to specify authorization model
  // Implementation depends on context of previous step
});

// Contextual tuples support
Given('I set contextual tuples:', function(this: TestWorld, dataTable: any) {
  const contextualTuples = [];
  
  for (const row of dataTable.raw()) {
    if (row[0] && row[1] && row[2]) { // Skip empty rows
      contextualTuples.push({
        user: row[0],
        relation: row[1],
        object: row[2]
      });
    }
  }

  // Store contextual tuples for use in subsequent API calls
  this.savedData.set('contextual_tuples', contextualTuples);
});

When('contextual tuples:', async function(this: TestWorld, dataTable: any) {
  const contextualTuples = [];
  
  for (const row of dataTable.raw()) {
    if (row[0] && row[1] && row[2]) { // Skip empty rows
      contextualTuples.push({
        user: row[0],
        relation: row[1],
        object: row[2]
      });
    }
  }

  // Store contextual tuples for use in subsequent API calls
  this.savedData.set('contextual_tuples', contextualTuples);
});

// StreamedListObjects API step definition
When('I call StreamedListObjects with:', async function(dataTable) {
  if (!this.client) {
    throw new Error('Client not configured');
  }

  const requestBody: Partial<ListObjectsRequest> = {};
  
  for (const row of dataTable.raw()) {
    if (row[0] && row[1]) {
      const key = row[0];
      const value = row[1];
      
      switch (key) {
        case 'type':
          requestBody.type = value;
          break;
        case 'relation':
          requestBody.relation = value;
          break;
        case 'user':
          requestBody.user = value;
          break;
      }
    }
  }

  // Add contextual tuples if available
  if (this.contextualTuples) {
    requestBody.contextual_tuples = this.contextualTuples;
  }

  // Add context object if available
  if (this.contextObject) {
    requestBody.context = this.contextObject;
  }

  await this.executeApiCall(async () => {
    // Check if StreamedListObjects is available in the JavaScript SDK
    if (typeof this.client.streamedListObjects === 'function') {
      // Use actual streaming API if available
      const stream = await this.client.streamedListObjects(requestBody);
      
      // Collect streamed objects
      this.streamedObjects = [];
      
      return new Promise((resolve, reject) => {
        stream.on('data', (chunk) => {
          try {
            const obj = JSON.parse(chunk);
            this.streamedObjects.push(obj);
          } catch (e) {
            // Handle non-JSON chunks or partial data
            console.warn('Failed to parse streaming chunk:', chunk);
          }
        });
        
        stream.on('end', () => {
          resolve({
            streaming: true,
            objects: this.streamedObjects
          });
        });
        
        stream.on('error', (error) => {
          reject(error);
        });
      });
    } else {
      // Fallback to regular ListObjects if streaming not supported
      const response = await this.client.listObjects(requestBody);
      
      // Convert regular response to streaming format for consistency
      this.streamedObjects = [];
      if (response.objects) {
        for (const obj of response.objects) {
          this.streamedObjects.push({ object: obj });
        }
      }
      
      return {
        streaming: false,
        objects: this.streamedObjects,
        fallback: true
      };
    }
  });
});

// Consistency preferences support
When('consistency preference {string}', function(this: TestWorld, preference: string) {
  this.savedData.set('consistency_preference', preference);
});

// Context object step definitions
Given('I set the context object:', function(this: TestWorld, dataTable: any) {
  const contextObject: any = {};
  
  for (const row of dataTable.hashes()) {
    const key = Object.keys(row)[0];
    const value = Object.values(row)[0] as string;
    
    if (key && value) {
      // Handle nested keys like "user.department"
      const keys = key.split('.');
      let current = contextObject;
      
      for (let i = 0; i < keys.length - 1; i++) {
        if (!current[keys[i]]) {
          current[keys[i]] = {};
        }
        current = current[keys[i]];
      }
      
      // Set the final value
      const finalKey = keys[keys.length - 1];
      current[finalKey] = value;
    }
  }
  
  this.savedData.set('context_object', contextObject);
});

Given('I set context object:', function(this: TestWorld, dataTable: any) {
  const contextObject: any = {};
  
  for (const row of dataTable.hashes()) {
    const key = Object.keys(row)[0];
    const value = Object.values(row)[0] as string;
    
    if (key && value) {
      contextObject[key] = value;
    }
  }
  
  this.savedData.set('context_object', contextObject);
});

Given('I set the context object as JSON:', function(this: TestWorld, jsonString: string) {
  try {
    const contextObject = JSON.parse(jsonString);
    this.savedData.set('context_object', contextObject);
  } catch (error: any) {
    throw new Error(`Invalid JSON in context object: ${error.message}`);
  }
});

// Enhanced Check with contextual tuples and consistency
When('I call Check with contextual tuples and consistency:', async function(this: TestWorld, dataTable: any) {
  if (!this.client) {
    throw new Error('Client not configured');
  }

  const checkRequest: any = {};
  
  for (const row of dataTable.hashes()) {
    const key = Object.keys(row)[0];
    const value = Object.values(row)[0];
    
    switch (key) {
      case 'user':
        checkRequest.user = value;
        break;
      case 'relation':
        checkRequest.relation = value;
        break;
      case 'object':
        checkRequest.object = value;
        break;
      case 'authorizationModelId':
        checkRequest.authorization_model_id = value;
        break;
    }
  }

  // Add contextual tuples if available
  if (this.contextualTuples && this.contextualTuples.length > 0) {
    checkRequest.contextual_tuples = this.contextualTuples;
  }

  // Add consistency preference if available
  if (this.savedData.has('consistency_preference')) {
    const consistency = this.savedData.get('consistency_preference');
    // Map consistency preference to SDK format
    checkRequest.consistency = consistency;
  }

  // Add context object if available
  if (this.savedData.has('context_object')) {
    const contextObject = this.savedData.get('context_object');
    checkRequest.context = contextObject;
  }

  await this.executeApiCall(async () => {
    return await this.client.check(checkRequest);
  });

  // Clear stored contextual tuples and consistency for next test
  this.contextualTuples = null;
  this.consistencyPreference = null;
});

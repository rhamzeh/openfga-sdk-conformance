import { Given, When, Then } from '@cucumber/cucumber';
import { TestWorld } from './world';
import { expect } from 'chai';

// Streaming API steps for JavaScript SDK
When('I call ReadChanges with:', async function(dataTable) {
  if (!this.client) {
    throw new Error('Client not configured');
  }

  const readChangesRequest: any = {};
  
  for (const row of dataTable.hashes()) {
    const key = Object.keys(row)[0];
    const value = Object.values(row)[0];
    
    switch (key) {
      case 'type':
        readChangesRequest.type = value;
        break;
      case 'pageSize':
        readChangesRequest.page_size = parseInt(value as string);
        break;
      case 'continuationToken':
        readChangesRequest.continuation_token = value;
        break;
    }
  }

  await this.executeApiCall(async () => {
    return await this.client.readChanges(readChangesRequest);
  });
});

When('I call ListObjects with:', async function(dataTable) {
  if (!this.client) {
    throw new Error('Client not configured');
  }

  const listObjectsRequest: any = {};
  
  for (const row of dataTable.hashes()) {
    const key = Object.keys(row)[0];
    const value = Object.values(row)[0];
    
    switch (key) {
      case 'type':
        listObjectsRequest.type = value;
        break;
      case 'relation':
        listObjectsRequest.relation = value;
        break;
      case 'user':
        listObjectsRequest.user = value;
        break;
      case 'pageSize':
        listObjectsRequest.page_size = parseInt(value as string);
        break;
      case 'continuationToken':
        listObjectsRequest.continuation_token = value;
        break;
    }
  }

  await this.executeApiCall(async () => {
    return await this.client.listObjects(listObjectsRequest);
  });
});

// Streaming response assertions
Then('the streaming response should contain {int} changes', function(count) {
  this.assertResponseSuccess();
  
  if (this.lastResponse && this.lastResponse.changes) {
    const actualCount = this.lastResponse.changes.length;
    expect(actualCount).to.equal(count);
  } else {
    throw new Error('Response does not contain changes array');
  }
});

Then('the streaming response should have continuation token', function() {
  this.assertResponseSuccess();
  
  if (this.lastResponse && this.lastResponse.continuation_token) {
    expect(this.lastResponse.continuation_token).to.not.be.empty;
  } else {
    throw new Error('Response does not contain continuation token');
  }
});

@client-configuration
Feature: Client Configuration Options
  Test SDK client configuration options like batch size limits and parallel request handling

  Background:
    Given I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"

  @batch-size-limits
  Scenario: ClientBatchCheck respects maxBatchSize configuration
    Given I configure the client with maxBatchSize 10
    When I call ClientBatchCheck with 15 permission checks
    Then the SDK should split the request into 2 batches
    And the first batch should contain 10 checks
    And the second batch should contain 5 checks
    And all checks should be processed successfully

  @batch-size-limits
  Scenario: ClientBatchCheck with batch size exactly at limit
    Given I configure the client with maxBatchSize 5
    When I call ClientBatchCheck with 5 permission checks
    Then the SDK should process all checks in a single batch
    And the response should contain 5 results

  @parallel-requests
  Scenario: ClientBatchCheck respects maxParallelRequests configuration
    Given I configure the client with maxParallelRequests 2
    When I call ClientBatchCheck with 20 permission checks
    Then the SDK should process at most 2 requests in parallel
    And all checks should be processed successfully
    And the response should contain 20 results

  @parallel-requests
  Scenario: ClientBatchCheck with single parallel request
    Given I configure the client with maxParallelRequests 1
    When I call ClientBatchCheck with 10 permission checks
    Then the SDK should process requests sequentially
    And all checks should be processed successfully
    And the response should contain 10 results

  @configuration-validation
  Scenario: Invalid maxBatchSize configuration
    When I configure the client with maxBatchSize 0
    Then the SDK should throw a configuration error
    And the error should indicate invalid batch size

  @configuration-validation
  Scenario: Invalid maxParallelRequests configuration
    When I configure the client with maxParallelRequests 0
    Then the SDK should throw a configuration error
    And the error should indicate invalid parallel request limit

  @performance-optimization
  Scenario: Large batch with optimal configuration
    Given I configure the client with:
      | maxBatchSize        | 50 |
      | maxParallelRequests | 5  |
    When I call ClientBatchCheck with 200 permission checks
    Then the SDK should split into 4 batches of 50 checks each
    And the SDK should process up to 5 batches in parallel
    And all 200 checks should be processed successfully
    And the total processing time should be optimized

  @default-configuration
  Scenario: Default configuration behavior
    Given I have a client with default configuration
    When I call ClientBatchCheck with 100 permission checks
    Then the SDK should use default maxBatchSize
    And the SDK should use default maxParallelRequests
    And all checks should be processed successfully

  @configuration-override
  Scenario: Runtime configuration override
    Given I configure the client with maxBatchSize 20
    When I call ClientBatchCheck with maxBatchSize 10 and 25 permission checks
    Then the runtime configuration should override the client configuration
    And the SDK should split into 3 batches (10, 10, 5)
    And all checks should be processed successfully

Feature: ClientBatchCheck API - Client-side parallel Check calls

  Background:
    Given I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"

  @client-batch-check
  Scenario: ClientBatchCheck with parallel Check calls
    When I call ClientBatchCheck with:
      | user:alice | viewer | document:readme |
      | user:bob   | editor | document:readme |
      | user:carol | admin  | document:readme |
    Then the response should be successful
    And the response should contain client batch check results
    And each check should have been processed individually
    And all checks should be processed in parallel
    And the first check should be "allowed"
    And the second check should be "allowed"
    And the third check should be "denied"

  @client-batch-check
  Scenario: ClientBatchCheck with contextual tuples
    Given contextual tuples:
      | user:temp | viewer | document:temp |
    When I call ClientBatchCheck with:
      | user:alice | viewer | document:readme |
      | user:temp  | viewer | document:temp  |
    Then the response should be successful
    And the response should contain client batch check results
    And contextual tuples should be applied to all checks
    And each check should have been processed individually
    And all checks should be processed in parallel

  @client-batch-check
  Scenario: ClientBatchCheck with context object
    Given I set the context object:
      | session_id | client-batch-session-123 |
      | ip_address | 192.168.1.100            |
    When I call ClientBatchCheck with:
      | user:alice | viewer | document:readme |
      | user:bob   | editor | document:readme |
    Then the response should be successful
    And the response should contain client batch check results
    And context should be applied to all checks
    And each check should have been processed individually
    And all checks should be processed in parallel

  @client-batch-check
  Scenario: ClientBatchCheck with custom headers
    Given I set the request header "X-Client-Batch-ID" to "client-batch-001"
    And I set the request header "Authorization" to "Bearer client-batch-token-123"
    When I call ClientBatchCheck with:
      | user:alice | viewer | document:readme |
      | user:bob   | editor | document:readme |
    Then the response should be successful
    And the response should contain client batch check results
    And each individual check should have included header "X-Client-Batch-ID" with value "client-batch-001"
    And each individual check should have included authorization header "Bearer client-batch-token-123"
    And each check should have been processed individually

  @client-batch-check
  Scenario: ClientBatchCheck with mixed results and error handling
    When I call ClientBatchCheck with:
      | user:alice   | viewer | document:readme    |
      | user:bob     | admin  | document:readme    |
      | user:charlie | viewer | document:forbidden |
    Then the response should be successful
    And the response should contain client batch check results
    And the results should have mixed allowed and denied outcomes
    And each result should include individual check details
    And each check should have been processed individually
    And all checks should be processed in parallel

  @client-batch-check
  Scenario: ClientBatchCheck performance with parallel processing
    When I call ClientBatchCheck with 10 permission checks
    Then the response should be successful
    And the response should contain 10 client batch check results
    And all checks should be processed in parallel
    And each check should have been processed individually

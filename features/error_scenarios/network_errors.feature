Feature: Network Error Scenarios

  Background:
    Given I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"

  @network-error
  Scenario: Request timeout
    Given the server response time is set to 30 seconds
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the request should timeout
    And the error should be a timeout error

  @network-error
  Scenario: Connection refused
    Given the server is unavailable
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the request should fail with connection error
    And the error message should contain "connection refused"

  @network-error
  Scenario: DNS resolution failure
    Given I have a client configured with invalid hostname "invalid.openfga.example"
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the request should fail with DNS error
    And the error message should contain "name resolution failed"

  @network-error
  Scenario: SSL certificate validation failure
    Given the server has an invalid SSL certificate
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the request should fail with SSL error
    And the error message should contain "certificate verification failed"

  @network-error
  Scenario: Network interruption during request
    Given the network connection is interrupted after 2 seconds
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the request should fail with network error
    And the error should indicate connection lost

  @network-error
  Scenario: Partial response corruption
    Given the server returns corrupted response data
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the request should fail with parsing error
    And the error message should contain "invalid response format"

  @network-error
  Scenario: Connection pool exhaustion
    Given the connection pool is exhausted
    When I make 100 concurrent Check requests
    Then some requests should fail with connection pool error
    And the error message should contain "no available connections"

  @network-error
  Scenario: HTTP/2 protocol error
    Given the server has HTTP/2 protocol issues
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the request should fail with protocol error
    And the client should fallback to HTTP/1.1 if supported

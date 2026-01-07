Feature: Token Authentication

  @token-auth
  Scenario: API call with bearer token
    Given I have a client configured with bearer token "test-token-123"
    And I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And the request should have been made with authorization header "Bearer test-token-123"

  @token-auth
  Scenario: API call with invalid token
    Given I have a client configured with bearer token "invalid-token"
    And I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should fail
    And the response should have status code 401
    And the error message should contain "unauthorized"

  @token-auth
  Scenario: Token refresh scenario
    Given I have a client configured with refreshable token
    And I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    And the token expires
    And I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then both responses should be successful
    And the token should have been refreshed automatically

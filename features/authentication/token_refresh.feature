Feature: Token Refresh Scenarios

  @token-refresh
  Scenario: Token refresh during long operations
    Given I have a client configured with refreshable token
    And I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"
    When I perform multiple operations over time:
      | operation   | delay_seconds |
      | Check       | 0             |
      | ListObjects | 30            |
      | Write       | 60            |
      | BatchCheck  | 90            |
    Then all operations should be successful
    And authentication tokens should be refreshed as needed
    And no authentication failures should occur

  @token-refresh
  Scenario: Token refresh with custom refresh endpoint
    Given I have a client configured with custom token refresh:
      | refresh_endpoint | https://auth.example.com/refresh |
      | refresh_token    | refresh-token-123                |
    And I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"
    When the access token expires
    And I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And the token should have been refreshed using the custom endpoint

  @token-refresh
  Scenario: Token refresh failure handling
    Given I have a client configured with invalid refresh token
    And I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"
    When the access token expires
    And I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should fail
    And the error should indicate token refresh failure

  @token-refresh
  Scenario: Automatic token refresh with retry
    Given I have a client configured with automatic token refresh
    And I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"
    When I make multiple concurrent requests with expired tokens
    Then all requests should eventually succeed
    And token refresh should happen automatically
    And concurrent requests should not cause multiple refresh attempts

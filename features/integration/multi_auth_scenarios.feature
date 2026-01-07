Feature: Multi-Authentication Scenarios

  @integration @multi-auth
  Scenario: Switch between authentication methods
    Given I have a client configured with no authentication
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    When I reconfigure the client with bearer token "test-token-123"
    And I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And the request should have been made with authorization header

  @integration @multi-auth
  Scenario: OIDC authentication with token refresh during operations
    Given I have a client configured with OIDC authentication
    When I perform multiple operations over time:
      | operation   | delay_seconds |
      | Check       | 0             |
      | ListObjects | 30            |
      | Write       | 60            |
      | BatchCheck  | 90            |
    Then all operations should be successful
    And authentication tokens should be refreshed as needed
    And no authentication failures should occur

  @integration @multi-auth
  Scenario: Authentication failure recovery
    Given I have a client configured with invalid credentials
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should fail with authentication error
    When I reconfigure the client with valid credentials
    And I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful

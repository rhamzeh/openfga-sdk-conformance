Feature: OIDC Base Authentication

  @oidc-base
  Scenario: OIDC authentication with client credentials
    Given I have a client configured with OIDC client credentials:
      | client_id     | test-client-id     |
      | client_secret | test-client-secret |
      | issuer        | https://auth.example.com |
    And I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And the request should have been made with a valid access token

  @oidc-base
  Scenario: OIDC token refresh
    Given I have a client configured with OIDC client credentials:
      | client_id     | test-client-id     |
      | client_secret | test-client-secret |
      | issuer        | https://auth.example.com |
    And I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    And the access token expires
    And I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then both responses should be successful
    And the access token should have been refreshed automatically

  @oidc-base
  Scenario: OIDC authentication failure
    Given I have a client configured with invalid OIDC credentials:
      | client_id     | invalid-client-id     |
      | client_secret | invalid-client-secret |
      | issuer        | https://auth.example.com |
    And I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should fail
    And the error should indicate authentication failure

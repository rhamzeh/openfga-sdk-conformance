Feature: OIDC Configurable Endpoints

  @oidc-configurable
  Scenario: OIDC with custom token endpoint
    Given I have a client configured with OIDC and custom token endpoint:
      | client_id     | test-client-id     |
      | client_secret | test-client-secret |
      | issuer        | https://auth.example.com |
      | token_endpoint | https://auth.example.com/custom/token |
    And I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And the token should have been obtained from the custom endpoint

  @oidc-configurable
  Scenario: OIDC with custom scopes
    Given I have a client configured with OIDC and custom scopes:
      | client_id     | test-client-id     |
      | client_secret | test-client-secret |
      | issuer        | https://auth.example.com |
      | scopes        | openid profile custom:scope |
    And I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And the token request should have included the custom scopes

  @oidc-configurable
  Scenario: OIDC with custom audience
    Given I have a client configured with OIDC and custom audience:
      | client_id     | test-client-id     |
      | client_secret | test-client-secret |
      | issuer        | https://auth.example.com |
      | audience      | https://api.openfga.example |
    And I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And the token should have the correct audience

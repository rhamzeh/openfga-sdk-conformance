Feature: OIDC Custom Claims

  @oidc-custom-claims @javascript-only
  Scenario: OIDC with custom claims in token
    Given I have a client configured with OIDC and custom claims:
      | client_id     | test-client-id     |
      | client_secret | test-client-secret |
      | issuer        | https://auth.example.com |
      | custom_claims | {"department": "engineering", "role": "admin"} |
    And I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And the token should contain the custom claims

  @oidc-custom-claims @javascript-only
  Scenario: OIDC with dynamic custom claims
    Given I have a client configured with OIDC and dynamic custom claims:
      | client_id     | test-client-id     |
      | client_secret | test-client-secret |
      | issuer        | https://auth.example.com |
      | claims_provider | function() { return {"timestamp": Date.now()}; } |
    And I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And the token should contain dynamically generated claims

  @oidc-custom-claims @javascript-only
  Scenario: OIDC with conditional custom claims
    Given I have a client configured with OIDC and conditional custom claims:
      | client_id     | test-client-id     |
      | client_secret | test-client-secret |
      | issuer        | https://auth.example.com |
      | claims_condition | user_type === "admin" |
      | custom_claims | {"admin_privileges": true} |
    And I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And the token should conditionally contain custom claims

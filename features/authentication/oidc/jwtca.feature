Feature: OIDC JWTCA (JWT Certificate Authority)

  @oidc-jwtca @javascript-only
  Scenario: OIDC with JWTCA certificate validation
    Given I have a client configured with OIDC and JWTCA:
      | client_id     | test-client-id     |
      | client_secret | test-client-secret |
      | issuer        | https://auth.example.com |
      | jwtca_cert    | -----BEGIN CERTIFICATE----- ... |
    And I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And the JWT should have been validated using the CA certificate

  @oidc-jwtca @javascript-only
  Scenario: OIDC with JWTCA and custom claims validation
    Given I have a client configured with OIDC, JWTCA, and custom claims:
      | client_id     | test-client-id     |
      | client_secret | test-client-secret |
      | issuer        | https://auth.example.com |
      | jwtca_cert    | -----BEGIN CERTIFICATE----- ... |
      | custom_claims | {"department": "engineering"} |
    And I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And the JWT should have been validated using the CA certificate
    And the token should contain the custom claims

  @oidc-jwtca @javascript-only
  Scenario: OIDC with invalid JWTCA certificate
    Given I have a client configured with OIDC and invalid JWTCA:
      | client_id     | test-client-id     |
      | client_secret | test-client-secret |
      | issuer        | https://auth.example.com |
      | jwtca_cert    | -----BEGIN CERTIFICATE----- INVALID ... |
    And I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should fail
    And the error should indicate certificate validation failure

Feature: End-to-End Workflows

  @integration @end-to-end
  Scenario: Complete authorization workflow
    Given I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"
    When I create an authorization model with:
      | type     | relations |
      | user     |           |
      | document | reader, writer |
    And I write tuples:
      | user     | relation | object        |
      | user:alice | writer   | document:doc1 |
      | user:bob   | reader   | document:doc1 |
    And I check permissions:
      | user     | relation | object        | expected |
      | user:alice | writer   | document:doc1 | allowed  |
      | user:alice | reader   | document:doc1 | allowed  |
      | user:bob   | reader   | document:doc1 | allowed  |
      | user:bob   | writer   | document:doc1 | denied   |
    Then all operations should be successful
    And the authorization model should be active
    And the tuples should be stored correctly
    And the permission checks should return expected results

  @integration @end-to-end
  Scenario: Multi-store workflow with different models
    When I create store "store-1" with authorization model:
      | type     | relations |
      | user     |           |
      | document | reader    |
    And I create store "store-2" with authorization model:
      | type     | relations |
      | user     |           |
      | file     | viewer    |
    And I write tuples to "store-1":
      | user     | relation | object        |
      | user:alice | reader   | document:doc1 |
    And I write tuples to "store-2":
      | user     | relation | object    |
      | user:alice | viewer   | file:f1   |
    And I check permissions in both stores
    Then both stores should operate independently
    And each store should use its own authorization model
    And cross-store operations should be isolated

  @integration @end-to-end
  Scenario: Complex relationship hierarchy workflow
    Given I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"
    When I create an authorization model with complex relationships:
      | type     | relations |
      | user     |           |
      | group    | member    |
      | folder   | owner, viewer |
      | document | parent, reader, writer |
    And I write hierarchical tuples:
      | user     | relation | object        |
      | user:alice | member   | group:eng     |
      | group:eng  | owner    | folder:proj1  |
      | folder:proj1 | parent | document:spec |
      | user:bob   | reader   | document:spec |
    And I expand relationships for "document:spec#reader"
    And I list objects for "user:alice" with relation "reader" and type "document"
    Then the hierarchy should be correctly established
    And the expand should show inherited permissions
    And the list objects should include accessible documents

  @integration @end-to-end
  Scenario: Authentication and authorization workflow
    Given I have a client configured with OIDC authentication
    And I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"
    When I authenticate with valid credentials
    And I perform multiple API operations:
      | operation   | parameters |
      | Check       | user:alice, viewer, document:doc1 |
      | ListObjects | user:alice, viewer, document |
      | Write       | user:bob, editor, document:doc2 |
      | BatchCheck  | multiple permission checks |
    Then all operations should be authenticated properly
    And all API calls should succeed
    And the authentication token should be used consistently

  @integration @integration
  Scenario: Multi-store authorization workflow
    Given I have multiple stores configured:
      | store_id                     | name           |
      | 01ARZ3NDEKTSV4RRFFQ69G5FAV  | primary-store  |
      | 01ARZ3NDEKTSV4RRFFQ69G5FAW  | secondary-store|
    When I perform operations across multiple stores
    Then all operations should be successful
    And each store should maintain separate authorization data

  @integration @multi-feature
  Scenario: Complete Authorization Workflow with Headers and Streaming
    Given I set the request header "X-Workflow-ID" to "auth-workflow-001"
    And I set the request header "X-Client-Version" to "integration-test-1.0"
    When I call Write with writes:
      | user:alice | viewer | document:readme |
      | user:bob   | editor | document:readme |
      | user:carol | owner  | document:readme |
    Then the response should be successful
    And the request should have included header "X-Workflow-ID" with value "auth-workflow-001"
    
    Given I set the request header "Authorization" to "Bearer workflow-token-123"
    And I set the request header "X-Permission-Check" to "viewer-access"
    When I call Check with:
      | user     | user:alice        |
      | relation | viewer            |
      | object   | document:readme   |
    Then the response should be successful
    And the response should be "allowed"
    And the authorization header should contain "Bearer workflow-token-123"

  @integration @contextual-tuples
  Scenario: Contextual Authorization with Headers and Advanced APIs
    Given I set the request header "X-Context-ID" to "contextual-auth-002"
    And contextual tuples:
      | user:temp-user | viewer | document:temp |
    And I set the context object:
      | key        | value           |
      | session_id | temp-session-123 |
      | ip_address | 192.168.1.100   |
    Given I set the request header "Authorization" to "Bearer contextual-token-456"
    When I call Check with:
      | user     | user:alice      |
      | relation | viewer          |
      | object   | document:readme |
    Then the response should be successful
    And the authorization header should contain "Bearer contextual-token-456"
    And the request should have included header "X-Context-ID" with value "contextual-auth-002"

  @integration @complex-permissions
  Scenario: Complex Permission Checks with Multiple Relations
    Given I set the request header "X-Complex-Setup" to "hierarchy-001"
    When I call Write with writes:
      | user:alice   | member | team:engineering    |
      | team:engineering | viewer | project:backend |
      | user:bob     | admin  | project:backend     |
    Then the response should be successful
    
    Given I set the request header "Authorization" to "Bearer hierarchy-token"
    And I set the request header "X-Permission-Type" to "indirect"
    When I call Check with:
      | user     | user:alice      |
      | relation | viewer          |
      | object   | project:backend |
    Then the response should be successful
    And the response should be "allowed"
    And the authorization header should contain "Bearer hierarchy-token"

Feature: Check API

  Background:
    Given I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"

  @check
  Scenario: Basic Check request
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And the response should have field "allowed" with value true

  @check
  Scenario: Check request with contextual tuples
    Given I set contextual tuples:
      | user     | relation | object        |
      | user:bob | editor   | document:doc1 |
    When I call Check with:
      | user     | user:bob      |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And the response should have field "allowed" with value true

  @check
  Scenario: Check request with context object
    Given I set context object:
      | key        | value |
      | ip_address | 1.1.1.1 |
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And the response should have field "allowed" with value true

  @check
  Scenario: Check request with authorization model ID
    Given I set authorization model ID to "01G50QVV17PECNVAHX1GG4Y5NC"
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And the response should have field "allowed" with value true

  @check
  Scenario: Check request with higher consistency
    Given I set consistency to "HIGHER_CONSISTENCY"
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And the response should have field "allowed" with value true

  @check
  Scenario: Check request denied
    When I call Check with:
      | user     | user:carol    |
      | relation | admin         |
      | object   | document:doc1 |
    Then the response should be successful
    And the response should have field "allowed" with value false

  @check
  Scenario: Check with contextual tuples affecting result
    When I call Check with:
      | user     | user:alice    |
      | relation | admin         |
      | object   | document:doc1 |
    And contextual tuples:
      | user:alice | admin | document:doc1 |
    Then the response should be successful
    And the result should be "allowed: true"

  @check
  Scenario: Check without contextual tuples should fail
    When I call Check with:
      | user     | user:alice    |
      | relation | admin         |
      | object   | document:doc1 |
    Then the response should be successful
    And the result should be "allowed: false"

  @check
  Scenario: Check with minimize latency consistency
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    And consistency preference "MINIMIZE_LATENCY"
    Then the response should be successful
    And the result should be "allowed: true"

  @check
  Scenario: Check with nested context object
    Given I set the context object:
      | key             | value                    |
      | user.department | engineering              |
      | user.level      | senior                   |
      | resource.type   | confidential             |
      | request.time    | 2024-01-15T10:30:00Z     |
    When I call Check with:
      | user     | user:bob      |
      | relation | editor        |
      | object   | document:doc2 |
    Then the response should be successful
    And the result should be "allowed: false"

  @check
  Scenario: Check with JSON context object
    Given I set the context object as JSON:
      """
      {
        "user": {
          "department": "finance",
          "clearance_level": 3,
          "active": true
        },
        "resource": {
          "classification": "restricted",
          "owner": "finance_team"
        },
        "request": {
          "timestamp": "2024-01-15T14:30:00Z",
          "ip_address": "192.168.1.100"
        }
      }
      """
    When I call Check with:
      | user     | user:carol    |
      | relation | viewer        |
      | object   | document:doc3 |
    Then the response should be successful

  @check @wildcard
  Scenario: Check with wildcard user
    When I call Check with:
      | user     | user:*        |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And the result should be "allowed: true"

  @check @wildcard
  Scenario: Check with type-bound public access
    When I call Check with:
      | user     | user:*           |
      | relation | public_viewer    |
      | object   | document:public1 |
    Then the response should be successful
    And the result should be "allowed: true"

  @check @userset
  Scenario: Check with nested userset expansion
    When I call Check with:
      | user     | group:engineering#member |
      | relation | viewer                   |
      | object   | document:doc1            |
    Then the response should be successful
    And the result should be "allowed: true"

  @check @userset
  Scenario: Check with complex userset hierarchy
    When I call Check with:
      | user     | organization:acme#admin |
      | relation | owner                   |
      | object   | document:confidential   |
    Then the response should be successful
    And the result should be "allowed: true"

  @check @userset
  Scenario: Check with inherited permissions
    When I call Check with:
      | user     | group:managers#member |
      | relation | admin                 |
      | object   | folder:projects       |
    Then the response should be successful
    And the result should be "allowed: true"

  @check @performance
  Scenario: Check with deep userset expansion
    Given I have a deeply nested userset hierarchy with 10 levels
    When I call Check with:
      | user     | group:level10#member |
      | relation | viewer               |
      | object   | document:deep        |
    Then the response should be successful
    And the operation should complete within 5 seconds

  @check @complex
  Scenario: Check with multiple contextual conditions
    Given I set contextual tuples:
      | user           | relation | object           |
      | user:alice     | member   | group:finance    |
      | group:finance  | viewer   | folder:reports   |
    And I set context object as JSON:
      """
      {
        "time": "2024-01-15T09:00:00Z",
        "location": "office",
        "security_level": "high"
      }
      """
    When I call Check with:
      | user     | user:alice      |
      | relation | viewer          |
      | object   | document:budget |
    Then the response should be successful
    And the result should be "allowed: true"

  @check @complex
  Scenario: Check with conditional relations
    Given I set context object as JSON:
      """
      {
        "user": {
          "department": "engineering",
          "clearance": "secret",
          "active": true
        },
        "resource": {
          "classification": "confidential"
        },
        "request": {
          "time": "2024-01-15T14:00:00Z",
          "method": "read"
        }
      }
      """
    When I call Check with:
      | user     | user:bob               |
      | relation | conditional_viewer     |
      | object   | document:classified    |
    Then the response should be successful
    And the result should be "allowed: false"

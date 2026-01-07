Feature: Malformed Request Error Scenarios

  Background:
    Given I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"

  @malformed-request
  Scenario: Invalid JSON in request body
    When I send a Check request with invalid JSON:
      """
      {
        "tuple_key": {
          "user": "user:alice",
          "relation": "viewer",
          "object": "document:doc1"
        },
        "invalid_field": 
      """
    Then the response should fail with a validation error
    And the error message should contain "invalid JSON"

  @malformed-request
  Scenario: Missing required fields
    When I call Check with:
      | user     |               |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should fail with a validation error
    And the error message should contain "user is required"

  @malformed-request
  Scenario: Invalid user format
    When I call Check with:
      | user     | invalid_user_format |
      | relation | viewer              |
      | object   | document:doc1       |
    Then the response should fail with a validation error
    And the error message should contain "invalid user format"

  @malformed-request
  Scenario: Invalid object format
    When I call Check with:
      | user     | user:alice      |
      | relation | viewer          |
      | object   | invalid:format: |
    Then the response should fail with a validation error
    And the error message should contain "invalid object format"

  @malformed-request
  Scenario: Extremely long field values
    When I call Check with:
      | user     | user:${"a" * 10000}    |
      | relation | viewer                 |
      | object   | document:doc1          |
    Then the response should fail with a validation error
    And the error message should contain "field too long"

  @malformed-request
  Scenario: Invalid characters in fields
    When I call Check with:
      | user     | user:alice\x00\x01 |
      | relation | viewer             |
      | object   | document:doc1      |
    Then the response should fail with a validation error
    And the error message should contain "invalid characters"

  @malformed-request
  Scenario: Malformed authorization model ID
    When I call Check with authorization model ID "invalid-model-id-format"
    And:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should fail with a validation error
    And the error message should contain "invalid authorization model ID format"

  @malformed-request
  Scenario: Invalid contextual tuples format
    When I call Check with malformed contextual tuples:
      """
      [
        {
          "user": "user:bob",
          "relation": "editor",
          "object": 
        }
      ]
      """
    And:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should fail with a validation error
    And the error message should contain "invalid contextual tuple format"

  @malformed-request
  Scenario: Invalid context object structure
    When I call Check with malformed context object:
      """
      {
        "nested": {
          "too": {
            "deep": {
              "structure": {
                "exceeds": {
                  "limit": "value"
                }
              }
            }
          }
        }
      }
      """
    And:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should fail with a validation error
    And the error message should contain "context object too complex"

  @malformed-request
  Scenario: Request body too large
    When I send a Check request with body size exceeding 1MB
    Then the response should fail with a validation error
    And the error message should contain "request too large"

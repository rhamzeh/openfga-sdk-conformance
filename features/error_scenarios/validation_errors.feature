Feature: Validation Errors

  Background:
    Given I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"

  @validation-errors
  Scenario: Missing required user field
    When I call Check with:
      | user     |               |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should fail
    And the response should have status code 400
    And the response should fail with a validation error
    And the error message should contain "user is required"

  @validation-errors
  Scenario: Missing required relation field
    When I call Check with:
      | user     | user:alice    |
      | relation |               |
      | object   | document:doc1 |
    Then the response should fail
    And the response should have status code 400
    And the response should fail with a validation error
    And the error message should contain "relation is required"

  @validation-errors
  Scenario: Missing required object field
    When I call Check with:
      | user     | user:alice |
      | relation | viewer     |
      | object   |            |
    Then the response should fail
    And the response should have status code 400
    And the response should fail with a validation error
    And the error message should contain "object is required"

  @validation-errors
  Scenario: Invalid tuple format
    When I call Write with writes:
      | user     | relation | object |
      | invalid  | viewer   | doc1   |
    Then the response should fail
    And the response should have status code 400
    And the response should fail with a validation error

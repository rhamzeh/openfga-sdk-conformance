Feature: Write API

  Background:
    Given I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"

  @write
  Scenario: Write with adds only
    When I call Write with writes:
      | user     | relation | object        |
      | user:bob | editor   | document:doc1 |
    Then the response should be successful

  @write
  Scenario: Write with deletes only
    When I call Write with deletes:
      | user     | relation | object        |
      | user:bob | editor   | document:doc1 |
    Then the response should be successful

  @write
  Scenario: Write with both adds and deletes
    When I call Write with writes:
      | user      | relation | object        |
      | user:alice | viewer   | document:doc1 |
    And I call Write with deletes:
      | user     | relation | object        |
      | user:bob | editor   | document:doc1 |
    Then the response should be successful

  @write
  Scenario: Write with authorization model ID
    Given I set authorization model ID to "01G50QVV17PECNVAHX1GG4Y5NC"
    When I call Write with writes:
      | user     | relation | object        |
      | user:bob | editor   | document:doc1 |
    Then the response should be successful

  @write
  Scenario: Write with conditional tuples
    When I call Write with conditional writes:
      | user     | relation | object        | condition |
      | user:bob | editor   | document:doc1 | ip_check  |
    Then the response should be successful

  @write
  Scenario: Write with duplicate handling - ignore
    When I call Write with onDuplicate option "ignore" and writes:
      | user     | relation | object        |
      | user:bob | editor   | document:doc1 |
    Then the response should be successful

  @write
  Scenario: Write with duplicate handling - fail
    When I call Write with onDuplicate option "fail" and writes:
      | user     | relation | object        |
      | user:bob | editor   | document:doc1 |
    Then the response should fail with a validation error
    And the error message should contain "duplicate tuple"

  @write
  Scenario: Write with missing delete handling - ignore
    When I call Write with onMissing option "ignore" and deletes:
      | user      | relation | object        |
      | user:carol | viewer   | document:doc1 |
    Then the response should be successful

  @write
  Scenario: Write with missing delete handling - fail
    When I call Write with onMissing option "fail" and deletes:
      | user      | relation | object        |
      | user:carol | viewer   | document:doc1 |
    Then the response should fail with a validation error
    And the error message should contain "tuple not found"

  @write
  Scenario: Write with transaction options - mixed operations
    When I call Write with transaction options:
      | onDuplicate | ignore |
      | onMissing   | ignore |
    And writes:
      | user     | relation | object        |
      | user:bob | editor   | document:doc1 |
    And deletes:
      | user      | relation | object        |
      | user:carol | viewer   | document:doc1 |
    Then the response should be successful

  @write
  Scenario: Write with bulk operations - partial success
    When I call Write with writes:
      | user      | relation | object        |
      | user:alice | viewer   | document:doc1 |
      | user:bob   | editor   | document:doc2 |
      | user:carol | admin    | document:doc3 |
    Then the response should be successful
    And each tuple should have individual status

  @write
  Scenario: Write with authorization model validation
    Given I set authorization model ID to "01G50QVV17PECNVAHX1GG4Y5NC"
    When I call Write with writes:
      | user     | relation     | object        |
      | user:bob | invalid_rel  | document:doc1 |
    Then the response should fail with a validation error
    And the error message should contain "relation not found in authorization model"

  @write
  Scenario: Write with concurrent conflict handling
    When I call Write concurrently with writes:
      | user      | relation | object        |
      | user:alice | viewer   | document:doc1 |
      | user:bob   | viewer   | document:doc1 |
    Then both responses should handle conflicts appropriately
    And at least one response should be successful

  @write
  Scenario: Write with large batch operations
    When I call Write with 1000 tuple writes
    Then the response should be successful
    And the operation should complete within 30 seconds
    And all tuples should be written successfully

  @write
  Scenario: Write with missing handling - ignore
    When I call Write with onMissing option "ignore" and deletes:
      | user     | relation | object        |
      | user:bob | editor   | document:doc1 |
    Then the response should be successful

Feature: DeleteTuples - Client-side convenience method

  Background:
    Given I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"

  @delete-tuples
  Scenario: DeleteTuples with single tuple
    When I call DeleteTuples with:
      | user     | relation | object        |
      | user:bob | editor   | document:doc1 |
    Then the response should be successful
    And the tuple should be deleted successfully

  @delete-tuples
  Scenario: DeleteTuples with multiple tuples
    When I call DeleteTuples with:
      | user     | relation | object        |
      | user:alice | viewer   | document:doc1 |
      | user:bob   | editor   | document:doc1 |
      | user:carol | admin    | document:doc1 |
    Then the response should be successful
    And all tuples should be deleted successfully

  @delete-tuples
  Scenario: DeleteTuples with authorization model ID
    Given I set authorization model ID to "01G50QVV17PECNVAHX1GG4Y5NC"
    When I call DeleteTuples with:
      | user     | relation | object        |
      | user:bob | editor   | document:doc1 |
    Then the response should be successful
    And the tuple should be deleted with the specified model

  @delete-tuples
  Scenario: DeleteTuples with onMissing option
    When I call DeleteTuples with onMissing option "IGNORE" and:
      | user     | relation | object        |
      | user:nonexistent | editor   | document:doc1 |
    Then the response should be successful
    And missing tuples should be ignored

  @delete-tuples
  Scenario: DeleteTuples with onMissing option "RETURN_ERROR"
    When I call DeleteTuples with onMissing option "RETURN_ERROR" and:
      | user     | relation | object        |
      | user:nonexistent | editor   | document:doc1 |
    Then the response should fail
    And the error should indicate missing tuple conflict

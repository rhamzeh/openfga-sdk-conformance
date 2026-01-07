Feature: WriteTuples - Client-side convenience method

  Background:
    Given I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"

  @write-tuples
  Scenario: WriteTuples with single tuple
    When I call WriteTuples with:
      | user     | relation | object        |
      | user:bob | editor   | document:doc1 |
    Then the response should be successful
    And the tuple should be written successfully

  @write-tuples
  Scenario: WriteTuples with multiple tuples
    When I call WriteTuples with:
      | user     | relation | object        |
      | user:alice | viewer   | document:doc1 |
      | user:bob   | editor   | document:doc1 |
      | user:carol | admin    | document:doc1 |
    Then the response should be successful
    And all tuples should be written successfully

  @write-tuples
  Scenario: WriteTuples with authorization model ID
    Given I set authorization model ID to "01G50QVV17PECNVAHX1GG4Y5NC"
    When I call WriteTuples with:
      | user     | relation | object        |
      | user:bob | editor   | document:doc1 |
    Then the response should be successful
    And the tuple should be written with the specified model

  @write-tuples
  Scenario: WriteTuples with onDuplicate option
    When I call WriteTuples with onDuplicate option "IGNORE" and:
      | user     | relation | object        |
      | user:bob | editor   | document:doc1 |
      | user:bob | editor   | document:doc1 |
    Then the response should be successful
    And duplicate tuples should be ignored

  @write-tuples
  Scenario: WriteTuples with conditional tuples
    When I call WriteTuples with conditional writes:
      | user     | relation | object        | condition |
      | user:bob | editor   | document:doc1 | ip_check  |
    Then the response should be successful
    And the conditional tuple should be written

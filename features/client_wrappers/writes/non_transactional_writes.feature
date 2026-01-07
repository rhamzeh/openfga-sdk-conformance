Feature: Non-transactional Writes - Client-side convenience method

  Background:
    Given I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"

  @non-transactional-writes
  Scenario: Non-transactional write with multiple tuples
    When I call Write in non-transactional mode with writes:
      | user     | relation | object        |
      | user:bob | editor   | document:doc1 |
      | user:alice | viewer | document:doc2 |
    Then the response should be successful
    And the write should be processed in non-transactional mode
    And each tuple should have individual status

  @non-transactional-writes
  Scenario: Non-transactional delete with multiple tuples
    When I call Write in non-transactional mode with deletes:
      | user     | relation | object        |
      | user:bob | editor   | document:doc1 |
      | user:alice | viewer | document:doc2 |
    Then the response should be successful
    And the write should be processed in non-transactional mode
    And each tuple should have individual status

  @non-transactional-writes
  Scenario: Non-transactional write with mixed success and failure
    When I call Write in non-transactional mode with writes:
      | user     | relation | object        |
      | user:bob | editor   | document:doc1 |
      | user:invalid | viewer | document:doc2 |
    Then the response should be successful
    And the write should be processed in non-transactional mode
    And some tuples should succeed while others fail
    And each tuple should have individual status

Feature: Not Found Errors

  @not-found-errors
  Scenario: Not found error with invalid store
    Given I have a client configured with store "invalid_store_id"
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should fail
    And the response should have status code 404
    And the error message should contain "store not found"

  @not-found-errors
  Scenario: Not found error with invalid authorization model
    Given I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"
    And I set authorization model ID to "invalid_model_id"
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should fail
    And the response should have status code 404
    And the error message should contain "authorization model not found"

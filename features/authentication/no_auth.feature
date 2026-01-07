Feature: No Authentication

  @no-auth
  Scenario: API call without authentication
    Given I have a client configured with no authentication
    And I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And the response should have field "allowed" with value true

  @no-auth
  Scenario: Multiple API calls without authentication
    Given I have a client configured with no authentication
    And I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    And I call ListObjects with:
      | user     | user:alice |
      | relation | viewer     |
      | type     | document   |
    Then both responses should be successful

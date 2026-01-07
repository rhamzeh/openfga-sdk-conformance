Feature: DeleteStore API

  @delete-store
  Scenario: Delete existing store
    Given I have created a store "test-store-to-delete"
    When I call DeleteStore for store "test-store-to-delete"
    Then the response should be successful
    And the store should be marked for deletion

  @delete-store
  Scenario: Delete non-existent store
    When I call DeleteStore for store "non-existent-store-id"
    Then the response should fail
    And the response should have status code 404
    And the error message should contain "store not found"

  @delete-store
  Scenario: Delete store with custom headers
    Given I have created a store "test-store-to-delete"
    And I set the request header "X-Custom-Header" to "test-value"
    When I call DeleteStore for store "test-store-to-delete"
    Then the response should be successful
    And the request should have been made with custom headers

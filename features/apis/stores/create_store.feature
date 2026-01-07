Feature: CreateStore API

  @create-store
  Scenario: Create store with name
    When I call CreateStore with name "test-store"
    Then the response should be successful
    And the response should contain a store ID
    And the response should contain the store name "test-store"

  @create-store
  Scenario: Create store without name
    When I call CreateStore with no parameters
    Then the response should be successful
    And the response should contain a store ID

  @create-store
  Scenario: Create multiple stores
    When I call CreateStore with name "store-1"
    And I call CreateStore with name "store-2"
    Then both responses should be successful
    And the store IDs should be different

  @create-store
  Scenario: Create store with custom headers
    Given I set the request header "X-Custom-Header" to "test-value"
    When I call CreateStore with name "test-store"
    Then the response should be successful
    And the request should have been made with custom headers

Feature: ListStores API

  @list-stores
  Scenario: List all stores
    When I call ListStores
    Then the response should be successful
    And the response should contain stores

  @list-stores
  Scenario: List stores with page size
    When I call ListStores with page size 10
    Then the response should be successful
    And the response should have at most 10 stores

  @list-stores
  Scenario: List stores with continuation token
    Given I have saved a continuation token from "previous_stores"
    When I call ListStores with continuation token from "previous_stores"
    Then the response should be successful

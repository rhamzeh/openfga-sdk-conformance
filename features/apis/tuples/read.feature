Feature: Read API

  Background:
    Given I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"

  @read
  Scenario: Read with no parameters
    When I call Read with no parameters
    Then the response should be successful
    And the response should contain tuples

  @read
  Scenario: Read with tuple key filter
    When I call Read with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And the response should contain tuples
    And the tuples should match the filter

  @read
  Scenario: Read with object filter only
    When I call Read with:
      | object | document:doc1 |
    Then the response should be successful
    And the response should contain tuples

  @read
  Scenario: Read with page size
    When I call Read with page size 10
    Then the response should be successful
    And the response should contain at most 10 tuples

  @read
  Scenario: Read with continuation token
    Given I have a previous Read response saved as "first_page"
    When I call Read with continuation token from "first_page"
    Then the response should be successful
    And the response should contain different tuples from "first_page"

  @read @pagination
  Scenario: Read with large page size
    When I call Read with page size 1000
    Then the response should be successful
    And the response should contain at most 1000 tuples
    And the operation should complete within 10 seconds

  @read @pagination
  Scenario: Read with invalid continuation token
    When I call Read with continuation token "invalid_token_123"
    Then the response should fail with a validation error
    And the error message should contain "invalid continuation token"

  @read @pagination
  Scenario: Read with expired continuation token
    When I call Read with continuation token "expired_token_456"
    Then the response should fail with a validation error
    And the error message should contain "expired continuation token"

  @read @pagination
  Scenario: Read pagination boundary conditions
    Given I have exactly 100 tuples in the store
    When I call Read with page size 50
    Then the response should be successful
    And the response should contain exactly 50 tuples
    And the response should have continuation token
    When I call Read with the continuation token
    Then the response should contain exactly 50 tuples
    And the response should not have continuation token

  @read @filtering
  Scenario: Read with multiple filter combinations
    When I call Read with:
      | user     | user:alice |
      | relation | viewer     |
    Then the response should be successful
    And all returned tuples should match the filter

  @read @filtering
  Scenario: Read with type-only object filter
    When I call Read with:
      | object | document: |
    Then the response should be successful
    And all returned objects should be of type "document"

  @read @filtering
  Scenario: Read with empty result set
    When I call Read with:
      | user     | user:nonexistent |
      | relation | viewer           |
      | object   | document:doc1    |
    Then the response should be successful
    And the response should be empty
    And the response should not have continuation token

  @read @filtering
  Scenario: Read with pagination and filtering combined
    When I call Read with:
      | object | document: |
    And page size 5
    Then the response should be successful
    And the response should contain at most 5 tuples
    And all returned objects should be of type "document"
    When I call Read with the continuation token and same filter
    Then the response should be successful
    And all returned objects should be of type "document"

  @read
  Scenario: Read with specific continuation token
    When I call Read with continuation token "eyJwayI6IkxBVEVTVF9OU0NPTkZJR19hdXRoMHN0b3JlIiwic2siOiIxem1qbXF3MWZLZExTcUoyN01MdTdqTjh0cWgifQ=="
    Then the response should be successful
    When I call Read with:
      | object | document: |
    Then the response should be successful
    And the response should contain tuples
    And all tuples should be of type "document"

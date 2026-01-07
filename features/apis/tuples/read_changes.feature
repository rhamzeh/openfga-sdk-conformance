Feature: ReadChanges API

  Background:
    Given I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"

  @read-changes
  Scenario: ReadChanges with no parameters
    When I call ReadChanges with no parameters
    Then the response should be successful
    And the response should contain changes

  @read-changes
  Scenario: ReadChanges with type filter
    When I call ReadChanges with type "document"
    Then the response should be successful
    And the response should contain changes
    And all changes should be for type "document"

  @read-changes
  Scenario: ReadChanges with page size
    When I call ReadChanges with page size 10
    Then the response should be successful
    And the response should contain changes
    And the response should have at most 10 changes

  @read-changes
  Scenario: ReadChanges with continuation token
    Given I have saved a continuation token from "previous_changes"
    When I call ReadChanges with continuation token from "previous_changes"
    Then the response should be successful
    And the response should contain changes

  @read-changes
  Scenario: ReadChanges shows write operations
    Given I have written some tuples
    When I call ReadChanges with no parameters
    Then the response should be successful
    And the response should contain changes
    And the changes should include write operations

  @read-changes
  Scenario: ReadChanges shows delete operations
    Given I have deleted some tuples
    When I call ReadChanges with no parameters
    Then the response should be successful
    And the response should contain changes
    And the changes should include delete operations

  @read-changes
  Scenario: ReadChanges with empty store
    Given I have an empty store
    When I call ReadChanges with no parameters
    Then the response should be successful
    And the response should contain no changes

  @read-changes
  Scenario: ReadChanges with specific change type
    When I call ReadChanges with:
      | type     | authorization_model |
      | pageSize | 10                  |
    Then the response should be successful
    And each change should have type "authorization_model"

  @read-changes
  Scenario: ReadChanges with invalid type
    When I call ReadChanges with:
      | type | invalid_type |
    Then the response should fail
    And the response should have status code 400
    And the error message should contain "invalid type"

  @read-changes
  Scenario: ReadChanges pagination limits
    When I call ReadChanges with:
      | pageSize | 1000 |
    Then the response should fail
    And the response should have status code 400
    And the error message should contain "page size too large"

  @read-changes
  Scenario: ReadChanges with empty result set
    When I call ReadChanges with:
      | type | tuple |
    And I set the request header "X-Test-Scenario" to "empty_changes"
    Then the response should be successful
    And the response should contain exactly 0 changes
    And the response should not have continuation token

  @read-changes
  Scenario: ReadChanges with time-based filtering - from timestamp
    When I call ReadChanges with from timestamp "2024-01-15T14:30:25Z"
    Then the response should be successful
    And the response should contain changes
    And all changes should be after timestamp "2024-01-15T14:30:25Z"

  @read-changes
  Scenario: ReadChanges with time-based filtering - to timestamp
    When I call ReadChanges with to timestamp "2024-01-15T16:30:25Z"
    Then the response should be successful
    And the response should contain changes
    And all changes should be before timestamp "2024-01-15T16:30:25Z"

  @read-changes
  Scenario: ReadChanges with time-based filtering - time range
    When I call ReadChanges with time range:
      | from | 2024-01-15T14:30:25Z |
      | to   | 2024-01-15T16:30:25Z |
    Then the response should be successful
    And the response should contain changes
    And all changes should be within time range "2024-01-15T14:30:25Z" to "2024-01-15T16:30:25Z"

  @read-changes
  Scenario: ReadChanges with relative time filtering
    When I call ReadChanges with from timestamp "2 hours ago"
    Then the response should be successful
    And the response should contain changes
    And all changes should be after relative timestamp "2 hours ago"

  @read-changes
  Scenario: ReadChanges with time filtering and type filter
    When I call ReadChanges with:
      | type | tuple                 |
      | from | 2024-01-15T14:30:25Z |
      | to   | 2024-01-15T16:30:25Z |
    Then the response should be successful
    And the response should contain changes
    And all changes should be for type "tuple"
    And all changes should be within time range "2024-01-15T14:30:25Z" to "2024-01-15T16:30:25Z"

  @read-changes
  Scenario: ReadChanges with invalid time format
    When I call ReadChanges with from timestamp "invalid-timestamp"
    Then the response should fail
    And the response should have status code 400
    And the error message should contain "invalid timestamp format"

  @read-changes
  Scenario: ReadChanges with future timestamp
    When I call ReadChanges with from timestamp "2030-01-01T00:00:00Z"
    Then the response should be successful
    And the response should contain exactly 0 changes

  @read-changes
  Scenario: ReadChanges with time filtering and pagination
    When I call ReadChanges with:
      | from     | 2024-01-15T14:30:25Z |
      | pageSize | 5                     |
    Then the response should be successful
    And the response should contain changes
    And the response should have at most 5 changes
    And all changes should be after timestamp "2024-01-15T14:30:25Z"

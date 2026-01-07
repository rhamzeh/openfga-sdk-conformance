Feature: Conditional Write API

  Background:
    Given I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"

  @conditional-write
  Scenario: Write with simple condition
    When I call Write with conditional writes:
      | user     | relation | object        | condition |
      | user:bob | editor   | document:doc1 | ip_check  |
    Then the response should be successful
    And the tuple should be written with condition "ip_check"

  @conditional-write
  Scenario: Write with complex condition expression
    When I call Write with conditional writes:
      | user      | relation | object        | condition                           |
      | user:alice | viewer   | document:doc2 | time_range && department_match      |
    Then the response should be successful
    And the tuple should be written with condition "time_range && department_match"

  @conditional-write
  Scenario: Write with nested condition logic
    When I call Write with conditional writes:
      | user     | relation | object        | condition                                    |
      | user:bob | admin    | folder:secure | (clearance_level >= 3) && (location == 'office') |
    Then the response should be successful
    And the tuple should be written with complex condition

  @conditional-write
  Scenario: Write with condition validation failure
    When I call Write with conditional writes:
      | user     | relation | object        | condition        |
      | user:bob | editor   | document:doc1 | invalid_condition |
    Then the response should fail with a validation error
    And the error message should contain "invalid condition"

  @conditional-write
  Scenario: Write with multiple conditional tuples
    When I call Write with conditional writes:
      | user      | relation | object        | condition     |
      | user:alice | viewer   | document:doc1 | time_check    |
      | user:bob   | editor   | document:doc1 | role_check    |
      | user:carol | admin    | document:doc1 | clearance_check |
    Then the response should be successful
    And all tuples should be written with their respective conditions

  @conditional-write
  Scenario: Write with condition and contextual tuples
    Given I set contextual tuples:
      | user     | relation | object           |
      | user:bob | member   | group:engineering |
    When I call Write with conditional writes:
      | user     | relation | object        | condition                    |
      | user:bob | viewer   | document:doc1 | group_membership && active   |
    Then the response should be successful
    And the condition should evaluate against contextual tuples

  @conditional-write
  Scenario: Write with time-based condition
    When I call Write with conditional writes:
      | user     | relation | object        | condition                                      |
      | user:bob | viewer   | document:doc1 | current_time >= '2024-01-01T00:00:00Z'        |
    Then the response should be successful
    And the condition should include time validation

  @conditional-write
  Scenario: Write with attribute-based condition
    When I call Write with conditional writes:
      | user     | relation | object        | condition                                    |
      | user:bob | viewer   | document:doc1 | user.department == 'engineering' && user.active |
    Then the response should be successful
    And the condition should validate user attributes

  @conditional-write
  Scenario: Write with resource-based condition
    When I call Write with conditional writes:
      | user     | relation | object        | condition                                        |
      | user:bob | viewer   | document:doc1 | resource.classification != 'top_secret'         |
    Then the response should be successful
    And the condition should validate resource attributes

  @conditional-write
  Scenario: Write with combined conditions and transaction options
    When I call Write with transaction options:
      | onDuplicate | ignore |
      | onMissing   | fail   |
    And conditional writes:
      | user     | relation | object        | condition                           |
      | user:bob | editor   | document:doc1 | (time_valid && location_valid)     |
    Then the response should be successful
    And the transaction should respect both conditions and options

  @conditional-write
  Scenario: Delete with condition
    When I call Write with conditional deletes:
      | user     | relation | object        | condition    |
      | user:bob | editor   | document:doc1 | cleanup_time |
    Then the response should be successful
    And the tuple should be deleted only if condition is met

  @conditional-write
  Scenario: Write with condition evaluation context
    Given I set context object as JSON:
      """
      {
        "user": {
          "department": "finance",
          "clearance": 3,
          "active": true
        },
        "resource": {
          "classification": "confidential",
          "owner": "finance_team"
        },
        "request": {
          "time": "2024-01-15T14:00:00Z",
          "ip": "192.168.1.100"
        }
      }
      """
    When I call Write with conditional writes:
      | user     | relation | object        | condition                                           |
      | user:bob | viewer   | document:doc1 | user.department == 'finance' && resource.classification == 'confidential' |
    Then the response should be successful
    And the condition should evaluate against the context object

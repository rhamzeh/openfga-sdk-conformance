Feature: WriteAuthorizationModel API

  Background:
    Given I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"

  @write-authorization-model
  Scenario: Write new authorization model
    When I call WriteAuthorizationModel with type definitions:
      | type     | relations |
      | user     |           |
      | document | reader, writer |
    Then the response should be successful
    And the response should contain an authorization model ID
    And the model ID should be valid

  @write-authorization-model
  Scenario: Write authorization model with complex relations
    When I call WriteAuthorizationModel with type definitions:
      | type     | relations |
      | user     |           |
      | group    | member    |
      | document | reader, writer, owner |
    Then the response should be successful
    And the response should contain an authorization model ID

  @write-authorization-model
  Scenario: Write authorization model with conditions
    When I call WriteAuthorizationModel with conditional type definitions:
      | type     | relations | conditions |
      | user     |           |            |
      | document | reader    | ip_check   |
    Then the response should be successful
    And the response should contain an authorization model ID

  @write-authorization-model
  Scenario: Write invalid authorization model
    When I call WriteAuthorizationModel with invalid type definitions:
      | type | relations |
      |      | reader    |
    Then the response should fail
    And the response should have status code 400
    And the response should fail with a validation error

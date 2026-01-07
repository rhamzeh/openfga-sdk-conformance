Feature: ReadAuthorizationModels API

  Background:
    Given I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"

  @read-authorization-models
  Scenario: Read all authorization models
    When I call ReadAuthorizationModels
    Then the response should be successful
    And the response should contain authorization models
    And the models should be sorted by creation time descending

  @read-authorization-models
  Scenario: Read authorization models with page size
    When I call ReadAuthorizationModels with page size 5
    Then the response should be successful
    And the response should contain authorization models
    And the response should have at most 5 models

  @read-authorization-models
  Scenario: Read authorization models with continuation token
    Given I have saved a continuation token from "previous_models"
    When I call ReadAuthorizationModels with continuation token from "previous_models"
    Then the response should be successful
    And the response should contain authorization models

  @read-authorization-models
  Scenario: Read authorization models from empty store
    Given I have an empty store
    When I call ReadAuthorizationModels
    Then the response should be successful
    And the response should contain no authorization models

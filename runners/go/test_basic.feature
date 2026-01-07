Feature: Basic Go SDK Runner Validation
  As a developer
  I want to validate the Go SDK runner works correctly
  So that I can run conformance tests

  @smoke
  Scenario: Client configuration
    Given I have a client configured with store "01H0H40Z9QDYNR71MRAG9FPE7M"
    Then the client should be configured successfully

  @smoke  
  Scenario: Basic check operation
    Given I have a client configured with store "01H0H40Z9QDYNR71MRAG9FPE7M"
    When I call Check with user "user:alice" relation "viewer" object "document:readme"
    Then the response should be successful

  @smoke
  Scenario: Read with no parameters
    Given I have a client configured with store "01H0H40Z9QDYNR71MRAG9FPE7M"
    When I call Read with no parameters
    Then the response should be successful

  @smoke
  Scenario: Write operation
    Given I have a client configured with store "01H0H40Z9QDYNR71MRAG9FPE7M"
    When I call Write with writes:
      | user:alice | viewer | document:readme |
    Then the response should be successful

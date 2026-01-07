Feature: WriteAssertions API

  Background:
    Given I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"

  @write-assertions
  Scenario: Write assertions for authorization model
    When I call WriteAssertions for authorization model "01G50QVV17PECNVAHX1GG4Y5NC" with:
      | user     | relation | object        | expectation |
      | user:alice | viewer   | document:doc1 | true        |
      | user:bob   | editor   | document:doc1 | true        |
      | user:carol | admin    | document:doc1 | false       |
    Then the response should be successful

  @write-assertions
  Scenario: Write assertions with contextual tuples
    When I call WriteAssertions for authorization model "01G50QVV17PECNVAHX1GG4Y5NC" with contextual tuples:
      | user     | relation | object        | expectation | contextual_tuples |
      | user:alice | viewer   | document:doc1 | true        | user:bob#editor@document:doc1 |
    Then the response should be successful

  @write-assertions
  Scenario: Overwrite existing assertions
    Given I have existing assertions for authorization model "01G50QVV17PECNVAHX1GG4Y5NC"
    When I call WriteAssertions for authorization model "01G50QVV17PECNVAHX1GG4Y5NC" with:
      | user     | relation | object        | expectation |
      | user:dave  | viewer   | document:doc2 | true        |
    Then the response should be successful
    And the old assertions should be replaced

  @write-assertions
  Scenario: Write assertions for non-existent model
    When I call WriteAssertions for authorization model "non-existent-model-id" with:
      | user     | relation | object        | expectation |
      | user:alice | viewer   | document:doc1 | true        |
    Then the response should fail
    And the response should have status code 404
    And the error message should contain "authorization model not found"

Feature: HTTP Versions Support

  Background:
    Given I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"

  @http-versions
  Scenario: HTTP/1.1 connection
    Given I have a client configured with HTTP version "1.1"
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And the connection should use HTTP/1.1

  @http-versions
  Scenario: HTTP/2 connection
    Given I have a client configured with HTTP version "2"
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And the connection should use HTTP/2

  @http-versions
  Scenario: HTTP/3 connection
    Given I have a client configured with HTTP version "3"
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And the connection should use HTTP/3

  @http-versions
  Scenario: Connection pooling with HTTP/2
    Given I have a client configured with HTTP version "2" and connection pooling
    When I make 5 concurrent Check requests
    Then all responses should be successful
    And the requests should reuse HTTP/2 connections
    And connection pooling should be utilized

  @http-versions
  Scenario: Automatic HTTP version negotiation
    Given I have a client configured with automatic HTTP version negotiation
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And the client should negotiate the best available HTTP version

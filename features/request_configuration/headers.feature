Feature: Request Headers Configuration

  Background:
    Given I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"

  @headers
  Scenario: Default headers on all requests
    Given I have a client configured with default headers:
      | X-Client-Version | 1.0.0 |
      | X-Source-App     | test-app |
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And the request should have included header "X-Client-Version" with value "1.0.0"
    And the request should have included header "X-Source-App" with value "test-app"

  @headers
  Scenario: Per-request header overrides
    Given I have a client configured with default headers:
      | X-Client-Version | 1.0.0 |
    And I set the request header "X-Client-Version" to "2.0.0"
    And I set the request header "X-Request-ID" to "req-123"
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And the request should have included header "X-Client-Version" with value "2.0.0"
    And the request should have included header "X-Request-ID" with value "req-123"

  @headers
  Scenario: Authorization header handling
    Given I set the request header "Authorization" to "Bearer custom-token-123"
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And the request should have included authorization header "Bearer custom-token-123"

  @headers
  Scenario: Content-Type header validation
    Given I set the request header "Content-Type" to "application/json; charset=utf-8"
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And the request should have included header "Content-Type" with value "application/json; charset=utf-8"

  @headers
  Scenario: Custom headers with special characters
    Given I set the request header "X-Custom-Data" to "value with spaces & symbols!"
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And the request should have included header "X-Custom-Data" with value "value with spaces & symbols!"

  @headers
  Scenario: Headers with unicode characters
    Given I set the request header "X-Unicode" to "测试值"
    When I call ListObjects with:
      | user     | user:charlie |
      | relation | viewer       |
      | type     | document     |
    Then the response should be successful
    And the request should have included header "X-Unicode" with value "测试值"

  @headers
  Scenario: Override default headers
    Given I set the request header "User-Agent" to "CustomSDK/1.0"
    And I set the request header "Accept" to "application/json"
    When I call Read with no parameters
    Then the response should be successful
    And the request should have included header "User-Agent" with value "CustomSDK/1.0"
    And the request should have included header "Accept" with value "application/json"

  @headers
  Scenario: Validate response headers
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And the response should include header "Content-Type" with value "application/json"
    And the response should include header "X-Request-ID"

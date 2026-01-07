Feature: SDK-Specific Concurrency and Async Patterns

  Background:
    Given I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"

  @concurrency @go-only
  Scenario: Goroutine safety with concurrent Check requests
    When I make 100 concurrent Check requests using goroutines:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then all requests should complete successfully
    And no race conditions should occur
    And the client should remain thread-safe

  @concurrency @go-only
  Scenario: Context cancellation with goroutines
    Given I create a context with 2 second timeout
    When I call Check with the context:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    And the request takes 5 seconds to complete
    Then the request should be cancelled
    And the error should indicate context cancellation

  @concurrency @javascript-only
  Scenario: Promise-based concurrent requests
    When I make 50 concurrent Check requests using Promises:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then all promises should resolve successfully
    And the requests should complete in parallel
    And no promise should be rejected due to concurrency

  @concurrency @javascript-only
  Scenario: Async/await pattern with error handling
    When I call Check using async/await pattern:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    And an error occurs during the request
    Then the error should be properly caught
    And the async function should handle the exception gracefully

  @concurrency @dotnet-only
  Scenario: Task-based concurrent operations
    When I make 75 concurrent Check requests using Tasks:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then all tasks should complete successfully
    And the operations should run in parallel
    And no task should deadlock

  @concurrency @dotnet-only
  Scenario: ConfigureAwait pattern compliance
    When I call Check using ConfigureAwait(false):
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the request should not capture synchronization context
    And the continuation should run on thread pool thread
    And no deadlock should occur in synchronous contexts

  @concurrency @dotnet-only
  Scenario: CancellationToken support
    Given I create a CancellationToken with 3 second timeout
    When I call Check with the cancellation token:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    And the request takes 5 seconds to complete
    Then the request should be cancelled
    And an OperationCancelledException should be thrown

  @concurrency @python-only
  Scenario: Asyncio concurrent requests
    When I make 60 concurrent Check requests using asyncio:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then all coroutines should complete successfully
    And the event loop should handle all requests efficiently
    And no asyncio.TimeoutError should occur

  @concurrency @python-only
  Scenario: Generator pattern for streaming responses
    When I call StreamedListObjects using generator pattern:
      | user     | user:alice |
      | relation | viewer     |
      | type     | document   |
    Then the response should yield objects incrementally
    And memory usage should remain constant
    And the generator should be properly closed

  @concurrency @java-only
  Scenario: CompletableFuture concurrent operations
    When I make 80 concurrent Check requests using CompletableFuture:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then all futures should complete successfully
    And the operations should execute asynchronously
    And no CompletionException should occur due to concurrency

  @concurrency @java-only
  Scenario: Thread safety with multiple threads
    When I call Check from 10 different threads simultaneously:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then all threads should complete successfully
    And the client should remain thread-safe
    And no ConcurrentModificationException should occur

  @concurrency
  Scenario: Connection pooling under load
    When I make 200 concurrent requests across all API endpoints
    Then the connection pool should handle the load efficiently
    And connections should be reused appropriately
    And no connection leaks should occur

  @concurrency
  Scenario: Rate limiting with concurrent requests
    Given the server has rate limiting enabled
    When I make 1000 concurrent Check requests
    Then some requests should be rate limited
    And the client should handle rate limit responses appropriately
    And successful requests should complete normally

  @concurrency
  Scenario: Memory usage under concurrent load
    When I make 500 concurrent requests of varying sizes
    Then memory usage should remain within acceptable bounds
    And no memory leaks should occur
    And garbage collection should function normally

  @concurrency
  Scenario: Error propagation in concurrent scenarios
    When I make 100 concurrent requests with 10% error rate
    Then successful requests should complete normally
    And failed requests should propagate errors correctly
    And no successful request should be affected by failed ones

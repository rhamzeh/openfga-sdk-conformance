Feature: Load Testing

  Background:
    Given I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"

  @load-test
  Scenario: High volume Check requests
    When I make 1000 Check requests within 60 seconds
    Then at least 95% of requests should succeed
    And the average response time should be less than 100ms
    And no memory leaks should occur

  @load-test
  Scenario: Sustained load over time
    When I make 100 requests per second for 10 minutes
    Then the system should maintain consistent performance
    And response times should not degrade significantly
    And error rates should remain below 1%

  @load-test @stress
  Scenario: Stress test with burst traffic
    When I send burst traffic of 5000 requests in 10 seconds
    Then the client should handle the burst gracefully
    And connection pooling should scale appropriately
    And no requests should fail due to resource exhaustion

  @load-test @endurance
  Scenario: Endurance test with mixed operations
    When I run mixed API operations for 2 hours:
      | operation | percentage | rate_per_second |
      | Check     | 60%        | 50              |
      | Write     | 20%        | 15              |
      | Read      | 15%        | 10              |
      | ListObjects | 5%       | 5               |
    Then all operations should maintain acceptable performance
    And memory usage should remain stable
    And no resource leaks should occur

  @load-test @scalability
  Scenario: Scalability test with increasing load
    When I gradually increase load from 10 to 1000 requests per second
    Then response times should scale linearly
    And the client should adapt to increased load
    And throughput should increase proportionally

  @load-test @large-payload
  Scenario: Large payload performance test
    When I send Write requests with 1000 tuples each
    And I make 100 such requests concurrently
    Then all requests should complete within 30 seconds
    And memory usage should not exceed 500MB
    And network bandwidth should be utilized efficiently

  @load-test @pagination
  Scenario: Pagination performance under load
    Given I have 100,000 tuples in the store
    When I paginate through all tuples with page size 100
    And I make 10 concurrent pagination sequences
    Then all sequences should complete successfully
    And pagination performance should remain consistent
    And memory usage should remain constant per page

  @load-test @streaming
  Scenario: Streaming performance test
    When I call StreamedListObjects for 50 concurrent streams
    And each stream returns 10,000 objects
    Then all streams should complete successfully
    And streaming should maintain consistent throughput
    And memory usage should not accumulate across streams

  @load-test @mixed-scenarios
  Scenario: Mixed workload performance
    When I run concurrent workloads:
      | workload_type | concurrent_users | duration_minutes |
      | read_heavy    | 50               | 30               |
      | write_heavy   | 20               | 30               |
      | query_heavy   | 30               | 30               |
    Then all workloads should complete successfully
    And performance should remain stable across workload types
    And resource utilization should be balanced

  @performance @load-testing
  Scenario: Spike load testing
    When I create a spike load:
      | operation | initial_rate | spike_rate | spike_duration |
      | Check     | 5            | 50         | 10 seconds     |
    Then the system should handle the spike gracefully
    And response times should recover after the spike
    And no requests should be dropped

  @performance @load-testing
  Scenario: Gradual load increase
    When I gradually increase load:
      | operation | start_rate | end_rate | duration |
      | Check     | 1          | 20       | 60 seconds |
    Then the system should scale appropriately
    And performance metrics should be within acceptable limits
    And resource usage should be monitored

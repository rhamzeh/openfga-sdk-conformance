using System;
using System.Collections.Generic;
using System.Linq;
using FluentAssertions;
using TechTalk.SpecFlow;

namespace OpenFga.Sdk.Conformance.StepDefinitions
{
    [Binding]
    public class RetrySteps
    {
        private readonly TestContext _testContext;

        public RetrySteps(TestContext testContext)
        {
            _testContext = testContext;
        }

        [Then(@"the request should have been retried (\d+) times")]
        public void ThenTheRequestShouldHaveBeenRetriedTimes(int expectedRetries)
        {
            var actualRetries = GetRetryCount();
            actualRetries.Should().Be(expectedRetries);
        }

        [Then(@"the request should not have been retried")]
        public void ThenTheRequestShouldNotHaveBeenRetried()
        {
            var retryCount = GetRetryCount();
            retryCount.Should().Be(0);
        }

        [Then(@"the final attempt should succeed")]
        public void ThenTheFinalAttemptShouldSucceed()
        {
            _testContext.LastError.Should().BeNull();
            _testContext.LastResponse.Should().NotBeNull();
        }

        [Then(@"the retry delays should follow exponential backoff")]
        public void ThenTheRetryDelaysShouldFollowExponentialBackoff()
        {
            var delays = GetRetryDelays();
            delays.Should().NotBeEmpty();
            
            // Verify exponential growth pattern
            for (int i = 1; i < delays.Count; i++)
            {
                delays[i].Should().BeGreaterThan(delays[i - 1]);
            }
        }

        [Then(@"the retry delays should be linear with base delay (\d+)ms")]
        public void ThenTheRetryDelaysShouldBeLinearWithBaseDelay(int baseDelay)
        {
            var delays = GetRetryDelays();
            delays.Should().NotBeEmpty();
            
            // Verify linear growth pattern
            for (int i = 0; i < delays.Count; i++)
            {
                var expectedDelay = baseDelay * (i + 1);
                var tolerance = baseDelay * 0.1; // 10% tolerance
                delays[i].Should().BeInRange(expectedDelay - tolerance, expectedDelay + tolerance);
            }
        }

        [Then(@"all retry attempts should have failed")]
        public void ThenAllRetryAttemptsShouldHaveFailed()
        {
            _testContext.LastError.Should().NotBeNull();
            var retryCount = GetRetryCount();
            retryCount.Should().BeGreaterThan(0);
        }

        [Then(@"the circuit breaker should be triggered after (\d+) failures")]
        public void ThenTheCircuitBreakerShouldBeTriggeredAfterFailures(int threshold)
        {
            var circuitBreakerTriggered = _testContext.SavedData.ContainsKey("circuit_breaker_triggered") && 
                                        (bool)_testContext.SavedData["circuit_breaker_triggered"];
            circuitBreakerTriggered.Should().BeTrue();
            
            var failureCount = _testContext.SavedData.ContainsKey("circuit_breaker_failure_count") ? 
                             (int)_testContext.SavedData["circuit_breaker_failure_count"] : 0;
            failureCount.Should().Be(threshold);
        }

        [Then(@"subsequent requests should fail fast without retries")]
        public void ThenSubsequentRequestsShouldFailFastWithoutRetries()
        {
            var circuitBreakerOpen = _testContext.SavedData.ContainsKey("circuit_breaker_open") && 
                                   (bool)_testContext.SavedData["circuit_breaker_open"];
            circuitBreakerOpen.Should().BeTrue();
            
            var retryCount = GetRetryCount();
            retryCount.Should().Be(0);
        }

        [Then(@"the configuration should fail with validation error")]
        public void ThenTheConfigurationShouldFailWithValidationError()
        {
            _testContext.LastError.Should().NotBeNull();
            _testContext.LastError.Message.Should().Contain("validation");
        }

        [Then(@"the retry delays should include random jitter")]
        public void ThenTheRetryDelaysShouldIncludeRandomJitter()
        {
            var delays = GetRetryDelays();
            delays.Count.Should().BeGreaterThan(1);
            
            // Verify that delays are not exactly exponential (indicating jitter)
            bool hasJitter = false;
            for (int i = 1; i < delays.Count; i++)
            {
                var expectedExponential = delays[0] * Math.Pow(2, i);
                var tolerance = expectedExponential * 0.3; // 30% tolerance for jitter
                if (Math.Abs(delays[i] - expectedExponential) > tolerance * 0.1)
                {
                    hasJitter = true;
                    break;
                }
            }
            hasJitter.Should().BeTrue();
        }

        private int GetRetryCount()
        {
            if (_testContext.SavedData.ContainsKey("retry_attempts"))
            {
                var attempts = (List<DateTime>)_testContext.SavedData["retry_attempts"];
                return attempts.Count;
            }
            return 0;
        }

        private List<int> GetRetryDelays()
        {
            if (_testContext.SavedData.ContainsKey("retry_delays"))
            {
                return (List<int>)_testContext.SavedData["retry_delays"];
            }
            return new List<int>();
        }
    }
}

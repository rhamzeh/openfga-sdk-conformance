package dev.openfga.sdk.conformance.steps;

import io.cucumber.java.en.Then;
import org.assertj.core.api.Assertions;
import dev.openfga.sdk.conformance.support.TestContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RetrySteps {
    
    private final TestContext testContext;

    public RetrySteps(TestContext testContext) {
        this.testContext = testContext;
    }

    @Then("the request should have been retried {int} times")
    public void theRequestShouldHaveBeenRetriedTimes(int expectedRetries) {
        int actualRetries = getRetryCount();
        Assertions.assertThat(actualRetries).isEqualTo(expectedRetries);
    }

    @Then("the request should not have been retried")
    public void theRequestShouldNotHaveBeenRetried() {
        int retryCount = getRetryCount();
        Assertions.assertThat(retryCount).isEqualTo(0);
    }

    @Then("the final attempt should succeed")
    public void theFinalAttemptShouldSucceed() {
        Assertions.assertThat(testContext.getLastError()).isNull();
        Assertions.assertThat(testContext.getLastResponse()).isNotNull();
    }

    @Then("the retry delays should follow exponential backoff")
    public void theRetryDelaysShouldFollowExponentialBackoff() {
        List<Integer> delays = getRetryDelays();
        Assertions.assertThat(delays).isNotEmpty();
        
        // Verify exponential growth pattern
        for (int i = 1; i < delays.size(); i++) {
            Assertions.assertThat(delays.get(i)).isGreaterThan(delays.get(i - 1));
        }
    }

    @Then("the retry delays should be linear with base delay {int}ms")
    public void theRetryDelaysShouldBeLinearWithBaseDelay(int baseDelay) {
        List<Integer> delays = getRetryDelays();
        Assertions.assertThat(delays).isNotEmpty();
        
        // Verify linear growth pattern
        for (int i = 0; i < delays.size(); i++) {
            int expectedDelay = baseDelay * (i + 1);
            double tolerance = baseDelay * 0.1; // 10% tolerance
            Assertions.assertThat(delays.get(i)).isBetween(
                (int)(expectedDelay - tolerance), 
                (int)(expectedDelay + tolerance)
            );
        }
    }

    @Then("all retry attempts should have failed")
    public void allRetryAttemptsShouldHaveFailed() {
        Assertions.assertThat(testContext.getLastError()).isNotNull();
        int retryCount = getRetryCount();
        Assertions.assertThat(retryCount).isGreaterThan(0);
    }

    @Then("the circuit breaker should be triggered after {int} failures")
    public void theCircuitBreakerShouldBeTriggeredAfterFailures(int threshold) {
        boolean circuitBreakerTriggered = testContext.getSavedData().containsKey("circuit_breaker_triggered") &&
                                         (Boolean) testContext.getSavedData().get("circuit_breaker_triggered");
        Assertions.assertThat(circuitBreakerTriggered).isTrue();
        
        int failureCount = testContext.getSavedData().containsKey("circuit_breaker_failure_count") ?
                          (Integer) testContext.getSavedData().get("circuit_breaker_failure_count") : 0;
        Assertions.assertThat(failureCount).isEqualTo(threshold);
    }

    @Then("subsequent requests should fail fast without retries")
    public void subsequentRequestsShouldFailFastWithoutRetries() {
        boolean circuitBreakerOpen = testContext.getSavedData().containsKey("circuit_breaker_open") &&
                                   (Boolean) testContext.getSavedData().get("circuit_breaker_open");
        Assertions.assertThat(circuitBreakerOpen).isTrue();
        
        int retryCount = getRetryCount();
        Assertions.assertThat(retryCount).isEqualTo(0);
    }

    @Then("the configuration should fail with validation error")
    public void theConfigurationShouldFailWithValidationError() {
        Assertions.assertThat(testContext.getLastError()).isNotNull();
        Assertions.assertThat(testContext.getLastError().getMessage()).contains("validation");
    }

    @Then("the retry delays should include random jitter")
    public void theRetryDelaysShouldIncludeRandomJitter() {
        List<Integer> delays = getRetryDelays();
        Assertions.assertThat(delays.size()).isGreaterThan(1);
        
        // Verify that delays are not exactly exponential (indicating jitter)
        boolean hasJitter = false;
        for (int i = 1; i < delays.size(); i++) {
            double expectedExponential = delays.get(0) * Math.pow(2, i);
            double tolerance = expectedExponential * 0.3; // 30% tolerance for jitter
            if (Math.abs(delays.get(i) - expectedExponential) > tolerance * 0.1) {
                hasJitter = true;
                break;
            }
        }
        Assertions.assertThat(hasJitter).isTrue();
    }

    @SuppressWarnings("unchecked")
    private int getRetryCount() {
        if (testContext.getSavedData().containsKey("retry_attempts")) {
            List<Long> attempts = (List<Long>) testContext.getSavedData().get("retry_attempts");
            return attempts.size();
        }
        return 0;
    }

    @SuppressWarnings("unchecked")
    private List<Integer> getRetryDelays() {
        if (testContext.getSavedData().containsKey("retry_delays")) {
            return (List<Integer>) testContext.getSavedData().get("retry_delays");
        }
        return new ArrayList<>();
    }
}

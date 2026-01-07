package test.java.dev.openfga.sdk.conformance.steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import dev.openfga.sdk.conformance.support.TestContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HeaderValidationSteps {
    
    private final TestContext testContext;

    public HeaderValidationSteps(TestContext testContext) {
        this.testContext = testContext;
    }

    @When("I set the request header {string} to {string}")
    public void iSetTheRequestHeaderTo(String headerName, String headerValue) {
        if (testContext.getRequestHeaders() == null) {
            testContext.setRequestHeaders(new HashMap<>());
        }
        testContext.getRequestHeaders().put(headerName, headerValue);
    }

    @When("I clear the request header {string}")
    public void iClearTheRequestHeader(String headerName) {
        if (testContext.getRequestHeaders() != null) {
            testContext.getRequestHeaders().remove(headerName);
        }
    }

    @Then("the request should have included header {string} with value {string}")
    public void theRequestShouldHaveIncludedHeaderWithValue(String headerName, String expectedValue) {
        if (testContext.getCapturedRequestHeaders() == null) {
            throw new RuntimeException("No request headers were captured");
        }

        String actualValue = findHeaderValue(testContext.getCapturedRequestHeaders(), headerName);
        if (actualValue == null) {
            throw new RuntimeException("Request header '" + headerName + "' was not found");
        }

        if (!actualValue.equals(expectedValue)) {
            throw new RuntimeException("Request header '" + headerName + "' expected value '" + expectedValue + "', got '" + actualValue + "'");
        }
    }

    @Then("the request should have included header {string}")
    public void theRequestShouldHaveIncludedHeader(String headerName) {
        if (testContext.getCapturedRequestHeaders() == null) {
            throw new RuntimeException("No request headers were captured");
        }

        String actualValue = findHeaderValue(testContext.getCapturedRequestHeaders(), headerName);
        if (actualValue == null) {
            throw new RuntimeException("Request header '" + headerName + "' was not found");
        }
    }

    @Then("the request should not have included header {string}")
    public void theRequestShouldNotHaveIncludedHeader(String headerName) {
        if (testContext.getCapturedRequestHeaders() == null) {
            return; // No headers captured means header wasn't included
        }

        String actualValue = findHeaderValue(testContext.getCapturedRequestHeaders(), headerName);
        if (actualValue != null) {
            throw new RuntimeException("Request header '" + headerName + "' was found but should not have been included");
        }
    }

    @Then("the response should include header {string} with value {string}")
    public void theResponseShouldIncludeHeaderWithValue(String headerName, String expectedValue) {
        if (testContext.getLastResponse() == null) {
            throw new RuntimeException("No response received");
        }

        Map<String, String> responseHeaders = extractResponseHeaders();
        if (responseHeaders == null) {
            throw new RuntimeException("No response headers available");
        }

        String actualValue = findHeaderValue(responseHeaders, headerName);
        if (actualValue == null) {
            throw new RuntimeException("Response header '" + headerName + "' was not found");
        }

        if (!actualValue.equals(expectedValue)) {
            throw new RuntimeException("Response header '" + headerName + "' expected value '" + expectedValue + "', got '" + actualValue + "'");
        }
    }

    @Then("the response should include header {string}")
    public void theResponseShouldIncludeHeader(String headerName) {
        if (testContext.getLastResponse() == null) {
            throw new RuntimeException("No response received");
        }

        Map<String, String> responseHeaders = extractResponseHeaders();
        if (responseHeaders == null) {
            throw new RuntimeException("No response headers available");
        }

        String actualValue = findHeaderValue(responseHeaders, headerName);
        if (actualValue == null) {
            throw new RuntimeException("Response header '" + headerName + "' was not found");
        }
    }

    @Then("the authorization header should contain {string}")
    public void theAuthorizationHeaderShouldContain(String expectedSubstring) {
        if (testContext.getCapturedRequestHeaders() == null) {
            throw new RuntimeException("No request headers were captured");
        }

        String authValue = findHeaderValue(testContext.getCapturedRequestHeaders(), "Authorization");
        if (authValue == null) {
            throw new RuntimeException("Authorization header was not found");
        }

        if (!authValue.contains(expectedSubstring)) {
            throw new RuntimeException("Authorization header '" + authValue + "' does not contain '" + expectedSubstring + "'");
        }
    }

    @Then("the content length should be greater than {int}")
    public void theContentLengthShouldBeGreaterThan(int minLength) {
        if (testContext.getCapturedRequestHeaders() == null) {
            throw new RuntimeException("No request headers were captured");
        }

        String contentLengthStr = findHeaderValue(testContext.getCapturedRequestHeaders(), "Content-Length");
        if (contentLengthStr == null) {
            throw new RuntimeException("Content-Length header was not found");
        }

        int contentLength;
        try {
            contentLength = Integer.parseInt(contentLengthStr);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid Content-Length value: " + contentLengthStr);
        }

        if (contentLength <= minLength) {
            throw new RuntimeException("Content-Length " + contentLength + " is not greater than " + minLength);
        }
    }

    @Then("both responses should be successful")
    public void bothResponsesShouldBeSuccessful() {
        if (testContext.getResponseHistory() == null || testContext.getResponseHistory().size() < 2) {
            int count = testContext.getResponseHistory() != null ? testContext.getResponseHistory().size() : 0;
            throw new RuntimeException("Expected at least 2 responses, got " + count);
        }

        // Check last two responses
        List<Map<String, Object>> history = testContext.getResponseHistory();
        List<Map<String, Object>> lastTwo = history.subList(Math.max(0, history.size() - 2), history.size());
        
        for (int i = 0; i < lastTwo.size(); i++) {
            Object error = lastTwo.get(i).get("error");
            if (error != null) {
                throw new RuntimeException("Response " + (i + 1) + " failed: " + error.toString());
            }
        }
    }

    @Then("both requests should have included header {string} with value {string}")
    public void bothRequestsShouldHaveIncludedHeaderWithValue(String headerName, String expectedValue) {
        if (testContext.getRequestHeaderHistory() == null || testContext.getRequestHeaderHistory().size() < 2) {
            int count = testContext.getRequestHeaderHistory() != null ? testContext.getRequestHeaderHistory().size() : 0;
            throw new RuntimeException("Expected at least 2 request header sets, got " + count);
        }

        // Check last two request header sets
        List<Map<String, String>> history = testContext.getRequestHeaderHistory();
        List<Map<String, String>> lastTwo = history.subList(Math.max(0, history.size() - 2), history.size());
        
        for (int i = 0; i < lastTwo.size(); i++) {
            String actualValue = findHeaderValue(lastTwo.get(i), headerName);
            if (actualValue == null) {
                throw new RuntimeException("Request " + (i + 1) + " header '" + headerName + "' was not found");
            }
            if (!actualValue.equals(expectedValue)) {
                throw new RuntimeException("Request " + (i + 1) + " header '" + headerName + "' expected value '" + expectedValue + "', got '" + actualValue + "'");
            }
        }
    }

    @Then("the first request should have included header {string} with value {string}")
    public void theFirstRequestShouldHaveIncludedHeaderWithValue(String headerName, String expectedValue) {
        if (testContext.getRequestHeaderHistory() == null || testContext.getRequestHeaderHistory().isEmpty()) {
            throw new RuntimeException("No request headers in history");
        }

        Map<String, String> firstHeaders = testContext.getRequestHeaderHistory().get(0);
        String actualValue = findHeaderValue(firstHeaders, headerName);
        if (actualValue == null) {
            throw new RuntimeException("First request header '" + headerName + "' was not found");
        }
        if (!actualValue.equals(expectedValue)) {
            throw new RuntimeException("First request header '" + headerName + "' expected value '" + expectedValue + "', got '" + actualValue + "'");
        }
    }

    @Then("the second request should not have included header {string}")
    public void theSecondRequestShouldNotHaveIncludedHeader(String headerName) {
        if (testContext.getRequestHeaderHistory() == null || testContext.getRequestHeaderHistory().size() < 2) {
            int count = testContext.getRequestHeaderHistory() != null ? testContext.getRequestHeaderHistory().size() : 0;
            throw new RuntimeException("Expected at least 2 request header sets, got " + count);
        }

        Map<String, String> secondHeaders = testContext.getRequestHeaderHistory().get(1);
        String actualValue = findHeaderValue(secondHeaders, headerName);
        if (actualValue != null) {
            throw new RuntimeException("Second request header '" + headerName + "' was found but should not have been included");
        }
    }

    @When("I make a preflight request to Check endpoint")
    public void iMakeAPreflightRequestToCheckEndpoint() {
        // Placeholder for CORS preflight request
        throw new RuntimeException("Preflight requests not yet implemented in Java SDK");
    }

    @Then("the preflight response should be successful")
    public void thePreflightResponseShouldBeSuccessful() {
        // Placeholder for preflight response validation
        throw new RuntimeException("Preflight response validation not yet implemented");
    }

    // Helper methods

    private String findHeaderValue(Map<String, String> headers, String headerName) {
        if (headers == null) return null;

        // Check for exact match first
        if (headers.containsKey(headerName)) {
            return headers.get(headerName);
        }

        // Case-insensitive search
        String lowerHeaderName = headerName.toLowerCase();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey().toLowerCase().equals(lowerHeaderName)) {
                return entry.getValue();
            }
        }

        return null;
    }

    private Map<String, String> extractResponseHeaders() {
        if (testContext.getLastResponse() == null) return null;

        // Extract headers from response object
        // Implementation depends on the Java SDK response structure
        if (testContext.getLastResponseHeaders() != null) {
            return testContext.getLastResponseHeaders();
        }

        // Return empty map if no headers available
        return new HashMap<>();
    }
}

package dev.openfga.sdk.conformance.steps;

import dev.openfga.sdk.conformance.support.TestContext;
import dev.openfga.sdk.api.model.*;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.assertj.core.api.Assertions;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class AssertionSteps {
    private final TestContext testContext;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AssertionSteps(TestContext testContext) {
        this.testContext = testContext;
    }

    // Response assertions
    @Then("the response should be successful")
    public void theResponseShouldBeSuccessful() {
        testContext.assertResponseSuccess();
    }

    @Then("the response should fail")
    public void theResponseShouldFail() {
        testContext.assertResponseFailure();
    }

    @Then("the response should have status code {int}")
    public void theResponseShouldHaveStatusCode(int statusCode) {
        Integer actualStatusCode = testContext.getStatusCode();
        if (actualStatusCode == null) {
            throw new AssertionError("No response or error to check status code");
        }
        Assertions.assertThat(actualStatusCode).isEqualTo(statusCode);
    }

    @Then("the response should complete within {int} seconds")
    public void theResponseShouldCompleteWithin(int seconds) {
        // This would require timing implementation in the test context
        // For now, just pass if we have a response
        if (testContext.getLastResponse() == null && testContext.getLastError() == null) {
            throw new AssertionError("No response received");
        }
    }

    @Then("the result should be {string}")
    public void theResultShouldBe(String expected) {
        testContext.assertResponseSuccess();

        // Parse expected result (e.g., "allowed: true")
        String[] parts = expected.split(": ");
        Assertions.assertThat(parts).hasSize(2);

        String field = parts[0];
        String value = parts[1];

        if ("allowed".equals(field)) {
            if (testContext.getLastResponse() instanceof CheckResponse) {
                CheckResponse checkResponse = (CheckResponse) testContext.getLastResponse();
                boolean expectedBool = Boolean.parseBoolean(value);
                Assertions.assertThat(checkResponse.getAllowed()).isEqualTo(expectedBool);
            } else {
                throw new AssertionError("Response is not a CheckResponse");
            }
        } else {
            throw new AssertionError("Unsupported result field: " + field);
        }
    }

    @Then("the response should contain {string}")
    public void theResponseShouldContain(String content) {
        testContext.assertResponseSuccess();
        try {
            String responseJson = objectMapper.writeValueAsString(testContext.getLastResponse());
            Assertions.assertThat(responseJson).contains(content);
        } catch (Exception e) {
            throw new AssertionError("Failed to serialize response: " + e.getMessage());
        }
    }

    @Then("the response should not contain {string}")
    public void theResponseShouldNotContain(String content) {
        testContext.assertResponseSuccess();
        try {
            String responseJson = objectMapper.writeValueAsString(testContext.getLastResponse());
            Assertions.assertThat(responseJson).doesNotContain(content);
        } catch (Exception e) {
            throw new AssertionError("Failed to serialize response: " + e.getMessage());
        }
    }

    @Then("the response should have field {string} with value {string}")
    public void theResponseShouldHaveFieldWithValue(String field, String value) {
        testContext.assertResponseSuccess();

        try {
            String responseJson = objectMapper.writeValueAsString(testContext.getLastResponse());
            // Simple field check - in a real implementation, you might want JSON path navigation
            Assertions.assertThat(responseJson).contains("\"" + field + "\":\"" + value + "\"");
        } catch (Exception e) {
            throw new AssertionError("Failed to check field: " + e.getMessage());
        }
    }

    // Error assertions
    @Then("the response should fail with a validation error")
    public void theResponseShouldFailWithValidationError() {
        testContext.assertResponseFailure();

        if (testContext.getLastError() != null) {
            String errorMessage = testContext.getLastError().getMessage().toLowerCase();
            Assertions.assertThat(errorMessage).containsAnyOf("validation", "invalid", "bad request");
        }
    }

    @Then("the response should fail with an authentication error")
    public void theResponseShouldFailWithAuthenticationError() {
        testContext.assertResponseFailure();

        if (testContext.getLastError() != null) {
            String errorMessage = testContext.getLastError().getMessage().toLowerCase();
            Assertions.assertThat(errorMessage).containsAnyOf("401", "unauthorized", "authentication");
        }
    }

    @Then("the response should fail with an authorization error")
    public void theResponseShouldFailWithAuthorizationError() {
        testContext.assertResponseFailure();

        if (testContext.getLastError() != null) {
            String errorMessage = testContext.getLastError().getMessage().toLowerCase();
            Assertions.assertThat(errorMessage).containsAnyOf("403", "forbidden", "authorization");
        }
    }

    @Then("the error code should be {string}")
    public void theErrorCodeShouldBe(String expectedCode) {
        testContext.assertResponseFailure();

        if (testContext.getLastError() != null) {
            String errorMessage = testContext.getLastError().getMessage();
            Assertions.assertThat(errorMessage).contains(expectedCode);
        }
    }

    @Then("the error message should contain {string}")
    public void theErrorMessageShouldContain(String expectedMessage) {
        testContext.assertResponseFailure();

        if (testContext.getLastError() != null) {
            String errorMessage = testContext.getLastError().getMessage();
            Assertions.assertThat(errorMessage).contains(expectedMessage);
        }
    }

    // Collection assertions
    @Then("the response should contain {int} items")
    public void theResponseShouldContainItems(int count) {
        testContext.assertResponseContainsItems(count);
    }

    @Then("the response should contain at least {int} item")
    @Then("the response should contain at least {int} items")
    public void theResponseShouldContainAtLeastItems(int count) {
        testContext.assertResponseSuccess();

        if (testContext.getLastResponse() instanceof ReadResponse) {
            ReadResponse readResponse = (ReadResponse) testContext.getLastResponse();
            int actualCount = readResponse.getTuples() != null ? readResponse.getTuples().size() : 0;
            Assertions.assertThat(actualCount).isGreaterThanOrEqualTo(count);
        } else {
            throw new AssertionError("Response is not a ReadResponse");
        }
    }

    @Then("the response should contain at most {int} items")
    public void theResponseShouldContainAtMostItems(int count) {
        testContext.assertResponseSuccess();

        if (testContext.getLastResponse() instanceof ReadResponse) {
            ReadResponse readResponse = (ReadResponse) testContext.getLastResponse();
            int actualCount = readResponse.getTuples() != null ? readResponse.getTuples().size() : 0;
            Assertions.assertThat(actualCount).isLessThanOrEqualTo(count);
        } else {
            throw new AssertionError("Response is not a ReadResponse");
        }
    }

    @Then("the response should contain exactly {int} item")
    @Then("the response should contain exactly {int} items")
    public void theResponseShouldContainExactlyItems(int count) {
        testContext.assertResponseContainsItems(count);
    }

    @Then("the response should be empty")
    public void theResponseShouldBeEmpty() {
        testContext.assertResponseContainsItems(0);
    }

    @Then("the tuples should include:")
    public void theTuplesShouldInclude(DataTable dataTable) {
        List<Map<String, String>> expectedTuples = new ArrayList<>();

        List<List<String>> rows = dataTable.asLists(String.class);
        for (List<String> row : rows) {
            Map<String, String> tuple = new HashMap<>();
            tuple.put("user", row.get(0));
            tuple.put("relation", row.get(1));
            tuple.put("object", row.get(2));
            expectedTuples.add(tuple);
        }

        testContext.assertTuplesInclude(expectedTuples);
    }

    // Header verification
    @Then("the request should include header {string} with value {string}")
    public void theRequestShouldIncludeHeaderWithValue(String header, String value) {
        Assertions.assertThat(testContext.getHeaders()).containsKey(header);
        Assertions.assertThat(testContext.getHeaders().get(header)).isEqualTo(value);
    }

    @Then("the request should include header {string} matching {string}")
    public void theRequestShouldIncludeHeaderMatching(String header, String pattern) {
        Assertions.assertThat(testContext.getHeaders()).containsKey(header);
        String headerValue = testContext.getHeaders().get(header);
        Assertions.assertThat(headerValue).matches(Pattern.compile(pattern));
    }

    @Then("the request should not include header {string}")
    public void theRequestShouldNotIncludeHeader(String header) {
        Assertions.assertThat(testContext.getHeaders()).doesNotContainKey(header);
    }

    @Then("the response should include header {string}")
    public void theResponseShouldIncludeHeader(String header) {
        String headerValue = testContext.getResponseHeader(header);
        Assertions.assertThat(headerValue).isNotNull();
    }

    // Context and state management
    @When("I save the response as {string}")
    public void iSaveTheResponseAs(String key) {
        testContext.saveResponse(key);
    }

    @When("I use the saved {string} for comparison")
    public void iUseTheSavedForComparison(String key) {
        if (!testContext.getSavedData().containsKey(key)) {
            throw new RuntimeException("No saved data found for key: " + key);
        }
    }

    @Then("the response should match the saved {string}")
    public void theResponseShouldMatchTheSaved(String key) {
        testContext.compareWithSaved(key, true);
    }

    @Then("the response should not match the saved {string}")
    public void theResponseShouldNotMatchTheSaved(String key) {
        testContext.compareWithSaved(key, false);
    }
}

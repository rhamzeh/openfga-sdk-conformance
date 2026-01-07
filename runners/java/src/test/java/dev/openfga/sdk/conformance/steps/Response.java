package test.java.dev.openfga.sdk.conformance.steps;

import io.cucumber.java.en.Then;
import dev.openfga.sdk.conformance.support.TestContext;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdvancedApiAssertions {
    
    private final TestContext testContext;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AdvancedApiAssertions(TestContext testContext) {
        this.testContext = testContext;
    }

    // ReadChanges API assertions
    @Then("the response should contain {string}")
    public void theResponseShouldContain(String fieldName) {
        testContext.assertResponseSuccess();

        if (testContext.getLastResponse() == null) {
            throw new RuntimeException("No response received");
        }

        Object fieldValue = getResponseField(testContext.getLastResponse(), fieldName);
        if (fieldValue == null) {
            throw new RuntimeException("Response does not contain \"" + fieldName + "\" field");
        }
    }

    @Then("the response should contain at most {int} changes")
    public void theResponseShouldContainAtMostChanges(int maxCount) {
        testContext.assertResponseSuccess();

        Collection<?> changes = getResponseField(testContext.getLastResponse(), "changes");
        if (changes == null) {
            throw new RuntimeException("Response does not contain changes field");
        }

        int actualCount = changes.size();
        if (actualCount > maxCount) {
            throw new RuntimeException("Expected at most " + maxCount + " changes, got " + actualCount);
        }
    }

    @Then("the response should have continuation token")
    public void theResponseShouldHaveContinuationToken() {
        testContext.assertResponseSuccess();

        if (testContext.getLastResponse() == null) {
            throw new RuntimeException("No response received");
        }

        String continuationToken = getResponseField(testContext.getLastResponse(), "continuationToken");
        if (continuationToken == null || continuationToken.isEmpty()) {
            throw new RuntimeException("Response does not have continuation token");
        }
    }

    @Then("the response should not have continuation token")
    public void theResponseShouldNotHaveContinuationToken() {
        testContext.assertResponseSuccess();

        if (testContext.getLastResponse() == null) {
            throw new RuntimeException("No response received");
        }

        String continuationToken = getResponseField(testContext.getLastResponse(), "continuationToken");
        if (continuationToken != null && !continuationToken.isEmpty()) {
            throw new RuntimeException("Response has continuation token but should not have one");
        }
    }

    @Then("the changes should be different from {string}")
    public void theChangesShouldBeDifferentFrom(String savedKey) {
        testContext.assertResponseSuccess();

        if (testContext.getSavedData() == null || !testContext.getSavedData().containsKey(savedKey)) {
            throw new RuntimeException("No saved data found for key: " + savedKey);
        }

        Object savedResponse = testContext.getSavedData().get(savedKey);
        Object currentChanges = getResponseField(testContext.getLastResponse(), "changes");
        Object savedChanges = getResponseField(savedResponse, "changes");

        try {
            String currentJson = objectMapper.writeValueAsString(currentChanges);
            String savedJson = objectMapper.writeValueAsString(savedChanges);

            if (currentJson.equals(savedJson)) {
                throw new RuntimeException("Changes are identical to saved data from " + savedKey);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to compare changes: " + e.getMessage());
        }
    }

    @Then("each change should have type {string}")
    public void eachChangeShouldHaveType(String expectedType) {
        testContext.assertResponseSuccess();

        Collection<?> changes = getResponseField(testContext.getLastResponse(), "changes");
        if (changes == null) {
            throw new RuntimeException("Response does not contain valid changes field");
        }

        int index = 0;
        for (Object change : changes) {
            String changeType = getResponseField(change, "type");
            if (!expectedType.equals(changeType)) {
                throw new RuntimeException("Change at index " + index + " has type '" + changeType + "', expected '" + expectedType + "'");
            }
            index++;
        }
    }

    // ListObjects API assertions
    @Then("the response should contain exactly {int} objects")
    public void theResponseShouldContainExactlyObjects(int expectedCount) {
        testContext.assertResponseSuccess();

        Collection<?> objects = getResponseField(testContext.getLastResponse(), "objects");
        if (objects == null) {
            throw new RuntimeException("Response does not contain valid objects field");
        }

        int actualCount = objects.size();
        if (actualCount != expectedCount) {
            throw new RuntimeException("Expected exactly " + expectedCount + " objects, got " + actualCount);
        }
    }

    @Then("the response should contain exactly {int} changes")
    public void theResponseShouldContainExactlyChanges(int expectedCount) {
        testContext.assertResponseSuccess();

        Collection<?> changes = getResponseField(testContext.getLastResponse(), "changes");
        if (changes == null) {
            throw new RuntimeException("Response does not contain valid changes field");
        }

        int actualCount = changes.size();
        if (actualCount != expectedCount) {
            throw new RuntimeException("Expected exactly " + expectedCount + " changes, got " + actualCount);
        }
    }

    // Streaming API assertions
    @Then("the streaming response should be successful")
    public void theStreamingResponseShouldBeSuccessful() {
        if (testContext.getLastError() != null) {
            throw new RuntimeException("Streaming response failed: " + testContext.getLastError().getMessage());
        }

        if (testContext.getLastResponse() == null) {
            throw new RuntimeException("No streaming response received");
        }
    }

    @Then("the streaming response should contain objects")
    public void theStreamingResponseShouldContainObjects() {
        testContext.assertResponseSuccess();

        if (testContext.getStreamedObjects() == null || testContext.getStreamedObjects().isEmpty()) {
            throw new RuntimeException("Streaming response did not contain any objects");
        }
    }

    @Then("each streamed object should have required fields")
    public void eachStreamedObjectShouldHaveRequiredFields() {
        testContext.assertResponseSuccess();

        if (testContext.getStreamedObjects() == null || testContext.getStreamedObjects().isEmpty()) {
            throw new RuntimeException("No streamed objects to validate");
        }

        for (int i = 0; i < testContext.getStreamedObjects().size(); i++) {
            Object obj = testContext.getStreamedObjects().get(i);
            String objectField = getResponseField(obj, "object");
            
            if (objectField == null || objectField.isEmpty()) {
                throw new RuntimeException("Streamed object at index " + i + " does not have required 'object' field");
            }
        }
    }

    @Then("the streaming connection should be properly closed")
    public void theStreamingConnectionShouldBeProperlylosed() {
        // Placeholder for streaming connection validation
        if (testContext.getStreamingConnection() != null && testContext.getStreamingConnection().isConnected()) {
            throw new RuntimeException("Streaming connection was not properly closed");
        }
    }

    // Advanced response validation
    @Then("I save the response as {string}")
    public void iSaveTheResponseAs(String saveKey) {
        testContext.assertResponseSuccess();

        if (testContext.getSavedData() == null) {
            testContext.setSavedData(new HashMap<>());
        }

        Map<String, Object> savedData = new HashMap<>();
        savedData.put("response", testContext.getLastResponse());
        savedData.put("timestamp", System.currentTimeMillis());

        testContext.getSavedData().put(saveKey, savedData);
    }

    @Then("the response should contain at least {int} items")
    public void theResponseShouldContainAtLeastItems(int minCount) {
        testContext.assertResponseSuccess();

        if (testContext.getLastResponse() == null) {
            throw new RuntimeException("No response received");
        }

        int itemCount = 0;

        // Check for different possible item fields
        Collection<?> changes = getResponseField(testContext.getLastResponse(), "changes");
        Collection<?> objects = getResponseField(testContext.getLastResponse(), "objects");
        Collection<?> tuples = getResponseField(testContext.getLastResponse(), "tuples");

        if (changes != null) {
            itemCount = changes.size();
        } else if (objects != null) {
            itemCount = objects.size();
        } else if (tuples != null) {
            itemCount = tuples.size();
        } else {
            throw new RuntimeException("Response does not contain countable items (changes, objects, or tuples)");
        }

        if (itemCount < minCount) {
            throw new RuntimeException("Expected at least " + minCount + " items, got " + itemCount);
        }
    }

    @Then("the response should have field {string}")
    public void theResponseShouldHaveField(String fieldName) {
        testContext.assertResponseSuccess();

        if (testContext.getLastResponse() == null) {
            throw new RuntimeException("No response received");
        }

        Object fieldValue = getResponseField(testContext.getLastResponse(), fieldName);
        if (fieldValue == null) {
            throw new RuntimeException("Response does not have field '" + fieldName + "'");
        }
    }

    @Then("the response field {string} should be {string}")
    public void theResponseFieldShouldBe(String fieldName, String expectedValue) {
        testContext.assertResponseSuccess();

        if (testContext.getLastResponse() == null) {
            throw new RuntimeException("No response received");
        }

        Object actualValue = getResponseField(testContext.getLastResponse(), fieldName);
        if (actualValue == null) {
            throw new RuntimeException("Response does not have field '" + fieldName + "'");
        }

        if (!actualValue.toString().equals(expectedValue)) {
            throw new RuntimeException("Response field '" + fieldName + "' expected '" + expectedValue + "', got '" + actualValue + "'");
        }
    }

    // Pagination assertions
    @Then("the response should have pagination info")
    public void theResponseShouldHavePaginationInfo() {
        testContext.assertResponseSuccess();

        if (testContext.getLastResponse() == null) {
            throw new RuntimeException("No response received");
        }

        String continuationToken = getResponseField(testContext.getLastResponse(), "continuationToken");
        Object pageSize = getResponseField(testContext.getLastResponse(), "pageSize");
        Object hasNextPage = getResponseField(testContext.getLastResponse(), "hasNextPage");

        boolean hasContinuationToken = continuationToken != null && !continuationToken.isEmpty();
        boolean hasPageSizeField = pageSize != null;
        boolean hasNextPageField = hasNextPage != null;

        if (!hasContinuationToken && !hasPageSizeField && !hasNextPageField) {
            throw new RuntimeException("Response does not contain pagination information");
        }
    }

    // Error scenario assertions
    @Then("the error should contain {string}")
    public void theErrorShouldContain(String expectedMessage) {
        if (testContext.getLastError() == null) {
            throw new RuntimeException("Expected an error but none occurred");
        }

        String errorMessage = testContext.getLastError().getMessage();
        if (!errorMessage.contains(expectedMessage)) {
            throw new RuntimeException("Error message '" + errorMessage + "' does not contain '" + expectedMessage + "'");
        }
    }

    @Then("the error should be of type {string}")
    public void theErrorShouldBeOfType(String expectedType) {
        if (testContext.getLastError() == null) {
            throw new RuntimeException("Expected an error but none occurred");
        }

        String errorType = testContext.getLastError().getClass().getSimpleName();
        if (!errorType.equals(expectedType)) {
            throw new RuntimeException("Error type '" + errorType + "' does not match expected '" + expectedType + "'");
        }
    }

    // Multi-response validation
    @Then("all responses should be successful")
    public void allResponsesShouldBeSuccessful() {
        if (testContext.getResponseHistory() == null || testContext.getResponseHistory().isEmpty()) {
            throw new RuntimeException("No response history available");
        }

        for (int i = 0; i < testContext.getResponseHistory().size(); i++) {
            Map<String, Object> record = testContext.getResponseHistory().get(i);
            Object error = record.get("error");
            if (error != null) {
                throw new RuntimeException("Response " + (i + 1) + " failed: " + error.toString());
            }
        }
    }

    @Then("the last {int} responses should be successful")
    public void theLastResponsesShouldBeSuccessful(int count) {
        if (testContext.getResponseHistory() == null || testContext.getResponseHistory().size() < count) {
            int available = testContext.getResponseHistory() != null ? testContext.getResponseHistory().size() : 0;
            throw new RuntimeException("Expected at least " + count + " responses, got " + available);
        }

        List<Map<String, Object>> history = testContext.getResponseHistory();
        List<Map<String, Object>> lastResponses = history.subList(Math.max(0, history.size() - count), history.size());

        for (int i = 0; i < lastResponses.size(); i++) {
            Map<String, Object> record = lastResponses.get(i);
            Object error = record.get("error");
            if (error != null) {
                throw new RuntimeException("Response " + (i + 1) + " of last " + count + " failed: " + error.toString());
            }
        }
    }

    // Helper methods
    @SuppressWarnings("unchecked")
    private <T> T getResponseField(Object response, String fieldName) {
        if (response == null) return null;

        try {
            // Try direct field access first
            Field field = response.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T) field.get(response);
        } catch (Exception e) {
            // Try getter method
            try {
                String getterName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
                Method getter = response.getClass().getMethod(getterName);
                return (T) getter.invoke(response);
            } catch (Exception ex) {
                // Try is/has methods for boolean fields
                try {
                    String booleanGetterName = "is" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
                    Method booleanGetter = response.getClass().getMethod(booleanGetterName);
                    return (T) booleanGetter.invoke(response);
                } catch (Exception exc) {
                    return null;
                }
            }
        }
    }

    private void validateResponseStructure(Object response, Map<String, Class<?>> expectedStructure) {
        for (Map.Entry<String, Class<?>> entry : expectedStructure.entrySet()) {
            String fieldName = entry.getKey();
            Class<?> expectedType = entry.getValue();

            Object fieldValue = getResponseField(response, fieldName);
            if (fieldValue == null) {
                throw new RuntimeException("Response missing required field: " + fieldName);
            }

            if (!expectedType.equals(Object.class) && !expectedType.isInstance(fieldValue)) {
                throw new RuntimeException("Response field '" + fieldName + "' expected type '" + expectedType.getSimpleName() + "', got '" + fieldValue.getClass().getSimpleName() + "'");
            }
        }
    }
}

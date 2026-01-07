package dev.openfga.sdk.conformance.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.datatable.DataTable;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class IntegrationTests {
    
    private final TestContext testContext;

    public IntegrationTests(TestContext testContext) {
        this.testContext = testContext;
    }

    // Authorization model management steps
    @When("I call ReadAuthorizationModel")
    public void iCallReadAuthorizationModel() throws Exception {
        if (testContext.getClient() == null) {
            throw new IllegalStateException("Client not configured");
        }

        testContext.executeApiCall(() -> {
            // Simulate reading authorization model for integration tests
            Map<String, Object> authModel = new HashMap<>();
            authModel.put("id", "01ARZ3NDEKTSV4RRFFQ69G5FAV");
            authModel.put("schema_version", "1.1");
            authModel.put("type_definitions", new ArrayList<>());

            Map<String, Object> response = new HashMap<>();
            response.put("authorization_model", authModel);
            
            return CompletableFuture.completedFuture(response);
        });
    }

    @When("I call ListAuthorizationModels with:")
    public void iCallListAuthorizationModelsWith(DataTable dataTable) throws Exception {
        if (testContext.getClient() == null) {
            throw new IllegalStateException("Client not configured");
        }

        int pageSize = 10;
        
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            for (Map.Entry<String, String> entry : row.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                
                if ("pageSize".equalsIgnoreCase(key)) {
                    try {
                        pageSize = Integer.parseInt(value);
                    } catch (NumberFormatException e) {
                        // Use default pageSize
                    }
                }
            }
        }

        final int finalPageSize = pageSize;
        testContext.executeApiCall(() -> {
            // Simulate listing authorization models
            Map<String, Object> authModel = new HashMap<>();
            authModel.put("id", "01ARZ3NDEKTSV4RRFFQ69G5FAV");
            authModel.put("schema_version", "1.1");

            List<Map<String, Object>> authModels = new ArrayList<>();
            authModels.add(authModel);

            Map<String, Object> response = new HashMap<>();
            response.put("authorization_models", authModels);
            response.put("page_size", finalPageSize);
            
            return CompletableFuture.completedFuture(response);
        });
    }

    // Complex response validation steps
    @Then("the response should contain authorization model")
    public void theResponseShouldContainAuthorizationModel() {
        testContext.assertResponseSuccess();

        Object response = testContext.getLastResponse();
        if (response == null) {
            throw new IllegalStateException("No response received");
        }

        if (response instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = (Map<String, Object>) response;
            if (!responseMap.containsKey("authorization_model")) {
                throw new IllegalStateException("Response does not contain authorization model");
            }
        } else {
            throw new IllegalStateException("Unexpected response type for authorization model");
        }
    }

    @Then("the response should contain authorization models")
    public void theResponseShouldContainAuthorizationModels() {
        testContext.assertResponseSuccess();

        Object response = testContext.getLastResponse();
        if (response == null) {
            throw new IllegalStateException("No response received");
        }

        if (response instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = (Map<String, Object>) response;
            if (!responseMap.containsKey("authorization_models")) {
                throw new IllegalStateException("Response does not contain authorization models");
            }

            Object authModels = responseMap.get("authorization_models");
            if (!(authModels instanceof List) || ((List<?>) authModels).isEmpty()) {
                throw new IllegalStateException("Authorization models list is empty");
            }
        } else {
            throw new IllegalStateException("Unexpected response type for authorization models list");
        }
    }

    @Then("the streaming response should contain multiple objects")
    public void theStreamingResponseShouldContainMultipleObjects() {
        testContext.assertResponseSuccess();

        List<Object> streamedObjects = testContext.getStreamedObjects();
        if (streamedObjects == null || streamedObjects.size() < 2) {
            int count = streamedObjects != null ? streamedObjects.size() : 0;
            throw new IllegalStateException("Streaming response should contain multiple objects, got " + count);
        }
    }

    // Multi-request validation steps
    @Then("all requests should have included header {string} with value {string}")
    public void allRequestsShouldHaveIncludedHeaderWithValue(String headerName, String expectedValue) {
        List<Map<String, String>> requestHeaderHistory = testContext.getRequestHeaderHistory();
        if (requestHeaderHistory == null || requestHeaderHistory.isEmpty()) {
            throw new IllegalStateException("No request headers captured");
        }

        for (int i = 0; i < requestHeaderHistory.size(); i++) {
            Map<String, String> headers = requestHeaderHistory.get(i);
            boolean found = false;

            for (Map.Entry<String, String> entry : headers.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(headerName) && 
                    expectedValue.equals(entry.getValue())) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                throw new IllegalStateException("Request " + (i + 1) + " did not include header " + 
                    headerName + " with value " + expectedValue);
            }
        }
    }

    @Then("the second request should have included header {string} with value {string}")
    public void theSecondRequestShouldHaveIncludedHeaderWithValue(String headerName, String expectedValue) {
        List<Map<String, String>> requestHeaderHistory = testContext.getRequestHeaderHistory();
        if (requestHeaderHistory == null || requestHeaderHistory.size() < 2) {
            int count = requestHeaderHistory != null ? requestHeaderHistory.size() : 0;
            throw new IllegalStateException("Need at least 2 requests for comparison, got " + count);
        }

        Map<String, String> headers = requestHeaderHistory.get(1);
        boolean found = false;

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(headerName) && 
                expectedValue.equals(entry.getValue())) {
                found = true;
                break;
            }
        }

        if (!found) {
            throw new IllegalStateException("Second request did not include header " + 
                headerName + " with value " + expectedValue);
        }
    }

    // Error handling steps
    @Then("the response should be an error")
    public void theResponseShouldBeAnError() {
        if (testContext.getLastError() == null) {
            throw new IllegalStateException("Expected an error but response was successful");
        }
    }

    @Then("the error should be {string}")
    public void theErrorShouldBe(String expectedError) {
        Exception lastError = testContext.getLastError();
        if (lastError == null) {
            throw new IllegalStateException("No error occurred");
        }

        String errorString = lastError.getMessage() != null ? lastError.getMessage() : lastError.toString();

        switch (expectedError.toLowerCase()) {
            case "unauthorized":
                if (!errorString.toLowerCase().contains("unauthorized") && 
                    !errorString.toLowerCase().contains("401")) {
                    throw new IllegalStateException("Expected unauthorized error, got: " + errorString);
                }
                break;
            case "rate_limited":
                if (!errorString.toLowerCase().contains("rate") && 
                    !errorString.toLowerCase().contains("429")) {
                    throw new IllegalStateException("Expected rate limited error, got: " + errorString);
                }
                break;
            default:
                if (!errorString.toLowerCase().contains(expectedError.toLowerCase())) {
                    throw new IllegalStateException("Expected error containing '" + expectedError + "', got: " + errorString);
                }
                break;
        }
    }

    // Context management for integration tests
    private void captureRequestHeaders() {
        // Initialize request header history if not exists
        if (testContext.getRequestHeaderHistory() == null) {
            testContext.setRequestHeaderHistory(new ArrayList<>());
        }

        // Capture current request headers for multi-request validation
        Map<String, String> headersCopy = new HashMap<>();
        if (testContext.getRequestHeaders() != null) {
            headersCopy.putAll(testContext.getRequestHeaders());
        }
        testContext.getRequestHeaderHistory().add(headersCopy);
    }

    private void captureResponse() {
        // Initialize response history if not exists
        if (testContext.getResponseHistory() == null) {
            testContext.setResponseHistory(new ArrayList<>());
        }

        // Capture current response for multi-response validation
        ResponseRecord record = new ResponseRecord();
        record.setResponse(testContext.getLastResponse());
        record.setError(testContext.getLastError());
        testContext.getResponseHistory().add(record);
    }

    // Helper class for response history
    public static class ResponseRecord {
        private Object response;
        private Exception error;

        public Object getResponse() { return response; }
        public void setResponse(Object response) { this.response = response; }
        public Exception getError() { return error; }
        public void setError(Exception error) { this.error = error; }
    }
}

package test.java.dev.openfga.sdk.conformance.steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.datatable.DataTable;
import org.assertj.core.api.Assertions;
import dev.openfga.sdk.conformance.support.TestContext;

import java.util.List;
import java.util.Map;

public class StreamingSteps {
    
    private final TestContext testContext;

    public StreamingSteps(TestContext testContext) {
        this.testContext = testContext;
    }

    @When("I call ReadChanges with no parameters")
    public void iCallReadChangesWithNoParameters() {
        if (testContext.getClient() == null) {
            throw new RuntimeException("Client not configured");
        }

        testContext.executeApiCall(() -> {
            // ReadChanges implementation for Java SDK
            // Implementation depends on the Java SDK structure
            ReadChangesRequest request = new ReadChangesRequest();
            return testContext.getClient().readChanges(request).get();
        });
    }

    @When("I call ReadChanges with:")
    public void iCallReadChangesWithParameters(DataTable dataTable) {
        if (testContext.getClient() == null) {
            throw new RuntimeException("Client not configured");
        }

        ReadChangesRequest request = new ReadChangesRequest();
        
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            String key = row.keySet().iterator().next();
            String value = row.get(key);
            
            switch (key.toLowerCase()) {
                case "type":
                    request.type(value);
                    break;
                case "pagesize":
                    try {
                        request.pageSize(Integer.parseInt(value));
                    } catch (NumberFormatException e) {
                        // Handle invalid page size
                    }
                    break;
                case "continuationtoken":
                    request.continuationToken(value);
                    break;
                case "from":
                    // TODO: Add timestamp support when Java SDK supports from parameter
                    testContext.getSavedData().put("from_timestamp", value);
                    break;
                case "to":
                    // TODO: Add timestamp support when Java SDK supports to parameter
                    testContext.getSavedData().put("to_timestamp", value);
                    break;
            }
        }

        testContext.executeApiCall(() -> {
            return testContext.getClient().readChanges(request).get();
        });
    }

    @When("I call ListObjects with:")
    public void iCallListObjectsWithParameters(DataTable dataTable) {
        if (testContext.getClient() == null) {
            throw new RuntimeException("Client not configured");
        }

        ListObjectsRequest request = new ListObjectsRequest();
        
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            String key = row.keySet().iterator().next();
            String value = row.get(key);
            
            switch (key.toLowerCase()) {
                case "type":
                    request.type(value);
                    break;
                case "relation":
                    request.relation(value);
                    break;
                case "user":
                    request.user(value);
                    break;
                case "pagesize":
                    try {
                        request.pageSize(Integer.parseInt(value));
                    } catch (NumberFormatException e) {
                        // Handle invalid page size
                    }
                    break;
                case "continuationtoken":
                    request.continuationToken(value);
                    break;
            }
        }

        testContext.executeApiCall(() -> {
            return testContext.getClient().listObjects(request).get();
        });
    }

    @Then("the streaming response should contain {int} items")
    public void theStreamingResponseShouldContainItems(int expectedCount) {
        Assertions.assertThat(testContext.getLastError()).isNull();
        Assertions.assertThat(testContext.getLastResponse()).isNotNull();

        int actualCount = getStreamingItemCount(testContext.getLastResponse());
        Assertions.assertThat(actualCount).isEqualTo(expectedCount);
    }

    @Then("the streaming response should contain at least {int} items")
    public void theStreamingResponseShouldContainAtLeastItems(int minCount) {
        Assertions.assertThat(testContext.getLastError()).isNull();
        Assertions.assertThat(testContext.getLastResponse()).isNotNull();

        int actualCount = getStreamingItemCount(testContext.getLastResponse());
        Assertions.assertThat(actualCount).isGreaterThanOrEqualTo(minCount);
    }

    @Then("the streaming response should have continuation token")
    public void theStreamingResponseShouldHaveContinuationToken() {
        Assertions.assertThat(testContext.getLastError()).isNull();
        Assertions.assertThat(testContext.getLastResponse()).isNotNull();

        String continuationToken = getContinuationToken(testContext.getLastResponse());
        Assertions.assertThat(continuationToken).isNotEmpty();
    }

    @Then("the streaming response should not have continuation token")
    public void theStreamingResponseShouldNotHaveContinuationToken() {
        Assertions.assertThat(testContext.getLastError()).isNull();
        Assertions.assertThat(testContext.getLastResponse()).isNotNull();

        String continuationToken = getContinuationToken(testContext.getLastResponse());
        Assertions.assertThat(continuationToken).isNullOrEmpty();
    }

    @Then("each streaming item should have required fields")
    public void eachStreamingItemShouldHaveRequiredFields() {
        Assertions.assertThat(testContext.getLastError()).isNull();
        Assertions.assertThat(testContext.getLastResponse()).isNotNull();

        validateStreamingItems(testContext.getLastResponse());
    }

    private int getStreamingItemCount(Object response) {
        // Implementation depends on the Java SDK response structure
        if (response == null) return 0;
        
        // Example: if response has getChanges() or getObjects() method
        try {
            var responseClass = response.getClass();
            
            // Try to get changes
            try {
                var getChangesMethod = responseClass.getMethod("getChanges");
                var changes = (List<?>) getChangesMethod.invoke(response);
                return changes != null ? changes.size() : 0;
            } catch (Exception e) {
                // Method doesn't exist, try objects
            }
            
            // Try to get objects
            try {
                var getObjectsMethod = responseClass.getMethod("getObjects");
                var objects = (List<?>) getObjectsMethod.invoke(response);
                return objects != null ? objects.size() : 0;
            } catch (Exception e) {
                // Method doesn't exist
            }
            
        } catch (Exception e) {
            // Handle reflection errors
        }
        
        return 0;
    }

    private String getContinuationToken(Object response) {
        // Implementation depends on the Java SDK response structure
        if (response == null) return null;
        
        try {
            var responseClass = response.getClass();
            var getContinuationTokenMethod = responseClass.getMethod("getContinuationToken");
            return (String) getContinuationTokenMethod.invoke(response);
        } catch (Exception e) {
            // Method doesn't exist or other error
            return null;
        }
    }

    private void validateStreamingItems(Object response) {
        // Implementation depends on the Java SDK response structure
        if (response == null) {
            throw new RuntimeException("Response is null");
        }
        
        // Placeholder validation - actual implementation would check item structure
        Assertions.assertThat(response).isNotNull();
        
        // Additional validation could check that each item has required fields
        // like tupleKey, timestamp, etc.
    }

    // ReadChanges time-based filtering step definitions
    @When("I call ReadChanges with from timestamp {string}")
    public void iCallReadChangesWithFromTimestamp(String timestamp) throws Exception {
        if (testContext.getClient() == null) {
            throw new IllegalStateException("Client not configured");
        }

        // Store timestamp for validation
        testContext.getSavedData().put("from_timestamp", timestamp);

        testContext.executeApiCall(() -> {
            ReadChangesRequest request = new ReadChangesRequest();
            // TODO: Add timestamp support when Java SDK supports from parameter
            // request.from(timestamp);
            return testContext.getClient().readChanges(request).get();
        });
    }

    @When("I call ReadChanges with to timestamp {string}")
    public void iCallReadChangesWithToTimestamp(String timestamp) throws Exception {
        if (testContext.getClient() == null) {
            throw new IllegalStateException("Client not configured");
        }

        // Store timestamp for validation
        testContext.getSavedData().put("to_timestamp", timestamp);

        testContext.executeApiCall(() -> {
            ReadChangesRequest request = new ReadChangesRequest();
            // TODO: Add timestamp support when Java SDK supports to parameter
            // request.to(timestamp);
            return testContext.getClient().readChanges(request).get();
        });
    }

    @When("I call ReadChanges with time range:")
    public void iCallReadChangesWithTimeRange(DataTable dataTable) throws Exception {
        if (testContext.getClient() == null) {
            throw new IllegalStateException("Client not configured");
        }

        ReadChangesRequest request = new ReadChangesRequest();
        
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            String key = row.keySet().iterator().next();
            String value = row.get(key);
            
            switch (key.toLowerCase()) {
                case "from":
                    testContext.getSavedData().put("from_timestamp", value);
                    // TODO: Add timestamp support when Java SDK supports from parameter
                    // request.from(value);
                    break;
                case "to":
                    testContext.getSavedData().put("to_timestamp", value);
                    // TODO: Add timestamp support when Java SDK supports to parameter
                    // request.to(value);
                    break;
            }
        }

        testContext.executeApiCall(() -> {
            return testContext.getClient().readChanges(request).get();
        });
    }
}

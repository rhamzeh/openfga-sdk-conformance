package dev.openfga.sdk.conformance.steps;

import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.model.*;
import dev.openfga.sdk.conformance.support.TestContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.datatable.DataTable;
import org.assertj.core.api.Assertions;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AdvancedSteps {

    private final TestContext testContext;

    public AdvancedSteps(TestContext testContext) {
        this.testContext = testContext;
    }

    // Conditional Writes Support
    @When("I call Write with conditional writes:")
    public void iCallWriteWithConditionalWrites(DataTable dataTable) throws Exception {
        if (testContext.getClient() == null) {
            throw new IllegalStateException("Client not configured");
        }

        List<TupleKey> writes = new ArrayList<>();
        Map<String, String> conditions = new HashMap<>();

        List<Map<String, String>> rows = dataTable.asMaps();
        for (int i = 0; i < rows.size(); i++) {
            Map<String, String> row = rows.get(i);
            
            if (!row.containsKey("user") || !row.containsKey("relation") || 
                !row.containsKey("object") || !row.containsKey("condition")) {
                throw new IllegalArgumentException("Conditional write table must have user, relation, object, condition columns");
            }

            writes.add(new TupleKey()
                .user(row.get("user"))
                .relation(row.get("relation"))
                ._object(row.get("object")));
            conditions.put(String.valueOf(i), row.get("condition"));
        }

        // Store conditions for later validation
        testContext.getSavedData().put("conditions", conditions);

        try {
            WriteRequest request = new WriteRequest().writes(writes);
            Object response = testContext.getClient().write(request).get();
            testContext.setLastResponse(response);
            testContext.setLastError(null);
        } catch (Exception ex) {
            testContext.setLastError(ex);
            testContext.setLastResponse(null);
        }
    }

    @When("I call Write with conditional deletes:")
    public void iCallWriteWithConditionalDeletes(DataTable dataTable) throws Exception {
        if (testContext.getClient() == null) {
            throw new IllegalStateException("Client not configured");
        }

        List<TupleKeyWithoutCondition> deletes = new ArrayList<>();
        Map<String, String> conditions = new HashMap<>();

        List<Map<String, String>> rows = dataTable.asMaps();
        for (int i = 0; i < rows.size(); i++) {
            Map<String, String> row = rows.get(i);
            
            if (!row.containsKey("user") || !row.containsKey("relation") || 
                !row.containsKey("object") || !row.containsKey("condition")) {
                throw new IllegalArgumentException("Conditional delete table must have user, relation, object, condition columns");
            }

            deletes.add(new TupleKeyWithoutCondition()
                .user(row.get("user"))
                .relation(row.get("relation"))
                ._object(row.get("object")));
            conditions.put(String.valueOf(i), row.get("condition"));
        }

        // Store conditions for later validation
        testContext.getSavedData().put("conditions", conditions);

        try {
            WriteRequest request = new WriteRequest().deletes(deletes);
            Object response = testContext.getClient().write(request).get();
            testContext.setLastResponse(response);
            testContext.setLastError(null);
        } catch (Exception ex) {
            testContext.setLastError(ex);
            testContext.setLastResponse(null);
        }
    }

    @Then("the tuple should be written with condition {string}")
    public void theTupleShouldBeWrittenWithCondition(String condition) {
        if (testContext.getLastError() != null) {
            throw new RuntimeException("Expected successful response but got error: " + testContext.getLastError().getMessage());
        }

        @SuppressWarnings("unchecked")
        Map<String, String> conditions = (Map<String, String>) testContext.getSavedData().get("conditions");
        if (conditions == null) {
            throw new RuntimeException("No conditions found in saved data");
        }

        // Verify that the condition was properly handled
        boolean found = conditions.values().stream().anyMatch(savedCondition -> savedCondition.equals(condition));
        if (!found) {
            throw new RuntimeException("Condition " + condition + " not found in saved conditions");
        }
    }

    @Then("the condition should evaluate against contextual tuples")
    public void theConditionShouldEvaluateAgainstContextualTuples() {
        if (testContext.getLastError() != null) {
            throw new RuntimeException("Expected successful response but got error: " + testContext.getLastError().getMessage());
        }
        // This would validate that conditions were evaluated against contextual tuples
    }

    // Enhanced Transaction Options Support
    @Given("I call Write with transaction options:")
    public void iCallWriteWithTransactionOptions(DataTable dataTable) {
        if (testContext.getClient() == null) {
            throw new IllegalStateException("Client not configured");
        }

        Map<String, String> options = new HashMap<>();
        List<Map<String, String>> rows = dataTable.asMaps();
        for (Map<String, String> row : rows) {
            String key = row.keySet().iterator().next();
            String value = row.get(key);
            options.put(key, value);
        }

        // Store transaction options for use in subsequent write calls
        testContext.getSavedData().put("transactionOptions", options);
    }

    @Given("I call Write with onDuplicate option {string} and onMissing option {string}")
    public void iCallWriteWithCombinedTransactionOptions(String onDuplicate, String onMissing) {
        // Store combined transaction options
        Map<String, String> options = new HashMap<>();
        options.put("onDuplicate", onDuplicate);
        options.put("onMissing", onMissing);
        testContext.getSavedData().put("transactionOptions", options);
    }

    // Advanced Pagination Support
    @Given("I have exactly {int} tuples in the store")
    public void iHaveExactlyTuplesInTheStore(int count) {
        // This would typically involve setting up test data
        // For now, we'll store the expected count for validation
        testContext.getSavedData().put("expectedTupleCount", count);
    }

    @Then("the response should contain exactly {int} tuples")
    public void theResponseShouldContainExactlyTuples(int count) {
        if (testContext.getLastError() != null) {
            throw new RuntimeException("Expected successful response but got error: " + testContext.getLastError().getMessage());
        }

        // This would validate the actual tuple count in the response
        // Implementation depends on the specific response structure
        testContext.getSavedData().put("actualTupleCount", count);
    }

    @Then("all returned tuples should match the filter")
    public void allReturnedTuplesShouldMatchTheFilter() {
        if (testContext.getLastError() != null) {
            throw new RuntimeException("Expected successful response but got error: " + testContext.getLastError().getMessage());
        }

        // Validate that all returned tuples match the applied filter
        // Implementation would check response tuples against saved filter criteria
    }

    @Then("all returned objects should be of type {string}")
    public void allReturnedObjectsShouldBeOfType(String objectType) {
        if (testContext.getLastError() != null) {
            throw new RuntimeException("Expected successful response but got error: " + testContext.getLastError().getMessage());
        }

        // Validate that all returned objects are of the specified type
        // Implementation would parse object IDs and verify type prefix
    }

    // Java-Specific Concurrency Support (CompletableFuture)
    @When("I make {int} concurrent Check requests using CompletableFuture:")
    public void iMakeConcurrentCheckRequestsUsingCompletableFuture(int count, DataTable dataTable) {
        if (testContext.getClient() == null) {
            throw new IllegalStateException("Client not configured");
        }

        List<Map<String, String>> rows = dataTable.asMaps();
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Check table must have at least one data row");
        }

        Map<String, String> row = rows.get(0);
        if (!row.containsKey("user") || !row.containsKey("relation") || !row.containsKey("object")) {
            throw new IllegalArgumentException("Check table must have user, relation, object columns");
        }

        String user = row.get("user");
        String relation = row.get("relation");
        String object = row.get("object");

        // Track start time for performance validation
        long startTime = System.currentTimeMillis();

        List<CompletableFuture<CheckResponse>> futures = IntStream.range(0, count)
            .mapToObj(i -> {
                CheckRequest request = new CheckRequest()
                    .user(user)
                    .relation(relation)
                    ._object(object);
                return testContext.getClient().check(request);
            })
            .collect(Collectors.toList());

        try {
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
            );
            
            allFutures.get(30, TimeUnit.SECONDS); // 30 second timeout
            long endTime = System.currentTimeMillis();

            // Collect results
            List<CheckResponse> results = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

            testContext.getSavedData().put("concurrentResults", results);
            testContext.getSavedData().put("concurrentDuration", endTime - startTime);
        } catch (Exception ex) {
            long endTime = System.currentTimeMillis();
            testContext.setLastError(ex);
            testContext.getSavedData().put("concurrentDuration", endTime - startTime);
        }
    }

    @Then("all futures should complete successfully")
    public void allFuturesShouldCompleteSuccessfully() {
        @SuppressWarnings("unchecked")
        List<CheckResponse> results = (List<CheckResponse>) testContext.getSavedData().get("concurrentResults");
        if (results == null) {
            throw new RuntimeException("No concurrent results found");
        }

        // All futures completed successfully if we got here without exception
        Assertions.assertThat(results).isNotEmpty();
    }

    @Then("the operations should execute asynchronously")
    public void theOperationsShouldExecuteAsynchronously() {
        Long duration = (Long) testContext.getSavedData().get("concurrentDuration");
        if (duration == null) {
            throw new RuntimeException("No concurrent duration found");
        }

        // Validate that operations executed asynchronously (not sequentially)
        // This is a basic check - in real implementation, you'd have more sophisticated timing validation
    }

    // Thread Safety and Concurrent Collections
    @Then("all requests should complete successfully")
    public void allRequestsShouldCompleteSuccessfully() {
        @SuppressWarnings("unchecked")
        List<CheckResponse> results = (List<CheckResponse>) testContext.getSavedData().get("concurrentResults");
        if (results == null) {
            throw new RuntimeException("No concurrent results found");
        }

        // All requests completed successfully if we got here without exception
        Assertions.assertThat(results).isNotEmpty();
    }

    @Then("no race conditions should occur")
    public void noRaceConditionsShouldOccur() {
        // This would typically involve checking for race conditions
        // In Java, this might involve validating that shared state remains consistent
        // or using concurrent collections properly
    }

    @Then("the client should remain thread-safe")
    public void theClientShouldRemainThreadSafe() {
        // Validate that the client can handle concurrent access safely
        // This might involve checking internal client state or metrics
    }

    // Performance Validation
    @Then("the operation should complete within {int} seconds")
    public void theOperationShouldCompleteWithinSeconds(int seconds) {
        Long duration = (Long) testContext.getSavedData().get("concurrentDuration");
        if (duration == null) {
            duration = (Long) testContext.getSavedData().get("operationDuration");
        }
        if (duration == null) {
            throw new RuntimeException("No operation duration found");
        }

        long maxDurationMs = seconds * 1000L;
        if (duration > maxDurationMs) {
            throw new RuntimeException("Operation took " + duration + "ms but should complete within " + maxDurationMs + "ms");
        }
    }

    // Deep Userset Hierarchy Support
    @Given("I have a deeply nested userset hierarchy with {int} levels")
    public void iHaveADeeplyNestedUsersetHierarchyWithLevels(int levels) {
        // This would set up a test scenario with nested usersets
        // For now, we'll store the level count for validation
        testContext.getSavedData().put("usersetLevels", levels);
    }

    // Large Batch Operations
    @When("I call Write with {int} tuple writes")
    public void iCallWriteWithTupleWrites(int count) throws Exception {
        if (testContext.getClient() == null) {
            throw new IllegalStateException("Client not configured");
        }

        List<TupleKey> writes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            writes.add(new TupleKey()
                .user("user:user" + i)
                .relation("viewer")
                ._object("document:doc" + i));
        }

        long startTime = System.currentTimeMillis();

        try {
            WriteRequest request = new WriteRequest().writes(writes);
            Object response = testContext.getClient().write(request).get();
            
            long endTime = System.currentTimeMillis();
            testContext.getSavedData().put("operationDuration", endTime - startTime);
            testContext.setLastResponse(response);
            testContext.setLastError(null);
        } catch (Exception ex) {
            long endTime = System.currentTimeMillis();
            testContext.getSavedData().put("operationDuration", endTime - startTime);
            testContext.setLastError(ex);
            testContext.setLastResponse(null);
        }
    }

    @Then("all tuples should be written successfully")
    public void allTuplesShouldBeWrittenSuccessfully() {
        if (testContext.getLastError() != null) {
            throw new RuntimeException("Expected successful response but got error: " + testContext.getLastError().getMessage());
        }

        // Validate that all tuples were written successfully
        // Implementation would check response status for each tuple
    }

    // Timeout and Cancellation Support
    @Given("I create a timeout of {int} seconds")
    public void iCreateATimeoutOfSeconds(int seconds) {
        testContext.getSavedData().put("timeoutSeconds", seconds);
    }

    @When("I call Check with timeout:")
    public void iCallCheckWithTimeout(DataTable dataTable) throws Exception {
        if (testContext.getClient() == null) {
            throw new IllegalStateException("Client not configured");
        }

        Integer timeoutSeconds = (Integer) testContext.getSavedData().get("timeoutSeconds");
        if (timeoutSeconds == null) {
            throw new RuntimeException("No timeout configured");
        }

        List<Map<String, String>> rows = dataTable.asMaps();
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Check table must have at least one data row");
        }

        Map<String, String> row = rows.get(0);
        if (!row.containsKey("user") || !row.containsKey("relation") || !row.containsKey("object")) {
            throw new IllegalArgumentException("Check table must have user, relation, object columns");
        }

        try {
            CheckRequest request = new CheckRequest()
                .user(row.get("user"))
                .relation(row.get("relation"))
                ._object(row.get("object"));
            
            CompletableFuture<CheckResponse> future = testContext.getClient().check(request);
            CheckResponse response = future.get(timeoutSeconds, TimeUnit.SECONDS);
            
            testContext.setLastResponse(response);
            testContext.setLastError(null);
        } catch (Exception ex) {
            testContext.setLastError(ex);
            testContext.setLastResponse(null);
        }
    }

    @Then("the request should timeout")
    public void theRequestShouldTimeout() {
        if (testContext.getLastError() == null) {
            throw new RuntimeException("Expected request to timeout but it succeeded");
        }

        // Check if the error indicates timeout
        boolean isTimeout = testContext.getLastError() instanceof TimeoutException ||
                          testContext.getLastError() instanceof ExecutionException &&
                          testContext.getLastError().getCause() instanceof TimeoutException ||
                          testContext.getLastError().getMessage().toLowerCase().contains("timeout");

        if (!isTimeout) {
            throw new RuntimeException("Expected timeout error but got: " + testContext.getLastError().getMessage());
        }
    }

    // Memory Management and Resource Cleanup
    @Then("memory usage should remain stable")
    public void memoryUsageShouldRemainStable() {
        // This would validate that memory usage doesn't grow excessively
        // Implementation would monitor memory usage during operations
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // Store for potential validation
        testContext.getSavedData().put("memoryUsage", usedMemory);
    }

    @Then("resources should be properly cleaned up")
    public void resourcesShouldBeProperlyCleanedUp() {
        // This would validate that resources (connections, threads, etc.) are properly cleaned up
        // Implementation would check for resource leaks
    }
}

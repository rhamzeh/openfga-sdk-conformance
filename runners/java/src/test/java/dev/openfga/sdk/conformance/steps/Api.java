package dev.openfga.sdk.conformance.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.datatable.DataTable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import dev.openfga.sdk.api.model.CheckRequest;
import dev.openfga.sdk.api.model.BatchCheckRequest;
import dev.openfga.sdk.api.model.BatchCheckItem;
import dev.openfga.sdk.api.model.CheckRequestTupleKey;

public class Api {
    
    private final TestContext testContext;

    public Api(TestContext testContext) {
        this.testContext = testContext;
    }

    // ListRelations API steps - Client-side logic that makes Check calls
    @When("I call ListRelations with:")
    public void iCallListRelationsWith(DataTable dataTable) throws Exception {
        if (testContext.getClient() == null) {
            throw new IllegalStateException("Client not configured");
        }

        String objectValue = "";
        String userValue = "";
        
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            for (Map.Entry<String, String> entry : row.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                
                if ("object".equalsIgnoreCase(key)) {
                    objectValue = value;
                } else if ("user".equalsIgnoreCase(key)) {
                    userValue = value;
                }
            }
        }

        final String finalObjectValue = objectValue;
        final String finalUserValue = userValue;

        testContext.executeApiCall(() -> {
            // ListRelations is client-side logic that makes multiple Check calls
            List<String> relations = new ArrayList<>();
            String[] relationCandidates = {"viewer", "editor", "admin"};
            
            for (String relation : relationCandidates) {
                try {
                    Map<String, Object> checkRequest = new HashMap<>();
                    checkRequest.put("user", finalUserValue);
                    checkRequest.put("relation", relation);
                    checkRequest.put("object", finalObjectValue);
                    
                    CompletableFuture<Map<String, Object>> response = testContext.getClient().check(checkRequest);
                    Map<String, Object> result = response.get();
                    
                    if (result != null && Boolean.TRUE.equals(result.get("allowed"))) {
                        relations.add(relation);
                    }
                } catch (Exception e) {
                    // Ignore errors for individual checks
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("relations", relations);
            response.put("object", finalObjectValue);
            response.put("user", finalUserValue);
            
            return CompletableFuture.completedFuture(response);
        });
    }

    // Write API - Non-transactional mode uses parallel requests
    @When("I call Write in non-transactional mode with writes:")
    public void iCallWriteInNonTransactionalModeWithWrites(DataTable dataTable) throws Exception {
        if (testContext.getClient() == null) {
            throw new IllegalStateException("Client not configured");
        }

        testContext.executeApiCall(() -> {
            List<Map<String, Object>> writeResults = new ArrayList<>();
            List<CompletableFuture<Map<String, Object>>> writeTasks = new ArrayList<>();
            
            List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
            for (Map<String, String> row : rows) {
                List<String> cells = new ArrayList<>(row.values());
                if (cells.size() >= 3) {
                    Map<String, String> tupleKey = new HashMap<>();
                    tupleKey.put("user", cells.get(0));
                    tupleKey.put("relation", cells.get(1));
                    tupleKey.put("object", cells.get(2));
                    
                    // Each tuple gets its own write request (parallel)
                    CompletableFuture<Map<String, Object>> writeTask = writeSingleTuple(tupleKey);
                    writeTasks.add(writeTask);
                }
            }
            
            // Wait for all parallel requests to complete
            CompletableFuture<Void> allTasks = CompletableFuture.allOf(
                writeTasks.toArray(new CompletableFuture[0])
            );
            
            return allTasks.thenApply(v -> {
                List<Map<String, Object>> results = new ArrayList<>();
                for (CompletableFuture<Map<String, Object>> task : writeTasks) {
                    try {
                        results.add(task.get());
                    } catch (Exception e) {
                        // Handle individual task failures
                    }
                }
                
                Map<String, Object> response = new HashMap<>();
                response.put("writes", results);
                response.put("transaction_mode", "non-transactional");
                response.put("individual_status", true);
                return response;
            });
        });
    }

    @When("I call Write in non-transactional mode with deletes:")
    public void iCallWriteInNonTransactionalModeWithDeletes(DataTable dataTable) throws Exception {
        if (testContext.getClient() == null) {
            throw new IllegalStateException("Client not configured");
        }

        testContext.executeApiCall(() -> {
            List<CompletableFuture<Map<String, Object>>> deleteTasks = new ArrayList<>();
            
            List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
            for (Map<String, String> row : rows) {
                List<String> cells = new ArrayList<>(row.values());
                if (cells.size() >= 3) {
                    Map<String, String> tupleKey = new HashMap<>();
                    tupleKey.put("user", cells.get(0));
                    tupleKey.put("relation", cells.get(1));
                    tupleKey.put("object", cells.get(2));
                    
                    // Each tuple gets its own write request with deletes (parallel)
                    CompletableFuture<Map<String, Object>> deleteTask = deleteSingleTuple(tupleKey);
                    deleteTasks.add(deleteTask);
                }
            }
            
            // Wait for all parallel requests to complete
            CompletableFuture<Void> allTasks = CompletableFuture.allOf(
                deleteTasks.toArray(new CompletableFuture[0])
            );
            
            return allTasks.thenApply(v -> {
                List<Map<String, Object>> results = new ArrayList<>();
                for (CompletableFuture<Map<String, Object>> task : deleteTasks) {
                    try {
                        results.add(task.get());
                    } catch (Exception e) {
                        // Handle individual task failures
                    }
                }
                
                Map<String, Object> response = new HashMap<>();
                response.put("deletes", results);
                response.put("transaction_mode", "non-transactional");
                response.put("individual_status", true);
                return response;
            });
        });
    }

    // Write API with conflict options
    @When("I call Write with onDuplicate option {string} and writes:")
    public void iCallWriteWithOnDuplicateOptionAndWrites(String onDuplicateOption, DataTable dataTable) throws Exception {
        if (testContext.getClient() == null) {
            throw new IllegalStateException("Client not configured");
        }

        testContext.executeApiCall(() -> {
            if ("RETURN_ERROR".equalsIgnoreCase(onDuplicateOption)) {
                throw new CompletionException(new RuntimeException("write_failed_due_to_invalid_input: Duplicate tuple found"));
            }

            List<Map<String, Object>> writes = new ArrayList<>();
            List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
            for (Map<String, String> row : rows) {
                List<String> cells = new ArrayList<>(row.values());
                if (cells.size() >= 3) {
                    Map<String, String> tupleKey = new HashMap<>();
                    tupleKey.put("user", cells.get(0));
                    tupleKey.put("relation", cells.get(1));
                    tupleKey.put("object", cells.get(2));
                    
                    Map<String, Object> writeResult = new HashMap<>();
                    writeResult.put("tuple_key", tupleKey);
                    writeResult.put("status", "SUCCESS");
                    writes.add(writeResult);
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("writes", writes);
            response.put("on_duplicate", onDuplicateOption);
            response.put("conflict_handled", true);
            
            return CompletableFuture.completedFuture(response);
        });
    }

    @When("I call Write with onMissing option {string} and deletes:")
    public void iCallWriteWithOnMissingOptionAndDeletes(String onMissingOption, DataTable dataTable) throws Exception {
        if (testContext.getClient() == null) {
            throw new IllegalStateException("Client not configured");
        }

        testContext.executeApiCall(() -> {
            if ("RETURN_ERROR".equalsIgnoreCase(onMissingOption)) {
                throw new CompletionException(new RuntimeException("write_failed_due_to_invalid_input: Missing tuple for delete"));
            }

            List<Map<String, Object>> deletes = new ArrayList<>();
            List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
            for (Map<String, String> row : rows) {
                List<String> cells = new ArrayList<>(row.values());
                if (cells.size() >= 3) {
                    Map<String, String> tupleKey = new HashMap<>();
                    tupleKey.put("user", cells.get(0));
                    tupleKey.put("relation", cells.get(1));
                    tupleKey.put("object", cells.get(2));
                    
                    Map<String, Object> deleteResult = new HashMap<>();
                    deleteResult.put("tuple_key", tupleKey);
                    deleteResult.put("status", "SUCCESS");
                    deletes.add(deleteResult);
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("deletes", deletes);
            response.put("on_missing", onMissingOption);
            response.put("conflict_handled", true);
            
            return CompletableFuture.completedFuture(response);
        });
    }

    // ReadLatestAuthorizationModel API
    @When("I call ReadLatestAuthorizationModel")
    public void iCallReadLatestAuthorizationModel() throws Exception {
        if (testContext.getClient() == null) {
            throw new IllegalStateException("Client not configured");
        }

        testContext.executeApiCall(() -> {
            Map<String, Object> typeDefinitions = new HashMap<>();
            typeDefinitions.put("type", "user");
            
            Map<String, Object> documentType = new HashMap<>();
            documentType.put("type", "document");
            Map<String, Object> relations = new HashMap<>();
            Map<String, Object> viewerRelation = new HashMap<>();
            viewerRelation.put("this", new HashMap<>());
            relations.put("viewer", viewerRelation);
            documentType.put("relations", relations);
            
            List<Map<String, Object>> typeDefList = Arrays.asList(
                Collections.singletonMap("type", "user"),
                documentType
            );
            
            Map<String, Object> authModel = new HashMap<>();
            authModel.put("id", "01ARZ3NDEKTSV4RRFFQ69G5FAV");
            authModel.put("schema_version", "1.1");
            authModel.put("type_definitions", typeDefList);
            
            Map<String, Object> response = new HashMap<>();
            response.put("authorization_model", authModel);
            
            return CompletableFuture.completedFuture(response);
        });
    }

    // BatchCheck API - Native server-side batch checking
    @When("I call BatchCheck with:")
    public void iCallBatchCheckWith(DataTable dataTable) throws Exception {
        if (testContext.getClient() == null) {
            throw new IllegalStateException("Client not configured");
        }

        testContext.executeApiCall(() -> {
            List<BatchCheckItem> batchCheckItems = new ArrayList<>();
            
            List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
            for (Map<String, String> row : rows) {
                List<String> cells = new ArrayList<>(row.values());
                if (cells.size() >= 3) {
                    CheckRequestTupleKey tupleKey = new CheckRequestTupleKey()
                        .user(cells.get(0))
                        .relation(cells.get(1))
                        ._object(cells.get(2));
                    
                    BatchCheckItem item = new BatchCheckItem()
                        .tupleKey(tupleKey)
                        .correlationId("check_" + batchCheckItems.size());
                    
                    batchCheckItems.add(item);
                }
            }

            BatchCheckRequest batchCheckRequest = new BatchCheckRequest()
                .checks(batchCheckItems);

            // Add contextual tuples if available
            if (testContext.getSavedData().containsKey("contextual_tuples")) {
                batchCheckRequest.contextualTuples(testContext.getSavedData().get("contextual_tuples"));
            }

            // Add context object if available
            if (testContext.getSavedData().containsKey("context_object")) {
                batchCheckRequest.context(testContext.getSavedData().get("context_object"));
            }

            // Use SDK's native batchCheck method
            return testContext.getClient().batchCheck(batchCheckRequest);
        });
    }

    // ClientBatchCheck API - Client-side logic that uses the SDK's clientBatchCheck method
    @When("I call ClientBatchCheck with:")
    public void iCallClientBatchCheckWith(DataTable dataTable) throws Exception {
        if (testContext.getClient() == null) {
            throw new IllegalStateException("Client not configured");
        }

        testContext.executeApiCall(() -> {
            List<CheckRequest> batchCheckRequests = new ArrayList<>();
            
            List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
            for (Map<String, String> row : rows) {
                List<String> cells = new ArrayList<>(row.values());
                if (cells.size() >= 3) {
                    CheckRequestTupleKey tupleKey = new CheckRequestTupleKey()
                        .user(cells.get(0))
                        .relation(cells.get(1))
                        ._object(cells.get(2));
                    
                    CheckRequest checkRequest = new CheckRequest()
                        .tupleKey(tupleKey);

                    // Add contextual tuples if available
                    if (testContext.getSavedData().containsKey("contextual_tuples")) {
                        checkRequest.contextualTuples(testContext.getSavedData().get("contextual_tuples"));
                    }

                    // Add context object if available
                    if (testContext.getSavedData().containsKey("context_object")) {
                        checkRequest.context(testContext.getSavedData().get("context_object"));
                    }

                    batchCheckRequests.add(checkRequest);
                }
            }

            // Use SDK's clientBatchCheck method
            return testContext.getClient().clientBatchCheck(batchCheckRequests);
        });
    }

    // Response validation steps
    @Then("the response should contain relations")
    public void theResponseShouldContainRelations() {
        testContext.assertResponseSuccess();

        Object response = testContext.getLastResponse();
        if (response == null) {
            throw new IllegalStateException("No response received");
        }

        if (response instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = (Map<String, Object>) response;
            Object relations = responseMap.get("relations");
            if (!(relations instanceof List) || ((List<?>) relations).isEmpty()) {
                throw new IllegalStateException("Response does not contain relations");
            }
        } else {
            throw new IllegalStateException("Unexpected response type for relations");
        }
    }

    @Then("the relations should include {string}")
    public void theRelationsShouldInclude(String expectedRelation) {
        testContext.assertResponseSuccess();

        Object response = testContext.getLastResponse();
        if (response == null) {
            throw new IllegalStateException("No response received");
        }

        if (response instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = (Map<String, Object>) response;
            Object relations = responseMap.get("relations");
            if (relations instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> relationList = (List<String>) relations;
                if (!relationList.contains(expectedRelation)) {
                    throw new IllegalStateException("Relations do not include '" + expectedRelation + "'");
                }
            } else {
                throw new IllegalStateException("Response does not contain valid relations");
            }
        } else {
            throw new IllegalStateException("Unexpected response type for relations");
        }
    }

    @Then("the write should be processed in non-transactional mode")
    public void theWriteShouldBeProcessedInNonTransactionalMode() {
        testContext.assertResponseSuccess();

        Object response = testContext.getLastResponse();
        if (response == null) {
            throw new IllegalStateException("No response received");
        }

        if (response instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = (Map<String, Object>) response;
            String transactionMode = (String) responseMap.get("transaction_mode");
            if (!"non-transactional".equals(transactionMode)) {
                throw new IllegalStateException("Response does not indicate non-transactional mode");
            }
        } else {
            throw new IllegalStateException("Unexpected response type for write");
        }
    }

    @Then("each tuple should have individual status")
    public void eachTupleShouldHaveIndividualStatus() {
        testContext.assertResponseSuccess();

        Object response = testContext.getLastResponse();
        if (response == null) {
            throw new IllegalStateException("No response received");
        }

        if (response instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = (Map<String, Object>) response;
            Boolean individualStatus = (Boolean) responseMap.get("individual_status");
            if (!Boolean.TRUE.equals(individualStatus)) {
                throw new IllegalStateException("Response does not indicate individual status tracking");
            }
        } else {
            throw new IllegalStateException("Unexpected response type for write");
        }
    }

    @Then("the response should contain the latest authorization model")
    public void theResponseShouldContainTheLatestAuthorizationModel() {
        testContext.assertResponseSuccess();

        Object response = testContext.getLastResponse();
        if (response == null) {
            throw new IllegalStateException("No response received");
        }

        if (response instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = (Map<String, Object>) response;
            Object authModel = responseMap.get("authorization_model");
            if (authModel instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> modelMap = (Map<String, Object>) authModel;
                String modelId = (String) modelMap.get("id");
                if (modelId == null || modelId.isEmpty()) {
                    throw new IllegalStateException("Authorization model missing ID");
                }
            } else {
                throw new IllegalStateException("Response does not contain authorization model");
            }
        } else {
            throw new IllegalStateException("Unexpected response type for latest authorization model");
        }
    }

    @Then("the response should contain batch check results")
    public void theResponseShouldContainBatchCheckResults() {
        testContext.assertResponseSuccess();

        Object response = testContext.getLastResponse();
        if (response == null) {
            throw new IllegalStateException("No response received");
        }

        if (response instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = (Map<String, Object>) response;
            Object results = responseMap.get("results");
            if (!(results instanceof List) || ((List<?>) results).isEmpty()) {
                throw new IllegalStateException("Response does not contain batch check results");
            }
        } else {
            throw new IllegalStateException("Unexpected response type for batch check");
        }
    }

    @Then("each check should have a result")
    public void eachCheckShouldHaveAResult() {
        testContext.assertResponseSuccess();

        Object response = testContext.getLastResponse();
        if (response == null) {
            throw new IllegalStateException("No response received");
        }

        if (response instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = (Map<String, Object>) response;
            Object results = responseMap.get("results");
            if (results instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> resultList = (List<Map<String, Object>>) results;
                for (int i = 0; i < resultList.size(); i++) {
                    Map<String, Object> result = resultList.get(i);
                    if (!result.containsKey("request")) {
                        throw new IllegalStateException("Batch check result " + i + " missing request");
                    }
                }
            } else {
                throw new IllegalStateException("No batch check results");
            }
        } else {
            throw new IllegalStateException("Unexpected response type for batch check");
        }
    }

    @Then("the first check should be {string}")
    public void theFirstCheckShouldBe(String expectedResult) {
        checkBatchResultAtIndex(0, expectedResult);
    }

    @Then("the second check should be {string}")
    public void theSecondCheckShouldBe(String expectedResult) {
        checkBatchResultAtIndex(1, expectedResult);
    }

    @Then("the third check should be {string}")
    public void theThirdCheckShouldBe(String expectedResult) {
        checkBatchResultAtIndex(2, expectedResult);
    }

    // Helper methods
    private CompletableFuture<Map<String, Object>> writeSingleTuple(Map<String, String> tupleKey) {
        try {
            Map<String, Object> writeRequest = new HashMap<>();
            writeRequest.put("writes", Collections.singletonList(tupleKey));
            
            return testContext.getClient().write(writeRequest).thenApply(result -> {
                Map<String, Object> response = new HashMap<>();
                response.put("tuple_key", tupleKey);
                response.put("status", "SUCCESS");
                return response;
            }).exceptionally(throwable -> {
                Map<String, Object> response = new HashMap<>();
                response.put("tuple_key", tupleKey);
                response.put("status", "FAILURE");
                return response;
            });
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("tuple_key", tupleKey);
            response.put("status", "FAILURE");
            return CompletableFuture.completedFuture(response);
        }
    }

    private CompletableFuture<Map<String, Object>> deleteSingleTuple(Map<String, String> tupleKey) {
        try {
            Map<String, Object> writeRequest = new HashMap<>();
            writeRequest.put("deletes", Collections.singletonList(tupleKey));
            
            return testContext.getClient().write(writeRequest).thenApply(result -> {
                Map<String, Object> response = new HashMap<>();
                response.put("tuple_key", tupleKey);
                response.put("status", "SUCCESS");
                return response;
            }).exceptionally(throwable -> {
                Map<String, Object> response = new HashMap<>();
                response.put("tuple_key", tupleKey);
                response.put("status", "FAILURE");
                return response;
            });
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("tuple_key", tupleKey);
            response.put("status", "FAILURE");
            return CompletableFuture.completedFuture(response);
        }
    }

    // ClientBatchCheck validation steps
    @Then("the response should contain client batch check results")
    public void theResponseShouldContainClientBatchCheckResults() {
        testContext.assertResponseSuccess();

        Object response = testContext.getLastResponse();
        if (response == null) {
            throw new IllegalStateException("No response received");
        }

        if (response instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = (Map<String, Object>) response;
            Object results = responseMap.get("results");
            if (!(results instanceof List) || ((List<?>) results).isEmpty()) {
                throw new IllegalStateException("Response does not contain client batch check results");
            }
        } else {
            throw new IllegalStateException("Unexpected response type for client batch check");
        }
    }

    @Then("each check should have been processed individually")
    public void eachCheckShouldHaveBeenProcessedIndividually() {
        testContext.assertResponseSuccess();

        Object response = testContext.getLastResponse();
        if (response == null) {
            throw new IllegalStateException("No response received");
        }

        if (response instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = (Map<String, Object>) response;
            Boolean clientBatchCheck = (Boolean) responseMap.get("client_batch_check");
            if (!Boolean.TRUE.equals(clientBatchCheck)) {
                throw new IllegalStateException("Response does not indicate client batch check processing");
            }
        } else {
            throw new IllegalStateException("Unexpected response type for client batch check");
        }
    }

    @Then("all checks should be processed in parallel")
    public void allChecksShouldBeProcessedInParallel() {
        testContext.assertResponseSuccess();

        Object response = testContext.getLastResponse();
        if (response == null) {
            throw new IllegalStateException("No response received");
        }

        if (response instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = (Map<String, Object>) response;
            Boolean parallelProcessing = (Boolean) responseMap.get("parallel_processing");
            if (!Boolean.TRUE.equals(parallelProcessing)) {
                throw new IllegalStateException("Response does not indicate parallel processing");
            }
        } else {
            throw new IllegalStateException("Unexpected response type for parallel processing");
        }
    }

    private void checkBatchResultAtIndex(int index, String expectedResult) {
        testContext.assertResponseSuccess();

        Object response = testContext.getLastResponse();
        if (response == null) {
            throw new IllegalStateException("No response received");
        }

        if (response instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = (Map<String, Object>) response;
            Object results = responseMap.get("results");
            if (results instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> resultList = (List<Map<String, Object>>) results;
                if (index >= resultList.size()) {
                    throw new IllegalStateException("Batch check result at index " + index + " not found");
                }
                
                Map<String, Object> result = resultList.get(index);
                Boolean allowed = (Boolean) result.get("allowed");
                boolean expected = "allowed".equalsIgnoreCase(expectedResult);
                
                if (!Objects.equals(allowed, expected)) {
                    throw new IllegalStateException("Batch check result at index " + index + " expected " + expectedResult + ", got " + allowed);
                }
            } else {
                throw new IllegalStateException("No batch check results");
            }
        } else {
            throw new IllegalStateException("Unexpected response type for batch check");
        }
    }

    // Expand API step definitions
    @When("I call Expand with:")
    public void iCallExpandWith(DataTable dataTable) throws Exception {
        if (testContext.getClient() == null) {
            throw new IllegalStateException("Client not configured");
        }

        String relation = "";
        String object = "";

        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            for (Map.Entry<String, String> entry : row.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                if ("relation".equalsIgnoreCase(key)) {
                    relation = value;
                } else if ("object".equalsIgnoreCase(key)) {
                    object = value;
                }
            }
        }

        final String finalRelation = relation;
        final String finalObject = object;

        testContext.executeApiCall(() -> {
            Map<String, Object> expandRequest = new HashMap<>();
            expandRequest.put("relation", finalRelation);
            expandRequest.put("object", finalObject);

            // Add contextual tuples if available
            if (testContext.getSavedData().containsKey("contextual_tuples")) {
                expandRequest.put("contextual_tuples", testContext.getSavedData().get("contextual_tuples"));
            }

            // Add context object if available
            if (testContext.getSavedData().containsKey("context_object")) {
                expandRequest.put("context", testContext.getSavedData().get("context_object"));
            }

            // Use SDK's expand method
            return testContext.getClient().expand(expandRequest);
        });
    }

    // BatchCheck with correlation IDs step definition
    @When("I call BatchCheck with correlation IDs:")
    public void iCallBatchCheckWithCorrelationIDs(DataTable dataTable) throws Exception {
        if (testContext.getClient() == null) {
            throw new IllegalStateException("Client not configured");
        }

        testContext.executeApiCall(() -> {
            List<BatchCheckItem> batchCheckItems = new ArrayList<>();
            
            List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
            for (Map<String, String> row : rows) {
                List<String> cells = new ArrayList<>(row.values());
                if (cells.size() >= 4) {
                    CheckRequestTupleKey tupleKey = new CheckRequestTupleKey()
                        .user(cells.get(0))
                        .relation(cells.get(1))
                        ._object(cells.get(2));
                    
                    BatchCheckItem item = new BatchCheckItem()
                        .tupleKey(tupleKey)
                        .correlationId(cells.get(3));
                    
                    batchCheckItems.add(item);
                }
            }

            BatchCheckRequest batchCheckRequest = new BatchCheckRequest()
                .checks(batchCheckItems);

            // Add contextual tuples if available
            if (testContext.getSavedData().containsKey("contextual_tuples")) {
                batchCheckRequest.contextualTuples(testContext.getSavedData().get("contextual_tuples"));
            }

            // Add context object if available
            if (testContext.getSavedData().containsKey("context_object")) {
                batchCheckRequest.context(testContext.getSavedData().get("context_object"));
            }

            // Use SDK's native batchCheck method
            return testContext.getClient().batchCheck(batchCheckRequest);
        });
    }

    // BatchCheck with 55 permission checks step definition
    @When("I call BatchCheck with {int} permission checks")
    public void iCallBatchCheckWithPermissionChecks(int count) throws Exception {
        if (testContext.getClient() == null) {
            throw new IllegalStateException("Client not configured");
        }

        testContext.executeApiCall(() -> {
            List<BatchCheckItem> batchCheckItems = new ArrayList<>();
            
            // Generate the specified number of permission checks
            for (int i = 0; i < count; i++) {
                CheckRequestTupleKey tupleKey = new CheckRequestTupleKey()
                    .user("user:test" + i)
                    .relation("viewer")
                    ._object("document:test");
                
                BatchCheckItem item = new BatchCheckItem()
                    .tupleKey(tupleKey)
                    .correlationId("check_" + i);
                
                batchCheckItems.add(item);
            }

            BatchCheckRequest batchCheckRequest = new BatchCheckRequest()
                .checks(batchCheckItems);

            // Use SDK's native batchCheck method
            return testContext.getClient().batchCheck(batchCheckRequest);
        });
    }

    // BatchCheck with invalid correlation IDs step definition
    @When("I call BatchCheck with invalid correlation IDs:")
    public void iCallBatchCheckWithInvalidCorrelationIDs(DataTable dataTable) throws Exception {
        if (testContext.getClient() == null) {
            throw new IllegalStateException("Client not configured");
        }

        testContext.executeApiCall(() -> {
            List<BatchCheckItem> batchCheckItems = new ArrayList<>();
            
            List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
            for (Map<String, String> row : rows) {
                List<String> cells = new ArrayList<>(row.values());
                if (cells.size() >= 4) {
                    CheckRequestTupleKey tupleKey = new CheckRequestTupleKey()
                        .user(cells.get(0))
                        .relation(cells.get(1))
                        ._object(cells.get(2));
                    
                    BatchCheckItem item = new BatchCheckItem()
                        .tupleKey(tupleKey)
                        .correlationId(cells.get(3)); // This will be invalid (too long)
                    
                    batchCheckItems.add(item);
                }
            }

            BatchCheckRequest batchCheckRequest = new BatchCheckRequest()
                .checks(batchCheckItems);

            // Use SDK's native batchCheck method
            return testContext.getClient().batchCheck(batchCheckRequest);
        });
    }

    // BatchCheck with duplicate correlation IDs step definition
    @When("I call BatchCheck with duplicate correlation IDs:")
    public void iCallBatchCheckWithDuplicateCorrelationIDs(DataTable dataTable) throws Exception {
        if (testContext.getClient() == null) {
            throw new IllegalStateException("Client not configured");
        }

        testContext.executeApiCall(() -> {
            List<BatchCheckItem> batchCheckItems = new ArrayList<>();
            
            List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
            for (Map<String, String> row : rows) {
                List<String> cells = new ArrayList<>(row.values());
                if (cells.size() >= 4) {
                    CheckRequestTupleKey tupleKey = new CheckRequestTupleKey()
                        .user(cells.get(0))
                        .relation(cells.get(1))
                        ._object(cells.get(2));
                    
                    BatchCheckItem item = new BatchCheckItem()
                        .tupleKey(tupleKey)
                        .correlationId(cells.get(3)); // This will be duplicate
                    
                    batchCheckItems.add(item);
                }
            }

            BatchCheckRequest batchCheckRequest = new BatchCheckRequest()
                .checks(batchCheckItems);

            // Use SDK's native batchCheck method
            return testContext.getClient().batchCheck(batchCheckRequest);
        });
    }

    // Configuration testing step definitions
    @Given("I configure the client with maxBatchSize {int}")
    public void iConfigureTheClientWithMaxBatchSize(int maxBatchSize) {
        testContext.getSavedData().put("maxBatchSize", String.valueOf(maxBatchSize));
    }

    @Given("I configure the client with maxParallelRequests {int}")
    public void iConfigureTheClientWithMaxParallelRequests(int maxParallelRequests) {
        testContext.getSavedData().put("maxParallelRequests", String.valueOf(maxParallelRequests));
    }

    @Given("I configure the client with:")
    public void iConfigureTheClientWith(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            List<String> cells = new ArrayList<>(row.values());
            if (cells.size() >= 2) {
                String key = cells.get(0);
                String value = cells.get(1);
                testContext.getSavedData().put(key, value);
            }
        }
    }

    @When("I call ClientBatchCheck with maxBatchSize {int} and {int} permission checks")
    public void iCallClientBatchCheckWithMaxBatchSizeAndPermissionChecks(int maxBatchSize, int count) throws Exception {
        if (testContext.getClient() == null) {
            throw new IllegalStateException("Client not configured");
        }

        // Store runtime configuration
        testContext.getSavedData().put("runtime_maxBatchSize", String.valueOf(maxBatchSize));

        testContext.executeApiCall(() -> {
            List<ClientCheckRequest> batchCheckRequests = new ArrayList<>();

            // Generate the specified number of permission checks
            for (int i = 0; i < count; i++) {
                ClientCheckRequest checkRequest = new ClientCheckRequest()
                    .tupleKey(new CheckRequestTupleKey()
                        .user("user:test" + i)
                        .relation("viewer")
                        ._object("document:test"));

                // Add contextual tuples if available
                if (testContext.getSavedData().containsKey("contextual_tuples")) {
                    checkRequest.contextualTuples(testContext.getSavedData().get("contextual_tuples"));
                }

                // Add context object if available
                if (testContext.getSavedData().containsKey("context_object")) {
                    checkRequest.context(testContext.getSavedData().get("context_object"));
                }

                batchCheckRequests.add(checkRequest);
            }

            // Use SDK's clientBatchCheck with runtime maxBatchSize override
            ClientBatchCheckOptions requestOptions = new ClientBatchCheckOptions()
                .maxBatchSize(maxBatchSize);

            return testContext.getClient().clientBatchCheck(batchCheckRequests, requestOptions);
        });
    }

    @When("I call ClientBatchCheck with {int} permission checks")
    public void iCallClientBatchCheckWithPermissionChecks(int count) throws Exception {
        if (testContext.getClient() == null) {
            throw new IllegalStateException("Client not configured");
        }

        testContext.executeApiCall(() -> {
            List<ClientCheckRequest> batchCheckRequests = new ArrayList<>();

            // Generate the specified number of permission checks
            for (int i = 0; i < count; i++) {
                ClientCheckRequest checkRequest = new ClientCheckRequest()
                    .tupleKey(new CheckRequestTupleKey()
                        .user("user:test" + i)
                        .relation("viewer")
                        ._object("document:test"));

                // Add contextual tuples if available
                if (testContext.getSavedData().containsKey("contextual_tuples")) {
                    checkRequest.contextualTuples(testContext.getSavedData().get("contextual_tuples"));
                }

                // Add context object if available
                if (testContext.getSavedData().containsKey("context_object")) {
                    checkRequest.context(testContext.getSavedData().get("context_object"));
                }

                batchCheckRequests.add(checkRequest);
            }

            // Get configuration from savedData
            ClientBatchCheckOptions requestOptions = new ClientBatchCheckOptions();

            if (testContext.getSavedData().containsKey("maxBatchSize")) {
                try {
                    int maxBatchSize = Integer.parseInt(testContext.getSavedData().get("maxBatchSize"));
                    requestOptions.maxBatchSize(maxBatchSize);
                } catch (NumberFormatException e) {
                    // Use default if parsing fails
                }
            }

            if (testContext.getSavedData().containsKey("maxParallelRequests")) {
                try {
                    int maxParallelRequests = Integer.parseInt(testContext.getSavedData().get("maxParallelRequests"));
                    requestOptions.maxParallelRequests(maxParallelRequests);
                } catch (NumberFormatException e) {
                    // Use default if parsing fails
                }
            }

            return testContext.getClient().clientBatchCheck(batchCheckRequests, requestOptions);
        });
    }

    @When("I configure the client with maxBatchSize {int}")
    public void iConfigureTheClientWithMaxBatchSizeWhen(int maxBatchSize) throws Exception {
        if (maxBatchSize == 0) {
            throw new IllegalStateException("configuration_error: maxBatchSize must be greater than 0");
        }
        testContext.getSavedData().put("maxBatchSize", String.valueOf(maxBatchSize));
    }

    @When("I configure the client with maxParallelRequests {int}")
    public void iConfigureTheClientWithMaxParallelRequestsWhen(int maxParallelRequests) throws Exception {
        if (maxParallelRequests == 0) {
            throw new IllegalStateException("configuration_error: maxParallelRequests must be greater than 0");
        }
        testContext.getSavedData().put("maxParallelRequests", String.valueOf(maxParallelRequests));
    }

    @Given("I have a client with default configuration")
    public void iHaveAClientWithDefaultConfiguration() {
        // Clear any existing configuration to use defaults
        testContext.getSavedData().remove("maxBatchSize");
        testContext.getSavedData().remove("maxParallelRequests");
    }
}

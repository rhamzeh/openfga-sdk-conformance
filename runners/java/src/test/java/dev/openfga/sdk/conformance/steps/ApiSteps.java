package dev.openfga.sdk.conformance.steps;

import dev.openfga.sdk.conformance.support.TestContext;
import dev.openfga.sdk.api.model.*;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.When;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ApiSteps {
    private final TestContext testContext;

    public ApiSteps(TestContext testContext) {
        this.testContext = testContext;
    }

    // Check API steps
    @When("I call Check with user {string} relation {string} object {string}")
    public void iCallCheckWith(String user, String relation, String object) {
        if (testContext.getClient() == null) {
            throw new RuntimeException("Client not configured");
        }

        testContext.executeApiCall(() -> {
            CheckRequest checkRequest = new CheckRequest()
                .tupleKey(new CheckRequestTupleKey()
                    .user(user)
                    .relation(relation)
                    .object(object));
            
            return testContext.getClient().check(checkRequest).get();
        });
    }

    @When("I call Check with:")
    public void iCallCheckWith(DataTable dataTable) {
        if (testContext.getClient() == null) {
            throw new RuntimeException("Client not configured");
        }

        CheckRequest checkRequest = new CheckRequest();
        CheckRequestTupleKey tupleKey = new CheckRequestTupleKey();
        
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            String key = row.keySet().iterator().next();
            String value = row.get(key);
            
            switch (key.toLowerCase()) {
                case "user":
                    tupleKey.user(value);
                    break;
                case "relation":
                    tupleKey.relation(value);
                    break;
                case "object":
                    tupleKey.object(value);
                    break;
                case "authorizationmodelid":
                    checkRequest.authorizationModelId(value);
                    break;
            }
        }
        
        checkRequest.tupleKey(tupleKey);

        // Add contextual tuples if available
        if (testContext.getSavedData().containsKey("contextual_tuples")) {
            @SuppressWarnings("unchecked")
            List<TupleKey> contextualTuples = (List<TupleKey>) testContext.getSavedData().get("contextual_tuples");
            checkRequest.contextualTuples(contextualTuples);
            testContext.getSavedData().remove("contextual_tuples");
        }

        // Add consistency preference if available
        if (testContext.getSavedData().containsKey("consistency_preference")) {
            String consistency = (String) testContext.getSavedData().get("consistency_preference");
            // Map string to enum - implementation depends on SDK structure
            testContext.getSavedData().remove("consistency_preference");
        }

        // Add context object if available
        if (testContext.getSavedData().containsKey("context_object")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> contextObject = (Map<String, Object>) testContext.getSavedData().get("context_object");
            checkRequest.context(contextObject);
            testContext.getSavedData().remove("context_object");
        }

        testContext.executeApiCall(() -> {
            return testContext.getClient().check(checkRequest).get();
        });
    }

    @When("contextual tuples:")
    public void contextualTuples(DataTable dataTable) {
        List<TupleKey> contextualTuples = new ArrayList<>();
        
        List<List<String>> rows = dataTable.asLists(String.class);
        for (List<String> row : rows) {
            if (row.size() >= 3 && !row.get(0).isEmpty()) {
                contextualTuples.add(new TupleKey()
                    .user(row.get(0))
                    .relation(row.get(1))
                    .object(row.get(2)));
            }
        }
        
        testContext.getSavedData().put("contextual_tuples", contextualTuples);
    }

    @When("consistency preference {string}")
    public void consistencyPreference(String preference) {
        testContext.getSavedData().put("consistency_preference", preference);
    }

    @Given("I set the context object:")
    public void iSetTheContextObject(DataTable dataTable) {
        Map<String, Object> contextObject = new HashMap<>();
        
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            String key = row.keySet().iterator().next();
            String value = row.get(key);
            
            if (key != null && !key.isEmpty() && value != null && !value.isEmpty()) {
                // Handle nested keys like "user.department"
                String[] keys = key.split("\\.");
                Map<String, Object> current = contextObject;
                
                for (int i = 0; i < keys.length - 1; i++) {
                    if (!current.containsKey(keys[i])) {
                        current.put(keys[i], new HashMap<String, Object>());
                    }
                    @SuppressWarnings("unchecked")
                    Map<String, Object> next = (Map<String, Object>) current.get(keys[i]);
                    current = next;
                }
                
                // Convert value to appropriate type
                Object parsedValue = value;
                if ("true".equals(value) || "false".equals(value)) {
                    parsedValue = Boolean.parseBoolean(value);
                } else {
                    try {
                        if (value.contains(".")) {
                            parsedValue = Double.parseDouble(value);
                        } else {
                            parsedValue = Integer.parseInt(value);
                        }
                    } catch (NumberFormatException e) {
                        // Keep as string if not a number
                    }
                }
                
                current.put(keys[keys.length - 1], parsedValue);
            }
        }
        
        testContext.getSavedData().put("context_object", contextObject);
    }

    @Given("I set the context object as JSON:")
    public void iSetTheContextObjectAsJson(String jsonString) {
        try {
            // Simple JSON parsing - in a real implementation, use a proper JSON library
            Map<String, Object> contextObject = parseJsonToMap(jsonString);
            testContext.getSavedData().put("context_object", contextObject);
        } catch (Exception e) {
            throw new RuntimeException("Invalid JSON in context object: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJsonToMap(String jsonString) {
        // This is a simplified JSON parser - in production, use Jackson or Gson
        // For now, we'll store the JSON string and let the SDK handle it
        Map<String, Object> result = new HashMap<>();
        result.put("_json", jsonString);
        return result;
    }

    @When("I call Check with authorization model {string}")
    public void iCallCheckWithAuthorizationModel(String modelId) {
        if (testContext.getClient() == null) {
            throw new RuntimeException("Client not configured");
        }

        testContext.executeApiCall(() -> {
            CheckRequest checkRequest = new CheckRequest()
                .authorizationModelId(modelId);
            
            return testContext.getClient().check(checkRequest).get();
        });
    }

    // Read API steps
    @When("I call Read with no parameters")
    public void iCallReadWithNoParameters() {
        if (testContext.getClient() == null) {
            throw new RuntimeException("Client not configured");
        }

        testContext.executeApiCall(() -> {
            ReadRequest readRequest = new ReadRequest();
            return testContext.getClient().read(readRequest).get();
        });
    }

    @When("I call Read with:")
    public void iCallReadWith(DataTable dataTable) {
        if (testContext.getClient() == null) {
            throw new RuntimeException("Client not configured");
        }

        ReadRequest readRequest = new ReadRequest();
        ReadRequestTupleKey tupleKey = null;
        
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            String key = row.keySet().iterator().next();
            String value = row.get(key);
            
            switch (key.toLowerCase()) {
                case "user":
                    if (tupleKey == null) tupleKey = new ReadRequestTupleKey();
                    tupleKey.user(value);
                    break;
                case "relation":
                    if (tupleKey == null) tupleKey = new ReadRequestTupleKey();
                    tupleKey.relation(value);
                    break;
                case "object":
                    if (tupleKey == null) tupleKey = new ReadRequestTupleKey();
                    tupleKey.object(value);
                    break;
                case "pagesize":
                    readRequest.pageSize(Integer.parseInt(value));
                    break;
                case "continuationtoken":
                    readRequest.continuationToken(value);
                    break;
            }
        }
        
        if (tupleKey != null) {
            readRequest.tupleKey(tupleKey);
        }

        testContext.executeApiCall(() -> {
            return testContext.getClient().read(readRequest).get();
        });
    }

    @When("I call Read with page size {int}")
    public void iCallReadWithPageSize(int pageSize) {
        if (testContext.getClient() == null) {
            throw new RuntimeException("Client not configured");
        }

        testContext.executeApiCall(() -> {
            ReadRequest readRequest = new ReadRequest()
                .pageSize(pageSize);
            
            return testContext.getClient().read(readRequest).get();
        });
    }

    @When("I call Read with continuation token {string}")
    public void iCallReadWithContinuationToken(String token) {
        if (testContext.getClient() == null) {
            throw new RuntimeException("Client not configured");
        }

        testContext.executeApiCall(() -> {
            ReadRequest readRequest = new ReadRequest()
                .continuationToken(token);
            
            return testContext.getClient().read(readRequest).get();
        });
    }

    @When("I call Read with continuation token from {string}")
    public void iCallReadWithContinuationTokenFrom(String savedKey) {
        if (testContext.getClient() == null) {
            throw new RuntimeException("Client not configured");
        }

        Object savedData = testContext.getSavedData().get(savedKey);
        if (savedData == null) {
            throw new RuntimeException("No saved data found for key: " + savedKey);
        }

        String token = "";
        if (savedData instanceof ReadResponse) {
            ReadResponse savedResponse = (ReadResponse) savedData;
            token = savedResponse.getContinuationToken() != null ? savedResponse.getContinuationToken() : "";
        }

        final String finalToken = token;
        testContext.executeApiCall(() -> {
            ReadRequest readRequest = new ReadRequest()
                .continuationToken(finalToken);
            
            return testContext.getClient().read(readRequest).get();
        });
    }

    // Write API steps
    @When("I call Write with writes:")
    public void iCallWriteWithWrites(DataTable dataTable) {
        if (testContext.getClient() == null) {
            throw new RuntimeException("Client not configured");
        }

        List<TupleKey> writes = new ArrayList<>();
        
        List<List<String>> rows = dataTable.asLists(String.class);
        for (List<String> row : rows) {
            writes.add(new TupleKey()
                .user(row.get(0))
                .relation(row.get(1))
                .object(row.get(2)));
        }

        testContext.executeApiCall(() -> {
            WriteRequest writeRequest = new WriteRequest()
                .writes(new TupleKeys().tupleKeys(writes));
            
            return testContext.getClient().write(writeRequest).get();
        });
    }

    @When("I call Write with deletes:")
    public void iCallWriteWithDeletes(DataTable dataTable) {
        if (testContext.getClient() == null) {
            throw new RuntimeException("Client not configured");
        }

        List<TupleKeyWithoutCondition> deletes = new ArrayList<>();
        
        List<List<String>> rows = dataTable.asLists(String.class);
        for (List<String> row : rows) {
            deletes.add(new TupleKeyWithoutCondition()
                .user(row.get(0))
                .relation(row.get(1))
                .object(row.get(2)));
        }

        testContext.executeApiCall(() -> {
            WriteRequest writeRequest = new WriteRequest()
                .deletes(new TupleKeys().tupleKeys(deletes.stream()
                    .map(tk -> new TupleKey().user(tk.getUser()).relation(tk.getRelation()).object(tk.getObject()))
                    .collect(java.util.stream.Collectors.toList())));
            
            return testContext.getClient().write(writeRequest).get();
        });
    }

    @When("I call Write with writes and deletes:")
    public void iCallWriteWithWritesAndDeletes(DataTable dataTable) {
        if (testContext.getClient() == null) {
            throw new RuntimeException("Client not configured");
        }

        List<TupleKey> writes = new ArrayList<>();
        List<TupleKeyWithoutCondition> deletes = new ArrayList<>();
        
        List<List<String>> rows = dataTable.asLists(String.class);
        for (List<String> row : rows) {
            String operation = row.get(0);
            String user = row.get(1);
            String relation = row.get(2);
            String object = row.get(3);
            
            if ("write".equals(operation)) {
                writes.add(new TupleKey().user(user).relation(relation).object(object));
            } else if ("delete".equals(operation)) {
                deletes.add(new TupleKeyWithoutCondition().user(user).relation(relation).object(object));
            }
        }

        testContext.executeApiCall(() -> {
            WriteRequest writeRequest = new WriteRequest()
                .writes(new TupleKeys().tupleKeys(writes))
                .deletes(new TupleKeys().tupleKeys(deletes.stream()
                    .map(tk -> new TupleKey().user(tk.getUser()).relation(tk.getRelation()).object(tk.getObject()))
                    .collect(java.util.stream.Collectors.toList())));
            
            return testContext.getClient().write(writeRequest).get();
        });
    }

    @When("authorization model {string}")
    public void authorizationModel(String modelId) {
        // This step is used in combination with other steps to specify authorization model
        // Implementation depends on context of previous step
    }
}

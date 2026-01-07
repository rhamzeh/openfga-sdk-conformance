package dev.openfga.sdk.conformance.support;

import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.configuration.ClientConfiguration;
import dev.openfga.sdk.api.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class TestContext {
    private OpenFgaClient client;
    private ClientConfiguration config;
    private Object lastResponse;
    private Exception lastError;
    private Map<String, Object> savedData = new HashMap<>();
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> responseHeaders = new HashMap<>();
    private Map<String, String> authConfig = new HashMap<>();
    private Integer statusCode;
    private String wiremockUrl = System.getenv().getOrDefault("WIREMOCK_URL", "http://localhost:8080");
    private ObjectMapper objectMapper = new ObjectMapper();

    public void reset() {
        client = null;
        config = null;
        lastResponse = null;
        lastError = null;
        savedData.clear();
        headers.clear();
        responseHeaders.clear();
        authConfig.clear();
        statusCode = null;
    }

    public void createClient(Map<String, String> configMap) throws Exception {
        try {
            ClientConfiguration.Builder configBuilder = new ClientConfiguration.Builder()
                    .apiUrl(wiremockUrl);
            
            if (configMap.containsKey("storeId")) {
                configBuilder.storeId(configMap.get("storeId"));
            }
            
            if (configMap.containsKey("authorizationModelId")) {
                configBuilder.authorizationModelId(configMap.get("authorizationModelId"));
            }
            
            config = configBuilder.build();
            client = new OpenFgaClient(config);
        } catch (Exception e) {
            throw new Exception("Failed to create OpenFGA client: " + e.getMessage(), e);
        }
    }

    public void executeApiCall(ApiCall apiCall) {
        try {
            lastResponse = apiCall.call();
            lastError = null;
        } catch (Exception e) {
            lastError = e;
            lastResponse = null;
            
            // Enhanced error handling with status code extraction
            extractStatusCodeFromException(e);
        }
    }

    private void extractStatusCodeFromException(Exception ex) {
        // Extract HTTP status code from various exception types
        String errorMessage = ex.getMessage().toLowerCase();
        
        if (errorMessage.contains("401") || errorMessage.contains("unauthorized")) {
            statusCode = 401;
        } else if (errorMessage.contains("403") || errorMessage.contains("forbidden")) {
            statusCode = 403;
        } else if (errorMessage.contains("404") || errorMessage.contains("not found")) {
            statusCode = 404;
        } else if (errorMessage.contains("400") || errorMessage.contains("bad request")) {
            statusCode = 400;
        }
        
        // Try to extract from exception data or cause
        if (ex.getCause() != null) {
            extractStatusCodeFromException(ex.getCause());
        }
    }

    // Enhanced authentication configuration
    public void configureAuthentication(String method, Map<String, String> config) {
        switch (method.toLowerCase()) {
            case "bearer":
                headers.put("Authorization", "Bearer " + config.get("token"));
                break;
            case "client_credentials":
                // Store credentials for token exchange
                authConfig.put("method", "client_credentials");
                authConfig.put("clientId", config.getOrDefault("clientId", ""));
                authConfig.put("clientSecret", config.getOrDefault("clientSecret", ""));
                authConfig.put("apiTokenIssuer", config.getOrDefault("apiTokenIssuer", ""));
                authConfig.put("apiAudience", config.getOrDefault("apiAudience", ""));
                break;
            default:
                throw new IllegalArgumentException("Unsupported authentication method: " + method);
        }
    }

    // Response header inspection
    public String getResponseHeader(String headerName) {
        return responseHeaders.get(headerName.toLowerCase());
    }

    // Enhanced status code checking
    public Integer getStatusCode() {
        if (statusCode != null) {
            return statusCode;
        }
        // Assume 200 for successful responses
        return lastResponse != null ? 200 : null;
    }

    public CompletableFuture<Void> executeAsyncApiCall(AsyncApiCall apiCall) {
        return apiCall.call()
                .thenAccept(response -> {
                    lastResponse = response;
                    lastError = null;
                })
                .exceptionally(throwable -> {
                    lastError = (Exception) throwable;
                    lastResponse = null;
                    return null;
                });
    }

    // Assertion helpers
    public void assertResponseSuccess() {
        if (lastError != null) {
            throw new AssertionError("Expected successful response, got error: " + lastError.getMessage());
        }
        Assertions.assertThat(lastResponse).isNotNull();
    }

    public void assertResponseFailure() {
        if (lastError == null) {
            throw new AssertionError("Expected error response, but got success");
        }
    }

    public void assertResponseContainsItems(int count) {
        assertResponseSuccess();
        
        if (lastResponse instanceof ReadResponse) {
            ReadResponse readResponse = (ReadResponse) lastResponse;
            int actualCount = readResponse.getTuples() != null ? readResponse.getTuples().size() : 0;
            Assertions.assertThat(actualCount).isEqualTo(count);
        } else {
            throw new AssertionError("Response is not a ReadResponse");
        }
    }

    public void assertTuplesInclude(List<Map<String, String>> expectedTuples) {
        assertResponseSuccess();
        
        if (lastResponse instanceof ReadResponse) {
            ReadResponse readResponse = (ReadResponse) lastResponse;
            List<Tuple> actualTuples = readResponse.getTuples();
            
            if (actualTuples == null) {
                throw new AssertionError("Response contains no tuples");
            }
            
            for (Map<String, String> expected : expectedTuples) {
                boolean found = actualTuples.stream().anyMatch(tuple -> 
                    tuple.getKey().getUser().equals(expected.get("user")) &&
                    tuple.getKey().getRelation().equals(expected.get("relation")) &&
                    tuple.getKey().getObject().equals(expected.get("object"))
                );
                
                if (!found) {
                    throw new AssertionError(String.format(
                        "Expected tuple not found: user=%s, relation=%s, object=%s",
                        expected.get("user"), expected.get("relation"), expected.get("object")
                    ));
                }
            }
        } else {
            throw new AssertionError("Response is not a ReadResponse");
        }
    }

    // Save and compare responses
    public void saveResponse(String key) {
        if (lastResponse == null) {
            throw new RuntimeException("No response to save");
        }
        savedData.put(key, lastResponse);
    }

    public void compareWithSaved(String key, boolean shouldMatch) {
        Object savedData = this.savedData.get(key);
        if (savedData == null) {
            throw new RuntimeException("No saved data found for key: " + key);
        }
        
        if (lastResponse == null) {
            throw new RuntimeException("No current response to compare");
        }

        try {
            String currentJson = objectMapper.writeValueAsString(lastResponse);
            String savedJson = objectMapper.writeValueAsString(savedData);
            boolean matches = currentJson.equals(savedJson);

            if (shouldMatch && !matches) {
                throw new AssertionError("Current response does not match saved response for key: " + key);
            } else if (!shouldMatch && matches) {
                throw new AssertionError("Current response matches saved response for key: " + key + " (expected different)");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to compare responses: " + e.getMessage(), e);
        }
    }

    // Getters and setters
    public OpenFgaClient getClient() { return client; }
    public ClientConfiguration getConfig() { return config; }
    public Object getLastResponse() { return lastResponse; }
    public Exception getLastError() { return lastError; }
    public Map<String, Object> getSavedData() { return savedData; }
    public Map<String, String> getHeaders() { return headers; }
    public String getWiremockUrl() { return wiremockUrl; }

    @FunctionalInterface
    public interface ApiCall {
        Object call() throws Exception;
    }

    @FunctionalInterface
    public interface AsyncApiCall {
        CompletableFuture<Object> call();
    }
}

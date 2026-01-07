package dev.openfga.sdk.conformance.steps;

import dev.openfga.sdk.conformance.support.TestContext;
import dev.openfga.sdk.api.configuration.ClientConfiguration;
import dev.openfga.sdk.api.configuration.Credentials;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.assertj.core.api.Assertions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientSteps {
    private final TestContext testContext;

    public ClientSteps(TestContext testContext) {
        this.testContext = testContext;
    }

    @Given("I have a client configured with store {string}")
    public void iHaveAClientConfiguredWithStore(String storeId) throws Exception {
        Map<String, String> config = new HashMap<>();
        config.put("storeId", storeId);
        testContext.createClient(config);
    }

    @Given("I have a client configured with:")
    public void iHaveAClientConfiguredWith(DataTable dataTable) throws Exception {
        Map<String, String> config = new HashMap<>();
        
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            String key = row.keySet().iterator().next();
            String value = row.get(key);
            
            switch (key.toLowerCase()) {
                case "apiurl":
                    // ApiUrl is handled in createClient
                    break;
                case "storeid":
                    config.put("storeId", value);
                    break;
                case "authorizationmodelid":
                    config.put("authorizationModelId", value);
                    break;
            }
        }
        
        testContext.createClient(config);
    }

    @Given("I configure authentication with bearer token {string}")
    public void iConfigureAuthenticationWithBearerToken(String token) {
        Map<String, String> config = new HashMap<>();
        config.put("token", token);
        testContext.configureAuthentication("bearer", config);
    }

    @Given("I configure client credentials authentication:")
    public void iConfigureClientCredentialsAuthentication(DataTable dataTable) {
        Map<String, String> credentials = new HashMap<>();
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        
        for (Map<String, String> row : rows) {
            String key = row.keySet().iterator().next();
            String value = row.get(key);
            credentials.put(key.toLowerCase(), value);
        }
        
        testContext.configureAuthentication("client_credentials", credentials);
    }

    @Given("I configure default headers:")
    public void iConfigureDefaultHeaders(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        
        for (Map<String, String> row : rows) {
            String key = row.keySet().iterator().next();
            String value = row.get(key);
            testContext.getHeaders().put(key, value);
        }
        
        // Apply headers to existing client if available
        if (testContext.getClient() != null) {
            // Note: OpenFGA Java SDK header setting would go here
            // Implementation depends on SDK capabilities
        }
    }

    @Given("I set the request header {string} to {string}")
    public void iSetTheRequestHeader(String key, String value) {
        testContext.setRequestHeader(key, value);
    }

    @Given("I configure retry settings:")
    public void iConfigureRetrySettings(DataTable dataTable) {
        Map<String, Object> retryConfig = new HashMap<>();
        
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            String key = row.keySet().iterator().next();
            String value = row.get(key);
            
            switch (key.toLowerCase()) {
                case "maxretries":
                    retryConfig.put("maxRetries", Integer.parseInt(value));
                    break;
                case "backofftype":
                    retryConfig.put("backoffType", value);
                    break;
                case "basedelay":
                    retryConfig.put("baseDelay", Integer.parseInt(value));
                    break;
                case "circuitbreakerenabled":
                    retryConfig.put("circuitBreakerEnabled", Boolean.parseBoolean(value));
                    break;
                case "circuitbreakerthreshold":
                    retryConfig.put("circuitBreakerThreshold", Integer.parseInt(value));
                    break;
                case "jitterenabled":
                    retryConfig.put("jitterEnabled", Boolean.parseBoolean(value));
                    break;
            }
        }
        
        // Validate retry configuration
        if (retryConfig.containsKey("maxRetries") && (Integer) retryConfig.get("maxRetries") < 0) {
            throw new IllegalArgumentException("maxRetries must be non-negative");
        }
        
        testContext.getSavedData().put("retry_config", retryConfig);
        testContext.getSavedData().put("retry_attempts", new ArrayList<Long>());
        testContext.getSavedData().put("retry_delays", new ArrayList<Integer>());
    }

    @Then("the client should be configured successfully")
    public void theClientShouldBeConfiguredSuccessfully() {
        Assertions.assertThat(testContext.getClient()).isNotNull();
        Assertions.assertThat(testContext.getConfig()).isNotNull();
    }
}

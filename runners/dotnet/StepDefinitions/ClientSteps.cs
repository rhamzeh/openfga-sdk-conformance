using TechTalk.SpecFlow;
using OpenFga.Sdk.Client;
using OpenFga.Sdk.Conformance.Support;
using FluentAssertions;

namespace OpenFga.Sdk.Conformance.StepDefinitions
{
    [Binding]
    public sealed class ClientSteps
    {
        private readonly TestContext _testContext;

        public ClientSteps(TestContext testContext)
        {
            _testContext = testContext;
        }

        [Given(@"I have a client configured with store ""([^""]*)""")]
        public async Task GivenIHaveAClientConfiguredWithStore(string storeId)
        {
            var config = new ClientConfiguration
            {
                StoreId = storeId
            };
            
            await _testContext.CreateClientAsync(config);
        }

        [Given(@"I have a client configured with:")]
        public async Task GivenIHaveAClientConfiguredWith(Table table)
        {
            var config = new ClientConfiguration();
            
            foreach (var row in table.Rows)
            {
                var key = row.Keys.First();
                var value = row[key];
                
                switch (key.ToLowerInvariant())
                {
                    case "apiurl":
                        // ApiUrl is handled in TestContext.CreateClientAsync
                        break;
                    case "storeid":
                        config.StoreId = value;
                        break;
                    case "authorizationmodelid":
                        config.AuthorizationModelId = value;
                        break;
                }
            }
            
            await _testContext.CreateClientAsync(config);
        }

        [Given(@"I configure authentication with bearer token ""([^""]*)""")]
        public async Task GivenIConfigureAuthenticationWithBearerToken(string token)
        {
            var config = new Dictionary<string, string> { ["token"] = token };
            _testContext.ConfigureAuthentication("bearer", config);
        }

        [Given(@"I configure client credentials authentication:")]
        public async Task GivenIConfigureClientCredentialsAuthentication(Table table)
        {
            var credentials = new Dictionary<string, string>();
        
            foreach (var row in table.Rows)
            {
                var key = row.Keys.First();
                var value = row[key];
                credentials[key.ToLowerInvariant()] = value;
            }
        
            _testContext.ConfigureAuthentication("client_credentials", credentials);
        }

        [Given(@"I configure default headers:")]
        public async Task GivenIConfigureDefaultHeaders(Table table)
        {
            foreach (var row in table.Rows)
            {
                var key = row.Keys.First();
                var value = row[key];
                _testContext.Headers[key] = value;
            }
            
            // Apply headers to existing client if available
            if (_testContext.Client != null)
            {
                // Note: OpenFGA .NET SDK header setting would go here
                // Implementation depends on SDK capabilities
            }
        }

        [Given(@"I set the request header ""([^""]*)"" to ""([^""]*)""")]
        public void GivenISetTheRequestHeader(string key, string value)
        {
            _testContext.SetRequestHeader(key, value);
        }

        [Given(@"I configure retry settings:")]
        public void GivenIConfigureRetrySettings(Table table)
        {
            var retryConfig = new Dictionary<string, object>();
            
            foreach (var row in table.Rows)
            {
                var key = row.Keys.First();
                var value = row[key];
                
                switch (key.ToLowerInvariant())
                {
                    case "maxretries":
                        retryConfig["MaxRetries"] = int.Parse(value);
                        break;
                    case "backofftype":
                        retryConfig["BackoffType"] = value;
                        break;
                    case "basedelay":
                        retryConfig["BaseDelay"] = int.Parse(value);
                        break;
                    case "circuitbreakerenabled":
                        retryConfig["CircuitBreakerEnabled"] = bool.Parse(value);
                        break;
                    case "circuitbreakerthreshold":
                        retryConfig["CircuitBreakerThreshold"] = int.Parse(value);
                        break;
                    case "jitterenabled":
                        retryConfig["JitterEnabled"] = bool.Parse(value);
                        break;
                }
            }
            
            // Validate retry configuration
            if (retryConfig.ContainsKey("MaxRetries") && (int)retryConfig["MaxRetries"] < 0)
            {
                throw new ArgumentException("maxRetries must be non-negative");
            }
            
            _testContext.SavedData["retry_config"] = retryConfig;
            _testContext.SavedData["retry_attempts"] = new List<DateTime>();
            _testContext.SavedData["retry_delays"] = new List<int>();
        }

        [Then(@"the client should be configured successfully")]
        public void ThenTheClientShouldBeConfiguredSuccessfully()
        {
            _testContext.Client.Should().NotBeNull();
            _testContext.Config.Should().NotBeNull();
        }
    }
}

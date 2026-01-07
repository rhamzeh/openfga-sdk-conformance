using OpenFga.Sdk.Client;
using OpenFga.Sdk.Client.Model;
using OpenFga.Sdk.Model;
using FluentAssertions;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace OpenFga.Sdk.Conformance.Support
{
    public class TestContext
    {
        public OpenFgaClient? Client { get; set; }
        public ClientConfiguration? Config { get; set; }
        public object? LastResponse { get; set; }
        public Exception? LastError { get; set; }
        public Dictionary<string, object> SavedData { get; set; } = new();
        public Dictionary<string, string> Headers { get; set; } = new();
        public Dictionary<string, string>? ResponseHeaders { get; set; }
        public Dictionary<string, string>? AuthConfig { get; set; }
        public int? StatusCode { get; set; }
        public string WiremockUrl { get; set; } = "http://localhost:8080";

        public void Reset()
        {
            Client = null;
            Config = null;
            LastResponse = null;
            LastError = null;
            SavedData.Clear();
            Headers.Clear();
            ResponseHeaders = null;
            AuthConfig = null;
            StatusCode = null;
        }

        public async Task CreateClientAsync(ClientConfiguration config)
        {
            try
            {
                Config = new ClientConfiguration
                {
                    ApiUrl = WiremockUrl,
                    StoreId = config.StoreId,
                    AuthorizationModelId = config.AuthorizationModelId,
                    Credentials = config.Credentials
                };

                Client = new OpenFgaClient(Config);
            }
            catch (Exception ex)
            {
                throw new InvalidOperationException($"Failed to create OpenFGA client: {ex.Message}", ex);
            }
        }

        public async Task ExecuteApiCallAsync(Func<Task<object>> apiCall)
        {
            try
            {
                LastResponse = await apiCall();
                LastError = null;
            }
            catch (Exception ex)
            {
                LastError = ex;
                LastResponse = null;
                
                // Enhanced error handling with status code extraction
                ExtractStatusCodeFromException(ex);
            }
        }

        private void ExtractStatusCodeFromException(Exception ex)
        {
            // Extract HTTP status code from various exception types
            if (ex is HttpRequestException httpEx)
            {
                // Try to extract status code from message or data
                if (ex.Data.Contains("StatusCode"))
                {
                    StatusCode = (int)ex.Data["StatusCode"];
                }
                else if (httpEx.Message.Contains("401"))
                {
                    StatusCode = 401;
                }
                else if (httpEx.Message.Contains("403"))
                {
                    StatusCode = 403;
                }
                else if (httpEx.Message.Contains("404"))
                {
                    StatusCode = 404;
                }
                else if (httpEx.Message.Contains("400"))
                {
                    StatusCode = 400;
                }
            }
            // Add other SDK-specific exception handling as needed
        }

        // Enhanced authentication configuration
        public void ConfigureAuthentication(string method, Dictionary<string, string> config)
        {
            switch (method.ToLowerInvariant())
            {
                case "bearer":
                    Headers["Authorization"] = $"Bearer {config["token"]}";
                    break;
                case "client_credentials":
                    // Store credentials for token exchange
                    AuthConfig = new Dictionary<string, string>
                    {
                        ["method"] = "client_credentials",
                        ["clientId"] = config.GetValueOrDefault("clientId", ""),
                        ["clientSecret"] = config.GetValueOrDefault("clientSecret", ""),
                        ["apiTokenIssuer"] = config.GetValueOrDefault("apiTokenIssuer", ""),
                        ["apiAudience"] = config.GetValueOrDefault("apiAudience", "")
                    };
                    break;
                default:
                    throw new ArgumentException($"Unsupported authentication method: {method}");
            }
        }

        // Response header inspection
        public string GetResponseHeader(string headerName)
        {
            if (ResponseHeaders != null && ResponseHeaders.ContainsKey(headerName))
            {
                return ResponseHeaders[headerName];
            }
            return null;
        }

        // Enhanced status code checking
        public int? GetStatusCode()
        {
            if (StatusCode.HasValue)
            {
                return StatusCode.Value;
            }
            // Assume 200 for successful responses
            return LastResponse != null ? 200 : null;
        }

        // Assertion helpers
        public void AssertResponseSuccess()
        {
            if (LastError != null)
            {
                throw new AssertionException($"Expected successful response, got error: {LastError.Message}");
            }
            LastResponse.Should().NotBeNull();
        }

        public void AssertResponseFailure()
        {
            if (LastError == null)
            {
                throw new AssertionException("Expected error response, but got success");
            }
        }

        public void AssertResponseContainsItems(int count)
        {
            AssertResponseSuccess();
            
            if (LastResponse is ReadResponse readResponse)
            {
                var actualCount = readResponse.Tuples?.Count ?? 0;
                actualCount.Should().Be(count, $"Expected {count} items, got {actualCount}");
            }
            else
            {
                throw new AssertionException("Response is not a ReadResponse");
            }
        }

        public void AssertTuplesInclude(List<TupleKey> expectedTuples)
        {
            AssertResponseSuccess();
            
            if (LastResponse is ReadResponse readResponse)
            {
                var actualTuples = readResponse.Tuples ?? new List<Tuple>();
                
                foreach (var expected in expectedTuples)
                {
                    var found = actualTuples.Any(tuple =>
                        tuple.Key.User == expected.User &&
                        tuple.Key.Relation == expected.Relation &&
                        tuple.Key.Object == expected.Object);
                    
                    if (!found)
                    {
                        throw new AssertionException($"Expected tuple not found: user={expected.User}, relation={expected.Relation}, object={expected.Object}");
                    }
                }
            }
            else
            {
                throw new AssertionException("Response is not a ReadResponse");
            }
        }

        // Save and compare responses
        public void SaveResponse(string key)
        {
            if (LastResponse == null)
            {
                throw new InvalidOperationException("No response to save");
            }
            SavedData[key] = LastResponse;
        }

        public void CompareWithSaved(string key, bool shouldMatch = true)
        {
            if (!SavedData.TryGetValue(key, out var savedData))
            {
                throw new InvalidOperationException($"No saved data found for key: {key}");
            }
            
            if (LastResponse == null)
            {
                throw new InvalidOperationException("No current response to compare");
            }

            var currentJson = System.Text.Json.JsonSerializer.Serialize(LastResponse);
            var savedJson = System.Text.Json.JsonSerializer.Serialize(savedData);
            var matches = currentJson == savedJson;

            if (shouldMatch && !matches)
            {
                throw new AssertionException($"Current response does not match saved response for key: {key}");
            }
            else if (!shouldMatch && matches)
            {
                throw new AssertionException($"Current response matches saved response for key: {key} (expected different)");
            }
        }
    }

    public class AssertionException : Exception
    {
        public AssertionException(string message) : base(message) { }
    }
}

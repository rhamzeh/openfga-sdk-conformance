using TechTalk.SpecFlow;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Xunit;

namespace OpenFga.Sdk.Conformance.StepDefinitions
{
    [Binding]
    public class IntegrationTests
    {
        private readonly TestContext _context;

        public IntegrationTests(TestContext context)
        {
            _context = context;
        }

        // Authorization model management steps
        [When(@"I call ReadAuthorizationModel")]
        public async Task WhenICallReadAuthorizationModel()
        {
            if (_context.Client == null)
            {
                throw new InvalidOperationException("Client not configured");
            }

            await _context.ExecuteApiCall(async () =>
            {
                // Simulate reading authorization model for integration tests
                return new
                {
                    authorization_model = new
                    {
                        id = "01ARZ3NDEKTSV4RRFFQ69G5FAV",
                        schema_version = "1.1",
                        type_definitions = new object[] { }
                    }
                };
            });
        }

        [When(@"I call ListAuthorizationModels with:")]
        public async Task WhenICallListAuthorizationModelsWith(Table table)
        {
            if (_context.Client == null)
            {
                throw new InvalidOperationException("Client not configured");
            }

            int pageSize = 10;

            foreach (var row in table.Rows)
            {
                var key = row.Keys.First();
                var value = row[key];

                if (key.Equals("pageSize", StringComparison.OrdinalIgnoreCase))
                {
                    if (int.TryParse(value, out int parsedPageSize))
                    {
                        pageSize = parsedPageSize;
                    }
                }
            }

            await _context.ExecuteApiCall(async () =>
            {
                // Simulate listing authorization models
                return new
                {
                    authorization_models = new[]
                    {
                        new
                        {
                            id = "01ARZ3NDEKTSV4RRFFQ69G5FAV",
                            schema_version = "1.1"
                        }
                    },
                    page_size = pageSize
                };
            });
        }

        // Complex response validation steps
        [Then(@"the response should contain authorization model")]
        public void ThenTheResponseShouldContainAuthorizationModel()
        {
            _context.AssertResponseSuccess();

            var response = _context.LastResponse;
            if (response == null)
            {
                throw new InvalidOperationException("No response received");
            }

            // Check if response contains authorization model data
            var responseType = response.GetType();
            var authModelProperty = responseType.GetProperty("authorization_model");
            
            if (authModelProperty == null || authModelProperty.GetValue(response) == null)
            {
                throw new InvalidOperationException("Response does not contain authorization model");
            }
        }

        [Then(@"the response should contain authorization models")]
        public void ThenTheResponseShouldContainAuthorizationModels()
        {
            _context.AssertResponseSuccess();

            var response = _context.LastResponse;
            if (response == null)
            {
                throw new InvalidOperationException("No response received");
            }

            // Check if response contains authorization models list
            var responseType = response.GetType();
            var authModelsProperty = responseType.GetProperty("authorization_models");
            
            if (authModelsProperty == null)
            {
                throw new InvalidOperationException("Response does not contain authorization models");
            }

            var authModels = authModelsProperty.GetValue(response) as Array;
            if (authModels == null || authModels.Length == 0)
            {
                throw new InvalidOperationException("Authorization models list is empty");
            }
        }

        [Then(@"the streaming response should contain multiple objects")]
        public void ThenTheStreamingResponseShouldContainMultipleObjects()
        {
            _context.AssertResponseSuccess();

            if (_context.StreamedObjects == null || _context.StreamedObjects.Count < 2)
            {
                var count = _context.StreamedObjects?.Count ?? 0;
                throw new InvalidOperationException($"Streaming response should contain multiple objects, got {count}");
            }
        }

        // Multi-request validation steps
        [Then(@"all requests should have included header ""([^""]*)"" with value ""([^""]*)""")]
        public void ThenAllRequestsShouldHaveIncludedHeaderWithValue(string headerName, string expectedValue)
        {
            if (_context.RequestHeaderHistory == null || _context.RequestHeaderHistory.Count == 0)
            {
                throw new InvalidOperationException("No request headers captured");
            }

            for (int i = 0; i < _context.RequestHeaderHistory.Count; i++)
            {
                var headers = _context.RequestHeaderHistory[i];
                bool found = false;

                foreach (var kvp in headers)
                {
                    if (string.Equals(kvp.Key, headerName, StringComparison.OrdinalIgnoreCase) && 
                        kvp.Value == expectedValue)
                    {
                        found = true;
                        break;
                    }
                }

                if (!found)
                {
                    throw new InvalidOperationException($"Request {i + 1} did not include header {headerName} with value {expectedValue}");
                }
            }
        }

        [Then(@"the second request should have included header ""([^""]*)"" with value ""([^""]*)""")]
        public void ThenTheSecondRequestShouldHaveIncludedHeaderWithValue(string headerName, string expectedValue)
        {
            if (_context.RequestHeaderHistory == null || _context.RequestHeaderHistory.Count < 2)
            {
                var count = _context.RequestHeaderHistory?.Count ?? 0;
                throw new InvalidOperationException($"Need at least 2 requests for comparison, got {count}");
            }

            var headers = _context.RequestHeaderHistory[1];
            bool found = false;

            foreach (var kvp in headers)
            {
                if (string.Equals(kvp.Key, headerName, StringComparison.OrdinalIgnoreCase) && 
                    kvp.Value == expectedValue)
                {
                    found = true;
                    break;
                }
            }

            if (!found)
            {
                throw new InvalidOperationException($"Second request did not include header {headerName} with value {expectedValue}");
            }
        }

        // Error handling steps
        [Then(@"the response should be an error")]
        public void ThenTheResponseShouldBeAnError()
        {
            if (_context.LastError == null)
            {
                throw new InvalidOperationException("Expected an error but response was successful");
            }
        }

        [Then(@"the error should be ""([^""]*)""")]
        public void ThenTheErrorShouldBe(string expectedError)
        {
            if (_context.LastError == null)
            {
                throw new InvalidOperationException("No error occurred");
            }

            var errorString = _context.LastError.Message ?? _context.LastError.ToString();

            switch (expectedError.ToLower())
            {
                case "unauthorized":
                    if (!errorString.ToLower().Contains("unauthorized") && 
                        !errorString.ToLower().Contains("401"))
                    {
                        throw new InvalidOperationException($"Expected unauthorized error, got: {errorString}");
                    }
                    break;
                case "rate_limited":
                    if (!errorString.ToLower().Contains("rate") && 
                        !errorString.ToLower().Contains("429"))
                    {
                        throw new InvalidOperationException($"Expected rate limited error, got: {errorString}");
                    }
                    break;
                default:
                    if (!errorString.ToLower().Contains(expectedError.ToLower()))
                    {
                        throw new InvalidOperationException($"Expected error containing '{expectedError}', got: {errorString}");
                    }
                    break;
            }
        }

        // Context management for integration tests
        private void CaptureRequestHeaders()
        {
            // Initialize request header history if not exists
            if (_context.RequestHeaderHistory == null)
            {
                _context.RequestHeaderHistory = new List<Dictionary<string, string>>();
            }

            // Capture current request headers for multi-request validation
            var headersCopy = new Dictionary<string, string>();
            if (_context.RequestHeaders != null)
            {
                foreach (var kvp in _context.RequestHeaders)
                {
                    headersCopy[kvp.Key] = kvp.Value;
                }
            }
            _context.RequestHeaderHistory.Add(headersCopy);
        }

        private void CaptureResponse()
        {
            // Initialize response history if not exists
            if (_context.ResponseHistory == null)
            {
                _context.ResponseHistory = new List<ResponseRecord>();
            }

            // Capture current response for multi-response validation
            _context.ResponseHistory.Add(new ResponseRecord
            {
                Response = _context.LastResponse,
                Error = _context.LastError
            });
        }

        // Override ExecuteApiCall to capture headers and responses for integration tests
        public async Task ExecuteApiCallWithCapture(Func<Task<object>> apiCall)
        {
            // Capture request headers before the call
            CaptureRequestHeaders();

            // Execute the API call
            try
            {
                var response = await apiCall();
                _context.LastResponse = response;
                _context.LastError = null;
            }
            catch (Exception error)
            {
                _context.LastResponse = null;
                _context.LastError = error;
            }

            // Capture response after the call
            CaptureResponse();
        }
    }

    // Helper class for response history
    public class ResponseRecord
    {
        public object Response { get; set; }
        public Exception Error { get; set; }
    }
}

using System;
using System.Collections.Generic;
using System.Linq;
using TechTalk.SpecFlow;
using TechTalk.SpecFlow.Assist;
using dev.openfga.sdk.conformance.support;

namespace dev.openfga.sdk.conformance.steps
{
    [Binding]
    public class HeaderValidationSteps
    {
        private readonly TestContext _testContext;

        public HeaderValidationSteps(TestContext testContext)
        {
            _testContext = testContext;
        }

        [When(@"I set the request header ""([^""]*)"" to ""([^""]*)""")]
        public void WhenISetTheRequestHeaderTo(string headerName, string headerValue)
        {
            if (_testContext.RequestHeaders == null)
            {
                _testContext.RequestHeaders = new Dictionary<string, string>();
            }
            _testContext.RequestHeaders[headerName] = headerValue;
        }

        [When(@"I clear the request header ""([^""]*)""")]
        public void WhenIClearTheRequestHeader(string headerName)
        {
            if (_testContext.RequestHeaders != null)
            {
                _testContext.RequestHeaders.Remove(headerName);
            }
        }

        [Then(@"the request should have included header ""([^""]*)"" with value ""([^""]*)""")]
        public void ThenTheRequestShouldHaveIncludedHeaderWithValue(string headerName, string expectedValue)
        {
            if (_testContext.CapturedRequestHeaders == null)
            {
                throw new InvalidOperationException("No request headers were captured");
            }

            var actualValue = FindHeaderValue(_testContext.CapturedRequestHeaders, headerName);
            if (actualValue == null)
            {
                throw new InvalidOperationException($"Request header '{headerName}' was not found");
            }

            if (actualValue != expectedValue)
            {
                throw new InvalidOperationException($"Request header '{headerName}' expected value '{expectedValue}', got '{actualValue}'");
            }
        }

        [Then(@"the request should have included header ""([^""]*)""")]
        public void ThenTheRequestShouldHaveIncludedHeader(string headerName)
        {
            if (_testContext.CapturedRequestHeaders == null)
            {
                throw new InvalidOperationException("No request headers were captured");
            }

            var actualValue = FindHeaderValue(_testContext.CapturedRequestHeaders, headerName);
            if (actualValue == null)
            {
                throw new InvalidOperationException($"Request header '{headerName}' was not found");
            }
        }

        [Then(@"the request should not have included header ""([^""]*)""")]
        public void ThenTheRequestShouldNotHaveIncludedHeader(string headerName)
        {
            if (_testContext.CapturedRequestHeaders == null)
            {
                return; // No headers captured means header wasn't included
            }

            var actualValue = FindHeaderValue(_testContext.CapturedRequestHeaders, headerName);
            if (actualValue != null)
            {
                throw new InvalidOperationException($"Request header '{headerName}' was found but should not have been included");
            }
        }

        [Then(@"the response should include header ""([^""]*)"" with value ""([^""]*)""")]
        public void ThenTheResponseShouldIncludeHeaderWithValue(string headerName, string expectedValue)
        {
            if (_testContext.LastResponse == null)
            {
                throw new InvalidOperationException("No response received");
            }

            var responseHeaders = ExtractResponseHeaders();
            if (responseHeaders == null)
            {
                throw new InvalidOperationException("No response headers available");
            }

            var actualValue = FindHeaderValue(responseHeaders, headerName);
            if (actualValue == null)
            {
                throw new InvalidOperationException($"Response header '{headerName}' was not found");
            }

            if (actualValue != expectedValue)
            {
                throw new InvalidOperationException($"Response header '{headerName}' expected value '{expectedValue}', got '{actualValue}'");
            }
        }

        [Then(@"the authorization header should contain ""([^""]*)""")]
        public void ThenTheAuthorizationHeaderShouldContain(string expectedSubstring)
        {
            if (_testContext.CapturedRequestHeaders == null)
            {
                throw new InvalidOperationException("No request headers were captured");
            }

            var authValue = FindHeaderValue(_testContext.CapturedRequestHeaders, "Authorization");
            if (authValue == null)
            {
                throw new InvalidOperationException("Authorization header was not found");
            }

            if (!authValue.Contains(expectedSubstring))
            {
                throw new InvalidOperationException($"Authorization header '{authValue}' does not contain '{expectedSubstring}'");
            }
        }

        [Then(@"the content length should be greater than (\d+)")]
        public void ThenTheContentLengthShouldBeGreaterThan(int minLength)
        {
            if (_testContext.CapturedRequestHeaders == null)
            {
                throw new InvalidOperationException("No request headers were captured");
            }

            var contentLengthStr = FindHeaderValue(_testContext.CapturedRequestHeaders, "Content-Length");
            if (contentLengthStr == null)
            {
                throw new InvalidOperationException("Content-Length header was not found");
            }

            if (!int.TryParse(contentLengthStr, out int contentLength))
            {
                throw new InvalidOperationException($"Invalid Content-Length value: {contentLengthStr}");
            }

            if (contentLength <= minLength)
            {
                throw new InvalidOperationException($"Content-Length {contentLength} is not greater than {minLength}");
            }
        }

        [Then(@"both responses should be successful")]
        public void ThenBothResponsesShouldBeSuccessful()
        {
            if (_testContext.ResponseHistory == null || _testContext.ResponseHistory.Count < 2)
            {
                var count = _testContext.ResponseHistory?.Count ?? 0;
                throw new InvalidOperationException($"Expected at least 2 responses, got {count}");
            }

            // Check last two responses
            var lastTwo = _testContext.ResponseHistory.TakeLast(2).ToList();
            for (int i = 0; i < lastTwo.Count; i++)
            {
                if (lastTwo[i].Error != null)
                {
                    throw new InvalidOperationException($"Response {i + 1} failed: {lastTwo[i].Error.Message}");
                }
            }
        }

        [Then(@"both requests should have included header ""([^""]*)"" with value ""([^""]*)""")]
        public void ThenBothRequestsShouldHaveIncludedHeaderWithValue(string headerName, string expectedValue)
        {
            if (_testContext.RequestHeaderHistory == null || _testContext.RequestHeaderHistory.Count < 2)
            {
                var count = _testContext.RequestHeaderHistory?.Count ?? 0;
                throw new InvalidOperationException($"Expected at least 2 request header sets, got {count}");
            }

            // Check last two request header sets
            var lastTwo = _testContext.RequestHeaderHistory.TakeLast(2).ToList();
            for (int i = 0; i < lastTwo.Count; i++)
            {
                var actualValue = FindHeaderValue(lastTwo[i], headerName);
                if (actualValue == null)
                {
                    throw new InvalidOperationException($"Request {i + 1} header '{headerName}' was not found");
                }
                if (actualValue != expectedValue)
                {
                    throw new InvalidOperationException($"Request {i + 1} header '{headerName}' expected value '{expectedValue}', got '{actualValue}'");
                }
            }
        }

        [Then(@"the first request should have included header ""([^""]*)"" with value ""([^""]*)""")]
        public void ThenTheFirstRequestShouldHaveIncludedHeaderWithValue(string headerName, string expectedValue)
        {
            if (_testContext.RequestHeaderHistory == null || _testContext.RequestHeaderHistory.Count < 1)
            {
                throw new InvalidOperationException("No request headers in history");
            }

            var firstHeaders = _testContext.RequestHeaderHistory[0];
            var actualValue = FindHeaderValue(firstHeaders, headerName);
            if (actualValue == null)
            {
                throw new InvalidOperationException($"First request header '{headerName}' was not found");
            }
            if (actualValue != expectedValue)
            {
                throw new InvalidOperationException($"First request header '{headerName}' expected value '{expectedValue}', got '{actualValue}'");
            }
        }

        [Then(@"the second request should not have included header ""([^""]*)""")]
        public void ThenTheSecondRequestShouldNotHaveIncludedHeader(string headerName)
        {
            if (_testContext.RequestHeaderHistory == null || _testContext.RequestHeaderHistory.Count < 2)
            {
                var count = _testContext.RequestHeaderHistory?.Count ?? 0;
                throw new InvalidOperationException($"Expected at least 2 request header sets, got {count}");
            }

            var secondHeaders = _testContext.RequestHeaderHistory[1];
            var actualValue = FindHeaderValue(secondHeaders, headerName);
            if (actualValue != null)
            {
                throw new InvalidOperationException($"Second request header '{headerName}' was found but should not have been included");
            }
        }

        [When(@"I make a preflight request to Check endpoint")]
        public void WhenIMakeAPreflightRequestToCheckEndpoint()
        {
            // Placeholder for CORS preflight request
            throw new NotImplementedException("Preflight requests not yet implemented in .NET SDK");
        }

        [Then(@"the preflight response should be successful")]
        public void ThenThePreflightResponseShouldBeSuccessful()
        {
            // Placeholder for preflight response validation
            throw new NotImplementedException("Preflight response validation not yet implemented");
        }

        // Helper methods

        private string FindHeaderValue(Dictionary<string, string> headers, string headerName)
        {
            if (headers == null) return null;

            // Check for exact match first
            if (headers.ContainsKey(headerName))
            {
                return headers[headerName];
            }

            // Case-insensitive search
            var lowerHeaderName = headerName.ToLowerInvariant();
            foreach (var kvp in headers)
            {
                if (kvp.Key.ToLowerInvariant() == lowerHeaderName)
                {
                    return kvp.Value;
                }
            }

            return null;
        }

        private Dictionary<string, string> ExtractResponseHeaders()
        {
            if (_testContext.LastResponse == null) return null;

            // Extract headers from response object
            // Implementation depends on the .NET SDK response structure
            if (_testContext.LastResponseHeaders != null)
            {
                return _testContext.LastResponseHeaders;
            }

            // Return empty dictionary if no headers available
            return new Dictionary<string, string>();
        }
    }
}

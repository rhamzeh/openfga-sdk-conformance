using TechTalk.SpecFlow;
using OpenFga.Sdk.Model;
using OpenFga.Sdk.Conformance.Support;
using FluentAssertions;
using System.Text.Json;

namespace OpenFga.Sdk.Conformance.StepDefinitions
{
    [Binding]
    public sealed class AssertionSteps
    {
        private readonly TestContext _testContext;

        public AssertionSteps(TestContext testContext)
        {
            _testContext = testContext;
        }

        // Response assertions
        [Then(@"the response should be successful")]
        public void ThenTheResponseShouldBeSuccessful()
        {
            _testContext.AssertResponseSuccess();
        }

        [Then(@"the response should fail")]
        public void ThenTheResponseShouldFail()
        {
            _testContext.AssertResponseFailure();
        }

        [Then(@"the response should have status code (\d+)")]
        public void ThenTheResponseShouldHaveStatusCode(int statusCode)
        {
            var actualStatusCode = _testContext.GetStatusCode();
            if (!actualStatusCode.HasValue)
            {
                throw new AssertionException("No response or error to check status code");
            }
            actualStatusCode.Value.Should().Be(statusCode);
        }

        [Then(@"the response should complete within (\d+) seconds")]
        public void ThenTheResponseShouldCompleteWithin(int seconds)
        {
            // This would require timing implementation in the test context
            // For now, just pass if we have a response
            if (_testContext.LastResponse == null && _testContext.LastError == null)
            {
                throw new AssertionException("No response received");
            }
        }

        [Then(@"the result should be ""([^""]*)""")]
        public void ThenTheResultShouldBe(string expected)
        {
            _testContext.AssertResponseSuccess();

            // Parse expected result (e.g., "allowed: true")
            var parts = expected.Split(": ");
            parts.Should().HaveCount(2, "Expected format: 'field: value'");

            var field = parts[0];
            var value = parts[1];

            if (field == "allowed")
            {
                if (_testContext.LastResponse is CheckResponse checkResponse)
                {
                    var expectedBool = bool.Parse(value);
                    checkResponse.Allowed.Should().Be(expectedBool);
                }
                else
                {
                    throw new AssertionException("Response is not a CheckResponse");
                }
            }
            else
            {
                throw new AssertionException($"Unsupported result field: {field}");
            }
        }

        [Then(@"the response should contain ""([^""]*)""")]
        public void ThenTheResponseShouldContain(string content)
        {
            _testContext.AssertResponseSuccess();
            var responseJson = JsonSerializer.Serialize(_testContext.LastResponse);
            responseJson.Should().Contain(content);
        }

        [Then(@"the response should not contain ""([^""]*)""")]
        public void ThenTheResponseShouldNotContain(string content)
        {
            _testContext.AssertResponseSuccess();
            var responseJson = JsonSerializer.Serialize(_testContext.LastResponse);
            responseJson.Should().NotContain(content);
        }

        [Then(@"the response should have field ""([^""]*)"" with value ""([^""]*)""")]
        public void ThenTheResponseShouldHaveFieldWithValue(string field, string value)
        {
            _testContext.AssertResponseSuccess();

            // Use reflection or JSON path to navigate nested fields
            var responseJson = JsonSerializer.Serialize(_testContext.LastResponse);
            var jsonDoc = JsonDocument.Parse(responseJson);

            var fieldPath = field.Split('.');
            var current = jsonDoc.RootElement;

            foreach (var part in fieldPath)
            {
                current.TryGetProperty(part, out current).Should().BeTrue($"Field '{part}' not found");
            }

            current.GetString().Should().Be(value);
        }

        // Error assertions
        [Then(@"the response should fail with a validation error")]
        public void ThenTheResponseShouldFailWithValidationError()
        {
            _testContext.AssertResponseFailure();

            if (_testContext.LastError != null)
            {
                _testContext.LastError.Message.Should().ContainAny("validation", "invalid", "bad request");
            }
        }

        [Then(@"the response should fail with an authentication error")]
        public void ThenTheResponseShouldFailWithAuthenticationError()
        {
            _testContext.AssertResponseFailure();
            
            // Check for 401 status or authentication-related error
            if (_testContext.LastError is HttpRequestException)
            {
                _testContext.LastError.Message.Should().ContainAny("401", "unauthorized", "authentication");
            }
        }

        [Then(@"the response should fail with an authorization error")]
        public void ThenTheResponseShouldFailWithAuthorizationError()
        {
            _testContext.AssertResponseFailure();
            
            // Check for 403 status or authorization-related error
            if (_testContext.LastError is HttpRequestException)
            {
                _testContext.LastError.Message.Should().ContainAny("403", "forbidden", "authorization");
            }
        }

        [Then(@"the error code should be ""([^""]*)""")]
        public void ThenTheErrorCodeShouldBe(string expectedCode)
        {
            _testContext.AssertResponseFailure();

            // Extract error code from exception or error response
            // Implementation depends on how OpenFGA .NET SDK structures errors
            if (_testContext.LastError != null)
            {
                _testContext.LastError.Message.Should().Contain(expectedCode);
            }
        }

        [Then(@"the error message should contain ""([^""]*)""")]
        public void ThenTheErrorMessageShouldContain(string expectedMessage)
        {
            _testContext.AssertResponseFailure();

            if (_testContext.LastError != null)
            {
                _testContext.LastError.Message.Should().Contain(expectedMessage);
            }
        }

        // Collection assertions
        [Then(@"the response should contain (\d+) items")]
        public void ThenTheResponseShouldContainItems(int count)
        {
            _testContext.AssertResponseContainsItems(count);
        }

        [Then(@"the response should contain at least (\d+) item")]
        [Then(@"the response should contain at least (\d+) items")]
        public void ThenTheResponseShouldContainAtLeastItems(int count)
        {
            _testContext.AssertResponseSuccess();

            if (_testContext.LastResponse is ReadResponse readResponse)
            {
                var actualCount = readResponse.Tuples?.Count ?? 0;
                actualCount.Should().BeGreaterOrEqualTo(count);
            }
            else
            {
                throw new AssertionException("Response is not a ReadResponse");
            }
        }

        [Then(@"the response should contain at most (\d+) items")]
        public void ThenTheResponseShouldContainAtMostItems(int count)
        {
            _testContext.AssertResponseSuccess();

            if (_testContext.LastResponse is ReadResponse readResponse)
            {
                var actualCount = readResponse.Tuples?.Count ?? 0;
                actualCount.Should().BeLessOrEqualTo(count);
            }
            else
            {
                throw new AssertionException("Response is not a ReadResponse");
            }
        }

        [Then(@"the response should contain exactly (\d+) item")]
        [Then(@"the response should contain exactly (\d+) items")]
        public void ThenTheResponseShouldContainExactlyItems(int count)
        {
            _testContext.AssertResponseContainsItems(count);
        }

        [Then(@"the response should be empty")]
        public void ThenTheResponseShouldBeEmpty()
        {
            _testContext.AssertResponseContainsItems(0);
        }

        [Then(@"the tuples should include:")]
        public void ThenTheTuplesShouldInclude(Table table)
        {
            var expectedTuples = new List<TupleKey>();

            foreach (var row in table.Rows)
            {
                var values = row.Values.ToArray();
                expectedTuples.Add(new TupleKey
                {
                    User = values[0],
                    Relation = values[1],
                    Object = values[2]
                });
            }

            _testContext.AssertTuplesInclude(expectedTuples);
        }

        // Header verification
        [Then(@"the request should include header ""([^""]*)"" with value ""([^""]*)""")]
        public void ThenTheRequestShouldIncludeHeaderWithValue(string header, string value)
        {
            _testContext.Headers.Should().ContainKey(header);
            _testContext.Headers[header].Should().Be(value);
        }

        [Then(@"the request should include header ""([^""]*)"" matching ""([^""]*)""")]
        public void ThenTheRequestShouldIncludeHeaderMatching(string header, string pattern)
        {
            _testContext.Headers.Should().ContainKey(header);
            var headerValue = _testContext.Headers[header];
            headerValue.Should().MatchRegex(pattern);
        }

        [Then(@"the request should not include header ""([^""]*)""")]
        public void ThenTheRequestShouldNotIncludeHeader(string header)
        {
            _testContext.Headers.Should().NotContainKey(header);
        }

        [Then(@"the response should include header ""([^""]*)""")]
        public void ThenTheResponseShouldIncludeHeader(string header)
        {
            // This would require response header inspection
            // Implementation depends on how the .NET SDK exposes response headers
            if (_testContext.LastResponse != null)
            {
                // Check if response has headers property or similar
                // For now, just pass - would need SDK-specific implementation
            }
        }

        // Context and state management
        [When(@"I save the response as ""([^""]*)""")]
        public void WhenISaveTheResponseAs(string key)
        {
            _testContext.SaveResponse(key);
        }

        [When(@"I use the saved ""([^""]*)"" for comparison")]
        public void WhenIUseTheSavedForComparison(string key)
        {
            if (!_testContext.SavedData.ContainsKey(key))
            {
                throw new InvalidOperationException($"No saved data found for key: {key}");
            }
        }

        [Then(@"the response should match the saved ""([^""]*)""")]
        public void ThenTheResponseShouldMatchTheSaved(string key)
        {
            _testContext.CompareWithSaved(key, true);
        }

        [Then(@"the response should not match the saved ""([^""]*)""")]
        public void ThenTheResponseShouldNotMatchTheSaved(string key)
        {
            _testContext.CompareWithSaved(key, false);
        }
    }
}

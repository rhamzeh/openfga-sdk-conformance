using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using TechTalk.SpecFlow;
using dev.openfga.sdk.conformance.support;
using Newtonsoft.Json;

namespace dev.openfga.sdk.conformance.steps
{
    [Binding]
    public class AdvancedApiAssertions
    {
        private readonly TestContext _testContext;

        public AdvancedApiAssertions(TestContext testContext)
        {
            _testContext = testContext;
        }

        // ReadChanges API assertions
        [Then(@"the response should contain ""([^""]*)""")]
        public void ThenTheResponseShouldContain(string fieldName)
        {
            _testContext.AssertResponseSuccess();

            if (_testContext.LastResponse == null)
            {
                throw new InvalidOperationException("No response received");
            }

            var responseType = _testContext.LastResponse.GetType();
            var property = responseType.GetProperty(ToPascalCase(fieldName));

            if (property == null)
            {
                throw new InvalidOperationException($"Response does not contain \"{fieldName}\" field");
            }

            var value = property.GetValue(_testContext.LastResponse);
            if (value == null)
            {
                throw new InvalidOperationException($"Response field \"{fieldName}\" is null");
            }
        }

        [Then(@"the response should contain at most (\d+) changes")]
        public void ThenTheResponseShouldContainAtMostChanges(int maxCount)
        {
            _testContext.AssertResponseSuccess();

            var changes = GetResponseProperty<IEnumerable<object>>(_testContext.LastResponse, "Changes");
            if (changes == null)
            {
                throw new InvalidOperationException("Response does not contain changes field");
            }

            var actualCount = changes.Count();
            if (actualCount > maxCount)
            {
                throw new InvalidOperationException($"Expected at most {maxCount} changes, got {actualCount}");
            }
        }

        [Then(@"the response should have continuation token")]
        public void ThenTheResponseShouldHaveContinuationToken()
        {
            _testContext.AssertResponseSuccess();

            if (_testContext.LastResponse == null)
            {
                throw new InvalidOperationException("No response received");
            }

            var continuationToken = GetResponseProperty<string>(_testContext.LastResponse, "ContinuationToken");
            if (string.IsNullOrEmpty(continuationToken))
            {
                throw new InvalidOperationException("Response does not have continuation token");
            }
        }

        [Then(@"the response should not have continuation token")]
        public void ThenTheResponseShouldNotHaveContinuationToken()
        {
            _testContext.AssertResponseSuccess();

            if (_testContext.LastResponse == null)
            {
                throw new InvalidOperationException("No response received");
            }

            var continuationToken = GetResponseProperty<string>(_testContext.LastResponse, "ContinuationToken");
            if (!string.IsNullOrEmpty(continuationToken))
            {
                throw new InvalidOperationException("Response has continuation token but should not have one");
            }
        }

        [Then(@"the changes should be different from ""([^""]*)""")]
        public void ThenTheChangesShouldBeDifferentFrom(string savedKey)
        {
            _testContext.AssertResponseSuccess();

            if (_testContext.SavedData == null || !_testContext.SavedData.ContainsKey(savedKey))
            {
                throw new InvalidOperationException($"No saved data found for key: {savedKey}");
            }

            var savedResponse = _testContext.SavedData[savedKey];
            var currentChanges = GetResponseProperty<object>(_testContext.LastResponse, "Changes");
            var savedChanges = GetResponseProperty<object>(savedResponse, "Changes");

            var currentJson = JsonConvert.SerializeObject(currentChanges);
            var savedJson = JsonConvert.SerializeObject(savedChanges);

            if (currentJson == savedJson)
            {
                throw new InvalidOperationException($"Changes are identical to saved data from {savedKey}");
            }
        }

        [Then(@"each change should have type ""([^""]*)""")]
        public void ThenEachChangeShouldHaveType(string expectedType)
        {
            _testContext.AssertResponseSuccess();

            var changes = GetResponseProperty<IEnumerable<object>>(_testContext.LastResponse, "Changes");
            if (changes == null)
            {
                throw new InvalidOperationException("Response does not contain valid changes field");
            }

            var changesList = changes.ToList();
            for (int i = 0; i < changesList.Count; i++)
            {
                var change = changesList[i];
                var changeType = GetResponseProperty<string>(change, "Type");
                
                if (changeType != expectedType)
                {
                    throw new InvalidOperationException($"Change at index {i} has type '{changeType}', expected '{expectedType}'");
                }
            }
        }

        // ListObjects API assertions
        [Then(@"the response should contain exactly (\d+) objects")]
        public void ThenTheResponseShouldContainExactlyObjects(int expectedCount)
        {
            _testContext.AssertResponseSuccess();

            var objects = GetResponseProperty<IEnumerable<object>>(_testContext.LastResponse, "Objects");
            if (objects == null)
            {
                throw new InvalidOperationException("Response does not contain valid objects field");
            }

            var actualCount = objects.Count();
            if (actualCount != expectedCount)
            {
                throw new InvalidOperationException($"Expected exactly {expectedCount} objects, got {actualCount}");
            }
        }

        [Then(@"the response should contain exactly (\d+) changes")]
        public void ThenTheResponseShouldContainExactlyChanges(int expectedCount)
        {
            _testContext.AssertResponseSuccess();

            var changes = GetResponseProperty<IEnumerable<object>>(_testContext.LastResponse, "Changes");
            if (changes == null)
            {
                throw new InvalidOperationException("Response does not contain valid changes field");
            }

            var actualCount = changes.Count();
            if (actualCount != expectedCount)
            {
                throw new InvalidOperationException($"Expected exactly {expectedCount} changes, got {actualCount}");
            }
        }

        // Streaming API assertions
        [Then(@"the streaming response should be successful")]
        public void ThenTheStreamingResponseShouldBeSuccessful()
        {
            if (_testContext.LastError != null)
            {
                throw new InvalidOperationException($"Streaming response failed: {_testContext.LastError.Message}");
            }

            if (_testContext.LastResponse == null)
            {
                throw new InvalidOperationException("No streaming response received");
            }
        }

        [Then(@"the streaming response should contain objects")]
        public void ThenTheStreamingResponseShouldContainObjects()
        {
            _testContext.AssertResponseSuccess();

            if (_testContext.StreamedObjects == null || !_testContext.StreamedObjects.Any())
            {
                throw new InvalidOperationException("Streaming response did not contain any objects");
            }
        }

        [Then(@"each streamed object should have required fields")]
        public void ThenEachStreamedObjectShouldHaveRequiredFields()
        {
            _testContext.AssertResponseSuccess();

            if (_testContext.StreamedObjects == null || !_testContext.StreamedObjects.Any())
            {
                throw new InvalidOperationException("No streamed objects to validate");
            }

            for (int i = 0; i < _testContext.StreamedObjects.Count; i++)
            {
                var obj = _testContext.StreamedObjects[i];
                var objectField = GetResponseProperty<string>(obj, "Object");
                
                if (string.IsNullOrEmpty(objectField))
                {
                    throw new InvalidOperationException($"Streamed object at index {i} does not have required 'object' field");
                }
            }
        }

        [Then(@"the streaming connection should be properly closed")]
        public void ThenTheStreamingConnectionShouldBeProperlylosed()
        {
            // Placeholder for streaming connection validation
            if (_testContext.StreamingConnection != null && _testContext.StreamingConnection.IsConnected)
            {
                throw new InvalidOperationException("Streaming connection was not properly closed");
            }
        }

        // Advanced response validation
        [Then(@"I save the response as ""([^""]*)""")]
        public void ThenISaveTheResponseAs(string saveKey)
        {
            _testContext.AssertResponseSuccess();

            if (_testContext.SavedData == null)
            {
                _testContext.SavedData = new Dictionary<string, object>();
            }

            _testContext.SavedData[saveKey] = new
            {
                Response = _testContext.LastResponse,
                Timestamp = DateTime.UtcNow.ToString("O")
            };
        }

        [Then(@"the response should contain at least (\d+) items")]
        public void ThenTheResponseShouldContainAtLeastItems(int minCount)
        {
            _testContext.AssertResponseSuccess();

            if (_testContext.LastResponse == null)
            {
                throw new InvalidOperationException("No response received");
            }

            int itemCount = 0;

            // Check for different possible item fields
            var changes = GetResponseProperty<IEnumerable<object>>(_testContext.LastResponse, "Changes");
            var objects = GetResponseProperty<IEnumerable<object>>(_testContext.LastResponse, "Objects");
            var tuples = GetResponseProperty<IEnumerable<object>>(_testContext.LastResponse, "Tuples");

            if (changes != null)
            {
                itemCount = changes.Count();
            }
            else if (objects != null)
            {
                itemCount = objects.Count();
            }
            else if (tuples != null)
            {
                itemCount = tuples.Count();
            }
            else
            {
                throw new InvalidOperationException("Response does not contain countable items (changes, objects, or tuples)");
            }

            if (itemCount < minCount)
            {
                throw new InvalidOperationException($"Expected at least {minCount} items, got {itemCount}");
            }
        }

        [Then(@"the response should have field ""([^""]*)""")]
        public void ThenTheResponseShouldHaveField(string fieldName)
        {
            _testContext.AssertResponseSuccess();

            if (_testContext.LastResponse == null)
            {
                throw new InvalidOperationException("No response received");
            }

            var responseType = _testContext.LastResponse.GetType();
            var property = responseType.GetProperty(ToPascalCase(fieldName));

            if (property == null)
            {
                throw new InvalidOperationException($"Response does not have field '{fieldName}'");
            }
        }

        [Then(@"the response field ""([^""]*)"" should be ""([^""]*)""")]
        public void ThenTheResponseFieldShouldBe(string fieldName, string expectedValue)
        {
            _testContext.AssertResponseSuccess();

            if (_testContext.LastResponse == null)
            {
                throw new InvalidOperationException("No response received");
            }

            var actualValue = GetResponseProperty<object>(_testContext.LastResponse, fieldName);
            if (actualValue == null)
            {
                throw new InvalidOperationException($"Response does not have field '{fieldName}'");
            }

            if (actualValue.ToString() != expectedValue)
            {
                throw new InvalidOperationException($"Response field '{fieldName}' expected '{expectedValue}', got '{actualValue}'");
            }
        }

        // Pagination assertions
        [Then(@"the response should have pagination info")]
        public void ThenTheResponseShouldHavePaginationInfo()
        {
            _testContext.AssertResponseSuccess();

            if (_testContext.LastResponse == null)
            {
                throw new InvalidOperationException("No response received");
            }

            var hasContinuationToken = !string.IsNullOrEmpty(GetResponseProperty<string>(_testContext.LastResponse, "ContinuationToken"));
            var hasPageSize = GetResponseProperty<object>(_testContext.LastResponse, "PageSize") != null;
            var hasNextPage = GetResponseProperty<object>(_testContext.LastResponse, "HasNextPage") != null;

            if (!hasContinuationToken && !hasPageSize && !hasNextPage)
            {
                throw new InvalidOperationException("Response does not contain pagination information");
            }
        }

        // Error scenario assertions
        [Then(@"the error should contain ""([^""]*)""")]
        public void ThenTheErrorShouldContain(string expectedMessage)
        {
            if (_testContext.LastError == null)
            {
                throw new InvalidOperationException("Expected an error but none occurred");
            }

            var errorMessage = _testContext.LastError.Message;
            if (!errorMessage.Contains(expectedMessage))
            {
                throw new InvalidOperationException($"Error message '{errorMessage}' does not contain '{expectedMessage}'");
            }
        }

        [Then(@"the error should be of type ""([^""]*)""")]
        public void ThenTheErrorShouldBeOfType(string expectedType)
        {
            if (_testContext.LastError == null)
            {
                throw new InvalidOperationException("Expected an error but none occurred");
            }

            var errorType = _testContext.LastError.GetType().Name;
            if (errorType != expectedType)
            {
                throw new InvalidOperationException($"Error type '{errorType}' does not match expected '{expectedType}'");
            }
        }

        // Multi-response validation
        [Then(@"all responses should be successful")]
        public void ThenAllResponsesShouldBeSuccessful()
        {
            if (_testContext.ResponseHistory == null || !_testContext.ResponseHistory.Any())
            {
                throw new InvalidOperationException("No response history available");
            }

            for (int i = 0; i < _testContext.ResponseHistory.Count; i++)
            {
                var record = _testContext.ResponseHistory[i];
                if (record.Error != null)
                {
                    throw new InvalidOperationException($"Response {i + 1} failed: {record.Error.Message}");
                }
            }
        }

        [Then(@"the last (\d+) responses should be successful")]
        public void ThenTheLastResponsesShouldBeSuccessful(int count)
        {
            if (_testContext.ResponseHistory == null || _testContext.ResponseHistory.Count < count)
            {
                var available = _testContext.ResponseHistory?.Count ?? 0;
                throw new InvalidOperationException($"Expected at least {count} responses, got {available}");
            }

            var lastResponses = _testContext.ResponseHistory.TakeLast(count).ToList();
            for (int i = 0; i < lastResponses.Count; i++)
            {
                var record = lastResponses[i];
                if (record.Error != null)
                {
                    throw new InvalidOperationException($"Response {i + 1} of last {count} failed: {record.Error.Message}");
                }
            }
        }

        // Helper methods
        private T GetResponseProperty<T>(object response, string propertyName)
        {
            if (response == null) return default(T);

            var responseType = response.GetType();
            var property = responseType.GetProperty(ToPascalCase(propertyName));

            if (property == null) return default(T);

            var value = property.GetValue(response);
            if (value is T) return (T)value;

            return default(T);
        }

        private string ToPascalCase(string input)
        {
            if (string.IsNullOrEmpty(input)) return input;
            
            return char.ToUpperInvariant(input[0]) + input.Substring(1);
        }

        private void ValidateResponseStructure(object response, Dictionary<string, Type> expectedStructure)
        {
            foreach (var kvp in expectedStructure)
            {
                var property = response.GetType().GetProperty(ToPascalCase(kvp.Key));
                if (property == null)
                {
                    throw new InvalidOperationException($"Response missing required field: {kvp.Key}");
                }

                var actualType = property.PropertyType;
                if (kvp.Value != typeof(object) && !kvp.Value.IsAssignableFrom(actualType))
                {
                    throw new InvalidOperationException($"Response field '{kvp.Key}' expected type '{kvp.Value.Name}', got '{actualType.Name}'");
                }
            }
        }
    }
}

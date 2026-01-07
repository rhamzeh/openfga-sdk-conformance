using TechTalk.SpecFlow;
using OpenFga.Sdk.Client.Model;
using OpenFga.Sdk.Model;
using OpenFga.Sdk.Conformance.Support;

namespace OpenFga.Sdk.Conformance.StepDefinitions
{
    [Binding]
    public sealed class ApiSteps
    {
        private readonly TestContext _testContext;

        public ApiSteps(TestContext testContext)
        {
            _testContext = testContext;
        }

        // Check API steps
        [When(@"I call Check with user ""([^""]*)"" relation ""([^""]*)"" object ""([^""]*)""")]
        public async Task WhenICallCheckWith(string user, string relation, string @object)
        {
            if (_testContext.Client == null)
            {
                throw new InvalidOperationException("Client not configured");
            }

            await _testContext.ExecuteApiCallAsync(async () =>
            {
                var checkRequest = new CheckRequest
                {
                    TupleKey = new CheckRequestTupleKey
                    {
                        User = user,
                        Relation = relation,
                        Object = @object
                    }
                };

                return await _testContext.Client.Check(checkRequest);
            });
        }

        [When(@"I call Check with:")]
        public async Task WhenICallCheckWith(Table table)
        {
            if (_testContext.Client == null)
            {
                throw new InvalidOperationException("Client not configured");
            }

            var checkRequest = new CheckRequest();
            var tupleKey = new CheckRequestTupleKey();
            
            foreach (var row in table.Rows)
            {
                var key = row.Keys.First();
                var value = row[key];
                
                switch (key.ToLowerInvariant())
                {
                    case "user":
                        tupleKey.User = value;
                        break;
                    case "relation":
                        tupleKey.Relation = value;
                        break;
                    case "object":
                        tupleKey.Object = value;
                        break;
                    case "authorizationmodelid":
                        checkRequest.AuthorizationModelId = value;
                        break;
                }
            }
            
            checkRequest.TupleKey = tupleKey;

            // Add contextual tuples if available
            if (_testContext.SavedData.ContainsKey("contextual_tuples"))
            {
                checkRequest.ContextualTuples = (List<TupleKey>)_testContext.SavedData["contextual_tuples"];
                _testContext.SavedData.Remove("contextual_tuples");
            }

            // Add consistency preference if available
            if (_testContext.SavedData.ContainsKey("consistency_preference"))
            {
                var consistency = _testContext.SavedData["consistency_preference"].ToString();
                // Map string to enum - implementation depends on SDK structure
                _testContext.SavedData.Remove("consistency_preference");
            }

            // Add context object if available
            if (_testContext.SavedData.ContainsKey("context_object"))
            {
                var contextObject = (Dictionary<string, object>)_testContext.SavedData["context_object"];
                checkRequest.Context = contextObject;
                _testContext.SavedData.Remove("context_object");
            }

            await _testContext.ExecuteApiCallAsync(async () =>
            {
                return await _testContext.Client.Check(checkRequest);
            });
        }

        [When(@"contextual tuples:")]
        public void WhenContextualTuples(Table table)
        {
            var contextualTuples = new List<TupleKey>();
            
            foreach (var row in table.Rows)
            {
                var cells = row.Values.ToArray();
                if (cells.Length >= 3 && !string.IsNullOrEmpty(cells[0]))
                {
                    contextualTuples.Add(new TupleKey
                    {
                        User = cells[0],
                        Relation = cells[1],
                        Object = cells[2]
                    });
                }
            }
            
            _testContext.SavedData["contextual_tuples"] = contextualTuples;
        }

        [When(@"consistency preference ""([^""]*)""")]
        public void WhenConsistencyPreference(string preference)
        {
            _testContext.SavedData["consistency_preference"] = preference;
        }

        [Given(@"I set the context object:")]
        public void GivenISetTheContextObject(Table table)
        {
            var contextObject = new Dictionary<string, object>();
            
            foreach (var row in table.Rows)
            {
                var key = row.Keys.First();
                var value = row[key];
                
                if (!string.IsNullOrEmpty(key) && !string.IsNullOrEmpty(value))
                {
                    // Handle nested keys like "user.department"
                    var keys = key.Split('.');
                    var current = contextObject;
                    
                    for (int i = 0; i < keys.Length - 1; i++)
                    {
                        if (!current.ContainsKey(keys[i]))
                        {
                            current[keys[i]] = new Dictionary<string, object>();
                        }
                        current = (Dictionary<string, object>)current[keys[i]];
                    }
                    
                    // Convert value to appropriate type
                    object parsedValue = value;
                    if (bool.TryParse(value, out bool boolValue))
                        parsedValue = boolValue;
                    else if (double.TryParse(value, out double doubleValue))
                        parsedValue = doubleValue;
                    else if (int.TryParse(value, out int intValue))
                        parsedValue = intValue;
                    
                    current[keys[keys.Length - 1]] = parsedValue;
                }
            }
            
            _testContext.SavedData["context_object"] = contextObject;
        }

        [Given(@"I set the context object as JSON:")]
        public void GivenISetTheContextObjectAsJson(string jsonString)
        {
            try
            {
                var contextObject = System.Text.Json.JsonSerializer.Deserialize<Dictionary<string, object>>(jsonString);
                _testContext.SavedData["context_object"] = contextObject;
            }
            catch (System.Text.Json.JsonException ex)
            {
                throw new ArgumentException($"Invalid JSON in context object: {ex.Message}");
            }
        }

        [When(@"I call Check with authorization model ""([^""]*)""")]
        public async Task WhenICallCheckWithAuthorizationModel(string modelId)
        {
            if (_testContext.Client == null)
            {
                throw new InvalidOperationException("Client not configured");
            }

            await _testContext.ExecuteApiCallAsync(async () =>
            {
                var checkRequest = new CheckRequest
                {
                    AuthorizationModelId = modelId
                };

                return await _testContext.Client.Check(checkRequest);
            });
        }

        // Read API steps
        [When(@"I call Read with no parameters")]
        public async Task WhenICallReadWithNoParameters()
        {
            if (_testContext.Client == null)
            {
                throw new InvalidOperationException("Client not configured");
            }

            await _testContext.ExecuteApiCallAsync(async () =>
            {
                var readRequest = new ReadRequest();
                return await _testContext.Client.Read(readRequest);
            });
        }

        [When(@"I call Read with:")]
        public async Task WhenICallReadWith(Table table)
        {
            if (_testContext.Client == null)
            {
                throw new InvalidOperationException("Client not configured");
            }

            var readRequest = new ReadRequest();

            foreach (var row in table.Rows)
            {
                var key = row.Keys.First();
                var value = row[key];

                switch (key.ToLowerInvariant())
                {
                    case "user":
                        readRequest.TupleKey = readRequest.TupleKey ?? new ReadRequestTupleKey();
                        readRequest.TupleKey.User = value;
                        break;
                    case "relation":
                        readRequest.TupleKey = readRequest.TupleKey ?? new ReadRequestTupleKey();
                        readRequest.TupleKey.Relation = value;
                        break;
                    case "object":
                        readRequest.TupleKey = readRequest.TupleKey ?? new ReadRequestTupleKey();
                        readRequest.TupleKey.Object = value;
                        break;
                    case "pagesize":
                        if (int.TryParse(value, out var pageSize))
                        {
                            readRequest.PageSize = pageSize;
                        }
                        break;
                    case "continuationtoken":
                        readRequest.ContinuationToken = value;
                        break;
                }
            }

            await _testContext.ExecuteApiCallAsync(async () =>
            {
                return await _testContext.Client.Read(readRequest);
            });
        }

        [When(@"I call Read with page size (\d+)")]
        public async Task WhenICallReadWithPageSize(int pageSize)
        {
            if (_testContext.Client == null)
            {
                throw new InvalidOperationException("Client not configured");
            }

            await _testContext.ExecuteApiCallAsync(async () =>
            {
                var readRequest = new ReadRequest
                {
                    PageSize = pageSize
                };
                return await _testContext.Client.Read(readRequest);
            });
        }

        [When(@"I call Read with continuation token ""([^""]*)""")]
        public async Task WhenICallReadWithContinuationToken(string token)
        {
            if (_testContext.Client == null)
            {
                throw new InvalidOperationException("Client not configured");
            }

            await _testContext.ExecuteApiCallAsync(async () =>
            {
                var readRequest = new ReadRequest
                {
                    ContinuationToken = token
                };
                return await _testContext.Client.Read(readRequest);
            });
        }

        [When(@"I call Read with continuation token from ""([^""]*)""")]
        public async Task WhenICallReadWithContinuationTokenFrom(string savedKey)
        {
            if (_testContext.Client == null)
            {
                throw new InvalidOperationException("Client not configured");
            }

            if (!_testContext.SavedData.TryGetValue(savedKey, out var savedData))
            {
                throw new InvalidOperationException($"No saved data found for key: {savedKey}");
            }

            var token = "";
            if (savedData is ReadResponse savedResponse)
            {
                token = savedResponse.ContinuationToken ?? "";
            }

            await _testContext.ExecuteApiCallAsync(async () =>
            {
                var readRequest = new ReadRequest
                {
                    ContinuationToken = token
                };
                return await _testContext.Client.Read(readRequest);
            });
        }

        // Write API steps
        [When(@"I call Write with writes:")]
        public async Task WhenICallWriteWithWrites(Table table)
        {
            if (_testContext.Client == null)
            {
                throw new InvalidOperationException("Client not configured");
            }

            var writes = new List<TupleKey>();

            foreach (var row in table.Rows)
            {
                var values = row.Values.ToArray();
                writes.Add(new TupleKey
                {
                    User = values[0],
                    Relation = values[1],
                    Object = values[2]
                });
            }

            await _testContext.ExecuteApiCallAsync(async () =>
            {
                var writeRequest = new WriteRequest
                {
                    Writes = new TupleKeys { TupleKeys_ = writes }
                };
                return await _testContext.Client.Write(writeRequest);
            });
        }

        [When(@"I call Write with deletes:")]
        public async Task WhenICallWriteWithDeletes(Table table)
        {
            if (_testContext.Client == null)
            {
                throw new InvalidOperationException("Client not configured");
            }

            var deletes = new List<TupleKeyWithoutCondition>();

            foreach (var row in table.Rows)
            {
                var values = row.Values.ToArray();
                deletes.Add(new TupleKeyWithoutCondition
                {
                    User = values[0],
                    Relation = values[1],
                    Object = values[2]
                });
            }

            await _testContext.ExecuteApiCallAsync(async () =>
            {
                var writeRequest = new WriteRequest
                {
                    Deletes = new TupleKeys { TupleKeys_ = deletes.Cast<TupleKey>().ToList() }
                };
                return await _testContext.Client.Write(writeRequest);
            });
        }

        [When(@"I call Write with writes and deletes:")]
        public async Task WhenICallWriteWithWritesAndDeletes(Table table)
        {
            if (_testContext.Client == null)
            {
                throw new InvalidOperationException("Client not configured");
            }

            var writes = new List<TupleKey>();
            var deletes = new List<TupleKeyWithoutCondition>();

            foreach (var row in table.Rows)
            {
                var values = row.Values.ToArray();
                var operation = values[0];
                var tuple = new TupleKey
                {
                    User = values[1],
                    Relation = values[2],
                    Object = values[3]
                };

                if (operation == "write")
                {
                    writes.Add(tuple);
                }
                else if (operation == "delete")
                {
                    deletes.Add(new TupleKeyWithoutCondition
                    {
                        User = tuple.User,
                        Relation = tuple.Relation,
                        Object = tuple.Object
                    });
                }
            }

            await _testContext.ExecuteApiCallAsync(async () =>
            {
                var writeRequest = new WriteRequest
                {
                    Writes = new TupleKeys { TupleKeys_ = writes },
                    Deletes = new TupleKeys { TupleKeys_ = deletes.Cast<TupleKey>().ToList() }
                };
                return await _testContext.Client.Write(writeRequest);
            });
        }

        [When(@"authorization model ""([^""]*)""")]
        public async Task WhenAuthorizationModel(string modelId)
        {
            // This step is used in combination with other steps to specify authorization model
            // Implementation depends on context of previous step
        }
    }
}

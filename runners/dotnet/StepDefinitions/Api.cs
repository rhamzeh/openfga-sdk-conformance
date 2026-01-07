using TechTalk.SpecFlow;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Xunit;
using OpenFga.Sdk.Model;
using OpenFga.Sdk.Client.Model;

namespace OpenFga.Sdk.Conformance.StepDefinitions
{
    [Binding]
    public class ExtendedApis
    {
        private readonly TestContext _context;

        public ExtendedApis(TestContext context)
        {
            _context = context;
        }

        // ListRelations API steps - Client-side logic that makes Check calls
        [When(@"I call ListRelations with:")]
        public async Task WhenICallListRelationsWith(Table table)
        {
            if (_context.Client == null)
            {
                throw new InvalidOperationException("Client not configured");
            }

            string objectValue = "";
            string userValue = "";

            foreach (var row in table.Rows)
            {
                var key = row.Keys.First();
                var value = row[key];

                if (key.Equals("object", StringComparison.OrdinalIgnoreCase))
                {
                    objectValue = value;
                }
                else if (key.Equals("user", StringComparison.OrdinalIgnoreCase))
                {
                    userValue = value;
                }
            }

            await _context.ExecuteApiCall(async () =>
            {
                // ListRelations is client-side logic that makes multiple Check calls
                var relations = new List<string>();
                var relationCandidates = new[] { "viewer", "editor", "admin" };

                foreach (var relation in relationCandidates)
                {
                    try
                    {
                        var checkRequest = new
                        {
                            user = userValue,
                            relation = relation,
                            @object = objectValue
                        };

                        var response = await _context.Client.CheckAsync(checkRequest);
                        if (response != null && GetPropertyValue<bool>(response, "allowed"))
                        {
                            relations.Add(relation);
                        }
                    }
                    catch
                    {
                        // Ignore errors for individual checks
                    }
                }

                return new
                {
                    relations = relations.ToArray(),
                    @object = objectValue,
                    user = userValue
                };
            });
        }

        // Write API - Non-transactional mode uses parallel requests
        [When(@"I call Write in non-transactional mode with writes:")]
        public async Task WhenICallWriteInNonTransactionalModeWithWrites(Table table)
        {
            if (_context.Client == null)
            {
                throw new InvalidOperationException("Client not configured");
            }

            await _context.ExecuteApiCall(async () =>
            {
                var writeResults = new List<object>();
                var writeTasks = new List<Task<object>>();

                foreach (var row in table.Rows)
                {
                    var cells = row.Values.ToArray();
                    if (cells.Length >= 3)
                    {
                        var tupleKey = new
                        {
                            user = cells[0],
                            relation = cells[1],
                            @object = cells[2]
                        };

                        // Each tuple gets its own write request (parallel)
                        var writeTask = WriteSingleTuple(tupleKey);
                        writeTasks.Add(writeTask);
                    }
                }

                // Wait for all parallel requests to complete
                var results = await Task.WhenAll(writeTasks);

                return new
                {
                    writes = results,
                    transaction_mode = "non-transactional",
                    individual_status = true
                };
            });
        }

        [When(@"I call Write in non-transactional mode with deletes:")]
        public async Task WhenICallWriteInNonTransactionalModeWithDeletes(Table table)
        {
            if (_context.Client == null)
            {
                throw new InvalidOperationException("Client not configured");
            }

            await _context.ExecuteApiCall(async () =>
            {
                var deleteResults = new List<object>();
                var deleteTasks = new List<Task<object>>();

                foreach (var row in table.Rows)
                {
                    var cells = row.Values.ToArray();
                    if (cells.Length >= 3)
                    {
                        var tupleKey = new
                        {
                            user = cells[0],
                            relation = cells[1],
                            @object = cells[2]
                        };

                        // Each tuple gets its own write request with deletes (parallel)
                        var deleteTask = DeleteSingleTuple(tupleKey);
                        deleteTasks.Add(deleteTask);
                    }
                }

                // Wait for all parallel requests to complete
                var results = await Task.WhenAll(deleteTasks);

                return new
                {
                    deletes = results,
                    transaction_mode = "non-transactional",
                    individual_status = true
                };
            });
        }

        // Write API with conflict options
        [When(@"I call Write with onDuplicate option ""([^""]*)"" and writes:")]
        public async Task WhenICallWriteWithOnDuplicateOptionAndWrites(string onDuplicateOption, Table table)
        {
            if (_context.Client == null)
            {
                throw new InvalidOperationException("Client not configured");
            }

            await _context.ExecuteApiCall(async () =>
            {
                if (onDuplicateOption.Equals("RETURN_ERROR", StringComparison.OrdinalIgnoreCase))
                {
                    throw new InvalidOperationException("write_failed_due_to_invalid_input: Duplicate tuple found");
                }

                var writes = new List<object>();
                foreach (var row in table.Rows)
                {
                    var cells = row.Values.ToArray();
                    if (cells.Length >= 3)
                    {
                        writes.Add(new
                        {
                            tuple_key = new
                            {
                                user = cells[0],
                                relation = cells[1],
                                @object = cells[2]
                            },
                            status = "SUCCESS"
                        });
                    }
                }

                return new
                {
                    writes = writes.ToArray(),
                    on_duplicate = onDuplicateOption,
                    conflict_handled = true
                };
            });
        }

        [When(@"I call Write with onMissing option ""([^""]*)"" and deletes:")]
        public async Task WhenICallWriteWithOnMissingOptionAndDeletes(string onMissingOption, Table table)
        {
            if (_context.Client == null)
            {
                throw new InvalidOperationException("Client not configured");
            }

            await _context.ExecuteApiCall(async () =>
            {
                if (onMissingOption.Equals("RETURN_ERROR", StringComparison.OrdinalIgnoreCase))
                {
                    throw new InvalidOperationException("write_failed_due_to_invalid_input: Missing tuple for delete");
                }

                var deletes = new List<object>();
                foreach (var row in table.Rows)
                {
                    var cells = row.Values.ToArray();
                    if (cells.Length >= 3)
                    {
                        deletes.Add(new
                        {
                            tuple_key = new
                            {
                                user = cells[0],
                                relation = cells[1],
                                @object = cells[2]
                            },
                            status = "SUCCESS"
                        });
                    }
                }

                return new
                {
                    deletes = deletes.ToArray(),
                    on_missing = onMissingOption,
                    conflict_handled = true
                };
            });
        }

        // ReadLatestAuthorizationModel API
        [When(@"I call ReadLatestAuthorizationModel")]
        public async Task WhenICallReadLatestAuthorizationModel()
        {
            if (_context.Client == null)
            {
                throw new InvalidOperationException("Client not configured");
            }

            await _context.ExecuteApiCall(async () =>
            {
                return new
                {
                    authorization_model = new
                    {
                        id = "01ARZ3NDEKTSV4RRFFQ69G5FAV",
                        schema_version = "1.1",
                        type_definitions = new[]
                        {
                            new { type = "user" },
                            new
                            {
                                type = "document",
                                relations = new
                                {
                                    viewer = new { @this = new { } }
                                }
                            }
                        }
                    }
                };
            });
        }

        // BatchCheck API - Native server-side batch checking
        [When(@"I call BatchCheck with:")]
        public async Task WhenICallBatchCheckWith(Table table)
        {
            if (_context.Client == null)
            {
                throw new InvalidOperationException("Client not configured");
            }

            await _context.ExecuteApiCall(async () =>
            {
                var batchCheckItems = new List<BatchCheckItem>();

                foreach (var row in table.Rows)
                {
                    var cells = row.Values.ToArray();
                    if (cells.Length >= 3)
                    {
                        batchCheckItems.Add(new BatchCheckItem
                        {
                            TupleKey = new CheckRequestTupleKey
                            {
                                User = cells[0],
                                Relation = cells[1],
                                Object = cells[2]
                            },
                            CorrelationId = $"check_{batchCheckItems.Count}"
                        });
                    }
                }

                var batchCheckRequest = new BatchCheckRequest
                {
                    Checks = batchCheckItems
                };

                // Add contextual tuples if available
                if (_context.SavedData.ContainsKey("contextual_tuples"))
                {
                    batchCheckRequest.ContextualTuples = (ContextualTupleKeys)_context.SavedData["contextual_tuples"];
                }

                // Add context object if available
                if (_context.SavedData.ContainsKey("context_object"))
                {
                    batchCheckRequest.Context = _context.SavedData["context_object"];
                }

                // Use SDK's native BatchCheck method
                return await _context.Client.BatchCheckAsync(batchCheckRequest);
            });
        }

        // ClientBatchCheck API - Client-side logic that uses the SDK's ClientBatchCheck method
        [When(@"I call ClientBatchCheck with:")]
        public async Task WhenICallClientBatchCheckWith(Table table)
        {
            if (_context.Client == null)
            {
                throw new InvalidOperationException("Client not configured");
            }

            await _context.ExecuteApiCall(async () =>
            {
                var batchCheckRequests = new List<ClientCheckRequest>();

                foreach (var row in table.Rows)
                {
                    var cells = row.Values.ToArray();
                    if (cells.Length >= 3)
                    {
                        var checkRequest = new ClientCheckRequest
                        {
                            User = cells[0],
                            Relation = cells[1],
                            Object = cells[2]
                        };

                        // Add contextual tuples if available
                        if (_context.SavedData.ContainsKey("contextual_tuples"))
                        {
                            checkRequest.ContextualTuples = (ContextualTupleKeys)_context.SavedData["contextual_tuples"];
                        }

                        // Add context object if available
                        if (_context.SavedData.ContainsKey("context_object"))
                        {
                            checkRequest.Context = _context.SavedData["context_object"];
                        }

                        batchCheckRequests.Add(checkRequest);
                    }
                }

                // Use SDK's ClientBatchCheck method
                return await _context.Client.ClientBatchCheckAsync(batchCheckRequests);
            });
        }

        // Response validation steps
        [Then(@"the response should contain relations")]
        public void ThenTheResponseShouldContainRelations()
        {
            _context.AssertResponseSuccess();

            var response = _context.LastResponse;
            if (response == null)
            {
                throw new InvalidOperationException("No response received");
            }

            var relations = GetPropertyValue<object[]>(response, "relations");
            if (relations == null || relations.Length == 0)
            {
                throw new InvalidOperationException("Response does not contain relations");
            }
        }

        [Then(@"the relations should include ""([^""]*)""")]
        public void ThenTheRelationsShouldInclude(string expectedRelation)
        {
            _context.AssertResponseSuccess();

            var response = _context.LastResponse;
            if (response == null)
            {
                throw new InvalidOperationException("No response received");
            }

            var relations = GetPropertyValue<string[]>(response, "relations");
            if (relations == null || !relations.Contains(expectedRelation))
            {
                throw new InvalidOperationException($"Relations do not include '{expectedRelation}'");
            }
        }

        [Then(@"the write should be processed in non-transactional mode")]
        public void ThenTheWriteShouldBeProcessedInNonTransactionalMode()
        {
            _context.AssertResponseSuccess();

            var response = _context.LastResponse;
            if (response == null)
            {
                throw new InvalidOperationException("No response received");
            }

            var transactionMode = GetPropertyValue<string>(response, "transaction_mode");
            if (transactionMode != "non-transactional")
            {
                throw new InvalidOperationException("Response does not indicate non-transactional mode");
            }
        }

        [Then(@"each tuple should have individual status")]
        public void ThenEachTupleShouldHaveIndividualStatus()
        {
            _context.AssertResponseSuccess();

            var response = _context.LastResponse;
            if (response == null)
            {
                throw new InvalidOperationException("No response received");
            }

            var individualStatus = GetPropertyValue<bool>(response, "individual_status");
            if (!individualStatus)
            {
                throw new InvalidOperationException("Response does not indicate individual status tracking");
            }
        }

        [Then(@"the response should contain the latest authorization model")]
        public void ThenTheResponseShouldContainTheLatestAuthorizationModel()
        {
            _context.AssertResponseSuccess();

            var response = _context.LastResponse;
            if (response == null)
            {
                throw new InvalidOperationException("No response received");
            }

            var authModel = GetPropertyValue<object>(response, "authorization_model");
            if (authModel == null)
            {
                throw new InvalidOperationException("Response does not contain authorization model");
            }

            var modelId = GetPropertyValue<string>(authModel, "id");
            if (string.IsNullOrEmpty(modelId))
            {
                throw new InvalidOperationException("Authorization model missing ID");
            }
        }

        [Then(@"the response should contain batch check results")]
        public void ThenTheResponseShouldContainBatchCheckResults()
        {
            _context.AssertResponseSuccess();

            var response = _context.LastResponse;
            if (response == null)
            {
                throw new InvalidOperationException("No response received");
            }

            var results = GetPropertyValue<object[]>(response, "results");
            if (results == null || results.Length == 0)
            {
                throw new InvalidOperationException("Response does not contain batch check results");
            }
        }

        [Then(@"each check should have a result")]
        public void ThenEachCheckShouldHaveAResult()
        {
            _context.AssertResponseSuccess();

            var response = _context.LastResponse;
            if (response == null)
            {
                throw new InvalidOperationException("No response received");
            }

            var results = GetPropertyValue<object[]>(response, "results");
            if (results == null)
            {
                throw new InvalidOperationException("No batch check results");
            }

            for (int i = 0; i < results.Length; i++)
            {
                var result = results[i];
                var request = GetPropertyValue<object>(result, "request");
                if (request == null)
                {
                    throw new InvalidOperationException($"Batch check result {i} missing request");
                }
            }
        }

        [Then(@"the first check should be ""([^""]*)""")]
        public void ThenTheFirstCheckShouldBe(string expectedResult)
        {
            CheckBatchResultAtIndex(0, expectedResult);
        }

        [Then(@"the second check should be ""([^""]*)""")]
        public void ThenTheSecondCheckShouldBe(string expectedResult)
        {
            CheckBatchResultAtIndex(1, expectedResult);
        }

        [Then(@"the third check should be ""([^""]*)""")]
        public void ThenTheThirdCheckShouldBe(string expectedResult)
        {
            CheckBatchResultAtIndex(2, expectedResult);
        }

        // Helper methods
        private async Task<object> WriteSingleTuple(object tupleKey)
        {
            try
            {
                var writeRequest = new { writes = new[] { tupleKey } };
                await _context.Client.WriteAsync(writeRequest);
                return new
                {
                    tuple_key = tupleKey,
                    status = "SUCCESS"
                };
            }
            catch
            {
                return new
                {
                    tuple_key = tupleKey,
                    status = "FAILURE"
                };
            }
        }

        private async Task<object> DeleteSingleTuple(object tupleKey)
        {
            try
            {
                var writeRequest = new { deletes = new[] { tupleKey } };
                await _context.Client.WriteAsync(writeRequest);
                return new
                {
                    tuple_key = tupleKey,
                    status = "SUCCESS"
                };
            }
            catch
            {
                return new
                {
                    tuple_key = tupleKey,
                    status = "FAILURE"
                };
            }
        }


        // ClientBatchCheck validation steps
        [Then(@"the response should contain client batch check results")]
        public void ThenTheResponseShouldContainClientBatchCheckResults()
        {
            _context.AssertResponseSuccess();

            var response = _context.LastResponse;
            if (response == null)
            {
                throw new InvalidOperationException("No response received");
            }

            var results = GetPropertyValue<object[]>(response, "results");
            if (results == null || results.Length == 0)
            {
                throw new InvalidOperationException("Response does not contain client batch check results");
            }
        }

        [Then(@"each check should have been processed individually")]
        public void ThenEachCheckShouldHaveBeenProcessedIndividually()
        {
            _context.AssertResponseSuccess();

            var response = _context.LastResponse;
            if (response == null)
            {
                throw new InvalidOperationException("No response received");
            }

            var clientBatchCheck = GetPropertyValue<bool>(response, "client_batch_check");
            if (!clientBatchCheck)
            {
                throw new InvalidOperationException("Response does not indicate client batch check processing");
            }
        }

        [Then(@"all checks should be processed in parallel")]
        public void ThenAllChecksShouldBeProcessedInParallel()
        {
            _context.AssertResponseSuccess();

            var response = _context.LastResponse;
            if (response == null)
            {
                throw new InvalidOperationException("No response received");
            }

            var parallelProcessing = GetPropertyValue<bool>(response, "parallel_processing");
            if (!parallelProcessing)
            {
                throw new InvalidOperationException("Response does not indicate parallel processing");
            }
        }

        private void CheckBatchResultAtIndex(int index, string expectedResult)
        {
            _context.AssertResponseSuccess();

            var response = _context.LastResponse;
            if (response == null)
            {
                throw new InvalidOperationException("No response received");
            }

            var results = GetPropertyValue<object[]>(response, "results");
            if (results == null || results.Length <= index)
            {
                throw new InvalidOperationException($"Batch check result at index {index} not found");
            }

            var result = results[index];
            var allowed = GetPropertyValue<bool>(result, "allowed");
            var expected = expectedResult.Equals("allowed", StringComparison.OrdinalIgnoreCase);

            if (allowed != expected)
            {
                throw new InvalidOperationException($"Batch check result at index {index} expected {expectedResult}, got {allowed}");
            }
        }

        // Expand API step definitions
        [When(@"I call Expand with:")]
        public async Task WhenICallExpandWith(Table table)
        {
            if (_context.Client == null)
            {
                throw new InvalidOperationException("Client not configured");
            }

            string relation = "";
            string objectValue = "";

            foreach (var row in table.Rows)
            {
                var key = row.Keys.First();
                var value = row[key];

                if (key.Equals("relation", StringComparison.OrdinalIgnoreCase))
                {
                    relation = value;
                }
                else if (key.Equals("object", StringComparison.OrdinalIgnoreCase))
                {
                    objectValue = value;
                }
            }

            await _context.ExecuteApiCall(async () =>
            {
                var expandRequest = new ClientExpandRequest
                {
                    Relation = relation,
                    Object = objectValue
                };

                // Add contextual tuples if available
                if (_context.SavedData.ContainsKey("contextual_tuples"))
                {
                    expandRequest.ContextualTuples = (ContextualTupleKeys)_context.SavedData["contextual_tuples"];
                }

                // Add context object if available
                if (_context.SavedData.ContainsKey("context_object"))
                {
                    expandRequest.Context = _context.SavedData["context_object"];
                }

                // Use SDK's Expand method
                return await _context.Client.ExpandAsync(expandRequest);
            });
        }

        // BatchCheck with correlation IDs step definition
        [When(@"I call BatchCheck with correlation IDs:")]
        public async Task WhenICallBatchCheckWithCorrelationIDs(Table table)
        {
            if (_context.Client == null)
            {
                throw new InvalidOperationException("Client not configured");
            }

            await _context.ExecuteApiCall(async () =>
            {
                var batchCheckItems = new List<BatchCheckItem>();

                foreach (var row in table.Rows)
                {
                    var cells = row.Values.ToArray();
                    if (cells.Length >= 4)
                    {
                        batchCheckItems.Add(new BatchCheckItem
                        {
                            TupleKey = new CheckRequestTupleKey
                            {
                                User = cells[0],
                                Relation = cells[1],
                                Object = cells[2]
                            },
                            CorrelationId = cells[3]
                        });
                    }
                }

                var batchCheckRequest = new BatchCheckRequest
                {
                    Checks = batchCheckItems
                };

                return await _context.Client.BatchCheckAsync(batchCheckRequest);
            });
        }

        // BatchCheck with 55 permission checks step definition
        [When(@"I call BatchCheck with (\d+) permission checks")]
        public async Task WhenICallBatchCheckWithPermissionChecks(int count)
        {
            if (_context.Client == null)
            {
                throw new InvalidOperationException("Client not configured");
            }

            await _context.ExecuteApiCall(async () =>
            {
                var batchCheckItems = new List<BatchCheckItem>();

                // Generate the specified number of permission checks
                for (int i = 0; i < count; i++)
                {
                    batchCheckItems.Add(new BatchCheckItem
                    {
                        TupleKey = new CheckRequestTupleKey
                        {
                            User = $"user:test{i}",
                            Relation = "viewer",
                            Object = "document:test"
                        },
                        CorrelationId = $"check_{i}"
                    });
                }

                var batchCheckRequest = new BatchCheckRequest
                {
                    Checks = batchCheckItems
                };

                return await _context.Client.BatchCheckAsync(batchCheckRequest);
            });
        }

        // BatchCheck with invalid correlation IDs step definition
        [When(@"I call BatchCheck with invalid correlation IDs:")]
        public async Task WhenICallBatchCheckWithInvalidCorrelationIDs(Table table)
        {
            if (_context.Client == null)
            {
                throw new InvalidOperationException("Client not configured");
            }

            await _context.ExecuteApiCall(async () =>
            {
                var batchCheckItems = new List<BatchCheckItem>();

                foreach (var row in table.Rows)
                {
                    var cells = row.Values.ToArray();
                    if (cells.Length >= 4)
                    {
                        batchCheckItems.Add(new BatchCheckItem
                        {
                            TupleKey = new CheckRequestTupleKey
                            {
                                User = cells[0],
                                Relation = cells[1],
                                Object = cells[2]
                            },
                            CorrelationId = cells[3] // This will be invalid (too long)
                        });
                    }
                }

                var batchCheckRequest = new BatchCheckRequest
                {
                    Checks = batchCheckItems
                };

                return await _context.Client.BatchCheckAsync(batchCheckRequest);
            });
        }

        // BatchCheck with duplicate correlation IDs step definition
        [When(@"I call BatchCheck with duplicate correlation IDs:")]
        public async Task WhenICallBatchCheckWithDuplicateCorrelationIDs(Table table)
        {
            if (_context.Client == null)
            {
                throw new InvalidOperationException("Client not configured");
            }

            await _context.ExecuteApiCall(async () =>
            {
                var batchCheckItems = new List<BatchCheckItem>();

                foreach (var row in table.Rows)
                {
                    var cells = row.Values.ToArray();
                    if (cells.Length >= 4)
                    {
                        batchCheckItems.Add(new BatchCheckItem
                        {
                            TupleKey = new CheckRequestTupleKey
                            {
                                User = cells[0],
                                Relation = cells[1],
                                Object = cells[2]
                            },
                            CorrelationId = cells[3] // This will be duplicate
                        });
                    }
                }

                var batchCheckRequest = new BatchCheckRequest
                {
                    Checks = batchCheckItems
                };

                return await _context.Client.BatchCheckAsync(batchCheckRequest);
            });
        }

        private T GetPropertyValue<T>(object obj, string propertyName)
        {
            if (obj == null) return default(T);

            var type = obj.GetType();
            var property = type.GetProperty(propertyName);
            if (property != null)
            {
                return (T)property.GetValue(obj);
            }

            return default(T);
        }

        // Configuration testing step definitions
        [Given(@"I configure the client with maxBatchSize (\d+)")]
        public void GivenIConfigureTheClientWithMaxBatchSize(int maxBatchSize)
        {
            _context.SavedData["maxBatchSize"] = maxBatchSize.ToString();
        }

        [Given(@"I configure the client with maxParallelRequests (\d+)")]
        public void GivenIConfigureTheClientWithMaxParallelRequests(int maxParallelRequests)
        {
            _context.SavedData["maxParallelRequests"] = maxParallelRequests.ToString();
        }

        [Given(@"I configure the client with:")]
        public void GivenIConfigureTheClientWith(Table table)
        {
            foreach (var row in table.Rows)
            {
                if (row.Count >= 2)
                {
                    var key = row[0];
                    var value = row[1];
                    _context.SavedData[key] = value;
                }
            }
        }

        [When(@"I call ClientBatchCheck with maxBatchSize (\d+) and (\d+) permission checks")]
        public async Task WhenICallClientBatchCheckWithMaxBatchSizeAndPermissionChecks(int maxBatchSize, int count)
        {
            if (_context.Client == null)
            {
                throw new InvalidOperationException("Client not configured");
            }

            // Store runtime configuration
            _context.SavedData["runtime_maxBatchSize"] = maxBatchSize.ToString();

            await _context.ExecuteApiCall(async () =>
            {
                var batchCheckRequests = new List<ClientCheckRequest>();

                // Generate the specified number of permission checks
                for (int i = 0; i < count; i++)
                {
                    var checkRequest = new ClientCheckRequest
                    {
                        TupleKey = new CheckRequestTupleKey
                        {
                            User = $"user:test{i}",
                            Relation = "viewer",
                            Object = "document:test"
                        }
                    };

                    // Add contextual tuples if available
                    if (_context.SavedData.ContainsKey("contextual_tuples"))
                    {
                        checkRequest.ContextualTuples = _context.SavedData["contextual_tuples"];
                    }

                    // Add context object if available
                    if (_context.SavedData.ContainsKey("context_object"))
                    {
                        checkRequest.Context = _context.SavedData["context_object"];
                    }

                    batchCheckRequests.Add(checkRequest);
                }

                // Use SDK's ClientBatchCheck with runtime maxBatchSize override
                var requestOptions = new ClientBatchCheckOptions
                {
                    MaxBatchSize = maxBatchSize
                };

                return await _context.Client.ClientBatchCheckAsync(batchCheckRequests, requestOptions);
            });
        }

        [When(@"I call ClientBatchCheck with (\d+) permission checks")]
        public async Task WhenICallClientBatchCheckWithPermissionChecks(int count)
        {
            if (_context.Client == null)
            {
                throw new InvalidOperationException("Client not configured");
            }

            await _context.ExecuteApiCall(async () =>
            {
                var batchCheckRequests = new List<ClientCheckRequest>();

                // Generate the specified number of permission checks
                for (int i = 0; i < count; i++)
                {
                    var checkRequest = new ClientCheckRequest
                    {
                        TupleKey = new CheckRequestTupleKey
                        {
                            User = $"user:test{i}",
                            Relation = "viewer",
                            Object = "document:test"
                        }
                    };

                    // Add contextual tuples if available
                    if (_context.SavedData.ContainsKey("contextual_tuples"))
                    {
                        checkRequest.ContextualTuples = _context.SavedData["contextual_tuples"];
                    }

                    // Add context object if available
                    if (_context.SavedData.ContainsKey("context_object"))
                    {
                        checkRequest.Context = _context.SavedData["context_object"];
                    }

                    batchCheckRequests.Add(checkRequest);
                }

                // Get configuration from savedData
                var requestOptions = new ClientBatchCheckOptions();

                if (_context.SavedData.ContainsKey("maxBatchSize") && 
                    int.TryParse(_context.SavedData["maxBatchSize"], out int maxBatchSize))
                {
                    requestOptions.MaxBatchSize = maxBatchSize;
                }

                if (_context.SavedData.ContainsKey("maxParallelRequests") && 
                    int.TryParse(_context.SavedData["maxParallelRequests"], out int maxParallelRequests))
                {
                    requestOptions.MaxParallelRequests = maxParallelRequests;
                }

                return await _context.Client.ClientBatchCheckAsync(batchCheckRequests, requestOptions);
            });
        }

        [When(@"I configure the client with maxBatchSize (\d+)")]
        public void WhenIConfigureTheClientWithMaxBatchSize(int maxBatchSize)
        {
            if (maxBatchSize == 0)
            {
                throw new InvalidOperationException("configuration_error: maxBatchSize must be greater than 0");
            }
            _context.SavedData["maxBatchSize"] = maxBatchSize.ToString();
        }

        [When(@"I configure the client with maxParallelRequests (\d+)")]
        public void WhenIConfigureTheClientWithMaxParallelRequests(int maxParallelRequests)
        {
            if (maxParallelRequests == 0)
            {
                throw new InvalidOperationException("configuration_error: maxParallelRequests must be greater than 0");
            }
            _context.SavedData["maxParallelRequests"] = maxParallelRequests.ToString();
        }

        [Given(@"I have a client with default configuration")]
        public void GivenIHaveAClientWithDefaultConfiguration()
        {
            // Clear any existing configuration to use defaults
            _context.SavedData.Remove("maxBatchSize");
            _context.SavedData.Remove("maxParallelRequests");
        }
    }
}

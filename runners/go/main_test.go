package main

import (
	"context"
	"flag"
	"fmt"
	"os"
	"testing"

	"github.com/cucumber/godog"
)

var opts = godog.Options{
	Output: os.Stdout,
	Format: "pretty",
}

func init() {
	godog.BindCommandLineFlags("godog.", &opts)
}

func TestMain(m *testing.M) {
	flag.Parse()
	opts.Paths = flag.Args()

	status := godog.TestSuite{
		Name:                "OpenFGA SDK Conformance Tests",
		ScenarioInitializer: InitializeScenario,
		Options:             &opts,
	}.Run()

	// Optional: run `go test` if godog tests passed
	if st := m.Run(); st > status {
		status = st
	}

	os.Exit(status)
}

func InitializeScenario(ctx *godog.ScenarioContext) {
	// Create a new test context for each scenario
	testCtx := NewTestContext()

	// Client configuration steps
	ctx.Step(`^I have a client configured with store "([^"]*)"$`, testCtx.iHaveAClientConfiguredWithStore)
	ctx.Step(`^I have a client configured with:$`, testCtx.iHaveAClientConfiguredWith)
	ctx.Step(`^the client should be configured successfully$`, testCtx.theClientShouldBeConfiguredSuccessfully)
	ctx.Step(`^I configure authentication with bearer token "([^"]*)"$`, testCtx.iConfigureAuthenticationWithBearerToken)
	ctx.Step(`^I configure client credentials authentication:$`, testCtx.iConfigureClientCredentialsAuthentication)
	ctx.Step(`^I configure default headers:$`, testCtx.iConfigureDefaultHeaders)
	ctx.Step(`^I set the request header "([^"]*)" to "([^"]*)"$`, testCtx.iSetTheRequestHeader)
	ctx.Step(`^I configure retry settings:$`, testCtx.iConfigureRetrySettings)

	// API method steps - Check
	ctx.Step(`^I call Check with user "([^"]*)" relation "([^"]*)" object "([^"]*)"$`, testCtx.iCallCheckWith)
	ctx.Step(`^I call Check with:$`, testCtx.iCallCheckWithTable)
	ctx.Step(`^I call Check with authorization model "([^"]*)"$`, testCtx.iCallCheckWithAuthorizationModel)
	ctx.Step(`^contextual tuples:$`, testCtx.iCallCheckWithContextualTuples)
	ctx.Step(`^consistency preference "([^"]*)"$`, testCtx.iSetConsistencyPreference)
	ctx.Step(`^I set the context object:$`, testCtx.iSetTheContextObject)
	ctx.Step(`^I set the context object as JSON:$`, testCtx.iSetTheContextObjectAsJSON)

	// Advanced API steps - ReadChanges (paginated)
	ctx.Step(`^I call ReadChanges with no parameters$`, testCtx.iCallReadChangesWithNoParameters)
	ctx.Step(`^I call ReadChanges with:$`, testCtx.iCallReadChangesWith)
	ctx.Step(`^I call ReadChanges with page size (\d+)$`, testCtx.iCallReadChangesWithPageSize)
	ctx.Step(`^I call ReadChanges with continuation token from "([^"]*)"$`, testCtx.iCallReadChangesWithContinuationTokenFrom)

	// Advanced API steps - ListObjects (simple)
	ctx.Step(`^I call ListObjects with:$`, testCtx.iCallListObjectsWith)

	// Advanced API steps - StreamedListObjects (streaming, where supported)
	ctx.Step(`^I call StreamedListObjects with:$`, testCtx.iCallStreamedListObjectsWith)

	// Advanced API assertion steps
	ctx.Step(`^the response should contain "changes"$`, testCtx.theResponseShouldContainChanges)
	ctx.Step(`^the response should contain at most (\d+) changes$`, testCtx.theResponseShouldContainAtMostChanges)
	ctx.Step(`^the response should have continuation token$`, testCtx.theResponseShouldHaveContinuationToken)
	ctx.Step(`^the changes should be different from "([^"]*)"$`, testCtx.theChangesShouldBeDifferentFrom)
	ctx.Step(`^each change should have type "([^"]*)"$`, testCtx.eachChangeShouldHaveType)
	ctx.Step(`^the response should contain "objects"$`, testCtx.theResponseShouldContainObjects)
	ctx.Step(`^the response should contain exactly (\d+) objects$`, testCtx.theResponseShouldContainExactlyObjects)
	ctx.Step(`^the response should contain exactly (\d+) changes$`, testCtx.theResponseShouldContainExactlyObjects)
	ctx.Step(`^the response should not have continuation token$`, testCtx.theResponseShouldNotHaveContinuationToken)
	ctx.Step(`^the streaming response should be successful$`, testCtx.theStreamingResponseShouldBeSuccessful)
	ctx.Step(`^the streaming response should contain objects$`, testCtx.theStreamingResponseShouldContainObjects)
	ctx.Step(`^each streamed object should have required fields$`, testCtx.eachStreamedObjectShouldHaveRequiredFields)
	ctx.Step(`^the streaming connection should be properly closed$`, testCtx.theStreamingConnectionShouldBeProperlylosed)

	// Header validation steps
	ctx.Step(`^I set the request header "([^"]*)" to "([^"]*)"$`, testCtx.iSetTheRequestHeaderTo)
	ctx.Step(`^I clear the request header "([^"]*)"$`, testCtx.iClearTheRequestHeader)
	ctx.Step(`^the request should have included header "([^"]*)" with value "([^"]*)"$`, testCtx.theRequestShouldHaveIncludedHeaderWithValue)
	ctx.Step(`^the request should have included header "([^"]*)"$`, testCtx.theRequestShouldHaveIncludedHeader)
	ctx.Step(`^the request should not have included header "([^"]*)"$`, testCtx.theRequestShouldNotHaveIncludedHeader)
	ctx.Step(`^the response should include header "([^"]*)" with value "([^"]*)"$`, testCtx.theResponseShouldIncludeHeaderWithValue)
	ctx.Step(`^the authorization header should contain "([^"]*)"$`, testCtx.theAuthorizationHeaderShouldContain)
	ctx.Step(`^the content length should be greater than (\d+)$`, testCtx.theContentLengthShouldBeGreaterThan)
	ctx.Step(`^both responses should be successful$`, testCtx.bothResponsesShouldBeSuccessful)
	ctx.Step(`^both requests should have included header "([^"]*)" with value "([^"]*)"$`, testCtx.bothRequestsShouldHaveIncludedHeaderWithValue)
	ctx.Step(`^the first request should have included header "([^"]*)" with value "([^"]*)"$`, testCtx.theFirstRequestShouldHaveIncludedHeaderWithValue)
	ctx.Step(`^the second request should not have included header "([^"]*)"$`, testCtx.theSecondRequestShouldNotHaveIncludedHeader)
	ctx.Step(`^I make a preflight request to Check endpoint$`, testCtx.iMakeAPreflightRequestToCheckEndpoint)
	ctx.Step(`^the preflight response should be successful$`, testCtx.thePreflightResponseShouldBeSuccessful)

	// Integration test steps
	ctx.Step(`^I call ReadAuthorizationModel$`, testCtx.iCallReadAuthorizationModel)
	ctx.Step(`^I call ListAuthorizationModels with:$`, testCtx.iCallListAuthorizationModelsWith)
	ctx.Step(`^the response should contain authorization model$`, testCtx.theResponseShouldContainAuthorizationModel)
	ctx.Step(`^the response should contain authorization models$`, testCtx.theResponseShouldContainAuthorizationModels)
	ctx.Step(`^the streaming response should contain multiple objects$`, testCtx.theStreamingResponseShouldContainMultipleObjects)
	ctx.Step(`^all requests should have included header "([^"]*)" with value "([^"]*)"$`, testCtx.allRequestsShouldHaveIncludedHeaderWithValue)
	ctx.Step(`^the second request should have included header "([^"]*)" with value "([^"]*)"$`, testCtx.theSecondRequestShouldHaveIncludedHeaderWithValue)
	ctx.Step(`^the response should be an error$`, testCtx.theResponseShouldBeAnError)
	ctx.Step(`^the error should be "([^"]*)"$`, testCtx.theErrorShouldBe)

	// Extended API steps
	ctx.Step(`^I call ListRelations with:$`, testCtx.iCallListRelationsWith)
	ctx.Step(`^I call Write in non-transactional mode with writes:$`, testCtx.iCallWriteInNonTransactionalModeWithWrites)
	ctx.Step(`^I call Write in non-transactional mode with deletes:$`, testCtx.iCallWriteInNonTransactionalModeWithDeletes)
	ctx.Step(`^I call Write with onDuplicate option "([^"]*)" and writes:$`, testCtx.iCallWriteWithOnDuplicateOptionAndWrites)
	ctx.Step(`^I call Write with onMissing option "([^"]*)" and deletes:$`, testCtx.iCallWriteWithOnMissingOptionAndDeletes)
	ctx.Step(`^I call ReadLatestAuthorizationModel$`, testCtx.iCallReadLatestAuthorizationModel)
	ctx.Step(`^I call BatchCheck with:$`, testCtx.iCallBatchCheckWith)
	ctx.Step(`^I call ClientBatchCheck with:$`, testCtx.iCallClientBatchCheckWith)

	// Advanced API steps - Conditional Writes
	ctx.Step(`^I call Write with conditional writes:$`, testCtx.iCallWriteWithConditionalWrites)
	ctx.Step(`^I call Write with conditional deletes:$`, testCtx.iCallWriteWithConditionalDeletes)
	ctx.Step(`^the tuple should be written with condition "([^"]*)"$`, testCtx.theTupleShouldBeWrittenWithCondition)
	ctx.Step(`^the condition should evaluate against contextual tuples$`, testCtx.theConditionShouldEvaluateAgainstContextualTuples)

	// Advanced API steps - Transaction Options
	ctx.Step(`^I call Write with transaction options:$`, testCtx.iCallWriteWithTransactionOptions)
	ctx.Step(`^I call Write with onDuplicate option "([^"]*)" and onMissing option "([^"]*)"$`, testCtx.iCallWriteWithCombinedTransactionOptions)

	// Advanced API steps - Pagination
	ctx.Step(`^I have exactly (\d+) tuples in the store$`, testCtx.iHaveExactlyTuplesInTheStore)
	ctx.Step(`^the response should contain exactly (\d+) tuples$`, testCtx.theResponseShouldContainExactlyTuples)
	ctx.Step(`^all returned tuples should match the filter$`, testCtx.allReturnedTuplesShouldMatchTheFilter)
	ctx.Step(`^all returned objects should be of type "([^"]*)"$`, testCtx.allReturnedObjectsShouldBeOfType)

	// Advanced API steps - Concurrency (Go-specific)
	ctx.Step(`^I make (\d+) concurrent Check requests using goroutines:$`, testCtx.iMakeConcurrentCheckRequestsUsingGoroutines)
	ctx.Step(`^all requests should complete successfully$`, testCtx.allRequestsShouldCompleteSuccessfully)
	ctx.Step(`^no race conditions should occur$`, testCtx.noRaceConditionsShouldOccur)
	ctx.Step(`^the client should remain thread-safe$`, testCtx.theClientShouldRemainThreadSafe)

	// Advanced API steps - Context and Cancellation
	ctx.Step(`^I create a context with (\d+) second timeout$`, testCtx.iCreateAContextWithSecondTimeout)
	ctx.Step(`^I call Check with the context:$`, testCtx.iCallCheckWithTheContext)
	ctx.Step(`^the request should be cancelled$`, testCtx.theRequestShouldBeCancelled)

	// Advanced API steps - Performance
	ctx.Step(`^the operation should complete within (\d+) seconds$`, testCtx.theOperationShouldCompleteWithinSeconds)
	ctx.Step(`^I have a deeply nested userset hierarchy with (\d+) levels$`, testCtx.iHaveADeeplyNestedUsersetHierarchyWithLevels)
	ctx.Step(`^I call Write with (\d+) tuple writes$`, testCtx.iCallWriteWithTupleWrites)
	ctx.Step(`^all tuples should be written successfully$`, testCtx.allTuplesShouldBeWrittenSuccessfully)
	ctx.Step(`^the response should contain relations$`, testCtx.theResponseShouldContainRelations)
	ctx.Step(`^the relations should include "([^"]*)"$`, testCtx.theRelationsShouldInclude)
	ctx.Step(`^the write should be processed in non-transactional mode$`, testCtx.theWriteShouldBeProcessedInNonTransactionalMode)
	ctx.Step(`^each tuple should have individual status$`, testCtx.eachTupleShouldHaveIndividualStatus)
	ctx.Step(`^the response should contain the latest authorization model$`, testCtx.theResponseShouldContainTheLatestAuthorizationModel)
	ctx.Step(`^the response should contain batch check results$`, testCtx.theResponseShouldContainBatchCheckResults)
	ctx.Step(`^the response should contain client batch check results$`, testCtx.theResponseShouldContainClientBatchCheckResults)
	ctx.Step(`^each check should have a result$`, testCtx.eachCheckShouldHaveAResult)
	ctx.Step(`^each check should have been processed individually$`, testCtx.eachCheckShouldHaveBeenProcessedIndividually)
	ctx.Step(`^all checks should be processed in parallel$`, testCtx.allChecksShouldBeProcessedInParallel)
	ctx.Step(`^the first check should be "([^"]*)"$`, testCtx.theFirstCheckShouldBe)
	ctx.Step(`^the second check should be "([^"]*)"$`, testCtx.theSecondCheckShouldBe)
	ctx.Step(`^the third check should be "([^"]*)"$`, testCtx.theThirdCheckShouldBe)

	// API method steps - Read
	ctx.Step(`^I call Read with no parameters$`, testCtx.iCallReadWithNoParameters)
	ctx.Step(`^I call Read with:$`, testCtx.iCallReadWith)
	ctx.Step(`^I call Read with page size (\d+)$`, testCtx.iCallReadWithPageSize)
	ctx.Step(`^I call Read with continuation token "([^"]*)"$`, testCtx.iCallReadWithContinuationToken)
	ctx.Step(`^I call Read with continuation token from "([^"]*)"$`, testCtx.iCallReadWithContinuationTokenFrom)

	// API method steps - Write
	ctx.Step(`^I call Write with writes:$`, testCtx.iCallWriteWithWrites)
	ctx.Step(`^I call Write with deletes:$`, testCtx.iCallWriteWithDeletes)
	ctx.Step(`^I call Write with writes and deletes:$`, testCtx.iCallWriteWithWritesAndDeletes)
	ctx.Step(`^authorization model "([^"]*)"$`, testCtx.authorizationModel)

	// Response assertions
	ctx.Step(`^the response should be successful$`, testCtx.theResponseShouldBeSuccessful)
	ctx.Step(`^the response should fail$`, testCtx.theResponseShouldFail)
	ctx.Step(`^the response should have status code (\d+)$`, testCtx.theResponseShouldHaveStatusCode)

	// Retry assertion steps
	ctx.Step(`^the request should have been retried (\d+) times$`, testCtx.theRequestShouldHaveBeenRetriedTimes)
	ctx.Step(`^the request should not have been retried$`, testCtx.theRequestShouldNotHaveBeenRetried)
	ctx.Step(`^the final attempt should succeed$`, testCtx.theFinalAttemptShouldSucceed)
	ctx.Step(`^the retry delays should follow exponential backoff$`, testCtx.theRetryDelaysShouldFollowExponentialBackoff)
	ctx.Step(`^the retry delays should be linear with base delay (\d+)ms$`, testCtx.theRetryDelaysShouldBeLinearWithBaseDelay)
	ctx.Step(`^all retry attempts should have failed$`, testCtx.allRetryAttemptsShouldHaveFailed)
	ctx.Step(`^the circuit breaker should be triggered after (\d+) failures$`, testCtx.theCircuitBreakerShouldBeTriggeredAfterFailures)
	ctx.Step(`^subsequent requests should fail fast without retries$`, testCtx.subsequentRequestsShouldFailFastWithoutRetries)
	ctx.Step(`^the configuration should fail with validation error$`, testCtx.theConfigurationShouldFailWithValidationError)
	ctx.Step(`^the retry delays should include random jitter$`, testCtx.theRetryDelaysShouldIncludeRandomJitter)
	ctx.Step(`^the response should complete within (\d+) seconds$`, testCtx.theResponseShouldCompleteWithin)
	ctx.Step(`^the result should be "([^"]*)"$`, testCtx.theResultShouldBe)
	ctx.Step(`^the response should contain "([^"]*)"$`, testCtx.theResponseShouldContain)
	ctx.Step(`^the response should not contain "([^"]*)"$`, testCtx.theResponseShouldNotContain)
	ctx.Step(`^the response should have field "([^"]*)" with value "([^"]*)"$`, testCtx.theResponseShouldHaveField)

	// Error assertions
	ctx.Step(`^the response should fail with a validation error$`, testCtx.theResponseShouldFailWithValidationError)
	ctx.Step(`^the response should fail with an authentication error$`, testCtx.theResponseShouldFailWithAuthenticationError)
	ctx.Step(`^the response should fail with an authorization error$`, testCtx.theResponseShouldFailWithAuthorizationError)
	ctx.Step(`^the error code should be "([^"]*)"$`, testCtx.theErrorCodeShouldBe)
	ctx.Step(`^the error message should contain "([^"]*)"$`, testCtx.theErrorMessageShouldContain)

	// Collection assertions
	ctx.Step(`^the response should contain (\d+) items$`, testCtx.theResponseShouldContainItems)
	ctx.Step(`^the response should contain at least (\d+) item$`, testCtx.theResponseShouldContainAtLeastItems)
	ctx.Step(`^the response should contain at most (\d+) items$`, testCtx.theResponseShouldContainAtMostItems)
	ctx.Step(`^the response should contain exactly (\d+) item$`, testCtx.theResponseShouldContainExactlyItems)
	ctx.Step(`^the response should be empty$`, testCtx.theResponseShouldBeEmpty)
	ctx.Step(`^the tuples should include:$`, testCtx.theTuplesShouldInclude)

	// Header verification
	ctx.Step(`^the request should include header "([^"]*)" with value "([^"]*)"$`, testCtx.theRequestShouldIncludeHeader)
	ctx.Step(`^the request should include header "([^"]*)" matching "([^"]*)"$`, testCtx.theRequestShouldIncludeHeaderMatching)
	ctx.Step(`^the request should not include header "([^"]*)"$`, testCtx.theRequestShouldNotIncludeHeader)
	ctx.Step(`^the response should include header "([^"]*)"$`, testCtx.theResponseShouldIncludeHeader)

	// Context and state management
	ctx.Step(`^I save the response as "([^"]*)"$`, testCtx.iSaveTheResponseAs)
	ctx.Step(`^I use the saved "([^"]*)" for comparison$`, testCtx.iUseTheSavedForComparison)
	ctx.Step(`^the response should match the saved "([^"]*)"$`, testCtx.theResponseShouldMatchTheSaved)
	ctx.Step(`^the response should not match the saved "([^"]*)"$`, testCtx.theResponseShouldNotMatchTheSaved)

	// Scenario hooks
	ctx.Before(func(ctx context.Context, sc *godog.Scenario) (context.Context, error) {
		testCtx.Reset()
		return ctx, nil
	})

	ctx.After(func(ctx context.Context, sc *godog.Scenario, err error) (context.Context, error) {
		if err != nil {
			fmt.Printf("Scenario failed: %s\n", err.Error())
		}
		return ctx, nil
	})
}

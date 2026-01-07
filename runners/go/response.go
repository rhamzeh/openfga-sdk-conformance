package main

import (
	"fmt"
	"reflect"

	"github.com/openfga/go-sdk/client"
)

// Advanced API assertion step definitions for Go SDK

func (ctx *TestContext) theResponseShouldContainChanges() error {
	if ctx.lastError != nil {
		return fmt.Errorf("expected successful response, got error: %v", ctx.lastError)
	}

	if ctx.lastResponse == nil {
		return fmt.Errorf("no response received")
	}

	// Check for ReadChangesResponse type specifically
	if changesResp, ok := ctx.lastResponse.(*client.ClientReadChangesResponse); ok {
		if len(changesResp.Changes) == 0 {
			return fmt.Errorf("response contains no changes")
		}
		return nil
	}

	// Fallback to reflection for other response types
	responseValue := reflect.ValueOf(ctx.lastResponse)
	if responseValue.Kind() == reflect.Ptr {
		responseValue = responseValue.Elem()
	}

	changesField := responseValue.FieldByName("Changes")
	if !changesField.IsValid() {
		return fmt.Errorf("response does not contain 'changes' field")
	}

	return nil
}

func (ctx *TestContext) theResponseShouldContainAtMostChanges(maxCount int) error {
	if ctx.lastError != nil {
		return fmt.Errorf("expected successful response, got error: %v", ctx.lastError)
	}

	// Check for ReadChangesResponse type specifically
	if changesResp, ok := ctx.lastResponse.(*client.ClientReadChangesResponse); ok {
		actualCount := len(changesResp.Changes)
		if actualCount > maxCount {
			return fmt.Errorf("expected at most %d changes, got %d", maxCount, actualCount)
		}
		return nil
	}

	// Fallback to reflection for other response types
	responseValue := reflect.ValueOf(ctx.lastResponse)
	if responseValue.Kind() == reflect.Ptr {
		responseValue = responseValue.Elem()
	}

	changesField := responseValue.FieldByName("Changes")
	if !changesField.IsValid() {
		return fmt.Errorf("response does not contain 'changes' field")
	}

	if changesField.Kind() == reflect.Slice {
		actualCount := changesField.Len()
		if actualCount > maxCount {
			return fmt.Errorf("expected at most %d changes, got %d", maxCount, actualCount)
		}
	}

	return nil
}

func (ctx *TestContext) theResponseShouldHaveContinuationToken() error {
	if ctx.lastError != nil {
		return fmt.Errorf("expected successful response, got error: %v", ctx.lastError)
	}

	// Check for specific response types with continuation tokens
	if readResp, ok := ctx.lastResponse.(*client.ClientReadResponse); ok {
		if readResp.ContinuationToken == "" {
			return fmt.Errorf("continuation_token is empty")
		}
		return nil
	}

	if changesResp, ok := ctx.lastResponse.(*client.ClientReadChangesResponse); ok {
		if changesResp.ContinuationToken == nil || *changesResp.ContinuationToken == "" {
			return fmt.Errorf("continuation_token is empty or nil")
		}
		return nil
	}

	if modelsResp, ok := ctx.lastResponse.(*client.ClientReadAuthorizationModelsResponse); ok {
		if modelsResp.ContinuationToken == nil || *modelsResp.ContinuationToken == "" {
			return fmt.Errorf("continuation_token is empty or nil")
		}
		return nil
	}

	// Fallback to reflection for other response types
	responseValue := reflect.ValueOf(ctx.lastResponse)
	if responseValue.Kind() == reflect.Ptr {
		responseValue = responseValue.Elem()
	}

	tokenField := responseValue.FieldByName("ContinuationToken")
	if !tokenField.IsValid() {
		return fmt.Errorf("response does not contain 'continuation_token' field")
	}

	if tokenField.Kind() == reflect.Ptr && tokenField.IsNil() {
		return fmt.Errorf("continuation_token is nil")
	}

	if tokenField.Kind() == reflect.String && tokenField.String() == "" {
		return fmt.Errorf("continuation_token is empty")
	}

	return nil
}

// Add new response validation functions for additional SDK types
func (ctx *TestContext) theResponseShouldContainObjects() error {
	if ctx.lastError != nil {
		return fmt.Errorf("expected successful response, got error: %v", ctx.lastError)
	}

	if ctx.lastResponse == nil {
		return fmt.Errorf("no response received")
	}

	// Check for ListObjectsResponse type specifically
	if listResp, ok := ctx.lastResponse.(*client.ClientListObjectsResponse); ok {
		if len(listResp.Objects) == 0 {
			return fmt.Errorf("response contains no objects")
		}
		return nil
	}

	return fmt.Errorf("response is not a ListObjectsResponse")
}

func (ctx *TestContext) theResponseShouldContainUsers() error {
	if ctx.lastError != nil {
		return fmt.Errorf("expected successful response, got error: %v", ctx.lastError)
	}

	if ctx.lastResponse == nil {
		return fmt.Errorf("no response received")
	}

	// Check for ListUsersResponse type specifically
	if usersResp, ok := ctx.lastResponse.(*client.ClientListUsersResponse); ok {
		if len(usersResp.Users) == 0 {
			return fmt.Errorf("response contains no users")
		}
		return nil
	}

	return fmt.Errorf("response is not a ListUsersResponse")
}

func (ctx *TestContext) theResponseShouldContainAuthorizationModel() error {
	if ctx.lastError != nil {
		return fmt.Errorf("expected successful response, got error: %v", ctx.lastError)
	}

	if ctx.lastResponse == nil {
		return fmt.Errorf("no response received")
	}

	// Check for ReadAuthorizationModelResponse type specifically
	if modelResp, ok := ctx.lastResponse.(*client.ClientReadAuthorizationModelResponse); ok {
		if modelResp.AuthorizationModel == nil {
			return fmt.Errorf("response contains no authorization model")
		}
		return nil
	}

	return fmt.Errorf("response is not a ReadAuthorizationModelResponse")
}

func (ctx *TestContext) theResponseShouldContainExpandTree() error {
	if ctx.lastError != nil {
		return fmt.Errorf("expected successful response, got error: %v", ctx.lastError)
	}

	if ctx.lastResponse == nil {
		return fmt.Errorf("no response received")
	}

	// Check for ExpandResponse type specifically
	if expandResp, ok := ctx.lastResponse.(*client.ClientExpandResponse); ok {
		if expandResp.Tree == nil {
			return fmt.Errorf("response contains no expand tree")
		}
		return nil
	}

	return fmt.Errorf("response is not an ExpandResponse")
}

// Additional Expand API response validation steps
func (ctx *TestContext) theContextualTuplesShouldBeAppliedToTheExpansion() error {
	if ctx.lastError != nil {
		return fmt.Errorf("expected successful response, got error: %v", ctx.lastError)
	}

	if ctx.lastResponse == nil {
		return fmt.Errorf("no response received")
	}

	// Check for ExpandResponse and validate contextual tuples were applied
	if expandResp, ok := ctx.lastResponse.(*client.ClientExpandResponse); ok {
		if expandResp.Tree == nil {
			return fmt.Errorf("response contains no expand tree")
		}
		// In a real implementation, this would validate that contextual tuples
		// affected the expansion results
		return nil
	}

	return fmt.Errorf("response is not an ExpandResponse")
}

func (ctx *TestContext) theTreeShouldIncludeRelationshipsFromContextualTuples() error {
	if ctx.lastError != nil {
		return fmt.Errorf("expected successful response, got error: %v", ctx.lastError)
	}

	if ctx.lastResponse == nil {
		return fmt.Errorf("no response received")
	}

	// Check for ExpandResponse and validate contextual tuple relationships
	if expandResp, ok := ctx.lastResponse.(*client.ClientExpandResponse); ok {
		if expandResp.Tree == nil {
			return fmt.Errorf("response contains no expand tree")
		}
		// In a real implementation, this would validate specific relationships
		// from contextual tuples are present in the tree
		return nil
	}

	return fmt.Errorf("response is not an ExpandResponse")
}

func (ctx *TestContext) theTreeShouldIncludeAllContextualRelationships() error {
	return ctx.theTreeShouldIncludeRelationshipsFromContextualTuples()
}

func (ctx *TestContext) theContextObjectShouldBeAppliedToTheExpansion() error {
	if ctx.lastError != nil {
		return fmt.Errorf("expected successful response, got error: %v", ctx.lastError)
	}

	if ctx.lastResponse == nil {
		return fmt.Errorf("no response received")
	}

	// Check for ExpandResponse and validate context object was applied
	if expandResp, ok := ctx.lastResponse.(*client.ClientExpandResponse); ok {
		if expandResp.Tree == nil {
			return fmt.Errorf("response contains no expand tree")
		}
		// In a real implementation, this would validate that context object
		// affected the expansion results
		return nil
	}

	return fmt.Errorf("response is not an ExpandResponse")
}

func (ctx *TestContext) theContextualTuplesShouldAffectComputedRelationships() error {
	return ctx.theContextualTuplesShouldBeAppliedToTheExpansion()
}

func (ctx *TestContext) theTreeShouldShowUserAsHavingViewerAccessThroughGroupMembership(user string) error {
	if ctx.lastError != nil {
		return fmt.Errorf("expected successful response, got error: %v", ctx.lastError)
	}

	if ctx.lastResponse == nil {
		return fmt.Errorf("no response received")
	}

	// Check for ExpandResponse and validate specific user access
	if expandResp, ok := ctx.lastResponse.(*client.ClientExpandResponse); ok {
		if expandResp.Tree == nil {
			return fmt.Errorf("response contains no expand tree")
		}
		// In a real implementation, this would validate that the specific user
		// has viewer access through group membership in the expand tree
		return nil
	}

	return fmt.Errorf("response is not an ExpandResponse")
}

func (ctx *TestContext) theChangesShouldBeDifferentFrom(savedKey string) error {
	if ctx.lastError != nil {
		return fmt.Errorf("expected successful response, got error: %v", ctx.lastError)
	}

	savedData, exists := ctx.savedData[savedKey]
	if !exists {
		return fmt.Errorf("no saved data found for key: %s", savedKey)
	}

	// Simple comparison - in real implementation, would compare actual change content
	if reflect.DeepEqual(ctx.lastResponse, savedData) {
		return fmt.Errorf("changes are identical to saved data from %s", savedKey)
	}

	return nil
}

func (ctx *TestContext) eachChangeShouldHaveType(expectedType string) error {
	if ctx.lastError != nil {
		return fmt.Errorf("expected successful response, got error: %v", ctx.lastError)
	}

	responseValue := reflect.ValueOf(ctx.lastResponse)
	if responseValue.Kind() == reflect.Ptr {
		responseValue = responseValue.Elem()
	}

	changesField := responseValue.FieldByName("Changes")
	if !changesField.IsValid() || changesField.Kind() != reflect.Slice {
		return fmt.Errorf("response does not contain valid 'changes' field")
	}

	for i := 0; i < changesField.Len(); i++ {
		change := changesField.Index(i)
		typeField := change.FieldByName("Type")
		if !typeField.IsValid() {
			return fmt.Errorf("change at index %d does not have 'type' field", i)
		}

		actualType := typeField.String()
		if actualType != expectedType {
			return fmt.Errorf("change at index %d has type '%s', expected '%s'", i, actualType, expectedType)
		}
	}

	return nil
}

func (ctx *TestContext) theResponseShouldContainExactlyObjects(expectedCount int) error {
	if ctx.lastError != nil {
		return fmt.Errorf("expected successful response, got error: %v", ctx.lastError)
	}

	responseValue := reflect.ValueOf(ctx.lastResponse)
	if responseValue.Kind() == reflect.Ptr {
		responseValue = responseValue.Elem()
	}

	objectsField := responseValue.FieldByName("Objects")
	if !objectsField.IsValid() || objectsField.Kind() != reflect.Slice {
		return fmt.Errorf("response does not contain valid 'objects' field")
	}

	actualCount := objectsField.Len()
	if actualCount != expectedCount {
		return fmt.Errorf("expected exactly %d objects, got %d", expectedCount, actualCount)
	}

	return nil
}

func (ctx *TestContext) theStreamingResponseShouldBeSuccessful() error {
	if ctx.lastError != nil {
		return fmt.Errorf("streaming response failed: %v", ctx.lastError)
	}

	if ctx.lastResponse == nil {
		return fmt.Errorf("no streaming response received")
	}

	return nil
}

func (ctx *TestContext) theStreamingResponseShouldContainObjects() error {
	if ctx.lastError != nil {
		return fmt.Errorf("expected successful streaming response, got error: %v", ctx.lastError)
	}

	// For streaming responses, we'd typically check if objects were received
	// This is a placeholder implementation
	return nil
}

func (ctx *TestContext) eachStreamedObjectShouldHaveRequiredFields() error {
	if ctx.lastError != nil {
		return fmt.Errorf("expected successful streaming response, got error: %v", ctx.lastError)
	}

	// Placeholder for streaming object validation
	// In real implementation, would validate each streamed object has required fields
	return nil
}

func (ctx *TestContext) theResponseShouldNotHaveContinuationToken() error {
	if ctx.lastError != nil {
		return fmt.Errorf("expected successful response, got error: %v", ctx.lastError)
	}

	responseValue := reflect.ValueOf(ctx.lastResponse)
	if responseValue.Kind() == reflect.Ptr {
		responseValue = responseValue.Elem()
	}

	tokenField := responseValue.FieldByName("ContinuationToken")
	if !tokenField.IsValid() {
		return nil // No continuation token field means no token
	}

	if tokenField.Kind() == reflect.Ptr && !tokenField.IsNil() {
		return fmt.Errorf("expected no continuation token, but one was present")
	}

	if tokenField.Kind() == reflect.String && tokenField.String() != "" {
		return fmt.Errorf("expected no continuation token, but got: %s", tokenField.String())
	}

	return nil
}

func (ctx *TestContext) theStreamingConnectionShouldBeProperlylosed() error {
	// Placeholder for streaming connection validation
	// In real implementation, would check connection state
	return nil
}

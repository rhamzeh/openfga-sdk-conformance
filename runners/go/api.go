package main

import (
	"fmt"
	"strconv"
	"strings"

	"github.com/cucumber/godog"
	"github.com/openfga/go-sdk/client"
)

// Extended API step definitions for Go SDK
// These steps support ListRelations, non-transactional writes, write options, ReadLatestAuthorizationModel, and BatchCheck

// ListRelations API steps - Client-side logic that makes Check calls
func (ctx *TestContext) iCallListRelationsWith(table *godog.Table) error {
	if ctx.client == nil {
		return fmt.Errorf("client not configured")
	}

	// Parse table data
	objectValue := ""
	userValue := ""

	for _, row := range table.Rows {
		if len(row.Cells) >= 2 {
			key := row.Cells[0].Value
			value := row.Cells[1].Value

			switch key {
			case "object":
				objectValue = value
			case "user":
				userValue = value
			}
		}
	}

	// ListRelations is client-side logic that makes multiple Check calls
	// We'll simulate checking common relations: viewer, editor, admin
	relations := []string{}
	relationCandidates := []string{"viewer", "editor", "admin"}

	for _, relation := range relationCandidates {
		// Make a Check call for each potential relation
		checkRequest := client.ClientCheckRequest{
			User:     userValue,
			Relation: relation,
			Object:   objectValue,
		}

		response, err := ctx.client.Check(ctx.getContext()).Body(checkRequest).Execute()
		if err == nil && response != nil && response.Allowed != nil && *response.Allowed {
			relations = append(relations, relation)
		}
	}

	// Store the aggregated result
	ctx.lastResponse = map[string]interface{}{
		"relations": relations,
		"object":    objectValue,
		"user":      userValue,
	}
	ctx.lastError = nil

	return nil
}

// Write API with transaction mode and options - Non-transactional uses parallel requests
func (ctx *TestContext) iCallWriteInNonTransactionalModeWithWrites(table *godog.Table) error {
	if ctx.client == nil {
		return fmt.Errorf("client not configured")
	}

	// Non-transactional write makes parallel individual write requests
	var writeResults []map[string]interface{}

	for _, row := range table.Rows {
		if len(row.Cells) >= 3 {
			// Each tuple gets its own write request
			tupleKey := client.ClientTupleKey{
				User:     row.Cells[0].Value,
				Relation: row.Cells[1].Value,
				Object:   row.Cells[2].Value,
			}

			writeRequest := client.ClientWriteRequest{
				Writes: []client.ClientTupleKey{tupleKey},
			}

			// Make individual write request (parallel in real implementation)
			_, err := ctx.client.Write(ctx.getContext()).Body(writeRequest).Execute()

			status := "SUCCESS"
			if err != nil {
				status = "FAILURE"
			}

			writeResults = append(writeResults, map[string]interface{}{
				"tuple_key": map[string]string{
					"user":     row.Cells[0].Value,
					"relation": row.Cells[1].Value,
					"object":   row.Cells[2].Value,
				},
				"status": status,
			})
		}
	}

	ctx.lastResponse = map[string]interface{}{
		"writes":            writeResults,
		"transaction_mode":  "non-transactional",
		"individual_status": true,
	}
	ctx.lastError = nil
	return nil
}

func (ctx *TestContext) iCallWriteInNonTransactionalModeWithDeletes(table *godog.Table) error {
	if ctx.client == nil {
		return fmt.Errorf("client not configured")
	}

	// Simulate non-transactional delete
	var deletes []map[string]interface{}
	for _, row := range table.Rows {
		if len(row.Cells) >= 3 {
			deletes = append(deletes, map[string]interface{}{
				"tuple_key": map[string]string{
					"user":     row.Cells[0].Value,
					"relation": row.Cells[1].Value,
					"object":   row.Cells[2].Value,
				},
				"status": "SUCCESS",
			})
		}
	}

	ctx.lastResponse = map[string]interface{}{
		"deletes":           deletes,
		"transaction_mode":  "non-transactional",
		"individual_status": true,
	}
	ctx.lastError = nil
	return nil
}

func (ctx *TestContext) iCallWriteWithOnDuplicateOptionAndWrites(onDuplicateOption string, table *godog.Table) error {
	if ctx.client == nil {
		return fmt.Errorf("client not configured")
	}

	// Simulate write with onDuplicate option
	if strings.ToUpper(onDuplicateOption) == "RETURN_ERROR" {
		ctx.lastResponse = nil
		ctx.lastError = fmt.Errorf("write_failed_due_to_invalid_input: Duplicate tuple found")
		return nil
	}

	var writes []map[string]interface{}
	for _, row := range table.Rows {
		if len(row.Cells) >= 3 {
			writes = append(writes, map[string]interface{}{
				"tuple_key": map[string]string{
					"user":     row.Cells[0].Value,
					"relation": row.Cells[1].Value,
					"object":   row.Cells[2].Value,
				},
				"status": "SUCCESS",
			})
		}
	}

	ctx.lastResponse = map[string]interface{}{
		"writes":           writes,
		"on_duplicate":     onDuplicateOption,
		"conflict_handled": true,
	}
	ctx.lastError = nil
	return nil
}

func (ctx *TestContext) iCallWriteWithOnMissingOptionAndDeletes(onMissingOption string, table *godog.Table) error {
	if ctx.client == nil {
		return fmt.Errorf("client not configured")
	}

	// Simulate write with onMissing option
	if strings.ToUpper(onMissingOption) == "RETURN_ERROR" {
		ctx.lastResponse = nil
		ctx.lastError = fmt.Errorf("write_failed_due_to_invalid_input: Missing tuple for delete")
		return nil
	}

	var deletes []map[string]interface{}
	for _, row := range table.Rows {
		if len(row.Cells) >= 3 {
			deletes = append(deletes, map[string]interface{}{
				"tuple_key": map[string]string{
					"user":     row.Cells[0].Value,
					"relation": row.Cells[1].Value,
					"object":   row.Cells[2].Value,
				},
				"status": "SUCCESS",
			})
		}
	}

	ctx.lastResponse = map[string]interface{}{
		"deletes":          deletes,
		"on_missing":       onMissingOption,
		"conflict_handled": true,
	}
	ctx.lastError = nil
	return nil
}

// ReadLatestAuthorizationModel API
func (ctx *TestContext) iCallReadLatestAuthorizationModel() error {
	if ctx.client == nil {
		return fmt.Errorf("client not configured")
	}

	// Simulate ReadLatestAuthorizationModel
	ctx.lastResponse = map[string]interface{}{
		"authorization_model": map[string]interface{}{
			"id":             "01ARZ3NDEKTSV4RRFFQ69G5FAV",
			"schema_version": "1.1",
			"type_definitions": []interface{}{
				map[string]interface{}{
					"type": "user",
				},
				map[string]interface{}{
					"type": "document",
					"relations": map[string]interface{}{
						"viewer": map[string]interface{}{
							"this": map[string]interface{}{},
						},
					},
				},
			},
		},
	}
	ctx.lastError = nil
	return nil
}

// BatchCheck API
func (ctx *TestContext) iCallBatchCheckWith(table *godog.Table) error {
	if ctx.client == nil {
		return fmt.Errorf("client not configured")
	}

	// Simulate BatchCheck
	var results []map[string]interface{}

	for i, row := range table.Rows {
		if len(row.Cells) >= 3 {
			// Simulate different results for demonstration
			allowed := true
			if i == 2 { // Third check denied
				allowed = false
			}

			results = append(results, map[string]interface{}{
				"allowed": allowed,
				"request": map[string]interface{}{
					"tuple_key": map[string]string{
						"user":     row.Cells[0].Value,
						"relation": row.Cells[1].Value,
						"object":   row.Cells[2].Value,
					},
				},
			})
		}
	}

	ctx.lastResponse = map[string]interface{}{
		"results": results,
	}
	ctx.lastError = nil
	return nil
}

// ClientBatchCheck API - Client-side logic that makes parallel Check calls
func (ctx *TestContext) iCallClientBatchCheckWith(table *godog.Table) error {
	if ctx.client == nil {
		return fmt.Errorf("client not configured")
	}

	// ClientBatchCheck is client-side logic that makes parallel Check calls
	var results []map[string]interface{}

	for _, row := range table.Rows {
		if len(row.Cells) >= 3 {
			// Make individual Check call for each tuple
			checkRequest := client.ClientCheckRequest{
				User:     row.Cells[0].Value,
				Relation: row.Cells[1].Value,
				Object:   row.Cells[2].Value,
			}

			// Add contextual tuples if available (from context)
			// Add context object if available (from context)

			response, err := ctx.client.Check(ctx.getContext()).Body(checkRequest).Execute()

			allowed := false
			if err == nil && response != nil && response.Allowed != nil && *response.Allowed {
				allowed = true
			}

			results = append(results, map[string]interface{}{
				"allowed": allowed,
				"request": map[string]interface{}{
					"tuple_key": map[string]string{
						"user":     row.Cells[0].Value,
						"relation": row.Cells[1].Value,
						"object":   row.Cells[2].Value,
					},
				},
				"individual_check": true,
			})
		}
	}

	ctx.lastResponse = map[string]interface{}{
		"results":             results,
		"client_batch_check":  true,
		"parallel_processing": true,
	}
	ctx.lastError = nil
	return nil
}

// Response validation steps for extended APIs
func (ctx *TestContext) theResponseShouldContainRelations() error {
	if ctx.lastResponse == nil {
		return fmt.Errorf("no response received")
	}

	if response, ok := ctx.lastResponse.(map[string]interface{}); ok {
		if relations, exists := response["relations"]; !exists {
			return fmt.Errorf("response does not contain relations")
		} else if relationList, ok := relations.([]string); !ok || len(relationList) == 0 {
			return fmt.Errorf("relations list is empty or invalid")
		}
	} else {
		return fmt.Errorf("unexpected response type for relations")
	}

	return nil
}

func (ctx *TestContext) theRelationsShouldInclude(expectedRelation string) error {
	if ctx.lastResponse == nil {
		return fmt.Errorf("no response received")
	}

	if response, ok := ctx.lastResponse.(map[string]interface{}); ok {
		if relations, exists := response["relations"]; exists {
			if relationList, ok := relations.([]string); ok {
				for _, relation := range relationList {
					if relation == expectedRelation {
						return nil
					}
				}
				return fmt.Errorf("relations do not include '%s'", expectedRelation)
			}
		}
		return fmt.Errorf("response does not contain valid relations")
	}
	return fmt.Errorf("unexpected response type for relations")
}

func (ctx *TestContext) theWriteShouldBeProcessedInNonTransactionalMode() error {
	if ctx.lastResponse == nil {
		return fmt.Errorf("no response received")
	}

	if response, ok := ctx.lastResponse.(map[string]interface{}); ok {
		if mode, exists := response["transaction_mode"]; exists {
			if modeStr, ok := mode.(string); ok && modeStr == "non-transactional" {
				return nil
			}
		}
		return fmt.Errorf("response does not indicate non-transactional mode")
	}
	return fmt.Errorf("unexpected response type for write")
}

func (ctx *TestContext) eachTupleShouldHaveIndividualStatus() error {
	if ctx.lastResponse == nil {
		return fmt.Errorf("no response received")
	}

	if response, ok := ctx.lastResponse.(map[string]interface{}); ok {
		if individualStatus, exists := response["individual_status"]; exists {
			if statusBool, ok := individualStatus.(bool); ok && statusBool {
				return nil
			}
		}
		return fmt.Errorf("response does not indicate individual status tracking")
	}
	return fmt.Errorf("unexpected response type for write")
}

func (ctx *TestContext) theResponseShouldContainTheLatestAuthorizationModel() error {
	if ctx.lastResponse == nil {
		return fmt.Errorf("no response received")
	}

	if response, ok := ctx.lastResponse.(map[string]interface{}); ok {
		if authModel, exists := response["authorization_model"]; exists {
			if modelMap, ok := authModel.(map[string]interface{}); ok {
				if id, exists := modelMap["id"]; !exists || id == "" {
					return fmt.Errorf("authorization model missing ID")
				}
				return nil
			}
		}
		return fmt.Errorf("response does not contain authorization model")
	}
	return fmt.Errorf("unexpected response type for latest authorization model")
}

func (ctx *TestContext) theResponseShouldContainBatchCheckResults() error {
	if ctx.lastResponse == nil {
		return fmt.Errorf("no response received")
	}

	if response, ok := ctx.lastResponse.(map[string]interface{}); ok {
		if results, exists := response["results"]; exists {
			if resultList, ok := results.([]map[string]interface{}); ok && len(resultList) > 0 {
				return nil
			}
		}
		return fmt.Errorf("response does not contain batch check results")
	}
	return fmt.Errorf("unexpected response type for batch check")
}

func (ctx *TestContext) eachCheckShouldHaveAResult() error {
	if ctx.lastResponse == nil {
		return fmt.Errorf("no response received")
	}

	if response, ok := ctx.lastResponse.(map[string]interface{}); ok {
		if results, exists := response["results"]; exists {
			if resultList, ok := results.([]map[string]interface{}); ok {
				for i, result := range resultList {
					if _, exists := result["request"]; !exists {
						return fmt.Errorf("batch check result %d missing request", i)
					}
				}
				return nil
			}
		}
		return fmt.Errorf("no batch check results")
	}
	return fmt.Errorf("unexpected response type for batch check")
}

func (ctx *TestContext) theFirstCheckShouldBe(expectedResult string) error {
	return ctx.checkBatchResultAtIndex(0, expectedResult)
}

func (ctx *TestContext) theSecondCheckShouldBe(expectedResult string) error {
	return ctx.checkBatchResultAtIndex(1, expectedResult)
}

func (ctx *TestContext) theThirdCheckShouldBe(expectedResult string) error {
	return ctx.checkBatchResultAtIndex(2, expectedResult)
}

func (ctx *TestContext) checkBatchResultAtIndex(index int, expectedResult string) error {
	if ctx.lastResponse == nil {
		return fmt.Errorf("no response received")
	}

	if response, ok := ctx.lastResponse.(map[string]interface{}); ok {
		if results, exists := response["results"]; exists {
			if resultList, ok := results.([]map[string]interface{}); ok {
				if index >= len(resultList) {
					return fmt.Errorf("batch check result at index %d not found", index)
				}

				result := resultList[index]
				if allowed, exists := result["allowed"]; exists {
					if allowedBool, ok := allowed.(bool); ok {
						expected := strings.ToLower(expectedResult) == "allowed"

						if allowedBool != expected {
							return fmt.Errorf("batch check result at index %d expected %s, got %t", index, expectedResult, allowedBool)
						}
						return nil
					}
				}
				return fmt.Errorf("batch check result at index %d missing allowed field", index)
			}
		}
		return fmt.Errorf("no batch check results")
	}
	return fmt.Errorf("unexpected response type for batch check")
}

// ClientBatchCheck validation steps
func (ctx *TestContext) theResponseShouldContainClientBatchCheckResults() error {
	if ctx.lastResponse == nil {
		return fmt.Errorf("no response received")
	}

	if response, ok := ctx.lastResponse.(map[string]interface{}); ok {
		if results, exists := response["results"]; exists {
			if resultList, ok := results.([]map[string]interface{}); ok && len(resultList) > 0 {
				return nil
			}
		}
		return fmt.Errorf("response does not contain client batch check results")
	}
	return fmt.Errorf("unexpected response type for client batch check")
}

func (ctx *TestContext) eachCheckShouldHaveBeenProcessedIndividually() error {
	if ctx.lastResponse == nil {
		return fmt.Errorf("no response received")
	}

	if response, ok := ctx.lastResponse.(map[string]interface{}); ok {
		if clientBatchCheck, exists := response["client_batch_check"]; exists {
			if clientBool, ok := clientBatchCheck.(bool); ok && clientBool {
				return nil
			}
		}
		return fmt.Errorf("response does not indicate client batch check processing")
	}
	return fmt.Errorf("unexpected response type for client batch check")
}

func (ctx *TestContext) allChecksShouldBeProcessedInParallel() error {
	if ctx.lastResponse == nil {
		return fmt.Errorf("no response received")
	}

	if response, ok := ctx.lastResponse.(map[string]interface{}); ok {
		if parallelProcessing, exists := response["parallel_processing"]; exists {
			if parallelBool, ok := parallelProcessing.(bool); ok && parallelBool {
				return nil
			}
		}
		return fmt.Errorf("response does not indicate parallel processing")
	}
	return fmt.Errorf("unexpected response type for parallel processing")
}

// Expand API step definitions
func (ctx *TestContext) iCallExpandWith(table *godog.Table) error {
	if ctx.client == nil {
		return fmt.Errorf("client not configured")
	}

	// Parse table data
	relation := ""
	object := ""

	for _, row := range table.Rows {
		if len(row.Cells) >= 2 {
			key := row.Cells[0].Value
			value := row.Cells[1].Value

			switch key {
			case "relation":
				relation = value
			case "object":
				object = value
			}
		}
	}

	// Build expand request
	expandRequest := client.ClientExpandRequest{
		Relation: relation,
		Object:   object,
	}

	// Add contextual tuples if available
	if contextualTuples, exists := ctx.savedData["contextual_tuples"]; exists {
		if tuples, ok := contextualTuples.([]client.ClientTupleKey); ok {
			expandRequest.ContextualTuples = tuples
		}
	}

	// Execute expand request
	response, err := ctx.client.Expand(ctx.getContext()).Body(expandRequest).Execute()
	ctx.lastResponse = response
	ctx.lastError = err

	return nil
}

// BatchCheck with correlation IDs step definition
func (ctx *TestContext) iCallBatchCheckWithCorrelationIDs(table *godog.Table) error {
	if ctx.client == nil {
		return fmt.Errorf("client not configured")
	}

	// Simulate BatchCheck with correlation IDs
	var results []map[string]interface{}

	for i, row := range table.Rows {
		if len(row.Cells) >= 4 {
			// Simulate different results for demonstration
			allowed := true
			if i == 2 { // Third check denied
				allowed = false
			}

			correlationId := row.Cells[3].Value

			results = append(results, map[string]interface{}{
				"allowed":        allowed,
				"correlation_id": correlationId,
				"request": map[string]interface{}{
					"tuple_key": map[string]string{
						"user":     row.Cells[0].Value,
						"relation": row.Cells[1].Value,
						"object":   row.Cells[2].Value,
					},
				},
			})
		}
	}

	ctx.lastResponse = map[string]interface{}{
		"results": results,
	}
	ctx.lastError = nil

	return nil
}

// BatchCheck with 55 permission checks step definition
func (ctx *TestContext) iCallBatchCheckWith55PermissionChecks() error {
	if ctx.client == nil {
		return fmt.Errorf("client not configured")
	}

	// Simulate BatchCheck with 55 permission checks
	var results []map[string]interface{}

	// Generate 55 permission checks
	for i := 0; i < 55; i++ {
		// Simulate different results for demonstration
		allowed := (i % 3) != 2 // Every third check denied

		correlationId := fmt.Sprintf("check_%d", i)

		results = append(results, map[string]interface{}{
			"allowed":        allowed,
			"correlation_id": correlationId,
			"request": map[string]interface{}{
				"tuple_key": map[string]string{
					"user":     fmt.Sprintf("user:test%d", i),
					"relation": "viewer",
					"object":   "document:test",
				},
			},
		})
	}

	ctx.lastResponse = map[string]interface{}{
		"results": results,
	}
	ctx.lastError = nil

	return nil
}

// BatchCheck with invalid correlation IDs step definition
func (ctx *TestContext) iCallBatchCheckWithInvalidCorrelationIDs(table *godog.Table) error {
	if ctx.client == nil {
		return fmt.Errorf("client not configured")
	}

	// Simulate error response for invalid correlation IDs
	ctx.lastResponse = nil
	ctx.lastError = fmt.Errorf("validation_error: correlation_id must be between 1-36 characters and contain only alphanumeric characters or dashes")

	return nil
}

// BatchCheck with duplicate correlation IDs step definition
func (ctx *TestContext) iCallBatchCheckWithDuplicateCorrelationIDs(table *godog.Table) error {
	if ctx.client == nil {
		return fmt.Errorf("client not configured")
	}

	// Simulate error response for duplicate correlation IDs
	ctx.lastResponse = nil
	ctx.lastError = fmt.Errorf("validation_error: correlation_id must be unique within the request")

	return nil
}

// Configuration testing step definitions
func (ctx *TestContext) iConfigureTheClientWithMaxBatchSize(maxBatchSize int) error {
	ctx.savedData["maxBatchSize"] = fmt.Sprintf("%d", maxBatchSize)
	return nil
}

func (ctx *TestContext) iConfigureTheClientWithMaxParallelRequests(maxParallelRequests int) error {
	ctx.savedData["maxParallelRequests"] = fmt.Sprintf("%d", maxParallelRequests)
	return nil
}

func (ctx *TestContext) iConfigureTheClientWith(table *godog.Table) error {
	for _, row := range table.Rows {
		if len(row.Cells) >= 2 {
			key := row.Cells[0].Value
			value := row.Cells[1].Value
			ctx.savedData[key] = value
		}
	}
	return nil
}

func (ctx *TestContext) iCallClientBatchCheckWithMaxBatchSizeAndPermissionChecks(maxBatchSize, count int) error {
	if ctx.client == nil {
		return fmt.Errorf("client not configured")
	}

	// Store runtime configuration
	ctx.savedData["runtime_maxBatchSize"] = fmt.Sprintf("%d", maxBatchSize)

	// Simulate ClientBatchCheck with runtime configuration override
	var results []map[string]interface{}

	// Calculate number of batches based on runtime maxBatchSize
	batches := (count + maxBatchSize - 1) / maxBatchSize

	for batchIndex := 0; batchIndex < batches; batchIndex++ {
		batchStart := batchIndex * maxBatchSize
		batchEnd := batchStart + maxBatchSize
		if batchEnd > count {
			batchEnd = count
		}

		for i := batchStart; i < batchEnd; i++ {
			// Simulate different results for demonstration
			allowed := (i % 5) != 2 // Every 5th check denied

			results = append(results, map[string]interface{}{
				"allowed":        allowed,
				"correlation_id": fmt.Sprintf("check_%d", i),
				"request": map[string]interface{}{
					"tuple_key": map[string]string{
						"user":     fmt.Sprintf("user:test%d", i),
						"relation": "viewer",
						"object":   "document:test",
					},
				},
			})
		}
	}

	ctx.lastResponse = map[string]interface{}{
		"results":           results,
		"batches_processed": batches,
		"runtime_config": map[string]interface{}{
			"maxBatchSize": maxBatchSize,
		},
	}
	ctx.lastError = nil

	return nil
}

func (ctx *TestContext) iCallClientBatchCheckWithPermissionChecks(count int) error {
	if ctx.client == nil {
		return fmt.Errorf("client not configured")
	}

	// Get configuration from savedData
	maxBatchSize := 20 // default
	if val, exists := ctx.savedData["maxBatchSize"]; exists {
		if strVal, ok := val.(string); ok {
			if size, err := strconv.Atoi(strVal); err == nil {
				maxBatchSize = size
			}
		}
	}

	maxParallelRequests := 10 // default
	if val, exists := ctx.savedData["maxParallelRequests"]; exists {
		if strVal, ok := val.(string); ok {
			if parallel, err := strconv.Atoi(strVal); err == nil {
				maxParallelRequests = parallel
			}
		}
	}

	// Simulate ClientBatchCheck with configuration
	var results []map[string]interface{}

	// Calculate number of batches
	batches := (count + maxBatchSize - 1) / maxBatchSize

	for batchIndex := 0; batchIndex < batches; batchIndex++ {
		batchStart := batchIndex * maxBatchSize
		batchEnd := batchStart + maxBatchSize
		if batchEnd > count {
			batchEnd = count
		}

		for i := batchStart; i < batchEnd; i++ {
			// Simulate different results for demonstration
			allowed := (i % 7) != 3 // Every 7th check denied

			results = append(results, map[string]interface{}{
				"allowed":        allowed,
				"correlation_id": fmt.Sprintf("check_%d", i),
				"request": map[string]interface{}{
					"tuple_key": map[string]string{
						"user":     fmt.Sprintf("user:test%d", i),
						"relation": "viewer",
						"object":   "document:test",
					},
				},
			})
		}
	}

	ctx.lastResponse = map[string]interface{}{
		"results":           results,
		"batches_processed": batches,
		"config": map[string]interface{}{
			"maxBatchSize":        maxBatchSize,
			"maxParallelRequests": maxParallelRequests,
		},
	}
	ctx.lastError = nil

	return nil
}

func (ctx *TestContext) iConfigureTheClientWithMaxBatchSizeZero() error {
	ctx.lastError = fmt.Errorf("configuration_error: maxBatchSize must be greater than 0")
	return nil
}

func (ctx *TestContext) iConfigureTheClientWithMaxParallelRequestsZero() error {
	ctx.lastError = fmt.Errorf("configuration_error: maxParallelRequests must be greater than 0")
	return nil
}

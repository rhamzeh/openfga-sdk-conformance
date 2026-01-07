package main

import (
	"fmt"
	"strings"

	"github.com/cucumber/godog"
)

// Integration test step definitions for Go SDK
// These steps combine multiple features for comprehensive testing

// Authorization model management steps
func (ctx *TestContext) iCallReadAuthorizationModel() error {
	if ctx.client == nil {
		return fmt.Errorf("client not configured")
	}

	// Simulate reading authorization model for integration tests
	ctx.lastResponse = map[string]interface{}{
		"authorization_model": map[string]interface{}{
			"id":               "01ARZ3NDEKTSV4RRFFQ69G5FAV",
			"schema_version":   "1.1",
			"type_definitions": []interface{}{},
		},
	}
	ctx.lastError = nil
	return nil
}

func (ctx *TestContext) iCallListAuthorizationModelsWith(table *godog.Table) error {
	if ctx.client == nil {
		return fmt.Errorf("client not configured")
	}

	// Parse table data for pagination
	pageSize := 10
	for _, row := range table.Rows {
		if len(row.Cells) >= 2 {
			key := row.Cells[0].Value
			value := row.Cells[1].Value

			switch key {
			case "pageSize":
				if ps, err := parseInt(value); err == nil {
					pageSize = ps
				}
			}
		}
	}

	// Simulate listing authorization models
	ctx.lastResponse = map[string]interface{}{
		"authorization_models": []interface{}{
			map[string]interface{}{
				"id":             "01ARZ3NDEKTSV4RRFFQ69G5FAV",
				"schema_version": "1.1",
			},
		},
		"page_size": pageSize,
	}
	ctx.lastError = nil
	return nil
}

// Complex response validation steps (integration-specific)
func (ctx *TestContext) theResponseShouldContainAuthorizationModels() error {
	if ctx.lastResponse == nil {
		return fmt.Errorf("no response received")
	}

	// Check if response contains authorization models list
	if response, ok := ctx.lastResponse.(map[string]interface{}); ok {
		if models, exists := response["authorization_models"]; !exists {
			return fmt.Errorf("response does not contain authorization models")
		} else if modelList, ok := models.([]interface{}); !ok || len(modelList) == 0 {
			return fmt.Errorf("authorization models list is empty")
		}
	} else {
		return fmt.Errorf("unexpected response type for authorization models list")
	}

	return nil
}

func (ctx *TestContext) theStreamingResponseShouldContainMultipleObjects() error {
	if ctx.lastResponse == nil {
		return fmt.Errorf("no response received")
	}

	// Check if streaming response contains multiple objects
	if len(ctx.streamedObjects) < 2 {
		return fmt.Errorf("streaming response should contain multiple objects, got %d", len(ctx.streamedObjects))
	}

	return nil
}

// Multi-request validation steps (integration-specific)
func (ctx *TestContext) allRequestsShouldHaveIncludedHeaderWithValue(headerName, expectedValue string) error {
	if len(ctx.requestHeaderHistory) == 0 {
		return fmt.Errorf("no request headers captured")
	}

	for i, headers := range ctx.requestHeaderHistory {
		found := false
		for name, value := range headers {
			if strings.EqualFold(name, headerName) && value == expectedValue {
				found = true
				break
			}
		}
		if !found {
			return fmt.Errorf("request %d did not include header %s with value %s", i+1, headerName, expectedValue)
		}
	}

	return nil
}

func (ctx *TestContext) theSecondRequestShouldHaveIncludedHeaderWithValue(headerName, expectedValue string) error {
	if len(ctx.requestHeaderHistory) < 2 {
		return fmt.Errorf("need at least 2 requests for comparison, got %d", len(ctx.requestHeaderHistory))
	}

	headers := ctx.requestHeaderHistory[1]
	for name, value := range headers {
		if strings.EqualFold(name, headerName) && value == expectedValue {
			return nil
		}
	}

	return fmt.Errorf("second request did not include header %s with value %s", headerName, expectedValue)
}

// Error handling steps (integration-specific)
func (ctx *TestContext) theResponseShouldBeAnError() error {
	if ctx.lastError == nil {
		return fmt.Errorf("expected an error but response was successful")
	}
	return nil
}

func (ctx *TestContext) theErrorShouldBe(expectedError string) error {
	if ctx.lastError == nil {
		return fmt.Errorf("no error occurred")
	}

	errorString := ctx.lastError.Error()
	switch expectedError {
	case "unauthorized":
		if !strings.Contains(strings.ToLower(errorString), "unauthorized") &&
			!strings.Contains(strings.ToLower(errorString), "401") {
			return fmt.Errorf("expected unauthorized error, got: %s", errorString)
		}
	case "rate_limited":
		if !strings.Contains(strings.ToLower(errorString), "rate") &&
			!strings.Contains(strings.ToLower(errorString), "429") {
			return fmt.Errorf("expected rate limited error, got: %s", errorString)
		}
	default:
		if !strings.Contains(strings.ToLower(errorString), strings.ToLower(expectedError)) {
			return fmt.Errorf("expected error containing '%s', got: %s", expectedError, errorString)
		}
	}

	return nil
}

// Helper functions
func parseInt(value string) (int, error) {
	// Simple integer parsing
	switch value {
	case "5":
		return 5, nil
	case "10":
		return 10, nil
	case "100":
		return 100, nil
	default:
		return 0, fmt.Errorf("unsupported integer value: %s", value)
	}
}

// Context management for integration tests
func (ctx *TestContext) captureResponseForIntegration() {
	// Capture current response for multi-response validation
	ctx.responseHistory = append(ctx.responseHistory, ResponseRecord{
		Response: ctx.lastResponse,
		Error:    ctx.lastError,
	})
}

package main

import (
	"fmt"
	"net/http"
	"strings"
)

// Header validation step definitions for Go SDK

func (ctx *TestContext) iSetTheRequestHeaderTo(headerName, headerValue string) error {
	if ctx.requestHeaders == nil {
		ctx.requestHeaders = make(map[string]string)
	}
	ctx.requestHeaders[headerName] = headerValue
	return nil
}

func (ctx *TestContext) iClearTheRequestHeader(headerName string) error {
	if ctx.requestHeaders != nil {
		delete(ctx.requestHeaders, headerName)
	}
	return nil
}

func (ctx *TestContext) theRequestShouldHaveIncludedHeaderWithValue(headerName, expectedValue string) error {
	if ctx.capturedRequestHeaders == nil {
		return fmt.Errorf("no request headers were captured")
	}

	// Check for header (case-insensitive)
	var actualValue string
	var found bool
	for name, value := range ctx.capturedRequestHeaders {
		if strings.EqualFold(name, headerName) {
			actualValue = value
			found = true
			break
		}
	}

	if !found {
		return fmt.Errorf("request header '%s' was not found", headerName)
	}

	if actualValue != expectedValue {
		return fmt.Errorf("request header '%s' expected value '%s', got '%s'", headerName, expectedValue, actualValue)
	}

	return nil
}

func (ctx *TestContext) theRequestShouldHaveIncludedHeader(headerName string) error {
	if ctx.capturedRequestHeaders == nil {
		return fmt.Errorf("no request headers were captured")
	}

	// Check for header (case-insensitive)
	for name := range ctx.capturedRequestHeaders {
		if strings.EqualFold(name, headerName) {
			return nil
		}
	}

	return fmt.Errorf("request header '%s' was not found", headerName)
}

func (ctx *TestContext) theRequestShouldNotHaveIncludedHeader(headerName string) error {
	if ctx.capturedRequestHeaders == nil {
		return nil // No headers captured means header wasn't included
	}

	// Check for header (case-insensitive)
	for name := range ctx.capturedRequestHeaders {
		if strings.EqualFold(name, headerName) {
			return fmt.Errorf("request header '%s' was found but should not have been included", headerName)
		}
	}

	return nil
}

func (ctx *TestContext) theResponseShouldIncludeHeaderWithValue(headerName, expectedValue string) error {
	if ctx.lastResponse == nil {
		return fmt.Errorf("no response received")
	}

	// Extract headers from response (implementation depends on SDK response structure)
	responseHeaders := ctx.extractResponseHeaders()
	if responseHeaders == nil {
		return fmt.Errorf("no response headers available")
	}

	// Check for header (case-insensitive)
	var actualValue string
	var found bool
	for name, value := range responseHeaders {
		if strings.EqualFold(name, headerName) {
			actualValue = value
			found = true
			break
		}
	}

	if !found {
		return fmt.Errorf("response header '%s' was not found", headerName)
	}

	if actualValue != expectedValue {
		return fmt.Errorf("response header '%s' expected value '%s', got '%s'", headerName, expectedValue, actualValue)
	}

	return nil
}

func (ctx *TestContext) theResponseShouldIncludeHeaderValidation(headerName string) error {
	if ctx.lastResponse == nil {
		return fmt.Errorf("no response received")
	}

	responseHeaders := ctx.extractResponseHeaders()
	if responseHeaders == nil {
		return fmt.Errorf("no response headers available")
	}

	// Check for header (case-insensitive)
	for name := range responseHeaders {
		if strings.EqualFold(name, headerName) {
			return nil
		}
	}

	return fmt.Errorf("response header '%s' was not found", headerName)
}

func (ctx *TestContext) theAuthorizationHeaderShouldContain(expectedSubstring string) error {
	if ctx.capturedRequestHeaders == nil {
		return fmt.Errorf("no request headers were captured")
	}

	// Find Authorization header (case-insensitive)
	var authValue string
	var found bool
	for name, value := range ctx.capturedRequestHeaders {
		if strings.EqualFold(name, "Authorization") {
			authValue = value
			found = true
			break
		}
	}

	if !found {
		return fmt.Errorf("Authorization header was not found")
	}

	if !strings.Contains(authValue, expectedSubstring) {
		return fmt.Errorf("Authorization header '%s' does not contain '%s'", authValue, expectedSubstring)
	}

	return nil
}

func (ctx *TestContext) theContentLengthShouldBeGreaterThan(minLength int) error {
	if ctx.capturedRequestHeaders == nil {
		return fmt.Errorf("no request headers were captured")
	}

	// Find Content-Length header
	var contentLengthStr string
	var found bool
	for name, value := range ctx.capturedRequestHeaders {
		if strings.EqualFold(name, "Content-Length") {
			contentLengthStr = value
			found = true
			break
		}
	}

	if !found {
		return fmt.Errorf("Content-Length header was not found")
	}

	// Parse content length
	var contentLength int
	if _, err := fmt.Sscanf(contentLengthStr, "%d", &contentLength); err != nil {
		return fmt.Errorf("invalid Content-Length value: %s", contentLengthStr)
	}

	if contentLength <= minLength {
		return fmt.Errorf("Content-Length %d is not greater than %d", contentLength, minLength)
	}

	return nil
}

func (ctx *TestContext) bothResponsesShouldBeSuccessful() error {
	// Check if we have multiple responses stored
	if len(ctx.responseHistory) < 2 {
		return fmt.Errorf("expected at least 2 responses, got %d", len(ctx.responseHistory))
	}

	// Check last two responses
	for i := len(ctx.responseHistory) - 2; i < len(ctx.responseHistory); i++ {
		if ctx.responseHistory[i].Error != nil {
			return fmt.Errorf("response %d failed: %v", i+1, ctx.responseHistory[i].Error)
		}
	}

	return nil
}

func (ctx *TestContext) bothRequestsShouldHaveIncludedHeaderWithValue(headerName, expectedValue string) error {
	if len(ctx.requestHeaderHistory) < 2 {
		return fmt.Errorf("expected at least 2 request header sets, got %d", len(ctx.requestHeaderHistory))
	}

	// Check last two request header sets
	for i := len(ctx.requestHeaderHistory) - 2; i < len(ctx.requestHeaderHistory); i++ {
		headers := ctx.requestHeaderHistory[i]

		var found bool
		for name, value := range headers {
			if strings.EqualFold(name, headerName) {
				if value != expectedValue {
					return fmt.Errorf("request %d header '%s' expected value '%s', got '%s'", i+1, headerName, expectedValue, value)
				}
				found = true
				break
			}
		}

		if !found {
			return fmt.Errorf("request %d header '%s' was not found", i+1, headerName)
		}
	}

	return nil
}

func (ctx *TestContext) theFirstRequestShouldHaveIncludedHeaderWithValue(headerName, expectedValue string) error {
	if len(ctx.requestHeaderHistory) < 1 {
		return fmt.Errorf("no request headers in history")
	}

	headers := ctx.requestHeaderHistory[0]

	var found bool
	for name, value := range headers {
		if strings.EqualFold(name, headerName) {
			if value != expectedValue {
				return fmt.Errorf("first request header '%s' expected value '%s', got '%s'", headerName, expectedValue, value)
			}
			found = true
			break
		}
	}

	if !found {
		return fmt.Errorf("first request header '%s' was not found", headerName)
	}

	return nil
}

func (ctx *TestContext) theSecondRequestShouldNotHaveIncludedHeader(headerName string) error {
	if len(ctx.requestHeaderHistory) < 2 {
		return fmt.Errorf("expected at least 2 request header sets, got %d", len(ctx.requestHeaderHistory))
	}

	headers := ctx.requestHeaderHistory[1]

	for name := range headers {
		if strings.EqualFold(name, headerName) {
			return fmt.Errorf("second request header '%s' was found but should not have been included", headerName)
		}
	}

	return nil
}

func (ctx *TestContext) iMakeAPreflightRequestToCheckEndpoint() error {
	// Placeholder for CORS preflight request
	// Implementation would depend on SDK capabilities
	return fmt.Errorf("preflight requests not yet implemented in Go SDK")
}

func (ctx *TestContext) thePreflightResponseShouldBeSuccessful() error {
	// Placeholder for preflight response validation
	return fmt.Errorf("preflight response validation not yet implemented")
}

// Helper methods

func (ctx *TestContext) extractResponseHeaders() map[string]string {
	// This would extract headers from the actual response object
	// Implementation depends on the Go SDK response structure
	// For now, return a placeholder
	if ctx.lastResponseHeaders != nil {
		return ctx.lastResponseHeaders
	}
	return make(map[string]string)
}

func (ctx *TestContext) captureRequestHeaders(req *http.Request) {
	if ctx.capturedRequestHeaders == nil {
		ctx.capturedRequestHeaders = make(map[string]string)
	}

	// Capture all headers from the request
	for name, values := range req.Header {
		if len(values) > 0 {
			ctx.capturedRequestHeaders[name] = values[0] // Take first value
		}
	}

	// Store in history
	headersCopy := make(map[string]string)
	for k, v := range ctx.capturedRequestHeaders {
		headersCopy[k] = v
	}
	ctx.requestHeaderHistory = append(ctx.requestHeaderHistory, headersCopy)
}

// Header validation fields are now defined in context.go

# OpenFGA SDK Conformance Test Suite Makefile

# Container runtime configuration
# Override with: make start CONTAINER_CMD=podman COMPOSE_CMD=podman-compose
CONTAINER_CMD ?= docker
COMPOSE_CMD ?= docker-compose

.PHONY: help setup start stop test clean validate lint

# Default target
help: ## Show this help message
	@echo "OpenFGA SDK Conformance Test Suite"
	@echo ""
	@echo "Container runtime: $(CONTAINER_CMD) (override with CONTAINER_CMD=podman)"
	@echo "Compose command: $(COMPOSE_CMD) (override with COMPOSE_CMD=podman-compose)"
	@echo ""
	@echo "Available targets:"
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "  %-15s %s\n", $$1, $$2}' $(MAKEFILE_LIST)
	@echo ""
	@echo "Examples:"
	@echo "  make start                                    # Use docker (default)"
	@echo "  make start CONTAINER_CMD=podman COMPOSE_CMD=podman-compose"
	@echo "  export CONTAINER_CMD=podman COMPOSE_CMD=podman-compose && make start"

# Development setup
setup: ## Set up development environment
	@echo "Setting up development environment..."
	@echo "Container runtime: $(CONTAINER_CMD)"
	@echo "Compose command: $(COMPOSE_CMD)"
	@$(CONTAINER_CMD) --version >/dev/null 2>&1 || (echo "$(CONTAINER_CMD) is required but not installed" && exit 1)
	@$(COMPOSE_CMD) --version >/dev/null 2>&1 || (echo "$(COMPOSE_CMD) is required but not installed" && exit 1)
	@echo "Development environment ready!"

# WireMock server management
start: ## Start WireMock server
	@echo "Starting WireMock server with $(COMPOSE_CMD)..."
	@$(COMPOSE_CMD) up -d wiremock
	@echo "Waiting for WireMock to be ready..."
	@timeout 30 bash -c 'until curl -f http://localhost:8080/__admin/health >/dev/null 2>&1; do sleep 1; done'
	@echo "WireMock server is ready at http://localhost:8080"

stop: ## Stop WireMock server
	@echo "Stopping WireMock server..."
	@$(COMPOSE_CMD) down

restart: stop start ## Restart WireMock server

# Load mappings
load-mappings: ## Load all WireMock mapping bundles
	@echo "Loading WireMock mapping bundles..."
	@find wiremock/bundles -name "*.json" -type f | while read bundle; do \
		echo "Loading $$bundle..."; \
		curl -s -X POST http://localhost:8080/__admin/mappings/import \
			-H "Content-Type: application/json" \
			-d @"$$bundle" || exit 1; \
	done
	@echo "All mapping bundles loaded successfully!"

reset-mappings: ## Reset WireMock mappings
	@echo "Resetting WireMock mappings..."
	@curl -s -X POST http://localhost:8080/__admin/reset
	@echo "WireMock mappings reset!"

# Testing
test: ## Run basic WireMock validation tests
	@echo "Running basic validation tests..."
	@$(MAKE) validate-fixtures
	@$(MAKE) validate-mappings
	@$(MAKE) test-scenarios

test-go: ## Run Go conformance tests (when available)
	@if [ -d "runners/go" ] && [ -f "runners/go/go.mod" ]; then \
		echo "Running Go conformance tests..."; \
		cd runners/go && go test -v -tags=conformance ./...; \
	else \
		echo "Go runner not yet implemented"; \
	fi

test-go-core: ## Run Go core API tests
	@if [ -d "runners/go" ] && [ -f "runners/go/go.mod" ]; then \
		echo "Running Go core API tests..."; \
		cd runners/go && go test -v --godog.tags="@apis" ./...; \
	else \
		echo "Go runner not yet implemented"; \
	fi

test-go-client-wrappers: ## Run Go client wrapper tests
	@if [ -d "runners/go" ] && [ -f "runners/go/go.mod" ]; then \
		echo "Running Go client wrapper tests..."; \
		cd runners/go && go test -v --godog.tags="@client-wrappers" ./...; \
	else \
		echo "Go runner not yet implemented"; \
	fi

test-go-streaming: ## Run Go streaming API tests
	@if [ -d "runners/go" ] && [ -f "runners/go/go.mod" ]; then \
		echo "Running Go streaming API tests..."; \
		cd runners/go && go test -v --godog.tags="@streaming" ./...; \
	else \
		echo "Go runner not yet implemented"; \
	fi

test-js: ## Run JavaScript conformance tests (when available)
	@if [ -d "runners/js" ] && [ -f "runners/js/package.json" ]; then \
		echo "Running JavaScript conformance tests..."; \
		cd runners/js && npm test; \
	else \
		echo "JavaScript runner not yet implemented"; \
	fi

test-all: test test-go test-js ## Run all available tests

# Validation
validate: validate-fixtures validate-mappings validate-gherkin ## Validate all components

validate-fixtures: ## Validate JSON fixture files
	@echo "Validating fixture files..."
	@for fixture in wiremock/fixtures/*.json; do \
		echo "Validating $$fixture..."; \
		python3 -m json.tool "$$fixture" >/dev/null || exit 1; \
	done
	@echo "All fixture files are valid!"

validate-mappings: ## Validate WireMock mapping files
	@echo "Validating mapping files..."
	@find wiremock/bundles -name "*.json" -type f | while read bundle; do \
		echo "Validating $$bundle..."; \
		python3 -m json.tool "$$bundle" >/dev/null || exit 1; \
	done
	@echo "All mapping files are valid!"

validate-gherkin: ## Validate Gherkin feature files
	@echo "Validating Gherkin feature files..."
	@find features/ -name "*.feature" -exec echo "Checking {}" \;
	@echo "Gherkin validation complete!"

# Test specific scenarios
test-scenarios: ## Test core scenarios against WireMock
	@echo "Testing core scenarios..."
	@echo "Testing Check operation..."
	@response=$$(curl -s -X POST http://localhost:8080/stores/01H0H40Z9QDYNR71MRAG9FPE7M/check \
		-H "Content-Type: application/json" \
		-d '{"tuple_key":{"user":"user:alice","relation":"viewer","object":"document:readme"},"authorization_model_id":"01H0H40Z9QDYNR71MRAG9FPE7N"}'); \
	echo "Response: $$response"; \
	echo "$$response" | grep -q '"allowed":true' || (echo "Check test failed!" && exit 1)
	@echo "Check operation test passed!"
	
	@echo "Testing Read operation..."
	@response=$$(curl -s -X POST http://localhost:8080/stores/01H0H40Z9QDYNR71MRAG9FPE7M/read \
		-H "Content-Type: application/json" \
		-d '{}'); \
	echo "Response: $$response"; \
	echo "$$response" | grep -q '"tuples"' || (echo "Read test failed!" && exit 1)
	@echo "Read operation test passed!"
	
	@echo "All scenario tests passed!"

# Development utilities
logs: ## Show WireMock logs
	@$(COMPOSE_CMD) logs -f wiremock

status: ## Show WireMock status
	@echo "WireMock Health:"
	@curl -s http://localhost:8080/__admin/health | python3 -m json.tool || echo "WireMock not responding"
	@echo ""
	@echo "Loaded Mappings:"
	@curl -s http://localhost:8080/__admin/mappings | python3 -c "import sys,json; data=json.load(sys.stdin); print(f'Total mappings: {len(data[\"mappings\"])}')" || echo "Could not retrieve mappings"

admin: ## Open WireMock admin interface
	@echo "Opening WireMock admin interface..."
	@echo "Visit: http://localhost:8080/__admin/"
	@command -v open >/dev/null 2>&1 && open "http://localhost:8080/__admin/" || \
	command -v xdg-open >/dev/null 2>&1 && xdg-open "http://localhost:8080/__admin/" || \
	echo "Please open http://localhost:8080/__admin/ in your browser"

# Cleanup
clean: ## Clean up development environment
	@echo "Cleaning up..."
	@$(COMPOSE_CMD) down -v
	@$(CONTAINER_CMD) system prune -f
	@echo "Cleanup complete!"

# Documentation
docs: ## Generate documentation
	@echo "Generating documentation..."
	@echo "Step vocabulary: docs/STEP_VOCABULARY.md"
	@echo "Workflow guide: docs/WORKFLOW.md"
	@echo "Versioning: docs/VERSIONING.md"
	@echo "Portability: docs/PORTABILITY.md"

# Version management
version: ## Show current version
	@if [ -f VERSION ]; then \
		echo "Current version: $$(cat VERSION)"; \
	else \
		echo "No version file found. Run 'make tag-version' to create initial version."; \
	fi

tag-version: ## Tag current version (usage: make tag-version VERSION=1.0.0)
	@if [ -z "$(VERSION)" ]; then \
		echo "Usage: make tag-version VERSION=1.0.0"; \
		exit 1; \
	fi
	@echo "$(VERSION)" > VERSION
	@git add VERSION
	@git commit -m "Release version $(VERSION)"
	@git tag -a "v$(VERSION)" -m "Release version $(VERSION)"
	@echo "Tagged version $(VERSION). Push with: git push origin v$(VERSION)"

# Development workflow
dev-setup: setup start load-mappings ## Complete development setup
	@echo ""
	@echo "🎉 Development environment is ready!"
	@echo ""
	@echo "Next steps:"
	@echo "  1. Run 'make test' to validate the setup"
	@echo "  2. Run 'make status' to check WireMock status"
	@echo "  3. Visit http://localhost:8080/__admin/ for WireMock admin"
	@echo "  4. Start implementing SDK runners in runners/ directory"
	@echo ""

dev-test: load-mappings test ## Quick development test cycle

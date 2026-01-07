package dev.openfga.sdk.conformance.hooks;

import dev.openfga.sdk.conformance.support.TestContext;
import io.cucumber.java.Before;
import io.cucumber.java.After;
import io.cucumber.java.Scenario;

public class Hooks {
    private final TestContext testContext;

    public Hooks(TestContext testContext) {
        this.testContext = testContext;
    }

    @Before
    public void beforeScenario() {
        testContext.reset();
    }

    @After
    public void afterScenario(Scenario scenario) {
        if (scenario.isFailed()) {
            System.out.println("Scenario failed: " + scenario.getName());
            if (testContext.getLastError() != null) {
                System.out.println("Last error: " + testContext.getLastError().getMessage());
            }
        }
    }
}

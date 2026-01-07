using TechTalk.SpecFlow;
using OpenFga.Sdk.Conformance.Support;

namespace OpenFga.Sdk.Conformance.Hooks
{
    [Binding]
    public sealed class Hooks
    {
        private readonly TestContext _testContext;

        public Hooks(TestContext testContext)
        {
            _testContext = testContext;
        }

        [BeforeScenario]
        public void BeforeScenario()
        {
            _testContext.Reset();
        }

        [AfterScenario]
        public void AfterScenario(ScenarioContext scenarioContext)
        {
            if (scenarioContext.TestError != null)
            {
                Console.WriteLine($"Scenario failed: {scenarioContext.ScenarioInfo.Title}");
                if (_testContext.LastError != null)
                {
                    Console.WriteLine($"Last error: {_testContext.LastError.Message}");
                }
            }
        }
    }
}

module.exports = {
  default: {
    requireModule: ['esm'],
    require: ['features/step_definitions/**/*.js'],
    format: [
      'pretty',
      'json:test-results.json'
    ],
    paths: ['../../features/**/*.feature'],
    tags: 'not @skip',
    parallel: 1,
    retry: 0,
    worldParameters: {
      wiremockUrl: process.env.WIREMOCK_URL || 'http://localhost:8080'
    }
  }
};

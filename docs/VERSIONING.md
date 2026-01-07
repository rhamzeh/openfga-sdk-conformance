# Versioning Strategy

This document outlines the versioning strategy for the OpenFGA SDK Conformance Test Suite.

## Semantic Versioning

The conformance suite follows [Semantic Versioning 2.0.0](https://semver.org/) with the format `MAJOR.MINOR.PATCH`.

### Version Components

#### MAJOR Version (X.y.z)
Increment when making **incompatible changes** that break existing SDK integrations:

- Breaking changes to step vocabulary
- Incompatible modifications to WireMock conventions
- Removal of existing scenarios or step definitions
- Changes to fixture schema that break existing runners

**Example**: Changing step `Given I have a client configured with store "..."` to `Given I configure a client with storeId "..."`

#### MINOR Version (x.Y.z)
Increment when adding **backward-compatible functionality**:

- New Gherkin scenarios
- Additional step definitions that don't conflict with existing ones
- New WireMock mapping bundles
- New fixture files
- New scenario tags or categories

**Example**: Adding new `@streaming` scenarios while keeping existing `@core` scenarios unchanged

#### PATCH Version (x.y.Z)
Increment for **backward-compatible bug fixes**:

- Corrections to existing WireMock mappings
- Fixes to response fixtures
- Documentation updates
- Typo corrections in feature files
- Bug fixes in existing step definitions

**Example**: Fixing a typo in a WireMock response fixture or correcting documentation

## Release Process

### Pre-release Versions

Use pre-release identifiers for development versions:
- `1.0.0-alpha.1` - Early development
- `1.0.0-beta.1` - Feature complete, testing phase
- `1.0.0-rc.1` - Release candidate

### Release Workflow

1. **Version Bump**: Update version in relevant files
2. **Changelog**: Document changes in `CHANGELOG.md`
3. **Tag Creation**: Create annotated git tag
4. **SDK Updates**: Update submodule references in SDK repositories

### Git Tagging

```bash
# Create annotated tag
git tag -a v1.2.3 -m "Release version 1.2.3"

# Push tag to remote
git push origin v1.2.3
```

## SDK Integration

### Submodule Pinning

SDKs integrate the conformance suite via git submodules, pinning to specific versions:

```bash
# Add conformance suite as submodule
git submodule add https://github.com/openfga/sdk-conformance.git conformance

# Pin to specific version
cd conformance
git checkout v1.2.3
cd ..
git add conformance
git commit -m "Pin conformance suite to v1.2.3"
```

### Version Compatibility Matrix

| Conformance Version | Go SDK | JS SDK | .NET SDK | Python SDK | Java SDK |
|-------------------|--------|--------|----------|------------|----------|
| v1.0.x            | ≥v0.3.0| ≥v0.3.0| ≥v0.3.0  | ≥v0.3.0    | ≥v0.3.0  |
| v1.1.x            | ≥v0.3.1| ≥v0.3.1| ≥v0.3.1  | ≥v0.3.1    | ≥v0.3.1  |

## Changelog Format

Follow [Keep a Changelog](https://keepachangelog.com/) format:

```markdown
# Changelog

## [1.2.3] - 2024-01-15

### Added
- New @streaming scenarios for NDJSON handling
- Support for context cancellation in streaming tests

### Changed
- Improved error messages in WireMock mappings

### Fixed
- Corrected fixture data for auth token refresh scenarios

### Deprecated
- Old step definition "Given I have configured client" (use "Given I have a client configured")

### Removed
- Obsolete @legacy scenarios

### Security
- Updated fixture data to remove sensitive test tokens
```

## Breaking Change Policy

### Deprecation Process

1. **Announce**: Document deprecation in changelog and docs
2. **Grace Period**: Maintain backward compatibility for at least one minor version
3. **Remove**: Remove deprecated functionality in next major version

### Migration Guides

For major version releases, provide migration guides:

```markdown
# Migration Guide: v1.x to v2.x

## Breaking Changes

### Step Vocabulary Changes
- `Given I have configured client` → `Given I have a client configured`
- `When I perform Check` → `When I call Check`

### WireMock Convention Changes
- Mapping IDs now use kebab-case instead of snake_case
- Request matching requires exact JSON equality
```

## Version Lifecycle

### Support Policy

- **Current Major**: Full support with new features and bug fixes
- **Previous Major**: Security fixes and critical bug fixes only
- **Older Majors**: End of life, no support

### End of Life Process

1. **Announcement**: 6 months notice before EOL
2. **Final Release**: Last patch release with EOL notice
3. **Archive**: Mark repository/branch as archived

## Automation

### CI/CD Integration

```yaml
# .github/workflows/release.yml
name: Release
on:
  push:
    tags: ['v*']
jobs:
  release:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Validate version format
        run: |
          if [[ ! "${{ github.ref_name }}" =~ ^v[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
            echo "Invalid version format"
            exit 1
          fi
      - name: Generate changelog
        run: |
          # Extract changelog for this version
          sed -n "/## \[${{ github.ref_name:1 }}\]/,/## \[/p" CHANGELOG.md > release-notes.md
      - name: Create GitHub release
        uses: actions/create-release@v1
        with:
          tag_name: ${{ github.ref }}
          release_name: Release ${{ github.ref }}
          body_path: release-notes.md
```

### Version Validation

Automated checks ensure version consistency:

- Git tag matches version in documentation
- Changelog entry exists for new version
- No breaking changes in minor/patch releases
- All tests pass before release

## Examples

### Version History Example

```
v0.1.0 - Initial release with @core scenarios
v0.2.0 - Added @auth scenarios
v0.2.1 - Fixed auth token fixture data
v0.3.0 - Added @headers and @client scenarios
v1.0.0 - First stable release
v1.1.0 - Added @streaming scenarios
v1.1.1 - Fixed streaming buffer size handling
v2.0.0 - Breaking: Redesigned step vocabulary
```

### SDK Version Pinning Example

```bash
# SDK repository updating conformance suite
git submodule update --remote conformance
cd conformance
git checkout v1.1.1
cd ..
git add conformance
git commit -m "Update conformance suite to v1.1.1"
```

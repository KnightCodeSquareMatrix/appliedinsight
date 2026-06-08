# Changelog

All notable changes to **应用能源：洞察** (Applied Energistics: Insight) are documented here.

## [0.9.0-beta] — 2026-06-08

### Branding
- Mod display name: **Applied Energistics: Insight** / **应用能源：洞察** (Mod ID: `appliedinsight`)
- Replaces working title *Applied Storage Sorter* and interim *Applied Conquer*

### Added
- **Smart Bus** — AE2 Part with JSON filter, in-game presets, bundled offline filter editor (`tools/filter-editor/`), up to 12 stack transfers/tick
- **Digital Asset Vault** — unified `digital_asset_vault` block: Cell absorption, import existing items, auto-accept, auto-expand
- **Sorter Command Block** terminal GUI — storage analysis, merge, profile-based sort
- AE2-themed GUI stack (`SorterBaseScreen`, dark_matter_v2 assets, GuideMe integration)
- Recipes & advancements for DAV, SCB, Smart Bus

### Changed
- Mod ID namespace: `appliedstoragesorter` → `appliedinsight`
- Config/logs/dumps paths: `config/appliedinsight/`, `logs/appliedinsight/`, `dumps/appliedinsight/`
- DAV consolidated from legacy + `new_digital_asset_vault` into single `digital_asset_vault`

### Removed (beta breaking)
- Legacy `digital_asset_vault` block/BE/GUI
- `new_digital_asset_vault` as separate block ID
- Digital Asset Management Card item & recipe
- `dark_matter_controller` unregistered (dev texture test only, sources kept)

### Known issues
- ADR-012 migration in progress (`StorageDiagnosis` vs legacy report types)
- Java package still `com.knightcode.appliedstoragesorter`; internal `NewDav*` class prefix retained
- Some orphaned assets from deprecated items remain on disk (harmless)

### Migration from pre-beta builds
1. Remove old mod JAR (`appliedstoragesorter-*.jar`)
2. Install `appliedinsight-0.9.0-beta.jar`
3. Copy `config/appliedstoragesorter/` → `config/appliedinsight/` if needed
4. Re-place mod blocks in worlds (old block entities will not load)

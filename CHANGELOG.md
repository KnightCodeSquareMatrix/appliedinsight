# Changelog

All notable changes to **应用能源：洞察** (Applied Energistics: Insight) are documented here.

## [0.9.2] — 2026-06-09

### Changed
- **Merge route (AE2-aligned)** — only **cell→cell** and **cell→external** moves; external storage buses are never merge sources (no pulling from drawers/chests)
- Cell→cell consolidates scattered items across internal drives/DAV; cell→external pushes into a Storage Bus that already holds the same item
- Zone move (`planAndMove`) uses the same source restriction; execution layer rejects external sources defensively

### Migration from 0.9.1
直接替换 JAR 即可。

## [0.9.1] — 2026-06-09

### Fixed
- **DAV GUI** — 修复主题工具栏首帧未初始化位置时的崩溃
- **DAV 输入槽** — 与 screen JSON 语义对齐（`appliedinsight_INPUT_CELL`）
- **DAV + JEI/EMI** — 拖拽扩容 Cell 后槽位显示即时刷新
- **DAV 容量显示** — GUI 正确计入内置基础容量（2048 字节 / 126 种）

### Added
- **DAV 手动扩容** — GUI「扩容一次」按钮，带详细状态反馈（缺 Cell、缺样板、合成中等）
- **DAV 自动扩容冷却** — 防止阈值触发后连续重试
- **DAV 战利品表** — 破坏方块正常掉落自身

### Changed
- **DAV 合成链路** — 实现 `ICraftingSimulationRequester`，改善 ME 自动合成预判
- **Smart Bus「有耐久」预设** — 文档与过滤器编辑器模板同步更新
- **README / Guidebook** — 围绕 DAV 聚合与 Smart Bus 过滤重写玩家说明
- GitHub 仓库链接统一为 `appliedinsight`

### Migration from 0.9.0-beta
直接替换 JAR 即可，无需改配置或重放方块。

## [0.9.0-beta] — 2026-06-08

### Branding
- Mod display name: **Applied Energistics: Insight** / **应用能源：洞察** (Mod ID: `appliedinsight`)
- Replaces working title *Applied Storage Sorter* and interim *Applied Conquer*

### Changed
- **Smart Bus「有耐久」预设** — 以 `minecraft:enchantable/durability` 标签为主，NBT `max_damage` / `damage` 兜底；修正仅查 NBT 时漏掉满耐久盔甲、易与攻击力混淆的问题

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

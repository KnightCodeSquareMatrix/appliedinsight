# 2026-06-04 Session Summary — Applied Energistics: Insight

## Quick Start for Next Agent

Read `docs/ai/AI_ENTRY.md` first, then this file.

## Build Status
**COMPILES** — `./gradlew compileJava compileTestJava` passes.

## Completed Changes

### 1. Architecture: ADR-012 (Ae2StorageAnalyzer elevation)
- `docs/ARCHITECTURE_DECISIONS.md` — ADR-012 + ADR-013 appended

### 2. Architecture Audit
- `docs/架构/RAW_ARCHITECTURE_AUDIT.md` — 715-line raw audit (7 dimensions)

### 3. RuntimeCell → CellInfo replacement (ADR-012 Stage 1A/C/D)
- **NEW**: `ae2/scan/CellInfo.java` — pure-data record (reference, zoneId, sourceBlockId, actionHost, storage, cellItemId, capacity)
- **DELETED**: `ae2/zone/RuntimeCell.java`
- **MODIFIED**: ZoneManager, RuntimeTopology, RuntimeZone, RuntimeZoneRegistryBuilder, Ae2StorageAnalyzer, SorterPlanFileLogger — all use CellInfo now
- **Zone.acceptItem()** — public API on RuntimeZone, cell selection encapsulated inside zone
- **ZonePlacementDecision** — no longer holds RuntimeCell, uses DriveCellReference+MEStorage+IActionHost

### 4. StorageDiagnosis (ADR-012 Stage 1B)
- **NEW**: `ae2/analysis/StorageDiagnosis.java` — shared analysis model with HealthSignal (severity), CellSignal (CellKind enum: INFINITE_KNOWN/INFINITE_SUSPECTED/HUGE/STANDARD/EXTERNAL/UNKNOWN)
- **MODIFIED**: `Ae2StorageAnalyzer.java` — new `analyze(RuntimeTopology, String dimensionId)` method alongside deprecated `analyze(IGrid)`
- Old consumers (SorterStorageAnalysisService, SorterStorageAnalysisDumpWriter) still use the deprecated method — backward compatible

### 5. New DAV Integration
- **DriveMachineAccessor**: added `appliedinsight:digital_asset_vault` to supported drives
- **RuntimeZoneRegistryBuilder**: now populates cellItemId and CellCapacity during topology build

### 6. Crafting Recipes
- `digital_asset_vault.json` — shaped 3x3: 2×ME Drive + 2×1k Cell + ME Controller → New DAV
- `sorter_command_block.json` — shaped 2×1: Quartz Glass + ME Controller → SCB (controller is placeholder)
- `smart_management_card.json` — shapeless: Paper + Glow Ink Sac → Smart Card
- All have advancements

### 7. SCB Block Model
- Changed from `cube_all` to `minecraft:block/orientable` (4-directional, terminal-like)
- Blockstate: `facing` property with 4 directions
- Textures: AE2 drive_front / drive (placeholder, needs custom texture)

### 8. SmartBus System (ADR-013 MVP)
**Current files after migration to AE2 part path:**
| File | Role |
|------|------|
| `ae2/part/SmartBusPart.java` | AE2 cable-bus part host; mode state, card slot inventory, part collision boxes, static model selection |
| `block/SmartBusMode.java` | Enum: IMPORT/EXPORT/STORAGE implements StringRepresentable |
| `menu/SmartBusMenu.java` | Lightweight menu bound to `SmartBusPart`, 1 card slot + player inventory |
| `client/screen/SmartBusScreen.java` | Simplified status panel for mode, card state, and filter summary |
| `network/SmartBusModePayload.java` | C→S for part mode cycling, addressed by host block pos + part side |
| `item/SmartManagementCardItem.java` | Right-click opens JSON config screen, stores filter in CUSTOM_DATA |
| `client/screen/SmartCardConfigScreen.java` | Filter-only JSON editor |
| `network/SmartCardConfigPayload.java` | C→S saves filter JSON to card CUSTOM_DATA |
| `menu/slot/SmartCardSlot.java` | Slot accepting only SmartManagementCardItem |
| `registry/SorterItems.java` | `smart_bus` now registered as AE2 `PartItem<SmartBusPart>` |

**Key migration result:**
- `smart_bus` is no longer implemented as an independent block for runtime use.
- Legacy `SmartBusBlock` / `SmartBusBlockEntity` sources were removed from the active path.
- Placement, outline preview, and collision now follow AE2 part mechanics because the item is a real AE2 part item.

**Models / resources:**
- Part runtime visuals reuse AE2 import/export/storage bus part models
- Item model now uses AE2 part-style item rendering instead of block-item fallback
- New language keys added for SmartBus screen and SmartManagementCard tooltip/config screen

**Interaction flow:**
- Right-click cable bus side with `smart_bus` part item → AE2 part placement path
- Empty-hand right-click part → open SmartBus GUI
- Empty-hand sneak-right-click part → cycle IMPORT→EXPORT→STORAGE (action bar feedback)
- Wrench sneak-right-click → PASS to AE2 part interaction path
- Right-click card in hand → filter JSON config screen

**NOT YET DONE:**
- Actual IMPORT/EXPORT/STORAGE runtime behavior (filter matching + item transfer / storage provider)
- SmartBus crafting recipe review in light of part-item semantics
- Dedicated darkmatter v2 SmartBus art direction
- Documentation ADR rewrite from old block wording to part wording (current ADR still contains historical block-based framing)

### 9. UI/UX feedback
- Mode cycle: action bar message "Switched to IMPORT/EXPORT/STORAGE mode"
- GUI empty card: orange hint "Insert a configured Smart Management Card"

## Pending / Deferred
- SCB and SmartBus textures (user wants to skip for now)
- `rule/route/` + `rule/zone/` deletion (~30 files) — user agreed they're unnecessary but wants to keep for now
- Logging boundary violations (4 files import net.minecraft/appeng) — deferred
- `SorterCommandResultPayload` network packet — still missing
- `ClientAnalysisPayloadHandler` — empty shim, needs cleanup
- Old `analyze(IGrid)` method — deprecated but still in use, migration deferred

## Key Architectural Decisions (New)
- **DAV is the default storage sink** — P2P handles routing, DAV absorbs everything else
- **SmartBus = one AE2 part, three modes** — card carries the filter, mode cycles via GUI or empty-hand sneak-right-click on the placed part
- **Filter DSL** (`rule/filter/`) retained — AE2's drag-and-drop filtering is insufficient
- **Zone/routing concept deprecated** — single DAV makes multi-zone routing unnecessary

## Files to Review for Next Session
- `docs/ARCHITECTURE_DECISIONS.md` — ADR-012, ADR-013
- `docs/架构/RAW_ARCHITECTURE_AUDIT.md` — dimension 7 (tech debt) for priority queue

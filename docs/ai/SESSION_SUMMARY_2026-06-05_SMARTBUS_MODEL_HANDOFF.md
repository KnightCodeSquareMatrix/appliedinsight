# 2026-06-05 Session Summary — SmartBus Model Handoff

## Quick Start

Read in this order:
1. `docs/ai/AI_ENTRY.md`
2. This file
3. If AE2 internals are needed, use the local source checkout at `ae2_source/Applied-Energistics-2-1.21.1`

## What Changed This Session

### 1. SmartBusMode now has EMPTY state

File: `src/main/java/com/knightcode/appliedinsight/block/SmartBusMode.java`

- Added `EMPTY("empty")` as first enum value (default)
- `next()`: EMPTY → IMPORT → EXPORT → STORAGE → IMPORT (never returns to EMPTY)
- `fromStoredName()`: unknown values return EMPTY
- SmartBusPart defaults to EMPTY on placement, `isSleeping()` includes EMPTY check

### 2. Card system removed, filter stored directly in SmartBusPart

Filter JSON is now stored as `String filterJson` in SmartBusPart NBT under key `"smart_filter"`.

Files deleted:
- `SmartManagementCardItem.java`
- `SmartCardConfigScreen.java`
- `SmartCardConfigPayload.java`
- `SmartCardSlot.java`
- `smart_management_card` recipe, advancement, item model, language keys

Files modified:
- `SmartBusPart.java` — `getFilterJson()`, `hasFilter()`, `hasValidFilter()`, `setFilterJson()`
- `SmartBusMenu.java` — removed card slot, only player inventory remains
- `SmartBusScreen.java` — redesigned to 238×220 wide screen with EditBox + Save/Clear/Delete buttons
- `SmartBusFilterPayload.java` (new) — client→server filter sync
- `AppliedStorageSorter.java` — replaced SmartCardConfigPayload with SmartBusFilterPayload
- `SorterItems.java` — removed card registration
- `SorterCreativeTabs.java` — removed card from creative menu
- Language files en_us + zh_cn updated

### 3. SmartBus model system: 4 mode-dependent PartModels

Each mode has its own self-contained model JSON:

| Mode | Model JSON | Front Texture | Symbol |
|------|-----------|---------------|--------|
| EMPTY | `smart_bus_empty.json` | `smart_bus_front_empty.png` | none (dark) |
| IMPORT | `smart_bus_import.json` | `smart_bus_front_import.png` | cyan arrow |
| EXPORT | `smart_bus_export.json` | `smart_bus_front_export.png` | amber arrow |
| STORAGE | `smart_bus_storage.json` | `smart_bus_front_storage.png` | green `[` bracket |

**Geometry**: Single cuboid element `[2,2,0] - [14,14,6]` (12×12×6 box).

**Collision box**: `bch.addBox(2, 2, 10, 14, 14, 16)`.

**Switching**: `SmartBusPart.getStaticModels()` returns the PartModel matching `SmartBusPart.mode`. This is the same pattern AE2's own `IOBusPart` uses.

**Registration**: All 4 model ResourceLocations registered via `PartModels.registerModels()` in `SorterItems.register()`.

### 4. IMPORT runtime behavior

`SmartBusPart` implements `IGridTickable`:
- Ticks only when mode == IMPORT and hasValidFilter() == true
- Uses `PartAdjacentApi<IItemHandler>` to access adjacent inventory
- Import algorithm: simulate ME insert → extract from adjacent → insert into ME → return overflow
- Extensive `[SmartBus]` logging at INFO (always) and VERBOSE (when `Config.VERBOSE_LOGGING` is true)

## Current Problem: Model Texture Placement

### What the user observes

The mode symbol textures are appearing on the wrong faces and at incorrect orientations. The user provided a screenshot showing the textures are misaligned.

### Root cause hypothesis

In AE2's part coordinate system:
- **north face** (z=0 in model) = the face pointing AWAY from the cable, toward the player during placement
- **south face** (z=max) = the face touching the cable
- **east/west/up/down** = the four peripheral faces around the cable direction

The AE2 model baker bakes the model in the default orientation. When the part is placed on a specific cable face, AE2's `QuadRotator` applies a rotation to the model quads based on `part.getSide()` and `part.getSpin()`. This means:

1. **The model JSON coordinates are in a canonical orientation** (part on north side of cable)
2. **At render time, AE2 rotates the quads** to face the actual placement direction
3. **The north face in the model JSON maps to the face pointing away from the cable block**

### Files involved

| File | Role |
|------|------|
| `assets/appliedinsight/models/part/smart_bus_empty.json` | EMPTY model (1 element, all faces use `#side` texture) |
| `assets/appliedinsight/models/part/smart_bus_import.json` | IMPORT model (east/west/up/down use `#front` = arrow texture) |
| `assets/appliedinsight/models/part/smart_bus_export.json` | EXPORT model |
| `assets/appliedinsight/models/part/smart_bus_storage.json` | STORAGE model |
| `assets/appliedinsight/textures/part/smart_bus_front_*.png` | Mode symbol textures |
| `assets/appliedinsight/textures/part/smart_bus_sides.png` | Side texture |
| `SmartBusPart.java` | `getStaticModels()` + `getBoxes()` |
| `SorterItems.java` | `PartModels.registerModels()` call |

### What needs to be fixed

1. **Face assignment**: Confirm which faces should show the mode symbol. The four faces perpendicular to the cable direction should show the mode indicator (the adjacent inventory could be on any of those four sides). The face facing away from the cable (front) and the face touching the cable (back) should be plain.

2. **UV orientation**: The texture UV coordinates may need flipping/rotating so the arrows point in the correct direction relative to the adjacent inventory. Currently the UVs may cause texture mirroring or wrong rotation on certain faces.

3. **Face-specific UVs**: Each face uses its own UV mapping. The `down` face may need `uv` that differs from `up` to avoid texture mirroring on the bottom.

### How AE2 parts handle orientation

AE2's own part models (e.g., `ae2:models/part/storage_bus_base.json`) use:
- `north` with `texture: "#front"` — this is the outward-facing face
- `south` with `texture: "#back"` — this is the cable-facing face
- `east/west/up/down` with `texture: "#sides"` — these are the peripheral faces

The indicator sub-models (`import_bus_off.json`, etc.) use faces on `east/west/up/down` as well.

### Reference AE2 files

- `ae2_source/Applied-Energistics-2-1.21.1/src/main/resources/assets/ae2/models/part/storage_bus_base.json` — canonical part model structure
- `ae2_source/Applied-Energistics-2-1.21.1/src/main/java/appeng/client/render/cablebus/CableBusBakedModel.java` — quad rotation logic (lines 193-201)
- `ae2_source/Applied-Energistics-2-1.21.1/src/main/java/appeng/client/render/cablebus/QuadRotator.java` — how faces are remapped

## Current Build Status

- `./gradlew compileJava` passes
- `./gradlew build` passes
- IMPORT runtime works correctly (verified in-game with filters)
- Model switches correctly per mode
- Textures are loading but placed on wrong faces

## Next Step for Agent

1. Read the current model JSONs in `assets/appliedinsight/models/part/`
2. Read AE2's `storage_bus_base.json` as reference for correct face→texture mapping
3. Fix face assignment and UV mapping so:
   - Mode symbols appear on the four faces perpendicular to the cable direction
   - Plain dark texture on the outward-facing and cable-facing faces
   - UVs are consistent across faces (no mirroring/flipping)
4. `./gradlew build` to verify

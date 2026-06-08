# 2026-06-05 Session Summary — SmartBus Part Migration Handoff

## Quick Start

Read in this order:
1. `docs/ai/AI_ENTRY.md`
2. `docs/ARCHITECTURE_DECISIONS.md` — focus on ADR-013 and ADR-014
3. This file
4. If AE2 internals are needed, use the local source checkout at `ae2_source/Applied-Energistics-2-1.21.1`

## What Changed This Session

### 1. SmartBus implementation was pivoted from fake block to real AE2 part

The previous SmartBus path tried to emulate AE2 buses with:
- `block/SmartBusBlock.java`
- `blockentity/SmartBusBlockEntity.java`

That path was wrong for the product goal because it could not get:
- AE2 cable-bus attachment semantics
- AE2 part collision boxes
- AE2 white placement preview / part outline
- AE2 part-host based menu addressing

The active implementation path is now:
- `src/main/java/com/knightcode/appliedinsight/ae2/part/SmartBusPart.java`
- `src/main/java/com/knightcode/appliedinsight/menu/SmartBusMenu.java`
- `src/main/java/com/knightcode/appliedinsight/client/screen/SmartBusScreen.java`
- `src/main/java/com/knightcode/appliedinsight/network/SmartBusModePayload.java`
- `src/main/java/com/knightcode/appliedinsight/item/SmartManagementCardItem.java`
- `src/main/java/com/knightcode/appliedinsight/client/screen/SmartCardConfigScreen.java`
- `src/main/java/com/knightcode/appliedinsight/network/SmartCardConfigPayload.java`
- `src/main/java/com/knightcode/appliedinsight/menu/slot/SmartCardSlot.java`

### 2. `smart_bus` item is now an AE2 `PartItem`

File:
- `src/main/java/com/knightcode/appliedinsight/registry/SorterItems.java`

Current behavior:
- `smart_bus` is registered as `PartItem<SmartBusPart>`
- part model resource locations are registered via `appeng.api.parts.PartModels.registerModels(...)`

Meaning:
- placement now goes through AE2 part placement path
- collision / preview / host attachment are governed by AE2 part system, not our custom block logic

### 3. Legacy SmartBus block runtime path was removed from active use

Files removed from active source path:
- `src/main/java/com/knightcode/appliedinsight/block/SmartBusBlock.java`
- `src/main/java/com/knightcode/appliedinsight/blockentity/SmartBusBlockEntity.java`

Also removed from registries:
- `SorterBlocks.SMART_BUS`
- `SorterBlockEntities.SMART_BUS`

Important:
- Some historical docs still reference the old block-based path; treat those as obsolete unless explicitly updated.

### 4. Card responsibility was narrowed to filter-only

Current behavior:
- `SmartManagementCardItem` stores filter JSON only
- `SmartCardConfigScreen` edits filter JSON only
- mode is no longer card-owned

Product model now is:
- part owns mode
- card owns filter

### 5. GUI path now follows part semantics

Current behavior:
- empty-hand right-click on placed part opens `SmartBusMenu`
- empty-hand sneak-right-click cycles `IMPORT -> EXPORT -> STORAGE`
- mode-cycle network packet addresses the part by host `BlockPos + side`

Key files:
- `SmartBusPart.java`
- `SmartBusMenu.java`
- `SmartBusScreen.java`
- `SmartBusModePayload.java`

### 6. Resource layer was updated enough to support the part path

Current resource intent:
- runtime part models reuse AE2 import/export/storage bus model semantics
- item model no longer falls back to block-item behavior
- language keys were added for:
  - SmartBus screen
  - Smart management card tooltip
  - Smart card config screen
  - localized mode display names

Files:
- `src/main/resources/assets/appliedinsight/models/item/smart_bus.json`
- `src/main/resources/assets/appliedinsight/lang/en_us.json`
- `src/main/resources/assets/appliedinsight/lang/zh_cn.json`

## Build Status

Verified in this session:
- `./gradlew compileJava` passes
- `./gradlew build` passes

This means the current codebase compiles after the SmartBus part migration.

## Architecture Decisions Updated

### ADR status
- `docs/ARCHITECTURE_DECISIONS.md`
- ADR-014 was updated from a proposed block cleanup plan into an adopted AE2-part migration record

High-level decision now is:
- SmartBus is not a standalone block
- SmartBus is one AE2 part with three modes
- card carries filter, part carries mode

## Known Current State

### Completed foundation
- Correct host layer selected: AE2 part system
- Compilation/build green
- Menu/screen/payload/card flow moved onto part path
- Legacy fake-block runtime removed

### Not yet implemented
These are the next real tasks. They are no longer architecture pivots; they are feature implementation tasks.

1. **IMPORT runtime logic**
- pull matching items from adjacent inventory into ME network
- likely implementation home: `SmartBusPart.java`
- likely dependencies: existing rule/filter matcher code + AE2 adjacent capability access patterns

2. **EXPORT runtime logic**
- pull matching items from ME network into adjacent inventory
- likely implementation home: `SmartBusPart.java`

3. **STORAGE runtime logic**
- expose adjacent storage to ME network like a storage bus, but filtered by card DSL
- may need AE2-style `IStorageProvider` / capability forwarding approach
- this is probably the most complex of the three modes

4. **Filter execution wiring**
- current UI and card storage exist
- runtime matching still needs to connect to existing filter DSL / matcher implementation

5. **Recipe / progression review**
- `smart_bus` was originally thought of as a block
- now it is a part item, so recipe expectations may need a design pass

6. **Art direction**
- current visuals intentionally reuse AE2 assets/semantics to get correctness first
- dedicated darkmatter v2 style is still pending

## Files Most Relevant For Next Session

### Our implementation
- `src/main/java/com/knightcode/appliedinsight/ae2/part/SmartBusPart.java`
- `src/main/java/com/knightcode/appliedinsight/menu/SmartBusMenu.java`
- `src/main/java/com/knightcode/appliedinsight/client/screen/SmartBusScreen.java`
- `src/main/java/com/knightcode/appliedinsight/network/SmartBusModePayload.java`
- `src/main/java/com/knightcode/appliedinsight/item/SmartManagementCardItem.java`
- `src/main/java/com/knightcode/appliedinsight/client/screen/SmartCardConfigScreen.java`
- `src/main/java/com/knightcode/appliedinsight/network/SmartCardConfigPayload.java`
- `src/main/java/com/knightcode/appliedinsight/registry/SorterItems.java`
- `src/main/java/com/knightcode/appliedinsight/registry/SorterMenus.java`

### Existing filter DSL / related project logic
Search around:
- `rule/filter/`
- existing matcher/evaluator classes already used elsewhere in the mod

### AE2 reference source
Use local checkout instead of Gradle cache when possible:
- `ae2_source/Applied-Energistics-2-1.21.1/AI_ENTRY.md`
- `ae2_source/Applied-Energistics-2-1.21.1/INSTRUCTIONS.md`
- `ae2_source/Applied-Energistics-2-1.21.1/CLASS_INDEX.md`

Especially relevant AE2 concepts/classes:
- `appeng.api.parts.IPart`
- `appeng.parts.AEBasePart`
- `appeng.parts.automation.IOBusPart`
- `appeng.parts.automation.ImportBusPart`
- `appeng.parts.automation.ExportBusPart`
- `appeng.parts.storagebus.StorageBusPart`
- `appeng.block.networking.CableBusBlock`
- `appeng.blockentity.networking.CableBusBlockEntity`
- `appeng.parts.CableBusContainer`
- `appeng.hooks.RenderBlockOutlineHook`
- `appeng.api.parts.PartHelper`
- `appeng.items.parts.PartItem`

## Important Warnings For Next Agent

1. Do not reintroduce SmartBus as a standalone block unless the user explicitly changes direction.
2. Do not treat old block-based SmartBus docs as ground truth; the code has already pivoted.
3. Prefer the local AE2 source checkout over jar extraction for future source reading.
4. The repository has many unrelated in-flight changes; do not summarize overall repo state as if SmartBus was the only workstream.
5. The current milestone is “correct AE2 part foundation”, not “runtime feature complete”.

## Suggested Next Step

Start with a narrow vertical slice:
1. Implement IMPORT mode runtime only
2. Verify compile/build
3. If possible, prepare for manual in-game verification
4. Then add EXPORT
5. Leave STORAGE for last, since it will likely require the deepest AE2 integration

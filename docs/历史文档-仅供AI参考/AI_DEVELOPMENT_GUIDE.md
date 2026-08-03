# AI Development Guide

This document is for AI assistants collaborating on this Minecraft mod project.

## Project Snapshot

- Project: `appliedinsight-template-1.21.1`
- Mod ID: `appliedinsight`
- Group: `com.knightcode.appliedstoragesorter`
- Minecraft: `1.21.1`
- NeoForge: `21.1.224`
- ModDevGradle: `2.0.141`
- Java: `21`
- Mappings: Mojang + Parchment (`2024.11.17` for MC `1.21.1`)

## Important Constraints

1. This project uses **NeoForge 1.21.1** APIs only.
2. Do **not** assume old Forge APIs, Fabric APIs, or older NeoForge registration patterns.
3. Do **not** rely on `genSources`. This project does not expose that task.
4. In this Continue + WSL environment, do **not** rely on direct terminal stdout/stderr for verification. Prefer writing results to files under `tmp/` or `tmp/api-cache/` and then reading those files.
5. Prefer the local extracted source index and local source trees for API/source lookup before ad-hoc jar inspection.
6. Minimize hallucination: if an API call is uncertain, inspect it first.
7. Distinguish **AE2 API classes** from **AE2 implementation classes**. This project currently compiles against the AE2 full mod jar as a `compileOnly` dependency, so implementation packages may be imported when they are an intentional part of this project's integration boundary.

## Source Layout

- Main mod entry: `src/main/java/com/knightcode/appliedstoragesorter/AppliedStorageSorter.java`
- Client-only entry: `src/main/java/com/knightcode/appliedstoragesorter/AppliedStorageSorterClient.java`
- Config example: `src/main/java/com/knightcode/appliedinsight/Config.java`
- Resources: `src/main/resources/`
- JSON schema resources: `src/main/resources/schema/`
- Generated mod metadata template: `src/main/templates/META-INF/neoforge.mods.toml`
- Generated resources output: `src/generated/resources/`

## Existing Project Patterns

Current code already demonstrates these preferred patterns:

- `@Mod(appliedinsight.MODID)` for the main mod class
- Constructor injection with `IEventBus` and `ModContainer`
- Mod event bus listeners via `modEventBus.addListener(...)`
- Command registration via `NeoForge.EVENT_BUS.addListener(...)`
- Client-only setup in a separate `@Mod(..., dist = Dist.CLIENT)` class
- File-based sorter logging instead of relying on the Minecraft console

When extending the mod, prefer staying stylistically consistent with these patterns.

## Build and Run Commands

Use these commands from the project root:

```bash
python3 tools/extract_api_sources.py
bash tools/source_lookup.sh appeng.api.networking.IGrid
bash tools/build_api_index.sh
./gradlew build
./gradlew classes
./gradlew runClient
./gradlew runServer
./gradlew runData
./gradlew tasks --all
./gradlew analyzeSorterDump -PdumpFile=src/main/resources/testfiles/me-dump-20260517-033650.json -PanalysisOutput=tmp/analysis.txt
./gradlew routingProfileJsonCli
./gradlew filterUiMetadataCli -PcliArgs="write-default tmp/filter-ui-metadata.json"
./gradlew analyzeSorterDumpRouting -PprofileFile=src/main/resources/testfiles/routing-profile-example.json -PdumpFile=src/main/resources/testfiles/me-dump-20260517-033650.json -PanalysisOutput=tmp/routing-analysis.txt
./gradlew analyzeSorterDumpRoutingSuggestions -PprofileFile=src/main/resources/testfiles/routing-profile-example.json -PdumpFile=src/main/resources/testfiles/me-dump-20260517-033650.json -PanalysisOutput=tmp/routing-suggestions.txt
./gradlew generateRoutingProfileDraft -PprofileFile=src/main/resources/testfiles/routing-profile-example.json -PdumpFile=src/main/resources/testfiles/me-dump-20260517-033650.json -PoutputProfile=tmp/generated-routing-profile.json -PanalysisOutput=tmp/generated-routing-profile.notes.txt
```

Notes:

- `genSources` is not available in this build.
- `build/` already contains ModDevGradle-generated artifacts after a successful setup/build.
- Common useful artifact: `build/moddev/artifacts/neoforge-21.1.224-sources.jar`
- Extracted local source trees are written under `tmp/api-src/`.
- The extracted source index is written to `tmp/api-cache/source-index.tsv`.

## Low-Token API Verification Workflow

This repository includes helper scripts under `tools/` to support a file-oriented lookup workflow that works better in Continue + WSL.

### Available Tools

#### `tools/extract_api_sources.py`
Extracts verified local source jars into:

- `tmp/api-src/neoforge_merged_sources/`
- `tmp/api-src/ae2_full_sources/`

And builds:

- `tmp/api-cache/source-index.tsv`
- `tmp/api-cache/source-index-summary.txt`

This is now the preferred source lookup workflow when you need to inspect concrete classes or package layout.

#### `tools/source_lookup.sh`
Usage:

```bash
bash tools/source_lookup.sh appeng.api.networking.IGrid
bash tools/source_lookup.sh ControllerBlockEntity
```

This searches `tmp/api-cache/source-index.tsv` and returns matching fully-qualified classes and extracted source paths.

#### `tools/build_api_index.sh`
Scans local Gradle caches and builds:

- `tmp/api-cache/jar-index.txt`
- `tmp/api-cache/class-index.tsv`

This creates a lookup table from fully-qualified class names to jar paths.

#### `tools/javap_lookup.sh`
Usage:

```bash
tools/javap_lookup.sh net.neoforged.neoforge.registries.DeferredRegister
```

Or write to a file:

```bash
tools/javap_lookup.sh net.minecraft.world.level.block.entity.BlockEntityType tmp/api-cache/dumps/BlockEntityType.txt
```

This uses `javap -public` to inspect callable API surface only:

- public constructors
- public methods
- public fields
- inheritance/interfaces

#### `tools/dump_known_apis.sh`
Batch-dumps the classes listed in `tools/javap_targets.txt` into `tmp/api-cache/dumps/`.

## Recommended AI Workflow

When asked to implement or explain code:

1. Identify the exact classes involved.
2. First check the extracted source index with `tools/source_lookup.sh` or by reading files under `tmp/api-src/`.
3. If you need callable signatures, inspect them with `tools/javap_lookup.sh` and write results to files under `tmp/` or `tmp/api-cache/`.
4. Base code suggestions on verified public signatures.
5. Reuse project-local conventions before introducing new architecture.
6. Keep changes small and incremental.
7. Avoid speculative imports or methods that have not been confirmed.
8. For AE2, verify whether a class is part of the API surface or only an implementation detail before importing it into mod code.

## What the AI Should Prefer

### Prefer

- Verified NeoForge 1.21.1 APIs
- Existing registration style from this project
- Small edits over broad rewrites
- File-based verification through `tmp/` artifacts
- The extracted source index under `tmp/api-cache/source-index.tsv`
- Public API inspection through `javap`
- Explicit package names when discussing unfamiliar classes

### Avoid

- Suggesting `genSources`
- Suggesting legacy Forge event/registry code without verification
- Assuming MCP/older mapping names
- Relying on direct terminal stdout/stderr in this Continue + WSL environment
- Importing AE2 implementation-only classes into mod code just because they exist in extracted sources
- Replacing working project structure with unrelated abstractions

## Common Tasks and How to Approach Them

### Add a new command
Check these first:

- `net.neoforged.neoforge.event.RegisterCommandsEvent`
- `net.minecraft.commands.Commands`
- `net.minecraft.commands.CommandSourceStack`

Prefer logging detailed debug output to a dedicated sorter log file instead of Minecraft console spam.

### Add AE2 integration
Check these first:

- `appeng.api.networking.IGrid`
- `appeng.api.networking.IGridNode`
- `appeng.api.networking.IManagedGridNode`
- relevant extracted AE2 sources under `tmp/api-src/ae2_full_sources/`

Before importing a class, verify whether it belongs to the intentional integration boundary for this mod. API classes are preferred where practical, but AE2 implementation classes are now allowed when the feature explicitly depends on them.

### Add use interaction code
Check:

- `net.minecraft.world.item.context.UseOnContext`
- `net.minecraft.world.level.block.Block`
- `net.minecraft.world.item.Item`

Verify method signatures before overriding interaction hooks.

### Register data generation logic
Use the existing `data` run configuration in `build.gradle`.
Output goes to:

- `src/generated/resources/`

## Project Facts Worth Remembering

- The mod metadata file is generated from `src/main/templates/META-INF/neoforge.mods.toml`.
- `sourceSets.main.resources` already includes `src/generated/resources`.
- `idea.module.downloadSources = true` is enabled, but the project now also maintains extracted local sources under `tmp/api-src/` for low-friction lookup.
- `Config.java` is currently an example config scaffold and may be simplified or replaced as real features are added.
- The current command/debugging workflow prefers a dedicated sorter log file over the noisy Minecraft console.
- The current AE2 dependency setup is `compileOnly` on the full AE2 mod jar plus runtime loading of the full AE2 mod, so implementation imports are legal when they are part of the project's chosen integration boundary.
- The project now also contains a **pure Java rule/routing layer** under `com.knightcode.appliedstoragesorter.rule.*`.
- The project now also contains a **profile generation layer** under `com.knightcode.appliedstoragesorter.profilegen.*` intended to be called by analyzers rather than hard-wired into one tool.
- The filter model now uses a **tree expression structure** (`FilterExpression` / `FilterGroup` / `FilterCondition`) aligned with React Query Builder.
- JSON schema resources now live under `src/main/resources/schema/`.
- `routing-profile.schema.json` is the configuration-facing schema and should be treated as the primary interchange contract for future editors.
- `sorter-network-dump.schema.json` describes the dump shape for tooling/interoperability; dump data is generally considered trustworthy when produced by this mod, so the schema is mainly a structural contract rather than a hostile-input validation layer.
- Filter UI metadata can be exported through `FilterUiMetadataCli` for React Query Builder field/operator/combinator setup.
- The routing profile JSON format is intended to be reusable by future React/Web configuration tooling.
- Current standalone analysis entry points include dump analysis, routing analysis, fallback suggestion analysis, draft profile generation, and filter UI metadata export, all runnable without launching Minecraft.

## Suggested Response Style for AI Assistants

When answering development questions for this repository:

1. State the exact class or API being used.
2. Mention whether it was verified from extracted local sources, `javap`, or both.
3. Keep explanations focused on usage, not implementation internals.
4. If uncertain, inspect the relevant class with the provided tools before coding.
5. Prefer actionable next steps over long theoretical explanations.

## Example Prompt Patterns

Good prompts for AI in this repo:

- "Use `tools/source_lookup.sh` to find the relevant AE2 grid classes."
- "Check `RegisterCommandsEvent` and add a new command registration."
- "Verify whether this AE2 class is API-visible before importing it."
- "Only use APIs verified from extracted sources or `tools/` scripts."

## Bottom Line

For this project, the safest AI workflow is:

- **prefer `tmp/api-cache/source-index.tsv` and `tmp/api-src/` for local source lookup**
- **use `javap` when callable public signatures matter**
- **write diagnostics to files instead of trusting interactive terminal output**
- **follow existing project conventions**
- **separate AE2 API classes from AE2 implementation classes before importing, but remember this project may intentionally use implementation classes when needed**
- **prefer keeping dump/routing/suggestion analyzers pure Java and runnable without Minecraft where practical**
- **treat routing profile JSON as a stable interchange format for future tooling**
- **treat `src/main/resources/schema/routing-profile.schema.json` as the config-facing schema contract**
- **treat the filter expression tree plus `FilterUiMetadataCli` output as the backend authority for React Query Builder integration**
- **treat dump schema mainly as a structure/documentation contract, not as a sign that dump inputs are inherently untrusted**
- **treat `RoutingProfileGenerator` as an interface boundary so smarter generation logic can replace heuristic implementations without changing analyzer call sites**

That is the default development policy for AI assistance in this repository.
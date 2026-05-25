# Applied Storage Sorter — Project Instructions

## First Read
Read `docs/ai/AI_ENTRY.md` first — it contains the complete project context, architecture rules, and common modification patterns.

## Quick Facts
- **Mod ID**: `appliedstoragesorter`, Minecraft 1.21.1, NeoForge 21.1.224, AE2 19.2.17
- **Build**: `./gradlew build copyModJarToPrism` (build + auto-deploy)
- **Two dump types**: `me-dump` (micro facts) and `storage-analysis` (macro analysis)
- **Cell capacity**: shared via `CellCapacityInspector.java`, reads `StorageCell` via reflection

## Architecture Rules
1. `rule/**` must NOT import `net.minecraft.*` or `appeng.*` — pure rule models only
2. Keep `route/zone/cell/execute` separated — don't mix them in one class
3. Loggers are infrastructure — don't pollute core classes with logging concerns
4. DAV/management cards are player input only — not routing engines
5. JSON changes must update `docs/日志与JSON字段契约.md` AND the frontend prompt

## Key Entry Points
| File | Purpose |
|------|---------|
| `AppliedStorageSorter.java` | Mod main class |
| `SorterCommands.java` | Command registration |
| `SorterNetworkDumpWriter.java` | me-dump writer |
| `Ae2StorageAnalyzer.java` | storage-analysis writer |
| `CellCapacityInspector.java` | Shared cell capacity reader |
| `RoutingEngine.java` | Route decision engine |
| `Ae2ZoneMoveExecutor.java` | Execution engine |

## Frontend
There is a separate React frontend project. See `docs/ai/frontend-vibe-coding-prompt.md` for the data contract and component design.

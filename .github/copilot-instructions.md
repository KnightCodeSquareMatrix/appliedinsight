# Applied Insight — Project Instructions

> GitHub Copilot 项目级指令。首次打开此项目时，Copilot 会自动读取此文件。

---

## First Read

Read `docs/ai/AI_ENTRY.md` first — it contains the complete project context, architecture rules, and common modification patterns.

---

## Quick Facts

- **Mod ID**: `appliedinsight`, Minecraft 1.21.1, NeoForge 21.1.224, AE2 19.2.17
- **Java**: 21, **Gradle**: 9.2.1
- **Build**: `./gradlew build copyModJarToPrism` (build + auto-deploy to PrismLauncher)
- **Two dump types**: `me-dump` (micro facts) and `storage-analysis` (macro analysis)
- **Cell capacity**: shared via `CellCapacityInspector.java`, reads `StorageCell` via reflection
- **Three capability lines**: merge (quick merge), me plan (zone governance), storageDump (health diagnosis)

---

## Architecture Rules

1. `rule/**` must NOT import `net.minecraft.*` or `appeng.*` — pure rule models only
2. Keep `route/zone/cell/execute` separated — don't mix them in one class
3. Loggers are infrastructure — don't pollute core classes with logging concerns
4. DAV/management cards are player input only — not routing engines
5. `network/` package is for serialization/transport only — no business logic
6. `block/` + `blockentity/` are for block declaration and lifecycle only
7. JSON changes must update `docs/日志与JSON字段契约.md` AND the frontend prompt

---

## Key Entry Points

| File | Purpose |
|------|---------|
| `appliedinsight.java` | Mod main class (event registration, network packet registration) |
| `SorterCommands.java` | Command registration (Brigadier) |
| `SorterNetworkDumpWriter.java` | me-dump writer |
| `SorterStorageAnalysisDumpWriter.java` | storage-analysis writer |
| `Ae2StorageAnalyzer.java` | Storage analysis engine |
| `CellCapacityInspector.java` | Shared cell capacity reader (reflection-based) |
| `RoutingEngine.java` | Route decision engine |
| `Ae2ZoneMoveExecutor.java` | Execution engine |
| `SorterCommandBlock.java` | GUI command block |
| `FileChunkedSender.java` | Chunked file transfer (128KB chunks) |

---

## Documentation Index

| Document | Purpose |
|----------|---------|
| `docs/ARCHITECTURE_REFERENCE.md` | Architecture overview with data flow diagrams |
| `docs/ARCHITECTURE_DECISIONS.md` | Architecture Decision Records (ADR-001 ~ ADR-008) |
| `docs/GLOSSARY.md` | Terminology (Chinese-English cross-reference) |
| `docs/整体逻辑.md` | Project main skeleton |
| `docs/类职责总览.md` | Class review conclusions |
| `docs/类职责/索引.md` | Index of 129 class responsibility docs |
| `docs/API_REFERENCE.md` | Backend API reference |
| `docs/COMMANDS_REFERENCE.md` | Complete command reference |
| `docs/EXTENSION_GUIDE.md` | Extension development guide |
| `docs/TESTING_GUIDE.md` | Testing guide |
| `docs/DEVELOPER_QUICKSTART.md` | Developer quick start |
| `docs/DEVELOPER_GUIDE.md` | Full developer guide |
| `docs/日志与JSON字段契约.md` | JSON field contract |
| `docs/前端对接说明.md` | Frontend contract |
| `docs/dashboard的设计哲学.md` | Dashboard design philosophy |
| `docs/下一步计划.md` | Current phase plan |
| `docs/ai/AI_ENTRY.md` | AI assistant entry point (complete) |
| `docs/ai/AI_DEVELOPMENT_GUIDE.md` | AI development guide (quick reference) |
| `docs/ai/CODE_REVIEW_CHECKLIST.md` | Code review checklist |
| `docs/ai/REFACTORING_GUIDE.md` | Refactoring guide |
| `docs/ai/FRONTEND_PROMPT.md` | Frontend prompt (lightweight) |
| `docs/ai/frontend-vibe-coding-prompt.md` | Frontend prompt (full, with all TypeScript types) |

---

## Frontend

There is a separate React frontend project. See `docs/ai/frontend-vibe-coding-prompt.md` for the data contract and component design.

---

## Custom Modes (Roo)

This project defines 4 custom Roo modes in `.roomodes`:
- **core-dev**: Core development (Java, NeoForge, AE2)
- **docs**: Documentation maintenance
- **analysis**: Architecture analysis
- **review**: Code review

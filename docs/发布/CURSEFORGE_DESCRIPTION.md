# Applied Energistics: Insight

**Applied Energistics: Insight** (`appliedinsight`) is an addon for **Applied Energistics 2** that helps you **see**, **filter**, and **grow** your ME storage — without constantly adding more drives.

**Highlights:** self-expanding **Digital Asset Vault** · **Smart Bus** with web JSON editor & one-click presets · **Sorter Command Block** for network analysis and merge — no commands to memorize.

---

## Requirements

| | |
|---|---|
| **Minecraft** | 1.21.1 |
| **Mod Loader** | NeoForge 21.1.224 |
| **AE2** | 19.2.17+ |
| **Java** | 21 |

Optional client mods: **JEI**, **EMI** (expansion cell drag-and-drop, filter editing).

---

## Filter tools — no hand-written JSON required

Smart Bus filters are powerful, but you **don't need to write JSON by hand**:

- **In-game presets** — one-click filters for **all items**, **items with durability**, and **ores**
- **Offline web editor** — visual rule builder bundled in the repo (`tools/filter-editor/`); runs in your browser, **no internet required**
- **Templates** — start from ready-made examples in the web editor, then tweak fields and operators
- **Copy for Game** — build rules in the browser, copy JSON, paste into Smart Bus GUI, save

**Recommended workflow:** open Smart Bus GUI → **Web Editor** → pick a template or preset → adjust rules → **Copy for Game** → paste in-game → **Save**.

---

## Digital Asset Vault — one block, growing storage

The **Digital Asset Vault (DAV)** is a centralized storage pool — not a slot-by-slot drive.

- **Built-in capacity:** 2048 bytes / 126 item types (equivalent to two empty 1k cells) — ready to use immediately
- **Cell absorption:** feed it empty AE2 storage cells to stack more bytes and types into one pool
- **Import existing stock:** pull scattered network items into the vault when first connected
- **Auto-accept:** make the DAV a preferred target for incoming items
- **Auto-expand:** at the usage threshold (default 80%), pull empty cells from the network or submit ME autocrafting jobs; finished cells are absorbed automatically
- **Manual expand once:** GUI button with clear status feedback (missing cell, missing pattern, crafting in progress, etc.)

**Auto-expand setup:** set your **expansion cell type** in the DAV GUI, then ensure either **empty cells are available on the network** or a matching **ME crafting pattern** exists for autocrafting. Without cells or patterns, auto-expand cannot run.

**Workflow:** connect to ME → enable auto-accept → (optional) import stock → absorb cells → set expansion cell type & confirm patterns → enable auto-expand.

---

## Smart Bus — JSON filters, import or export

The **Smart Bus** is a cable part with **FilterExpression JSON** — more flexible than stacking fuzzy upgrade cards.

- **Modes:** unconfigured / **import** (back → network) / **export** (network → back) — toggle with Shift+right-click or the GUI
- **Filters:** item ID, mod ID, tags, durability, stack size, `AND` / `OR`, nested groups
- **Presets & web editor:** see **Filter tools** above — presets in-game, templates + visual editor in the browser
- **Throughput:** up to **12 full stacks per tick** by default (configurable in `config/appliedinsight-server.toml`) — saves many upgrade-card slots on crowded bus lines

**Example** — import only `ae2` mod items:

```json
{
  "combinator": "AND",
  "rules": [
    { "field": "MOD_ID", "operator": "EQUALS", "value": "ae2" }
  ]
}
```

---

## Sorter Command Block — analyze, merge, sort

Right-click the **Sorter Command Block** for a terminal GUI.

| Button | What it does |
|--------|----------------|
| **Storage Analysis** | Network health: internal/external split, fragmentation, DAV capacity overview |
| **Merge** | Consolidate scattered item types across cells to free type slots |
| **Plan & Move** | Zone-based sorting via `config/appliedinsight/profiles/` routing profiles (server operators) |

Works well with DAV: **merge** reduces fragmentation → DAV regains type headroom. The analysis UI labels DAV separately from traditional drive cells.

---

## Quick Start

1. Install **AE2** + **Applied Energistics: Insight**
2. Craft the **Digital Asset Vault**, **Smart Bus**, and **Sorter Command Block**
3. Connect them to your ME network
4. Open the Command Block → **Storage Analysis** → **Merge** if fragmented → set up DAV
5. (Optional) Place Smart Bus on a cable → use a **preset** or the **web editor** → paste filter → set import/export mode

In-game guide chapters are included via **GuideMe**.

---

## Links

- **GitHub:** https://github.com/KnightCodeSquareMatrix/appliedinsight
- **Releases:** https://github.com/KnightCodeSquareMatrix/appliedinsight/releases
- **Issues & feedback:** https://github.com/KnightCodeSquareMatrix/appliedinsight/issues

---

## Version note

Current release: **v0.9.1** — post-beta stabilization. Feedback and bug reports are very welcome.

**License:** All Rights Reserved.

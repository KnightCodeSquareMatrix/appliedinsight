# Smart Bus Filter Editor

Fully **offline** static UI for building `FilterExpression` JSON. Double-click `index.html` to open — no HTTP server, no network.

## Quick start

1. Open `tools/filter-editor/index.html` in your browser (double-click).
2. Build filter rules.
3. Click **Copy for Game**.
4. In Minecraft Smart Bus GUI: **Paste** → **Save**.

If the browser blocks clipboard on `file://`, use the **Manual JSON paste** box at the bottom, or select the JSON preview and press Ctrl+C.

## Regenerate metadata

When Java filter fields/operators change:

```sh
./gradlew exportFilterEditorMetadata
```

This updates `metadata.js` (required next to `index.html`).

## In-game **Web Editor** button

No network required. Leave `filterEditorUrl` **blank** in config (default).

The mod extracts bundled assets to:

```text
config/appliedinsight/filter-editor/index.html
```

and opens them via `file://` in your default browser.

Optional: set `filterEditorUrl` only if you host a custom editor elsewhere.

## Build integration

`./gradlew build` runs `syncFilterEditorAssets`, copying `tools/filter-editor/` into the mod jar.

Files bundled: `index.html`, `app.js`, `i18n.js`, `templates.js`, `style.css`, `metadata.js`.

**Quick templates** (same definitions as in-game presets; source: `templates.js` + `SmartBusFilterPresets.java`):

| Template | Meaning |
|----------|---------|
| **All items** | `ITEM_ID` REGEX `.+` |
| **Durability items** | OR: `TAG` = `minecraft:enchantable/durability`; or NBT `max_damage` / `damage` components (durability wear, **not** attack damage) |
| **Ores** | `c:ores` tag, `*:ores/*` paths, or IDs ending with `_ore` |

More templates may be added in future releases.

Language: click **中文** / **English** in the toolbar. Preference is saved in `localStorage`. Default follows browser language.

## Workflow example

```json
{
  "combinator": "OR",
  "rules": [
    {
      "combinator": "AND",
      "rules": [
        { "field": "MOD_ID", "operator": "EQUALS", "value": "ae2" },
        { "field": "TAG", "operator": "CONTAINS", "value": "ingots" }
      ]
    },
    { "field": "MOD_ID", "operator": "EQUALS", "value": "minecraft" }
  ]
}
```

Nested groups are supported in the UI (up to 32 levels, 256 conditions total).

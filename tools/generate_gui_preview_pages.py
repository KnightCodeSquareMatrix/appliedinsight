#!/usr/bin/env python3
from __future__ import annotations

import argparse
from pathlib import Path

from gui_texture_common import (
    COLOR_SWATCH_ORDER,
    DEFAULT_PREVIEW_OUTPUT_DIR,
    DEFAULT_TEMPLATE_FILE,
    build_button_preview_sheet,
    build_digital_asset_vault_preview,
    build_guide_toolbar_preview,
    build_palette_swatches,
    build_sorter_command_block_preview,
    build_status_lamps_preview,
    load_ae2_reference_assets,
    load_themes,
    select_themes,
    write_manifest,
)


def generate_theme(theme, out_dir: Path, ae2_assets) -> None:
    out_dir.mkdir(parents=True, exist_ok=True)
    build_sorter_command_block_preview(theme, ae2_assets).save_png(out_dir / "sorter_command_block_preview.png")
    build_digital_asset_vault_preview(theme).save_png(out_dir / "digital_asset_vault_preview.png")
    build_guide_toolbar_preview(theme, ae2_assets).save_png(out_dir / "guide_toolbar_preview.png")
    build_button_preview_sheet(theme, ae2_assets).save_png(out_dir / "button_preview.png")
    build_status_lamps_preview(theme).save_png(out_dir / "status_lamps_preview.png")
    build_palette_swatches(theme).save_png(out_dir / "palette_swatches.png")
    write_manifest(
        out_dir / "manifest.json",
        {
            "kind": "preview-pages",
            "theme": theme.name,
            "description": theme.description,
            "generated_files": [
                "sorter_command_block_preview.png",
                "digital_asset_vault_preview.png",
                "guide_toolbar_preview.png",
                "button_preview.png",
                "status_lamps_preview.png",
                "palette_swatches.png",
            ],
            "color_groups": {group: list(keys) for group, keys in theme.colors.items()},
            "swatch_order": [{"group": group, "key": key} for group, key in COLOR_SWATCH_ORDER],
            "colors": {
                group: {
                    key: "#{:02X}{:02X}{:02X}{:02X}".format(*value)
                    for key, value in entries.items()
                }
                for group, entries in theme.colors.items()
            },
            "ae2_reused_assets": [
                "vertical_buttons_bg.png",
                "button.png",
                "button_highlighted.png",
                "button_disabled.png",
                "states.png",
            ],
        },
    )


def main() -> None:
    parser = argparse.ArgumentParser(description="Generate GUI preview pages using shared textures plus AE2 reference sprites.")
    parser.add_argument("--template-file", type=Path, default=DEFAULT_TEMPLATE_FILE, help="Theme template JSON file.")
    parser.add_argument("--output-dir", type=Path, default=DEFAULT_PREVIEW_OUTPUT_DIR, help="Preview output directory.")
    parser.add_argument("--theme", action="append", default=[], help="Generate only the named theme. Can be repeated.")
    args = parser.parse_args()

    ae2_assets = load_ae2_reference_assets()
    for theme in select_themes(load_themes(args.template_file), args.theme):
        out_dir = args.output_dir / theme.name
        generate_theme(theme, out_dir, ae2_assets)
        print(f"[ok] generated preview theme '{theme.name}' -> {out_dir}")


if __name__ == "__main__":
    main()

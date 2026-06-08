#!/usr/bin/env python3
from __future__ import annotations

import argparse
from pathlib import Path

from gui_texture_common import (
    COLOR_SWATCH_ORDER,
    DEFAULT_AE2_REFERENCE_DIR,
    DEFAULT_RESOURCE_TEXTURE_ROOT,
    DEFAULT_TEMPLATE_FILE,
    DEFAULT_TEXTURE_OUTPUT_DIR,
    build_themed_ae2_sprites,
    build_digital_asset_vault_texture,
    build_palette_swatches,
    build_sorter_command_block_panel,
    load_ae2_reference_assets,
    load_themes,
    select_themes,
    write_manifest,
)


def generate_theme(theme, out_dir: Path, ae2_assets) -> None:
    texture_root = out_dir / DEFAULT_RESOURCE_TEXTURE_ROOT
    sprite_root = texture_root / "sprites"
    texture_root.mkdir(parents=True, exist_ok=True)
    sprite_root.mkdir(parents=True, exist_ok=True)

    themed_sprites = build_themed_ae2_sprites(theme, ae2_assets)

    build_sorter_command_block_panel(theme).save_png(texture_root / "sorter_command_block_terminal.png")
    build_digital_asset_vault_texture(theme).save_png(texture_root / "digital_asset_vault.png")
    themed_sprites.vertical_toolbar_background.save_png(sprite_root / "vertical_buttons_bg.png")
    themed_sprites.button_normal.save_png(sprite_root / "button.png")
    themed_sprites.button_hover.save_png(sprite_root / "button_highlighted.png")
    themed_sprites.button_disabled.save_png(sprite_root / "button_disabled.png")
    themed_sprites.toolbar_button_background.save_png(sprite_root / "toolbar_button_background.png")
    themed_sprites.help_icon.save_png(sprite_root / "help_icon.png")
    build_palette_swatches(theme).save_png(out_dir / "palette_swatches.png")

    (sprite_root / "vertical_buttons_bg.png.mcmeta").write_text(
        (DEFAULT_AE2_REFERENCE_DIR / "vertical_buttons_bg.png.mcmeta").read_text(encoding="utf-8"),
        encoding="utf-8",
    )
    for name in ("button.png.mcmeta", "button_highlighted.png.mcmeta", "button_disabled.png.mcmeta"):
        (sprite_root / name).write_text(
            (DEFAULT_AE2_REFERENCE_DIR / "button.png.mcmeta").read_text(encoding="utf-8"),
            encoding="utf-8",
        )

    write_manifest(
        out_dir / "manifest.json",
        {
            "kind": "runtime-textures",
            "theme": theme.name,
            "description": theme.description,
            "generated_files": [
                (DEFAULT_RESOURCE_TEXTURE_ROOT / "sorter_command_block_terminal.png").as_posix(),
                (DEFAULT_RESOURCE_TEXTURE_ROOT / "digital_asset_vault.png").as_posix(),
                (DEFAULT_RESOURCE_TEXTURE_ROOT / "sprites" / "vertical_buttons_bg.png").as_posix(),
                (DEFAULT_RESOURCE_TEXTURE_ROOT / "sprites" / "button.png").as_posix(),
                (DEFAULT_RESOURCE_TEXTURE_ROOT / "sprites" / "button_highlighted.png").as_posix(),
                (DEFAULT_RESOURCE_TEXTURE_ROOT / "sprites" / "button_disabled.png").as_posix(),
                (DEFAULT_RESOURCE_TEXTURE_ROOT / "sprites" / "toolbar_button_background.png").as_posix(),
                (DEFAULT_RESOURCE_TEXTURE_ROOT / "sprites" / "help_icon.png").as_posix(),
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
        },
    )


def main() -> None:
    parser = argparse.ArgumentParser(description="Generate runtime GUI textures from the theme templates.")
    parser.add_argument("--template-file", type=Path, default=DEFAULT_TEMPLATE_FILE, help="Theme template JSON file.")
    parser.add_argument("--output-dir", type=Path, default=DEFAULT_TEXTURE_OUTPUT_DIR, help="Texture output directory.")
    parser.add_argument("--theme", action="append", default=[], help="Generate only the named theme. Can be repeated.")
    args = parser.parse_args()

    ae2_assets = load_ae2_reference_assets()
    for theme in select_themes(load_themes(args.template_file), args.theme):
        out_dir = args.output_dir / theme.name
        generate_theme(theme, out_dir, ae2_assets)
        print(f"[ok] generated runtime textures for '{theme.name}' -> {out_dir}")


if __name__ == "__main__":
    main()

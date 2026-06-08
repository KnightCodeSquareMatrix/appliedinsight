#!/usr/bin/env python3
"""Generate themed GUI assets into src/main/resources for appliedstoragesorter."""

from __future__ import annotations

import shutil
from pathlib import Path

from generate_new_dav_texture import build_new_dav_texture
from generate_sorter_command_block_block_texture import main as generate_sorter_command_block_block_textures
from gui_texture_common import (
    DEFAULT_AE2_REFERENCE_DIR,
    DEFAULT_TEMPLATE_FILE,
    build_digital_asset_vault_texture,
    build_smart_bus_texture,
    build_sorter_command_block_panel,
    build_themed_ae2_sprites,
    build_themed_background,
    build_themed_controller_textures,
    build_themed_states_atlas,
    load_ae2_reference_assets,
    load_themes,
    save_rgba_png,
)

THEME_NAME = "applied_dark_matter_v2"
PROJECT_ROOT = Path(__file__).resolve().parent.parent
ASSET_ROOT = PROJECT_ROOT / "src/main/resources/assets/appliedstoragesorter"
TEXTURE_GUI = ASSET_ROOT / "textures/gui"
TEXTURE_BLOCK = ASSET_ROOT / "textures/block"
SPRITE_ROOT = TEXTURE_GUI / "sprites"


def main() -> None:
    themes = {theme.name: theme for theme in load_themes(DEFAULT_TEMPLATE_FILE)}
    theme = themes[THEME_NAME]
    ae2_assets = load_ae2_reference_assets()
    themed_sprites = build_themed_ae2_sprites(theme, ae2_assets)

    TEXTURE_GUI.mkdir(parents=True, exist_ok=True)
    SPRITE_ROOT.mkdir(parents=True, exist_ok=True)

    themed_sprites.button_normal.save_png(SPRITE_ROOT / "button.png")
    themed_sprites.button_hover.save_png(SPRITE_ROOT / "button_highlighted.png")
    themed_sprites.button_disabled.save_png(SPRITE_ROOT / "button_disabled.png")
    themed_sprites.vertical_toolbar_background.save_png(SPRITE_ROOT / "vertical_buttons_bg.png")

    for name in ("button.png.mcmeta", "button_highlighted.png.mcmeta", "button_disabled.png.mcmeta"):
        shutil.copy2(DEFAULT_AE2_REFERENCE_DIR / name, SPRITE_ROOT / name)
    shutil.copy2(
        DEFAULT_AE2_REFERENCE_DIR / "vertical_buttons_bg.png.mcmeta",
        SPRITE_ROOT / "vertical_buttons_bg.png.mcmeta",
    )

    build_themed_states_atlas(theme, ae2_assets).save_png(TEXTURE_GUI / "states.png")
    build_themed_background(theme, ae2_assets).save_png(TEXTURE_GUI / "background.png")

    build_sorter_command_block_panel(theme).save_png(TEXTURE_GUI / "sorter_command_block.png")
    build_digital_asset_vault_texture(theme).save_png(TEXTURE_GUI / "digital_asset_vault.png")
    build_smart_bus_texture(theme).save_png(TEXTURE_GUI / "smart_bus.png")

    new_dav = build_new_dav_texture(THEME_NAME)
    save_rgba_png(TEXTURE_GUI / "new_digital_asset_vault.png", new_dav.width, new_dav.height, bytes(new_dav.pixels))

    TEXTURE_BLOCK.mkdir(parents=True, exist_ok=True)
    for name, image in build_themed_controller_textures(theme).items():
        image.save_png(TEXTURE_BLOCK / name)
    generate_sorter_command_block_block_textures()

    print(f"[ok] generated themed GUI assets for '{THEME_NAME}' -> {ASSET_ROOT}")


if __name__ == "__main__":
    main()

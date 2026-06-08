#!/usr/bin/env python3
"""Generate the New DAV GUI background texture aligned with NewDigitalAssetVaultScreen/Menu."""

from __future__ import annotations

from pathlib import Path

from gui_texture_common import (
    DEFAULT_TEMPLATE_FILE,
    PixelCanvas,
    draw_panel_box,
    draw_slot,
    load_themes,
    mix,
    save_rgba_png,
)

OUTPUT = (
    Path(__file__).resolve().parent.parent
    / "src/main/resources/assets/appliedstoragesorter/textures/gui/new_digital_asset_vault.png"
)

PANEL_WIDTH = 226
PANEL_HEIGHT = 238
FILE_WIDTH = 256
FILE_HEIGHT = 256

HEADER_HEIGHT = 20
INVENTORY_SECTION_Y = 156
COLUMN_DIVIDER_X = 128

LEFT_SECTION = (8, 22, 116, 132)
# Right column: title (28) → slot (44) → stats (68+); panel wraps content with bottom padding
RIGHT_SECTION = (134, 22, 84, 88)

# Sync with NewDigitalAssetVaultMenu
RIGHT_SECTION_X = 134
RIGHT_SECTION_WIDTH = 84
SLOT_SIZE = 18
INPUT_SLOT_X = RIGHT_SECTION_X + (RIGHT_SECTION_WIDTH - SLOT_SIZE) // 2
INPUT_SLOT_Y = 44

PLAYER_INV_X = 30
PLAYER_INV_ROWS = (162, 180, 198)
PLAYER_HOTBAR_Y = 220
PLAYER_INV_COLS = 9
SLOT_STEP = 18


def build_new_dav_texture(theme_name: str = "applied_dark_matter_v2") -> PixelCanvas:
    themes = {theme.name: theme for theme in load_themes(DEFAULT_TEMPLATE_FILE)}
    theme = themes[theme_name]

    bg_outline = theme.color("background", "outline_dark")
    bg_light = theme.color("background", "edge_light")
    bg_fill = theme.color("background", "fill")
    bg_shadow = theme.color("background", "shadow")
    section_outline = theme.color("section", "outline_dark")
    section_light = theme.color("section", "edge_light")
    section_fill = theme.color("section", "fill")
    section_shadow = theme.color("section", "shadow")

    canvas = PixelCanvas(FILE_WIDTH, FILE_HEIGHT)
    inventory_header_fill = mix(bg_fill, bg_outline, 0.28)

    # Title row keeps panel fill (matches AE2 drive.png and vertical_buttons_bg).
    draw_panel_box(canvas, 0, 0, PANEL_WIDTH, PANEL_HEIGHT, bg_outline, bg_light, bg_fill, bg_shadow)
    canvas.fill_rect(3, INVENTORY_SECTION_Y, PANEL_WIDTH - 6, 1, section_light)
    canvas.fill_rect(3, INVENTORY_SECTION_Y + 1, PANEL_WIDTH - 6, 5, inventory_header_fill)

    for rect in (LEFT_SECTION, RIGHT_SECTION):
        x, y, width, height = rect
        draw_panel_box(canvas, x, y, width, height, section_outline, section_light, section_fill, section_shadow)

    divider_top = LEFT_SECTION[1]
    divider_bottom = LEFT_SECTION[1] + LEFT_SECTION[3]
    canvas.fill_rect(COLUMN_DIVIDER_X, divider_top, 1, divider_bottom - divider_top, section_light)

    draw_slot(canvas, INPUT_SLOT_X, INPUT_SLOT_Y, theme)
    for row_y in PLAYER_INV_ROWS:
        for col in range(PLAYER_INV_COLS):
            draw_slot(canvas, PLAYER_INV_X + col * SLOT_STEP, row_y, theme)
    for col in range(PLAYER_INV_COLS):
        draw_slot(canvas, PLAYER_INV_X + col * SLOT_STEP, PLAYER_HOTBAR_Y, theme)

    return canvas


def main() -> None:
    canvas = build_new_dav_texture()
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    save_rgba_png(OUTPUT, FILE_WIDTH, FILE_HEIGHT, bytes(canvas.pixels))
    print(f"[ok] wrote {OUTPUT} ({PANEL_WIDTH}x{PANEL_HEIGHT} content in {FILE_WIDTH}x{FILE_HEIGHT})")


if __name__ == "__main__":
    main()

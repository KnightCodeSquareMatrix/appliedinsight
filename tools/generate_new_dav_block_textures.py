#!/usr/bin/env python3
"""Generate deep-blue vault block textures for New DAV (no drive bay slots).

Run:

    python tools/generate_new_dav_block_textures.py

Outputs to ``textures/block/new_dav/``:
  - side.png, top.png, bottom.png — shell faces
  - vault_front.png — recessed vault panel (single core emblem, not drive slots)
  - vault_front_lights.png — emissive front overlay (cutout model)
"""

from __future__ import annotations

from pathlib import Path

from gui_texture_common import (
    DEFAULT_TEMPLATE_FILE,
    PixelCanvas,
    load_themes,
    mix,
    save_rgba_png,
)

OUTPUT_DIR = (
    Path(__file__).resolve().parent.parent
    / "src/main/resources/assets/appliedstoragesorter/textures/block/new_dav"
)
THEME_NAME = "applied_dark_matter_v2"
SIZE = 16
TRANSPARENT = (0, 0, 0, 0)

# Deeper blue than the default theme — DAV identity vs AE2 drive gray-blue.
DEEP_NAVY = (0x12, 0x1E, 0x38, 0xFF)
DEEP_PANEL = (0x1A, 0x2C, 0x52, 0xFF)
COBALT_EDGE = (0x3A, 0x5E, 0xA8, 0xFF)
COBALT_GLOW = (0x5A, 0x8E, 0xE8, 0xFF)


def vault_palette(theme) -> dict[str, tuple[int, int, int, int]]:
    outline = mix(theme.color("background", "outline_dark"), DEEP_NAVY, 0.55)
    fill = mix(theme.color("background", "fill"), DEEP_PANEL, 0.65)
    shadow = mix(theme.color("background", "shadow"), DEEP_NAVY, 0.7)
    edge = mix(theme.color("background", "edge_light"), COBALT_EDGE, 0.45)
    accent = mix(theme.color("status", "online"), COBALT_GLOW, 0.35)
    panel = mix(fill, DEEP_PANEL, 0.4)
    panel_shadow = mix(shadow, DEEP_NAVY, 0.35)
    return {
        "outline": outline,
        "fill": fill,
        "shadow": shadow,
        "edge": edge,
        "accent": accent,
        "panel": panel,
        "panel_shadow": panel_shadow,
    }


def draw_vault_shell(canvas: PixelCanvas, palette: dict, *, front_panel: bool) -> None:
    outline = palette["outline"]
    fill = palette["fill"]
    shadow = palette["shadow"]
    edge = palette["edge"]
    accent = palette["accent"]
    panel = palette["panel"]
    panel_shadow = palette["panel_shadow"]

    canvas.fill_rect(0, 0, SIZE, SIZE, outline)
    canvas.fill_rect(1, 1, SIZE - 2, SIZE - 2, fill)
    canvas.draw_hline(1, 1, SIZE - 2, edge)
    canvas.draw_vline(1, 1, SIZE - 2, edge)
    canvas.draw_hline(1, SIZE - 2, SIZE - 2, shadow)
    canvas.draw_vline(SIZE - 2, 1, SIZE - 2, shadow)

    if not front_panel:
        return

    # Recessed vault door — single panel, no horizontal slot dividers.
    canvas.fill_rect(3, 3, SIZE - 6, SIZE - 6, panel_shadow)
    canvas.fill_rect(4, 4, SIZE - 8, SIZE - 8, panel)
    canvas.draw_hline(4, 4, SIZE - 8, mix(edge, accent, 0.25))
    canvas.draw_vline(4, 4, SIZE - 8, mix(edge, accent, 0.25))
    canvas.draw_hline(4, SIZE - 5, SIZE - 8, shadow)
    canvas.draw_vline(SIZE - 5, 4, SIZE - 8, shadow)

    # Subtle corner brackets (vault casing, not cell bays).
    canvas.fill_rect(5, 5, 2, 2, mix(edge, accent, 0.15))
    canvas.fill_rect(SIZE - 7, 5, 2, 2, mix(edge, accent, 0.15))
    canvas.fill_rect(5, SIZE - 7, 2, 2, mix(edge, accent, 0.15))
    canvas.fill_rect(SIZE - 7, SIZE - 7, 2, 2, mix(edge, accent, 0.15))


def draw_vault_core_emblem(canvas: PixelCanvas, palette: dict) -> None:
    """Central absorption core — diamond emblem, not a row of drive LEDs."""
    accent = palette["accent"]
    bright = mix(palette["accent"], COBALT_GLOW, 0.5)
    dim = mix(accent, palette["panel_shadow"], 0.45)

    center_x, center_y = 8, 8
    diamond = [
        (0, -3), (-1, -2), (1, -2),
        (-2, -1), (-1, -1), (0, -1), (1, -1), (2, -1),
        (-3, 0), (-2, 0), (-1, 0), (0, 0), (1, 0), (2, 0), (3, 0),
        (-2, 1), (-1, 1), (0, 1), (1, 1), (2, 1),
        (-1, 2), (0, 2), (1, 2),
        (0, 3),
    ]
    for dx, dy in diamond:
        x, y = center_x + dx, center_y + dy
        if 4 <= x < SIZE - 4 and 4 <= y < SIZE - 4:
            dist = abs(dx) + abs(dy)
            color = bright if dist <= 2 else dim if dist == 3 else accent
            canvas.fill_rect(x, y, 1, 1, color)


def draw_vault_front_lights(palette: dict) -> PixelCanvas:
    canvas = PixelCanvas(SIZE, SIZE, TRANSPARENT)
    bright = mix(COBALT_GLOW, palette["accent"], 0.25)
    mid = mix(palette["accent"], palette["panel_shadow"], 0.2)
    soft = mix(mid, TRANSPARENT, 0.35)

    # Soft vertical core beam (single vault channel).
    canvas.fill_rect(7, 4, 2, 8, soft)
    canvas.fill_rect(7, 5, 2, 6, mid)
    canvas.fill_rect(8, 6, 1, 4, bright)

    # Diamond core glow.
    core_pixels = [
        (8, 6, bright), (7, 7, mid), (8, 7, bright), (9, 7, mid),
        (8, 8, bright), (7, 8, mid), (9, 8, mid), (8, 9, mid),
    ]
    for x, y, color in core_pixels:
        canvas.fill_rect(x, y, 1, 1, color)

    return canvas


def generate_new_dav_block_textures(theme_name: str = THEME_NAME) -> None:
    theme = {entry.name: entry for entry in load_themes(DEFAULT_TEMPLATE_FILE)}[theme_name]
    palette = vault_palette(theme)
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

    for name in ("side", "top", "bottom"):
        canvas = PixelCanvas(SIZE, SIZE)
        draw_vault_shell(canvas, palette, front_panel=False)
        save_rgba_png(OUTPUT_DIR / f"{name}.png", SIZE, SIZE, bytes(canvas.pixels))

    front = PixelCanvas(SIZE, SIZE)
    draw_vault_shell(front, palette, front_panel=True)
    draw_vault_core_emblem(front, palette)
    save_rgba_png(OUTPUT_DIR / "vault_front.png", SIZE, SIZE, bytes(front.pixels))

    lights = draw_vault_front_lights(palette)
    save_rgba_png(OUTPUT_DIR / "vault_front_lights.png", SIZE, SIZE, bytes(lights.pixels))

    print(f"[ok] generated New DAV vault block textures for '{theme_name}' -> {OUTPUT_DIR}")


def main() -> None:
    generate_new_dav_block_textures()


if __name__ == "__main__":
    main()

#!/usr/bin/env python3
"""Generate 16x16 block textures for the Sorter Command Block (terminal shell, not controller)."""

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
    / "src/main/resources/assets/appliedstoragesorter/textures/block"
)
THEME_NAME = "applied_dark_matter_v2"
SIZE = 16
TRANSPARENT = (0, 0, 0, 0)


def draw_terminal_shell(canvas: PixelCanvas, theme, *, front_panel: bool) -> None:
    outline = theme.color("background", "outline_dark")
    fill = theme.color("background", "fill")
    shadow = theme.color("background", "shadow")
    edge = mix(theme.color("background", "edge_light"), shadow, 0.55)
    accent = mix(theme.color("background", "edge_light"), fill, 0.35)

    canvas.fill_rect(0, 0, SIZE, SIZE, outline)
    canvas.fill_rect(1, 1, SIZE - 2, SIZE - 2, fill)
    canvas.draw_hline(1, 1, SIZE - 2, accent)
    canvas.draw_vline(1, 1, SIZE - 2, accent)
    canvas.draw_hline(1, SIZE - 2, SIZE - 2, shadow)
    canvas.draw_vline(SIZE - 2, 1, SIZE - 2, shadow)

    if front_panel:
        canvas.fill_rect(3, 3, SIZE - 6, SIZE - 6, mix(shadow, outline, 0.4))
        canvas.fill_rect(4, 4, SIZE - 8, SIZE - 8, mix(fill, shadow, 0.25))
        canvas.draw_hline(4, 4, SIZE - 8, mix(accent, fill, 0.5))
        canvas.draw_vline(4, 4, SIZE - 8, mix(accent, fill, 0.5))


def draw_front_lights(theme) -> PixelCanvas:
    canvas = PixelCanvas(SIZE, SIZE, TRANSPARENT)
    online = theme.color("status", "online")
    bright = theme.color("help_button", "frame_light")
    dim = mix(online, theme.color("background", "shadow"), 0.45)

    canvas.fill_rect(2, 5, SIZE - 4, 1, dim)
    canvas.fill_rect(2, 10, SIZE - 4, 1, dim)
    canvas.fill_rect(1, 6, SIZE - 2, 1, online)
    canvas.fill_rect(1, 9, SIZE - 2, 1, online)
    canvas.fill_rect(0, 7, SIZE, 1, bright)
    canvas.fill_rect(0, 8, SIZE, 1, mix(bright, online, 0.35))

    return canvas


def main() -> None:
    theme = {entry.name: entry for entry in load_themes(DEFAULT_TEMPLATE_FILE)}[THEME_NAME]
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

    side = PixelCanvas(SIZE, SIZE)
    draw_terminal_shell(side, theme, front_panel=False)
    save_rgba_png(OUTPUT_DIR / "sorter_command_block_side.png", SIZE, SIZE, bytes(side.pixels))

    top = PixelCanvas(SIZE, SIZE)
    draw_terminal_shell(top, theme, front_panel=False)
    save_rgba_png(OUTPUT_DIR / "sorter_command_block_top.png", SIZE, SIZE, bytes(top.pixels))

    front = PixelCanvas(SIZE, SIZE)
    draw_terminal_shell(front, theme, front_panel=True)
    save_rgba_png(OUTPUT_DIR / "sorter_command_block_front.png", SIZE, SIZE, bytes(front.pixels))

    lights = draw_front_lights(theme)
    save_rgba_png(OUTPUT_DIR / "sorter_command_block_front_lights.png", SIZE, SIZE, bytes(lights.pixels))

    print(f"[ok] generated SCB block textures for '{THEME_NAME}' -> {OUTPUT_DIR}")


if __name__ == "__main__":
    main()

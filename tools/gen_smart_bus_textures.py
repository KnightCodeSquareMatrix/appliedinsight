"""Generate Smart Bus part icon textures (16x16).

Usage:
  python tools/gen_smart_bus_textures.py          # production textures
  python tools/gen_smart_bus_textures.py --debug  # UV debug sheets only

Per-face side textures are full face atlases (panel + glyph in the UV
rectangle used by the model). Single cuboid element only — no overlay quads.

Import >  toward cable (+Z)
Export <  toward exterior (-Z / north)
"""
from __future__ import annotations

import argparse
import os

from PIL import Image

BASE = os.path.join(
    os.path.dirname(__file__),
    "..",
    "src",
    "main",
    "resources",
    "assets",
    "appliedstoragesorter",
    "textures",
    "part",
)

BG = (14, 15, 29, 255)
EDGE = (22, 24, 40, 255)
IMPORT_C = (0, 200, 255, 255)
EXPORT_C = (255, 180, 0, 255)
TRANSPARENT = (0, 0, 0, 0)

# UV rectangles on 16×16 atlas — must match model JSON (element 2,2,0 → 14,14,6)
FACE_UV: dict[str, tuple[int, int, int, int]] = {
    "east": (10, 2, 16, 14),
    "west": (0, 2, 6, 14),
    "up": (2, 0, 14, 6),
    "down": (2, 10, 14, 16),
}

# Image-space direction for glyph tip (x right, y down) at rotation 0 / uv [4,4,12,12]
IMPORT_FACE_DIR: dict[str, str] = {
    "east": "left",
    "west": "right",
    "up": "down",
    "down": "up",
}
EXPORT_FACE_DIR: dict[str, str] = {
    "east": "right",
    "west": "left",
    "up": "up",
    "down": "down",
}

SIDE_FACES = ("east", "west", "up", "down")

# Inset inside the 1px EDGE border drawn by _fill_face_panel.
_CHEVRON_PAD = 1


def _draw_line(
    px,
    color: tuple[int, int, int, int],
    x0: int,
    y0: int,
    x1: int,
    y1: int,
    thickness: int,
) -> None:
    steps = max(abs(x1 - x0), abs(y1 - y0), 1)
    for i in range(steps + 1):
        t = i / steps
        x = int(round(x0 + (x1 - x0) * t))
        y = int(round(y0 + (y1 - y0) * t))
        for dy in range(-(thickness // 2), thickness // 2 + 1):
            for dx in range(-(thickness // 2), thickness // 2 + 1):
                px_x, px_y = x + dx, y + dy
                if 0 <= px_x < 16 and 0 <= px_y < 16:
                    px[px_x, px_y] = color


def _draw_big_chevron(
    px,
    color: tuple[int, int, int, int],
    u1: int,
    v1: int,
    u2: int,
    v2: int,
    direction: str,
) -> None:
    """Fill the face UV with a max-size hollow > / < / ^ / v chevron."""
    x0 = u1 + _CHEVRON_PAD
    y0 = v1 + _CHEVRON_PAD
    x1 = u2 - _CHEVRON_PAD
    y1 = v2 - _CHEVRON_PAD
    w, h = x1 - x0, y1 - y0
    if w <= 2 or h <= 2:
        return

    last_x = x1 - 1
    last_y = y1 - 1
    tip_x = x0 + w // 2
    tip_y = y0 + h // 2
    stroke = max(1, min(w, h) // 3)

    if direction == "right":
        _draw_line(px, color, x0, y0, last_x, tip_y, stroke)
        _draw_line(px, color, x0, last_y, last_x, tip_y, stroke)
    elif direction == "left":
        _draw_line(px, color, last_x, y0, x0, tip_y, stroke)
        _draw_line(px, color, last_x, last_y, x0, tip_y, stroke)
    elif direction == "up":
        _draw_line(px, color, x0, last_y, tip_x, y0, stroke)
        _draw_line(px, color, last_x, last_y, tip_x, y0, stroke)
    elif direction == "down":
        _draw_line(px, color, x0, y0, tip_x, last_y, stroke)
        _draw_line(px, color, last_x, y0, tip_x, last_y, stroke)
    else:
        raise ValueError(direction)


def _clear(img: Image.Image) -> None:
    px = img.load()
    for y in range(16):
        for x in range(16):
            px[x, y] = TRANSPARENT


def fill_bg(img: Image.Image) -> None:
    px = img.load()
    for y in range(16):
        for x in range(16):
            px[x, y] = BG


def _fill_face_panel(img: Image.Image, face: str) -> tuple[int, int, int, int]:
    u1, v1, u2, v2 = FACE_UV[face]
    px = img.load()
    for y in range(v1, v2):
        for x in range(u1, u2):
            px[x, y] = BG
    for x in range(u1, u2):
        px[x, v1] = EDGE
        px[x, v2 - 1] = EDGE
    for y in range(v1, v2):
        px[u1, y] = EDGE
        px[u2 - 1, y] = EDGE
    return u1, v1, u2, v2


def draw_full_side_face(
    img: Image.Image,
    face: str,
    color: tuple[int, int, int, int],
    direction: str,
) -> None:
    """One 16×16 atlas; opaque panel + max-size chevron in FACE_UV[face]."""
    _clear(img)
    u1, v1, u2, v2 = _fill_face_panel(img, face)
    _draw_big_chevron(img.load(), color, u1, v1, u2, v2, direction)


def draw_empty(img: Image.Image) -> None:
    fill_bg(img)


def draw_sides(img: Image.Image) -> None:
    fill_bg(img)
    px = img.load()
    edge = (22, 24, 40, 255)
    for y in range(16):
        for x in range(16):
            if x in (0, 15) or y in (0, 15):
                px[x, y] = edge


def _write_full_side_textures(mode: str, face_dirs: dict[str, str], color) -> None:
    for face in SIDE_FACES:
        img = Image.new("RGBA", (16, 16))
        draw_full_side_face(img, face, color, face_dirs[face])
        path = os.path.join(BASE, f"smart_bus_{mode}_{face}.png")
        img.save(path)
        print("wrote", path)


def write_production_textures() -> None:
    os.makedirs(BASE, exist_ok=True)

    _write_full_side_textures("import", IMPORT_FACE_DIR, IMPORT_C)
    _write_full_side_textures("export", EXPORT_FACE_DIR, EXPORT_C)

    empty = Image.new("RGBA", (16, 16))
    draw_empty(empty)
    empty_path = os.path.join(BASE, "smart_bus_front_empty.png")
    empty.save(empty_path)
    print("wrote", empty_path)

    sides = Image.new("RGBA", (16, 16))
    draw_sides(sides)
    sides_path = os.path.join(BASE, "smart_bus_sides.png")
    sides.save(sides_path)
    print("wrote", sides_path)


# ── UV debug (unchanged) ───────────────────────────────────────────

_DIGIT_3x5: dict[str, tuple[str, ...]] = {
    "1": (".#.", "..#", "..#", "..#", "..#"),
    "2": ("###", "..#", "###", "#..", "###"),
    "3": ("###", "..#", "###", "..#", "###"),
    "4": ("#.#", "#.#", "###", "..#", "..#"),
    "5": ("###", "#..", "###", "..#", "###"),
    "6": ("###", "#..", "###", "#.#", "###"),
}

UV_DEBUG_FACES: tuple[tuple[str, int], ...] = (
    ("north", 1),
    ("south", 2),
    ("east", 3),
    ("west", 4),
    ("up", 5),
    ("down", 6),
)


def _draw_digit_3x5(px, ox: int, oy: int, ch: str, color: tuple[int, int, int, int]) -> None:
    pattern = _DIGIT_3x5[ch]
    for row, line in enumerate(pattern):
        for col, dot in enumerate(line):
            if dot == "#":
                px[ox + col, oy + row] = color


def draw_uv_debug_face(img: Image.Image, face_number: int) -> None:
    px = img.load()
    bg = (128, 128, 128, 255)
    text = (255, 255, 255, 255)
    corners = {
        (0, 0): (255, 0, 0, 255),
        (15, 0): (0, 255, 0, 255),
        (0, 15): (0, 0, 255, 255),
        (15, 15): (255, 255, 0, 255),
    }
    for y in range(16):
        for x in range(16):
            px[x, y] = bg
    for (x, y), color in corners.items():
        px[x, y] = color
    digit_w, digit_h = 3, 5
    _draw_digit_3x5(px, (16 - digit_w) // 2, (16 - digit_h) // 2, str(face_number), text)


def write_uv_debug_textures() -> None:
    os.makedirs(BASE, exist_ok=True)
    for _face_name, face_number in UV_DEBUG_FACES:
        img = Image.new("RGBA", (16, 16))
        draw_uv_debug_face(img, face_number)
        path = os.path.join(BASE, f"smart_bus_uv_debug_{face_number}.png")
        img.save(path)
        print("wrote", path)


def main() -> None:
    parser = argparse.ArgumentParser(description="Generate Smart Bus part textures")
    parser.add_argument("--debug", action="store_true", help="Generate UV debug textures only")
    args = parser.parse_args()
    if args.debug:
        write_uv_debug_textures()
    else:
        write_production_textures()


if __name__ == "__main__":
    main()

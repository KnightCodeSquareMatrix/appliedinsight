#!/usr/bin/env python3
"""Generate DAV Cell item texture from AE2 item storage cell + deep-blue tint."""

from __future__ import annotations

import zipfile
from pathlib import Path

from gui_texture_common import PngImage, blit_png_onto, find_ae2_runtime_jar, mix, read_png_from_bytes, save_rgba_png

OUTPUT = (
    Path(__file__).resolve().parent.parent
    / "src/main/resources/assets/appliedinsight/textures/item/dav_cell.png"
)

# Match DAV block identity (see generate_new_dav_block_textures.py).
DEEP_BLUE = (0x1A, 0x3A, 0x6E, 0xFF)
OVERLAY_STRENGTH = 0.52


def read_ae2_texture(archive_path: str) -> PngImage:
    with zipfile.ZipFile(find_ae2_runtime_jar(), "r") as archive:
        return read_png_from_bytes(archive.read(archive_path))


def apply_deep_blue_overlay(image: PngImage, tint: tuple[int, int, int, int], strength: float) -> PngImage:
    out = bytearray(image.pixels)
    for index in range(0, len(out), 4):
        pixel = tuple(out[index:index + 4])
        if pixel[3] == 0:
            continue
        blended = mix(pixel, tint, strength)
        out[index:index + 4] = bytes((blended[0], blended[1], blended[2], pixel[3]))
    return PngImage(image.width, image.height, bytes(out))


def build_dav_cell_texture() -> PngImage:
    base = read_ae2_texture("assets/ae2/textures/item/item_storage_cell_1k.png")
    led = read_ae2_texture("assets/ae2/textures/item/storage_cell_led.png")
    combined = blit_png_onto(base, led, 0, 0)
    return apply_deep_blue_overlay(combined, DEEP_BLUE, OVERLAY_STRENGTH)


def main() -> None:
    image = build_dav_cell_texture()
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    save_rgba_png(OUTPUT, image.width, image.height, image.pixels)
    print(f"[ok] wrote {OUTPUT} ({image.width}x{image.height})")


if __name__ == "__main__":
    main()

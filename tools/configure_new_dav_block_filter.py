#!/usr/bin/env python3
"""Bake deep-blue filters into New DAV block textures (no runtime overlay).

Edit CONFIG below, then run:

    python tools/configure_new_dav_block_filter.py

Sources:
  - ``textures/block/new_digital_asset_vault_front.png`` (unfiltered master, never overwritten)
  - AE2 drive / generic sprites (read from Gradle cache jar)

Outputs filtered PNGs to ``textures/block/new_dav/``.
Does not modify the block model JSON (vault cube layout is maintained separately).
Recompile / relaunch the client to preview in-world.
"""

from __future__ import annotations

import zipfile
from dataclasses import dataclass
from pathlib import Path

from gui_texture_common import PngImage, find_ae2_runtime_jar, read_png, save_rgba_png

REPO_ROOT = Path(__file__).resolve().parent.parent
TEXTURE_OUT_DIR = (
    REPO_ROOT / "src/main/resources/assets/appliedstoragesorter/textures/block/new_dav"
)
VAULT_FRONT_SOURCE = (
    REPO_ROOT
    / "src/main/resources/assets/appliedstoragesorter/textures/block/new_digital_asset_vault_front.png"
)

AE2_PREFIX = "assets/ae2/textures/block/"


@dataclass(frozen=True)
class FilterLayer:
    color: str  # ``#RRGGBB``
    alpha: float  # 0.0 = unchanged, 1.0 = solid tint color


# ---------------------------------------------------------------------------
# Edit these values, then run this script.
# ---------------------------------------------------------------------------
CONFIG = {
    # Vault front face (block FACING): stronger deep-blue filter.
    "front": FilterLayer(color="#2E4A9E", alpha=0.38),
    # Top, bottom, sides, inside panels: lighter filter.
    "other": FilterLayer(color="#4A6BB8", alpha=0.24),
}
# ---------------------------------------------------------------------------

# output_name -> (filter tier, source kind, source path in jar or mod)
TEXTURE_JOBS: dict[str, tuple[str, str, str]] = {
    "vault_front": ("front", "mod", str(VAULT_FRONT_SOURCE.relative_to(REPO_ROOT))),
    "back": ("other", "ae2", f"{AE2_PREFIX}generics/back.png"),
    "generic_front": ("other", "ae2", f"{AE2_PREFIX}generics/front.png"),
    "side": ("other", "ae2", f"{AE2_PREFIX}generics/side.png"),
    "bottom": ("other", "ae2", f"{AE2_PREFIX}generics/bottom.png"),
    "top": ("other", "ae2", f"{AE2_PREFIX}generics/top.png"),
    "inside": ("other", "ae2", f"{AE2_PREFIX}drive/drive_inside.png"),
    "inside_top": ("other", "ae2", f"{AE2_PREFIX}drive/drive_inside_top.png"),
    "inside_bottom": ("other", "ae2", f"{AE2_PREFIX}drive/drive_inside_bottom.png"),
}


def parse_hex_rgb(color: str) -> tuple[int, int, int]:
    value = color.strip().lstrip("#")
    if len(value) == 8:
        value = value[2:]
    if len(value) != 6:
        raise ValueError(f"Expected #RRGGBB, got {color!r}")
    return int(value[0:2], 16), int(value[2:4], 16), int(value[4:6], 16)


def recolor_front_highlights(image: PngImage) -> PngImage:
    """Shift lavender/purple glow on the vault front master toward cobalt blue."""
    cobalt = (106, 154, 232)  # #6A9AE8
    out = bytearray(len(image.pixels))
    for index in range(0, len(image.pixels), 4):
        r, g, b, a = image.pixels[index : index + 4]
        if a == 0:
            out[index : index + 4] = image.pixels[index : index + 4]
            continue
        brightness = (r + g + b) / 3
        purple_glow = brightness > 70 and b > g and r > g and r > 90
        if purple_glow:
            strength = min(1.0, (brightness - 70) / 110)
            cr, cg, cb = cobalt
            nr = int(r + (cr - r) * strength * 0.65)
            ng = int(g + (cg - g) * strength * 0.65)
            nb = int(b + (cb - b) * strength * 0.45)
            out[index : index + 4] = bytes((min(255, nr), min(255, ng), min(255, nb), a))
        else:
            out[index : index + 4] = image.pixels[index : index + 4]
    return PngImage(image.width, image.height, bytes(out))


def apply_color_filter(image: PngImage, tint_rgb: tuple[int, int, int], alpha: float) -> PngImage:
    tr, tg, tb = tint_rgb
    inv = 1.0 - alpha
    out = bytearray(len(image.pixels))
    for index in range(0, len(image.pixels), 4):
        r, g, b, a = image.pixels[index : index + 4]
        if a == 0:
            out[index : index + 4] = image.pixels[index : index + 4]
            continue
        nr = int(r * inv + tr * alpha)
        ng = int(g * inv + tg * alpha)
        nb = int(b * inv + tb * alpha)
        out[index : index + 4] = bytes((min(255, nr), min(255, ng), min(255, nb), a))
    return PngImage(image.width, image.height, bytes(out))


def extract_front_lights(front: PngImage) -> PngImage:
    """Emissive overlay for the vault-cube model from bright pixels on the filtered front."""
    out = bytearray(len(front.pixels))
    for index in range(0, len(front.pixels), 4):
        r, g, b, a = front.pixels[index : index + 4]
        if a == 0:
            out[index : index + 4] = b"\x00\x00\x00\x00"
            continue
        brightness = (r + g + b) / 3
        if brightness > 95 and b >= g:
            glow = min(255, int((brightness - 95) * 3.2))
            out[index : index + 4] = bytes((min(255, b), min(255, g + 20), min(255, r + 10), min(a, glow)))
        else:
            out[index : index + 4] = b"\x00\x00\x00\x00"
    return PngImage(front.width, front.height, bytes(out))


def load_source_image(kind: str, path: str, ae2_archive: zipfile.ZipFile) -> PngImage:
    if kind == "mod":
        mod_path = REPO_ROOT / path
        if not mod_path.exists():
            raise FileNotFoundError(f"Missing mod texture source: {mod_path}")
        return read_png(mod_path)
    if kind == "ae2":
        try:
            return read_png_from_zip(ae2_archive, path)
        except KeyError as exc:
            raise FileNotFoundError(f"Missing AE2 texture in jar: {path}") from exc
    raise ValueError(f"Unknown source kind: {kind!r}")


def read_png_from_zip(archive: zipfile.ZipFile, path: str) -> PngImage:
    from gui_texture_common import read_png_from_bytes

    return read_png_from_bytes(archive.read(path))


def preview(name: str, layer: FilterLayer) -> str:
    r, g, b = parse_hex_rgb(layer.color)
    return f"{name:>5}: #{r:02X}{g:02X}{b:02X}  alpha={layer.alpha:.3f}"


def main() -> None:
    front = CONFIG["front"]
    other = CONFIG["other"]
    if not isinstance(front, FilterLayer) or not isinstance(other, FilterLayer):
        raise TypeError("CONFIG entries must be FilterLayer instances")
    for label, layer in ("front", front), ("other", other):
        if not 0.0 <= layer.alpha <= 1.0:
            raise ValueError(f"CONFIG['{label}'].alpha must be between 0.0 and 1.0")

    tiers = {
        "front": (parse_hex_rgb(front.color), front.alpha),
        "other": (parse_hex_rgb(other.color), other.alpha),
    }

    TEXTURE_OUT_DIR.mkdir(parents=True, exist_ok=True)
    ae2_jar = find_ae2_runtime_jar()
    filtered_front: PngImage | None = None

    with zipfile.ZipFile(ae2_jar, "r") as ae2_archive:
        for output_name, (tier, kind, source_path) in TEXTURE_JOBS.items():
            tint_rgb, alpha = tiers[tier]
            source = load_source_image(kind, source_path, ae2_archive)
            if output_name == "vault_front":
                source = recolor_front_highlights(source)
            filtered = apply_color_filter(source, tint_rgb, alpha)
            if output_name == "vault_front":
                filtered_front = filtered
            out_path = TEXTURE_OUT_DIR / f"{output_name}.png"
            save_rgba_png(out_path, filtered.width, filtered.height, filtered.pixels)
            print(f"Wrote {out_path.relative_to(REPO_ROOT)}  ({tier}, alpha={alpha:.3f})")

    if filtered_front is not None:
        lights = extract_front_lights(filtered_front)
        lights_path = TEXTURE_OUT_DIR / "vault_front_lights.png"
        save_rgba_png(lights_path, lights.width, lights.height, lights.pixels)
        print(f"Wrote {lights_path.relative_to(REPO_ROOT)}  (derived from vault_front)")

    print()
    print("Filter settings:")
    print(preview("front", front), " -> vault_front.png")
    print(preview("other", other), " -> all other faces")
    print()
    print("Master (unfiltered) front kept at:")
    print(f"  {VAULT_FRONT_SOURCE.relative_to(REPO_ROOT)}")
    print()
    print("Next: recompile and relaunch the client.")


if __name__ == "__main__":
    main()

#!/usr/bin/env python3
"""Generate themed ME Controller block textures from AE2 originals."""

from __future__ import annotations

import argparse
import json
from pathlib import Path

from gui_texture_common import (
    DEFAULT_TEMPLATE_FILE,
    build_themed_controller_textures,
    controller_core_color_manifest,
    load_themes,
    select_themes,
    write_manifest,
)

PROJECT_ROOT = Path(__file__).resolve().parent.parent
DEFAULT_OUTPUT_DIR = (
    PROJECT_ROOT / "src/main/resources/assets/appliedstoragesorter/textures/block"
)
DEFAULT_MANIFEST_DIR = Path(__file__).resolve().parent / "generated_controller_textures"


def generate_theme(theme, out_dir: Path, write_color_manifest: bool) -> None:
    textures = build_themed_controller_textures(theme)
    for name, image in textures.items():
        image.save_png(out_dir / name)

    if write_color_manifest:
        manifest_dir = DEFAULT_MANIFEST_DIR / theme.name
        manifest_dir.mkdir(parents=True, exist_ok=True)
        write_manifest(manifest_dir / "core_colors.json", controller_core_color_manifest(theme))
        write_manifest(
            manifest_dir / "manifest.json",
            {
                "theme": theme.name,
                "description": theme.description,
                "source": "assets/ae2/textures/block/controller*.png",
                "output_dir": str(out_dir.relative_to(PROJECT_ROOT)).replace("\\", "/"),
                "files": list(textures.keys()),
            },
        )


def main() -> None:
    parser = argparse.ArgumentParser(description="Generate themed ME Controller block textures.")
    parser.add_argument("--template-file", type=Path, default=DEFAULT_TEMPLATE_FILE)
    parser.add_argument("--output-dir", type=Path, default=DEFAULT_OUTPUT_DIR)
    parser.add_argument("--theme", action="append", default=[], help="Theme name. Defaults to applied_dark_matter_v2.")
    parser.add_argument("--manifest", action="store_true", help="Write core color mapping JSON under tools/generated_controller_textures/")
    args = parser.parse_args()

    themes = select_themes(load_themes(args.template_file), args.theme or ["applied_dark_matter_v2"])
    for theme in themes:
        generate_theme(theme, args.output_dir, args.manifest)
        print(f"[ok] generated controller textures for '{theme.name}' -> {args.output_dir}")


if __name__ == "__main__":
    main()

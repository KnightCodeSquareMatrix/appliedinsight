#!/usr/bin/env python3
from __future__ import annotations

import argparse
from pathlib import Path

from generate_gui_preview_pages import generate_theme as generate_preview_theme
from generate_gui_runtime_textures import generate_theme as generate_runtime_theme
from gui_texture_common import (
    DEFAULT_PREVIEW_OUTPUT_DIR,
    DEFAULT_TEMPLATE_FILE,
    DEFAULT_TEXTURE_OUTPUT_DIR,
    load_ae2_reference_assets,
    load_themes,
    select_themes,
)


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Generate GUI preview pages, runtime textures, or both from the shared theme templates."
    )
    parser.add_argument("--template-file", type=Path, default=DEFAULT_TEMPLATE_FILE, help="Theme template JSON file.")
    parser.add_argument(
        "--mode",
        choices=("preview", "textures", "all"),
        default="all",
        help="Which output pipeline to run.",
    )
    parser.add_argument("--preview-output-dir", type=Path, default=DEFAULT_PREVIEW_OUTPUT_DIR, help="Preview output directory.")
    parser.add_argument("--texture-output-dir", type=Path, default=DEFAULT_TEXTURE_OUTPUT_DIR, help="Runtime texture output directory.")
    parser.add_argument("--theme", action="append", default=[], help="Generate only the named theme. Can be repeated.")
    args = parser.parse_args()

    themes = select_themes(load_themes(args.template_file), args.theme)
    ae2_assets = load_ae2_reference_assets() if args.mode in ("preview", "textures", "all") else None

    for theme in themes:
        if args.mode in ("preview", "all"):
            preview_dir = args.preview_output_dir / theme.name
            generate_preview_theme(theme, preview_dir, ae2_assets)
            print(f"[ok] generated preview theme '{theme.name}' -> {preview_dir}")
        if args.mode in ("textures", "all"):
            texture_dir = args.texture_output_dir / theme.name
            generate_runtime_theme(theme, texture_dir, ae2_assets)
            print(f"[ok] generated runtime textures for '{theme.name}' -> {texture_dir}")


if __name__ == "__main__":
    main()

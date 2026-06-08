#!/usr/bin/env python3
from __future__ import annotations

import json
import struct
import zlib
import zipfile
from dataclasses import dataclass
from pathlib import Path
from typing import Iterable


SCRIPT_DIR = Path(__file__).resolve().parent
PROJECT_ROOT = SCRIPT_DIR.parent
DEFAULT_TEMPLATE_FILE = SCRIPT_DIR / "gui_palette_templates.json"
DEFAULT_PREVIEW_OUTPUT_DIR = SCRIPT_DIR / "generated_gui_previews"
DEFAULT_TEXTURE_OUTPUT_DIR = SCRIPT_DIR / "generated_gui_textures"
DEFAULT_AE2_REFERENCE_DIR = SCRIPT_DIR / "ae2_reference_sprites"
DEFAULT_RESOURCE_TEXTURE_ROOT = Path("assets") / "appliedstoragesorter" / "textures" / "gui"

SCB_PANEL_WIDTH = 440
SCB_PANEL_HEIGHT = 224
SCB_PREVIEW_WIDTH = 472
SCB_PREVIEW_HEIGHT = 236
SCB_PREVIEW_PANEL_X = 26
SCB_PREVIEW_PANEL_Y = 8

SCB_HEADER_HEIGHT = 20
SCB_FOOTER_HEIGHT = 16
SCB_HORIZONTAL_GAP = 8
SCB_VERTICAL_GAP = 6
SCB_LEFT_COL_WIDTH = 120
SCB_MIDDLE_COL_WIDTH = 140
SCB_RIGHT_COL_WIDTH = 148
SCB_BUTTON_WIDTH = 100
SCB_BUTTON_HEIGHT = 18
SCB_BUTTON_GAP = 12
SCB_BUTTONS_TOP_PADDING = 30
SCB_BUTTONS_BOTTOM_PADDING = 18

AE2_TOOLBAR_LEFT = 3
AE2_TOOLBAR_TOP = 1
AE2_TOOLBAR_MARGIN = 2
AE2_TOOLBAR_VERTICAL_SPACING = 6

DAV_TEXTURE_WIDTH = 256
DAV_TEXTURE_HEIGHT = 256
DAV_PANEL_WIDTH = 176
DAV_PANEL_HEIGHT = 223
DAV_HEADER_HEIGHT = 84
DAV_STORAGE_SECTION_HEIGHT = 117
DAV_CARD_PANEL_START_Y = 201
DAV_CARD_PANEL_HEIGHT = 22

DAV_PREVIEW_WIDTH = 208
DAV_PREVIEW_HEIGHT = 248
DAV_PREVIEW_PANEL_X = 16
DAV_PREVIEW_PANEL_Y = 12

STATUS_LAMP_SIZE = 7

HELP_ICON_RECT = (176, 0, 16, 16)
TOOLBAR_BUTTON_BG_RECT = (176, 128, 18, 20)

REQUIRED_COLOR_GROUPS = {
    "background": ("outline_dark", "edge_light", "fill", "shadow"),
    "section": ("outline_dark", "edge_light", "fill", "shadow"),
    "help_button": ("icon_light", "icon_shadow", "frame_light", "frame_dark", "face"),
    "action_button": ("outline_dark", "edge_light", "face", "shadow"),
    "storage_slot": ("outline_dark", "edge_light", "face", "shadow"),
    "text": ("primary", "muted", "warning"),
    "status": ("online", "warning", "offline", "lamp_border"),
}

COLOR_SWATCH_ORDER = [
    ("background", "outline_dark"),
    ("background", "edge_light"),
    ("background", "fill"),
    ("background", "shadow"),
    ("section", "outline_dark"),
    ("section", "edge_light"),
    ("section", "fill"),
    ("section", "shadow"),
    ("help_button", "icon_light"),
    ("help_button", "icon_shadow"),
    ("help_button", "frame_light"),
    ("help_button", "frame_dark"),
    ("help_button", "face"),
    ("action_button", "outline_dark"),
    ("action_button", "edge_light"),
    ("action_button", "face"),
    ("action_button", "shadow"),
    ("storage_slot", "outline_dark"),
    ("storage_slot", "edge_light"),
    ("storage_slot", "face"),
    ("storage_slot", "shadow"),
    ("text", "primary"),
    ("text", "muted"),
    ("text", "warning"),
    ("status", "online"),
    ("status", "warning"),
    ("status", "offline"),
    ("status", "lamp_border"),
]

AE2_REFERENCE_FILES = {
    "vertical_buttons_bg.png": "assets/ae2/textures/gui/sprites/vertical_buttons_bg.png",
    "vertical_buttons_bg.png.mcmeta": "assets/ae2/textures/gui/sprites/vertical_buttons_bg.png.mcmeta",
    "button.png": "assets/ae2/textures/gui/sprites/button.png",
    "button.png.mcmeta": "assets/ae2/textures/gui/sprites/button.png.mcmeta",
    "button_highlighted.png": "assets/ae2/textures/gui/sprites/button_highlighted.png",
    "button_highlighted.png.mcmeta": "assets/ae2/textures/gui/sprites/button_highlighted.png.mcmeta",
    "button_disabled.png": "assets/ae2/textures/gui/sprites/button_disabled.png",
    "button_disabled.png.mcmeta": "assets/ae2/textures/gui/sprites/button_disabled.png.mcmeta",
    "states.png": "assets/ae2/textures/guis/states.png",
    "background.png": "assets/ae2/textures/guis/background.png",
}


@dataclass(frozen=True)
class Theme:
    name: str
    description: str
    colors: dict[str, dict[str, tuple[int, int, int, int]]]

    def color(self, group: str, key: str) -> tuple[int, int, int, int]:
        return self.colors[group][key]


@dataclass(frozen=True)
class NineSliceBorder:
    left: int
    top: int
    right: int
    bottom: int


@dataclass(frozen=True)
class PngImage:
    width: int
    height: int
    pixels: bytes

    def crop(self, x: int, y: int, width: int, height: int) -> PngImage:
        out = bytearray(width * height * 4)
        for row in range(height):
            src_start = ((y + row) * self.width + x) * 4
            src_end = src_start + width * 4
            dst_start = row * width * 4
            out[dst_start:dst_start + width * 4] = self.pixels[src_start:src_end]
        return PngImage(width, height, bytes(out))

    def save_png(self, path: Path) -> None:
        save_rgba_png(path, self.width, self.height, self.pixels)


@dataclass(frozen=True)
class Ae2ReferenceAssets:
    vertical_toolbar_background: PngImage
    vertical_toolbar_background_border: NineSliceBorder
    button_normal: PngImage
    button_hover: PngImage
    button_disabled: PngImage
    button_border: NineSliceBorder
    states: PngImage
    background: PngImage


@dataclass(frozen=True)
class ThemedAe2Sprites:
    vertical_toolbar_background: PngImage
    button_normal: PngImage
    button_hover: PngImage
    button_disabled: PngImage
    toolbar_button_background: PngImage
    help_icon: PngImage


class PixelCanvas:
    def __init__(self, width: int, height: int, fill: tuple[int, int, int, int] = (0, 0, 0, 0)) -> None:
        self.width = width
        self.height = height
        self.pixels = bytearray(fill * (width * height))

    def fill(self, color: tuple[int, int, int, int]) -> None:
        self.pixels[:] = bytearray(color * (self.width * self.height))

    def fill_rect(self, x: int, y: int, width: int, height: int, color: tuple[int, int, int, int]) -> None:
        x0 = max(0, x)
        y0 = max(0, y)
        x1 = min(self.width, x + width)
        y1 = min(self.height, y + height)
        if x0 >= x1 or y0 >= y1:
            return

        row = bytes(color * (x1 - x0))
        for py in range(y0, y1):
            start = (py * self.width + x0) * 4
            end = start + len(row)
            self.pixels[start:end] = row

    def draw_hline(self, x: int, y: int, width: int, color: tuple[int, int, int, int]) -> None:
        self.fill_rect(x, y, width, 1, color)

    def draw_vline(self, x: int, y: int, height: int, color: tuple[int, int, int, int]) -> None:
        self.fill_rect(x, y, 1, height, color)

    def draw_bevel_box(
        self,
        x: int,
        y: int,
        width: int,
        height: int,
        face: tuple[int, int, int, int],
        highlight: tuple[int, int, int, int],
        shadow: tuple[int, int, int, int],
        *,
        depth: int = 1,
        inset: bool = False,
    ) -> None:
        self.fill_rect(x, y, width, height, face)
        top_left = shadow if inset else highlight
        bottom_right = highlight if inset else shadow
        for depth_index in range(depth):
            self.draw_hline(x + depth_index, y + depth_index, width - depth_index * 2, top_left)
            self.draw_vline(x + depth_index, y + depth_index, height - depth_index * 2, top_left)
            self.draw_hline(x + depth_index, y + height - 1 - depth_index, width - depth_index * 2, bottom_right)
            self.draw_vline(x + width - 1 - depth_index, y + depth_index, height - depth_index * 2, bottom_right)

    def blit_image(self, image: PngImage, x: int, y: int) -> None:
        for py in range(image.height):
            dst_y = y + py
            if dst_y < 0 or dst_y >= self.height:
                continue
            for px in range(image.width):
                dst_x = x + px
                if dst_x < 0 or dst_x >= self.width:
                    continue
                src_index = (py * image.width + px) * 4
                src_rgba = image.pixels[src_index:src_index + 4]
                if src_rgba[3] == 0:
                    continue
                self._blend_pixel(dst_x, dst_y, src_rgba)

    def stretch_blit(
        self,
        image: PngImage,
        src_x: int,
        src_y: int,
        src_width: int,
        src_height: int,
        dst_x: int,
        dst_y: int,
        dst_width: int,
        dst_height: int,
    ) -> None:
        if src_width <= 0 or src_height <= 0 or dst_width <= 0 or dst_height <= 0:
            return
        for row in range(dst_height):
            sample_y = src_y + min(src_height - 1, (row * src_height) // dst_height)
            for col in range(dst_width):
                sample_x = src_x + min(src_width - 1, (col * src_width) // dst_width)
                src_index = (sample_y * image.width + sample_x) * 4
                rgba = image.pixels[src_index:src_index + 4]
                if rgba[3] == 0:
                    continue
                self._blend_pixel(dst_x + col, dst_y + row, rgba)

    def draw_nine_slice(self, image: PngImage, border: NineSliceBorder, x: int, y: int, width: int, height: int) -> None:
        center_width = image.width - border.left - border.right
        center_height = image.height - border.top - border.bottom
        dst_center_width = max(0, width - border.left - border.right)
        dst_center_height = max(0, height - border.top - border.bottom)

        patches = [
            (0, 0, border.left, border.top, x, y, border.left, border.top),
            (border.left, 0, center_width, border.top, x + border.left, y, dst_center_width, border.top),
            (image.width - border.right, 0, border.right, border.top, x + width - border.right, y, border.right, border.top),
            (0, border.top, border.left, center_height, x, y + border.top, border.left, dst_center_height),
            (border.left, border.top, center_width, center_height, x + border.left, y + border.top, dst_center_width, dst_center_height),
            (image.width - border.right, border.top, border.right, center_height, x + width - border.right, y + border.top, border.right, dst_center_height),
            (0, image.height - border.bottom, border.left, border.bottom, x, y + height - border.bottom, border.left, border.bottom),
            (border.left, image.height - border.bottom, center_width, border.bottom, x + border.left, y + height - border.bottom, dst_center_width, border.bottom),
            (image.width - border.right, image.height - border.bottom, border.right, border.bottom, x + width - border.right, y + height - border.bottom, border.right, border.bottom),
        ]
        for patch in patches:
            self.stretch_blit(image, *patch)

    def save_png(self, path: Path) -> None:
        save_rgba_png(path, self.width, self.height, bytes(self.pixels))

    def to_image(self) -> PngImage:
        return PngImage(self.width, self.height, bytes(self.pixels))

    def _blend_pixel(self, x: int, y: int, src_rgba: bytes) -> None:
        if x < 0 or y < 0 or x >= self.width or y >= self.height:
            return

        dst_index = (y * self.width + x) * 4
        src_alpha = src_rgba[3]
        if src_alpha == 255:
            self.pixels[dst_index:dst_index + 4] = src_rgba
            return

        dst_r, dst_g, dst_b, dst_a = self.pixels[dst_index:dst_index + 4]
        src_r, src_g, src_b, _ = src_rgba
        out_a = src_alpha + (dst_a * (255 - src_alpha) + 127) // 255
        if out_a == 0:
            self.pixels[dst_index:dst_index + 4] = bytes((0, 0, 0, 0))
            return

        src_weight = src_alpha * 255
        dst_weight = dst_a * (255 - src_alpha)
        out_r = (src_r * src_weight + dst_r * dst_weight + out_a * 127) // (out_a * 255)
        out_g = (src_g * src_weight + dst_g * dst_weight + out_a * 127) // (out_a * 255)
        out_b = (src_b * src_weight + dst_b * dst_weight + out_a * 127) // (out_a * 255)
        self.pixels[dst_index:dst_index + 4] = bytes((out_r, out_g, out_b, out_a))


def parse_color(value: str) -> tuple[int, int, int, int]:
    text = value.strip().lstrip("#")
    if len(text) == 6:
        return tuple(int(text[index:index + 2], 16) for index in range(0, 6, 2)) + (255,)
    if len(text) == 8:
        return tuple(int(text[index:index + 2], 16) for index in range(0, 8, 2))
    raise ValueError(f"Unsupported color value: {value}")


def load_themes(path: Path) -> list[Theme]:
    payload = json.loads(path.read_text(encoding="utf-8"))
    themes: list[Theme] = []
    for name, entry in payload["themes"].items():
        colors = normalize_theme_colors(entry["colors"])
        themes.append(Theme(name=name, description=entry["description"], colors=colors))
    return themes


def normalize_theme_colors(raw_colors: dict) -> dict[str, dict[str, tuple[int, int, int, int]]]:
    if all(group in raw_colors for group in REQUIRED_COLOR_GROUPS):
        colors = {
            group: {key: parse_color(raw_colors[group][key]) for key in keys}
            for group, keys in REQUIRED_COLOR_GROUPS.items()
        }
        validate_theme_groups(colors)
        return colors

    flat = {key: parse_color(value) for key, value in raw_colors.items()}
    colors = {
        "background": {
            "outline_dark": flat["frame_shadow"],
            "edge_light": flat["frame_highlight"],
            "fill": flat["frame_face"],
            "shadow": flat.get("footer_face", mix(flat["frame_face"], flat["frame_shadow"], 0.45)),
        },
        "section": {
            "outline_dark": flat["section_shadow"],
            "edge_light": flat["section_highlight"],
            "fill": flat["section_face"],
            "shadow": mix(flat["section_face"], flat["section_shadow"], 0.42),
        },
        "help_button": {
            "icon_light": flat["text_primary"],
            "icon_shadow": mix(flat["frame_shadow"], flat["lamp_border"], 0.5),
            "frame_light": flat.get("button_highlight", flat["frame_highlight"]),
            "frame_dark": flat["frame_shadow"],
            "face": flat.get("button_face", flat["header_face"]),
        },
        "action_button": {
            "outline_dark": flat["frame_shadow"],
            "edge_light": flat.get("button_highlight", flat["frame_highlight"]),
            "face": flat.get("button_face", flat["header_face"]),
            "shadow": flat.get("button_shadow", mix(flat["frame_shadow"], flat["frame_face"], 0.35)),
        },
        "storage_slot": {
            "outline_dark": flat["frame_shadow"],
            "edge_light": flat["cell_highlight"],
            "face": flat["cell_face"],
            "shadow": flat["cell_shadow"],
        },
        "text": {
            "primary": flat["text_primary"],
            "muted": flat["text_muted"],
            "warning": flat["text_warning"],
        },
        "status": {
            "online": flat["status_online"],
            "warning": flat["status_warning"],
            "offline": flat["status_offline"],
            "lamp_border": flat["lamp_border"],
        },
    }
    validate_theme_groups(colors)
    return colors


def validate_theme_groups(colors: dict[str, dict[str, tuple[int, int, int, int]]]) -> None:
    missing_groups = [group for group in REQUIRED_COLOR_GROUPS if group not in colors]
    if missing_groups:
        raise ValueError(f"Theme is missing groups: {', '.join(missing_groups)}")
    for group, keys in REQUIRED_COLOR_GROUPS.items():
        missing_keys = [key for key in keys if key not in colors[group]]
        if missing_keys:
            raise ValueError(f"Theme group '{group}' is missing keys: {', '.join(missing_keys)}")


def select_themes(all_themes: list[Theme], chosen: Iterable[str]) -> list[Theme]:
    if not chosen:
        return all_themes
    names = set(chosen)
    selected = [theme for theme in all_themes if theme.name in names]
    missing = names.difference(theme.name for theme in selected)
    if missing:
        raise ValueError(f"Unknown theme(s): {', '.join(sorted(missing))}")
    return selected


def ensure_ae2_reference_assets(reference_dir: Path = DEFAULT_AE2_REFERENCE_DIR) -> None:
    missing = [name for name in AE2_REFERENCE_FILES if not (reference_dir / name).exists()]
    if not missing:
        return

    jar_path = find_ae2_runtime_jar()
    reference_dir.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(jar_path, "r") as archive:
        for name, archive_path in AE2_REFERENCE_FILES.items():
            target_path = reference_dir / name
            if target_path.exists():
                continue
            target_path.write_bytes(archive.read(archive_path))


def load_ae2_reference_assets(reference_dir: Path = DEFAULT_AE2_REFERENCE_DIR) -> Ae2ReferenceAssets:
    ensure_ae2_reference_assets(reference_dir)
    return Ae2ReferenceAssets(
        vertical_toolbar_background=read_png(reference_dir / "vertical_buttons_bg.png"),
        vertical_toolbar_background_border=read_nine_slice_border(reference_dir / "vertical_buttons_bg.png.mcmeta"),
        button_normal=read_png(reference_dir / "button.png"),
        button_hover=read_png(reference_dir / "button_highlighted.png"),
        button_disabled=read_png(reference_dir / "button_disabled.png"),
        button_border=read_nine_slice_border(reference_dir / "button.png.mcmeta"),
        states=read_png(reference_dir / "states.png"),
        background=read_png(reference_dir / "background.png"),
    )


def build_themed_ae2_sprites(theme: Theme, ae2_assets: Ae2ReferenceAssets) -> ThemedAe2Sprites:
    background_outline = theme.color("background", "outline_dark")
    background_light = theme.color("background", "edge_light")
    background_fill = theme.color("background", "fill")
    background_shadow = theme.color("background", "shadow")

    button_outline = theme.color("action_button", "outline_dark")
    button_light = theme.color("action_button", "edge_light")
    button_face = theme.color("action_button", "face")
    button_shadow = theme.color("action_button", "shadow")

    hover_face = mix(button_face, theme.color("text", "primary"), 0.18)
    hover_light = mix(button_light, theme.color("text", "primary"), 0.22)
    hover_shadow = mix(button_shadow, theme.color("text", "primary"), 0.12)

    disabled_face = mix(button_face, theme.color("background", "fill"), 0.55)
    disabled_light = mix(button_light, theme.color("background", "shadow"), 0.38)
    disabled_shadow = mix(button_shadow, theme.color("background", "fill"), 0.42)

    bg_x, bg_y, bg_width, bg_height = TOOLBAR_BUTTON_BG_RECT
    icon_x, icon_y, icon_width, icon_height = HELP_ICON_RECT
    toolbar_button_background = ae2_assets.states.crop(bg_x, bg_y, bg_width, bg_height)
    help_icon = ae2_assets.states.crop(icon_x, icon_y, icon_width, icon_height)

    return ThemedAe2Sprites(
        vertical_toolbar_background=recolor_image_palette(
            ae2_assets.vertical_toolbar_background,
            {
                (0x41, 0x3F, 0x54, 0xFF): background_outline,
                (0xF2, 0xF2, 0xF2, 0xFF): background_light,
                (0xCB, 0xCC, 0xD4, 0xFF): background_fill,
                (0x87, 0x8F, 0xA5, 0xFF): background_shadow,
            },
        ),
        button_normal=recolor_image_palette(
            ae2_assets.button_normal,
            {
                (0x41, 0x3F, 0x54, 0xFF): button_outline,
                (0xAD, 0xB0, 0xC4, 0xFF): button_light,
                (0x9A, 0x9F, 0xB4, 0xFF): button_face,
                (0x69, 0x6D, 0x88, 0xFF): button_shadow,
            },
        ),
        button_hover=recolor_image_palette(
            ae2_assets.button_hover,
            {
                (0x41, 0x3F, 0x54, 0xFF): button_outline,
                (0xDA, 0xFF, 0xFF, 0xFF): hover_light,
                (0x9C, 0xD3, 0xFF, 0xFF): hover_face,
                (0x70, 0x8C, 0xBA, 0xFF): hover_shadow,
                (0xFF, 0xFF, 0xFF, 0x00): (0, 0, 0, 0),
            },
        ),
        button_disabled=recolor_image_palette(
            ae2_assets.button_disabled,
            {
                (0x41, 0x3F, 0x54, 0xFF): button_outline,
                (0x87, 0x8F, 0xA5, 0xFF): disabled_light,
                (0x69, 0x6D, 0x88, 0xFF): disabled_shadow,
                (0xFF, 0xFF, 0xFF, 0x00): (0, 0, 0, 0),
            },
        ),
        toolbar_button_background=recolor_image_palette(
            toolbar_button_background, toolbar_button_chrome_palette(theme)),
        help_icon=recolor_image_palette(help_icon, help_glyph_palette(theme)),
    )


def find_ae2_runtime_jar() -> Path:
    base = Path.home() / ".gradle" / "caches" / "modules-2" / "files-2.1" / "org.appliedenergistics" / "appliedenergistics2"
    if not base.exists():
        raise FileNotFoundError("AE2 runtime jar not found in Gradle cache.")

    candidates = sorted(
        path for path in base.glob("*/*/appliedenergistics2-*.jar")
        if "-sources" not in path.name and "-javadoc" not in path.name
    )
    if not candidates:
        raise FileNotFoundError("AE2 runtime jar not found in Gradle cache.")
    return candidates[-1]


def read_nine_slice_border(path: Path) -> NineSliceBorder:
    payload = json.loads(path.read_text(encoding="utf-8"))
    border = payload["gui"]["scaling"]["border"]
    if isinstance(border, int):
        return NineSliceBorder(border, border, border, border)
    return NineSliceBorder(
        border["left"],
        border["top"],
        border["right"],
        border["bottom"],
    )


def read_png(path: Path) -> PngImage:
    data = path.read_bytes()
    if data[:8] != b"\x89PNG\r\n\x1a\n":
        raise ValueError(f"{path} is not a PNG file.")

    offset = 8
    width = height = bit_depth = color_type = interlace = None
    idat_chunks: list[bytes] = []

    while offset < len(data):
        chunk_length = struct.unpack(">I", data[offset:offset + 4])[0]
        chunk_type = data[offset + 4:offset + 8]
        chunk_data = data[offset + 8:offset + 8 + chunk_length]
        offset += 12 + chunk_length

        if chunk_type == b"IHDR":
            width, height, bit_depth, color_type, _, _, interlace = struct.unpack(">IIBBBBB", chunk_data)
        elif chunk_type == b"IDAT":
            idat_chunks.append(chunk_data)
        elif chunk_type == b"IEND":
            break

    if width is None or height is None or bit_depth is None or color_type is None or interlace is None:
        raise ValueError(f"{path} is missing IHDR data.")
    if bit_depth != 8 or interlace != 0 or color_type not in (6, 2):
        raise ValueError(
            f"{path} uses unsupported PNG format: bit_depth={bit_depth}, color_type={color_type}, interlace={interlace}"
        )

    channels = 4 if color_type == 6 else 3
    stride = width * channels
    raw = zlib.decompress(b"".join(idat_chunks))
    pos = 0
    previous = bytearray(stride)
    rgba = bytearray(width * height * 4)

    for row in range(height):
        filter_type = raw[pos]
        pos += 1
        filtered = bytearray(raw[pos:pos + stride])
        pos += stride
        scanline = unfilter_scanline(filter_type, filtered, previous, channels)
        previous = bytearray(scanline)
        if color_type == 6:
            dst_start = row * width * 4
            rgba[dst_start:dst_start + stride] = scanline
        else:
            dst_start = row * width * 4
            for col in range(width):
                src = col * 3
                dst = dst_start + col * 4
                rgba[dst:dst + 4] = bytes((scanline[src], scanline[src + 1], scanline[src + 2], 255))

    return PngImage(width, height, bytes(rgba))


def unfilter_scanline(filter_type: int, scanline: bytearray, previous: bytearray, bytes_per_pixel: int) -> bytearray:
    if filter_type == 0:
        return scanline
    if filter_type == 1:
        for index in range(len(scanline)):
            left = scanline[index - bytes_per_pixel] if index >= bytes_per_pixel else 0
            scanline[index] = (scanline[index] + left) & 0xFF
        return scanline
    if filter_type == 2:
        for index in range(len(scanline)):
            scanline[index] = (scanline[index] + previous[index]) & 0xFF
        return scanline
    if filter_type == 3:
        for index in range(len(scanline)):
            left = scanline[index - bytes_per_pixel] if index >= bytes_per_pixel else 0
            up = previous[index]
            scanline[index] = (scanline[index] + ((left + up) // 2)) & 0xFF
        return scanline
    if filter_type == 4:
        for index in range(len(scanline)):
            left = scanline[index - bytes_per_pixel] if index >= bytes_per_pixel else 0
            up = previous[index]
            up_left = previous[index - bytes_per_pixel] if index >= bytes_per_pixel else 0
            scanline[index] = (scanline[index] + paeth_predictor(left, up, up_left)) & 0xFF
        return scanline
    raise ValueError(f"Unsupported PNG filter type: {filter_type}")


def paeth_predictor(left: int, up: int, up_left: int) -> int:
    predictor = left + up - up_left
    left_distance = abs(predictor - left)
    up_distance = abs(predictor - up)
    up_left_distance = abs(predictor - up_left)
    if left_distance <= up_distance and left_distance <= up_left_distance:
        return left
    if up_distance <= up_left_distance:
        return up
    return up_left


def recolor_image_tritone(
    image: PngImage,
    dark: tuple[int, int, int, int],
    mid: tuple[int, int, int, int],
    light: tuple[int, int, int, int],
) -> PngImage:
    out = bytearray(len(image.pixels))
    for index in range(0, len(image.pixels), 4):
        src_r, src_g, src_b, src_a = image.pixels[index:index + 4]
        if src_a == 0:
            continue
        luminance = (src_r * 299 + src_g * 587 + src_b * 114) / 255000.0
        if luminance <= 0.5:
            ratio = luminance / 0.5 if luminance > 0 else 0.0
            color = mix(dark, mid, ratio)
        else:
            ratio = (luminance - 0.5) / 0.5
            color = mix(mid, light, ratio)
        out[index:index + 4] = bytes((color[0], color[1], color[2], src_a))
    return PngImage(image.width, image.height, bytes(out))


def recolor_image_palette(
    image: PngImage,
    mapping: dict[tuple[int, int, int, int], tuple[int, int, int, int]],
) -> PngImage:
    out = bytearray(image.pixels)
    for index in range(0, len(out), 4):
        rgba = tuple(out[index:index + 4])
        if rgba in mapping:
            replacement = mapping[rgba]
            out[index:index + 4] = bytes(replacement)
    return PngImage(image.width, image.height, bytes(out))


def save_rgba_png(path: Path, width: int, height: int, pixels: bytes) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    raw = bytearray()
    stride = width * 4
    for y in range(height):
        raw.append(0)
        start = y * stride
        raw.extend(pixels[start:start + stride])

    def chunk(tag: bytes, payload: bytes) -> bytes:
        return (
            struct.pack(">I", len(payload))
            + tag
            + payload
            + struct.pack(">I", zlib.crc32(tag + payload) & 0xFFFFFFFF)
        )

    ihdr = struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0)
    idat = zlib.compress(bytes(raw), level=9)
    png = b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", ihdr) + chunk(b"IDAT", idat) + chunk(b"IEND", b"")
    path.write_bytes(png)


def draw_status_lamp(canvas: PixelCanvas, x: int, y: int, fill: tuple[int, int, int, int], border: tuple[int, int, int, int]) -> None:
    canvas.fill_rect(x, y, STATUS_LAMP_SIZE, STATUS_LAMP_SIZE, border)
    canvas.fill_rect(x + 1, y + 1, STATUS_LAMP_SIZE - 2, STATUS_LAMP_SIZE - 2, fill)
    canvas.fill_rect(x + 2, y + 2, 2, 1, mix(fill, (255, 255, 255, 255), 0.4))


def mix(base: tuple[int, int, int, int], other: tuple[int, int, int, int], ratio: float) -> tuple[int, int, int, int]:
    clamped = max(0.0, min(1.0, ratio))
    return tuple(round(base[index] * (1.0 - clamped) + other[index] * clamped) for index in range(4))


def draw_inner_glow_frame(
    canvas: PixelCanvas,
    x: int,
    y: int,
    width: int,
    height: int,
    bright: tuple[int, int, int, int],
    shaded: tuple[int, int, int, int],
) -> None:
    if width < 4 or height < 4:
        return
    canvas.draw_hline(x + 1, y + 1, width - 2, bright)
    canvas.draw_vline(x + 1, y + 1, height - 2, bright)
    canvas.draw_hline(x + 1, y + height - 2, width - 2, shaded)
    canvas.draw_vline(x + width - 2, y + 1, height - 2, shaded)


def draw_panel_box(
    canvas: PixelCanvas,
    x: int,
    y: int,
    width: int,
    height: int,
    outline_dark: tuple[int, int, int, int],
    edge_light: tuple[int, int, int, int],
    fill: tuple[int, int, int, int],
    shadow: tuple[int, int, int, int],
) -> None:
    if width < 7 or height < 7:
        return
    # Layer order from inside to outside:
    # fill -> accent line -> 3d side -> outer outline
    canvas.fill_rect(x, y, width, height, outline_dark)
    canvas.fill_rect(x + 1, y + 1, width - 2, height - 2, shadow)
    canvas.fill_rect(x + 2, y + 2, width - 4, height - 4, edge_light)
    canvas.fill_rect(x + 3, y + 3, width - 6, height - 6, fill)


def get_scb_column_rects() -> dict[str, tuple[int, int, int, int]]:
    content_top = SCB_HEADER_HEIGHT + SCB_VERTICAL_GAP
    content_bottom = SCB_PANEL_HEIGHT - SCB_FOOTER_HEIGHT - SCB_VERTICAL_GAP
    content_height = content_bottom - content_top

    left_x = SCB_HORIZONTAL_GAP
    middle_x = left_x + SCB_LEFT_COL_WIDTH + SCB_HORIZONTAL_GAP
    right_x = middle_x + SCB_MIDDLE_COL_WIDTH + SCB_HORIZONTAL_GAP

    return {
        "left": (left_x, content_top, SCB_LEFT_COL_WIDTH, content_height),
        "middle": (middle_x, content_top, SCB_MIDDLE_COL_WIDTH, content_height),
        "right": (right_x, content_top, SCB_RIGHT_COL_WIDTH, content_height),
    }


def get_scb_button_positions() -> list[tuple[int, int]]:
    columns = get_scb_column_rects()
    left_x, left_y, left_width, left_height = columns["left"]
    button_x = left_x + (left_width - SCB_BUTTON_WIDTH) // 2
    block_height = SCB_BUTTON_HEIGHT * 3 + SCB_BUTTON_GAP * 2
    available = left_height - SCB_BUTTONS_TOP_PADDING - SCB_BUTTONS_BOTTOM_PADDING
    start_y = left_y + SCB_BUTTONS_TOP_PADDING + max(0, (available - block_height) // 2)
    return [
        (button_x, start_y),
        (button_x, start_y + SCB_BUTTON_HEIGHT + SCB_BUTTON_GAP),
        (button_x, start_y + (SCB_BUTTON_HEIGHT + SCB_BUTTON_GAP) * 2),
    ]


def build_sorter_command_block_panel(theme: Theme) -> PixelCanvas:
    canvas = PixelCanvas(SCB_PANEL_WIDTH, SCB_PANEL_HEIGHT)
    bg_outline = theme.color("background", "outline_dark")
    bg_light = theme.color("background", "edge_light")
    bg_fill = theme.color("background", "fill")
    bg_shadow = theme.color("background", "shadow")
    section_outline = theme.color("section", "outline_dark")
    section_light = theme.color("section", "edge_light")
    section_fill = theme.color("section", "fill")
    section_shadow = theme.color("section", "shadow")

    footer_fill = mix(bg_fill, bg_outline, 0.34)

    draw_panel_box(canvas, 0, 0, SCB_PANEL_WIDTH, SCB_PANEL_HEIGHT, bg_outline, bg_light, bg_fill, bg_shadow)
    canvas.fill_rect(3, SCB_PANEL_HEIGHT - SCB_FOOTER_HEIGHT + 1, SCB_PANEL_WIDTH - 6, SCB_FOOTER_HEIGHT - 4, footer_fill)

    for rect in get_scb_column_rects().values():
        x, y, width, height = rect
        draw_panel_box(canvas, x, y, width, height, section_outline, section_light, section_fill, section_shadow)
    return canvas


def build_digital_asset_vault_texture(theme: Theme) -> PixelCanvas:
    canvas = PixelCanvas(DAV_TEXTURE_WIDTH, DAV_TEXTURE_HEIGHT)
    bg_outline = theme.color("background", "outline_dark")
    bg_light = theme.color("background", "edge_light")
    bg_fill = theme.color("background", "fill")
    bg_shadow = theme.color("background", "shadow")
    section_fill = theme.color("section", "fill")
    footer_fill = mix(bg_fill, bg_outline, 0.34)

    draw_panel_box(canvas, 0, 0, DAV_PANEL_WIDTH, DAV_PANEL_HEIGHT, bg_outline, bg_light, bg_fill, bg_shadow)
    canvas.fill_rect(3, DAV_HEADER_HEIGHT + 1, DAV_PANEL_WIDTH - 6, DAV_STORAGE_SECTION_HEIGHT - 3, section_fill)
    canvas.fill_rect(3, DAV_CARD_PANEL_START_Y + 1, DAV_PANEL_WIDTH - 6, DAV_CARD_PANEL_HEIGHT - 4, footer_fill)

    start_x = 18
    start_y = 24
    for row in range(5):
        for col in range(2):
            draw_slot(canvas, start_x + col * 18, start_y + row * 18, theme)

    inventory_x = 8
    inventory_y = 98
    for row in range(4):
        for col in range(9):
            draw_slot(canvas, inventory_x + col * 18, inventory_y + row * 18, theme)

    draw_slot(canvas, DAV_PANEL_WIDTH - 26, DAV_CARD_PANEL_START_Y + 2, theme)
    return canvas


def draw_slot(canvas: PixelCanvas, x: int, y: int, theme: Theme) -> None:
    draw_panel_box(
        canvas,
        x,
        y,
        16,
        16,
        theme.color("storage_slot", "outline_dark"),
        theme.color("storage_slot", "edge_light"),
        theme.color("storage_slot", "face"),
        theme.color("storage_slot", "shadow"),
    )


def build_sorter_command_block_preview(theme: Theme, ae2_assets: Ae2ReferenceAssets) -> PixelCanvas:
    canvas = PixelCanvas(SCB_PREVIEW_WIDTH, SCB_PREVIEW_HEIGHT)
    panel = build_sorter_command_block_panel(theme).to_image()
    themed_sprites = build_themed_ae2_sprites(theme, ae2_assets)
    canvas.blit_image(panel, SCB_PREVIEW_PANEL_X, SCB_PREVIEW_PANEL_Y)

    draw_ae2_vertical_toolbar(canvas, themed_sprites.vertical_toolbar_background, ae2_assets.vertical_toolbar_background_border,
            SCB_PREVIEW_PANEL_X, SCB_PREVIEW_PANEL_Y, button_count=1)
    draw_ae2_help_button(canvas, themed_sprites, SCB_PREVIEW_PANEL_X, SCB_PREVIEW_PANEL_Y)

    for button_x, button_y in get_scb_button_positions():
        draw_ae2_button(
            canvas,
            themed_sprites.button_normal,
            ae2_assets.button_border,
            SCB_PREVIEW_PANEL_X + button_x,
            SCB_PREVIEW_PANEL_Y + button_y,
            SCB_BUTTON_WIDTH,
            SCB_BUTTON_HEIGHT,
        )

    draw_status_lamp(
        canvas,
        SCB_PREVIEW_PANEL_X + SCB_PANEL_WIDTH - 84,
        SCB_PREVIEW_PANEL_Y + 8,
        theme.color("status", "online"),
        theme.color("status", "lamp_border"),
    )
    return canvas


def build_digital_asset_vault_preview(theme: Theme) -> PixelCanvas:
    canvas = PixelCanvas(DAV_PREVIEW_WIDTH, DAV_PREVIEW_HEIGHT)
    texture = build_digital_asset_vault_texture(theme).to_image()
    cropped = texture.crop(0, 0, DAV_PANEL_WIDTH, DAV_PANEL_HEIGHT)
    canvas.blit_image(cropped, DAV_PREVIEW_PANEL_X, DAV_PREVIEW_PANEL_Y)
    return canvas


def draw_ae2_vertical_toolbar(
    canvas: PixelCanvas,
    image: PngImage,
    border: NineSliceBorder,
    panel_x: int,
    panel_y: int,
    *,
    button_count: int,
) -> None:
    if button_count <= 0:
        return

    button_width = 16
    button_height = 16
    bound_x = AE2_TOOLBAR_LEFT - button_width - AE2_TOOLBAR_MARGIN * 2
    bound_y = AE2_TOOLBAR_TOP
    current_y = AE2_TOOLBAR_TOP + AE2_TOOLBAR_MARGIN + button_count * (button_height + AE2_TOOLBAR_VERTICAL_SPACING)
    bound_width = button_width + AE2_TOOLBAR_MARGIN * 2
    bound_height = current_y - bound_y
    draw_x = panel_x + bound_x - 2
    draw_y = panel_y + bound_y - 1
    draw_width = bound_width + 1
    draw_height = bound_height + 4
    canvas.draw_nine_slice(
        image,
        border,
        draw_x,
        draw_y,
        draw_width,
        draw_height,
    )


def draw_ae2_help_button(canvas: PixelCanvas, sprites: ThemedAe2Sprites, panel_x: int, panel_y: int) -> None:
    button_x = panel_x + AE2_TOOLBAR_LEFT - AE2_TOOLBAR_MARGIN - 16
    button_y = panel_y + AE2_TOOLBAR_TOP + AE2_TOOLBAR_MARGIN
    canvas.blit_image(sprites.toolbar_button_background, button_x - 1, button_y)
    canvas.blit_image(sprites.help_icon, button_x, button_y + 1)


def draw_ae2_button(
    canvas: PixelCanvas,
    image: PngImage,
    border: NineSliceBorder,
    x: int,
    y: int,
    width: int,
    height: int,
) -> None:
    canvas.draw_nine_slice(image, border, x, y, width, height)


def build_button_preview_sheet(theme: Theme, ae2_assets: Ae2ReferenceAssets) -> PixelCanvas:
    themed_sprites = build_themed_ae2_sprites(theme, ae2_assets)
    canvas = PixelCanvas(140, 90)
    draw_ae2_button(canvas, themed_sprites.button_normal, ae2_assets.button_border, 16, 12, 100, 18)
    draw_ae2_button(canvas, themed_sprites.button_hover, ae2_assets.button_border, 16, 36, 100, 18)
    draw_ae2_button(canvas, themed_sprites.button_disabled, ae2_assets.button_border, 16, 60, 100, 18)
    return canvas


def build_guide_toolbar_preview(theme: Theme, ae2_assets: Ae2ReferenceAssets) -> PixelCanvas:
    themed_sprites = build_themed_ae2_sprites(theme, ae2_assets)
    canvas = PixelCanvas(44, 44)
    draw_ae2_vertical_toolbar(
        canvas,
        themed_sprites.vertical_toolbar_background,
        ae2_assets.vertical_toolbar_background_border,
        23,
        4,
        button_count=1,
    )
    draw_ae2_help_button(canvas, themed_sprites, 23, 4)
    return canvas


def build_status_lamps_preview(theme: Theme) -> PixelCanvas:
    canvas = PixelCanvas(54, 16)
    lamp_border = theme.color("status", "lamp_border")
    draw_status_lamp(canvas, 4, 4, theme.color("status", "offline"), lamp_border)
    draw_status_lamp(canvas, 22, 4, theme.color("status", "warning"), lamp_border)
    draw_status_lamp(canvas, 40, 4, theme.color("status", "online"), lamp_border)
    return canvas


def toolbar_button_chrome_palette(theme: Theme) -> dict[tuple[int, int, int, int], tuple[int, int, int, int]]:
    """AE2 TOOLBAR_BUTTON_BACKGROUND uses action_button chrome, not panel fill."""
    button_outline = theme.color("action_button", "outline_dark")
    button_light = theme.color("action_button", "edge_light")
    button_face = theme.color("action_button", "face")
    button_shadow = theme.color("action_button", "shadow")
    hover_face = mix(button_face, theme.color("text", "primary"), 0.18)
    hover_light = mix(button_light, theme.color("text", "primary"), 0.22)
    hover_shadow = mix(button_shadow, theme.color("text", "primary"), 0.12)
    return {
        (0x41, 0x3F, 0x54, 0xFF): button_outline,
        (0xAD, 0xB0, 0xC4, 0xFF): button_light,
        (0x9A, 0x9F, 0xB4, 0xFF): button_face,
        (0x69, 0x6D, 0x88, 0xFF): button_shadow,
        (0x9C, 0xD3, 0xFF, 0xFF): hover_face,
        (0xDA, 0xFF, 0xFF, 0xFF): hover_light,
        (0x70, 0x8C, 0xBA, 0xFF): hover_shadow,
    }


def help_glyph_palette(theme: Theme) -> dict[tuple[int, int, int, int], tuple[int, int, int, int]]:
    """HELP is only the glyph; chrome comes from TOOLBAR_BUTTON_BACKGROUND underneath."""
    return {
        (0xF2, 0xF2, 0xF2, 0xFF): theme.color("text", "primary"),
        (0x4D, 0x4D, 0x67, 0xFF): theme.color("text", "muted"),
        (0x00, 0x00, 0x00, 0x00): (0, 0, 0, 0),
    }


def chrome_palette_map(theme: Theme) -> dict[tuple[int, int, int, int], tuple[int, int, int, int]]:
    palette = {
        (0xF2, 0xF2, 0xF2, 0xFF): theme.color("background", "edge_light"),
        (0xCB, 0xCC, 0xD4, 0xFF): theme.color("background", "fill"),
        (0x87, 0x8F, 0xA5, 0xFF): theme.color("background", "shadow"),
        (0x00, 0x00, 0x00, 0x00): (0, 0, 0, 0),
    }
    palette.update(toolbar_button_chrome_palette(theme))
    return palette


def blit_png_onto(base: PngImage, overlay: PngImage, x: int, y: int) -> PngImage:
    pixels = bytearray(base.pixels)
    for py in range(overlay.height):
        dst_y = y + py
        if dst_y < 0 or dst_y >= base.height:
            continue
        for px in range(overlay.width):
            dst_x = x + px
            if dst_x < 0 or dst_x >= base.width:
                continue
            src_i = (py * overlay.width + px) * 4
            if overlay.pixels[src_i + 3] == 0:
                continue
            dst_i = (dst_y * base.width + dst_x) * 4
            pixels[dst_i:dst_i + 4] = overlay.pixels[src_i:src_i + 4]
    return PngImage(base.width, base.height, bytes(pixels))


def build_themed_states_atlas(theme: Theme, ae2_assets: Ae2ReferenceAssets) -> PngImage:
    atlas = recolor_image_palette(ae2_assets.states, chrome_palette_map(theme))
    icon_x, icon_y, icon_width, icon_height = HELP_ICON_RECT
    help_icon = ae2_assets.states.crop(icon_x, icon_y, icon_width, icon_height)
    help_icon = recolor_image_palette(help_icon, help_glyph_palette(theme))
    return blit_png_onto(atlas, help_icon, icon_x, icon_y)


def build_themed_background(theme: Theme, ae2_assets: Ae2ReferenceAssets) -> PngImage:
    return recolor_image_palette(ae2_assets.background, chrome_palette_map(theme))


AE2_CONTROLLER_TEXTURES = (
    "controller.png",
    "controller_powered.png",
    "controller_column.png",
    "controller_column_powered.png",
    "controller_inside_a.png",
    "controller_inside_a_powered.png",
    "controller_inside_b.png",
    "controller_inside_b_powered.png",
    "controller_lights.png",
    "controller_column_lights.png",
    "controller_conflict.png",
    "controller_column_conflict.png",
)

CONTROLLER_BODY_CORE_COLORS: dict[tuple[int, int, int, int], tuple[str, str]] = {
    (0x1A, 0x1C, 0x1C, 0xFF): ("background", "outline_dark"),
    (0x1F, 0x1F, 0x1F, 0xFF): ("background", "outline_dark"),
    (0x1F, 0x22, 0x21, 0xFF): ("background", "outline_dark"),
    (0x26, 0x28, 0x26, 0xFF): ("background", "shadow"),
    (0x2D, 0x2F, 0x2D, 0xFF): ("background", "shadow"),
    (0x41, 0x3F, 0x54, 0xFF): ("action_button", "outline_dark"),
    (0x4D, 0x4D, 0x67, 0xFF): ("background", "shadow"),
    (0x78, 0x7E, 0x97, 0xFF): ("storage_slot", "shadow"),
    (0x87, 0x8F, 0xA5, 0xFF): ("background", "shadow"),
    (0x9A, 0x9F, 0xB4, 0xFF): ("section", "fill"),
    (0xCB, 0xCC, 0xD4, 0xFF): ("background", "fill"),
    (0xE8, 0xE8, 0xEA, 0xFF): ("section", "edge_light"),
    (0xF2, 0xF2, 0xF2, 0xFF): ("background", "edge_light"),
}

CONTROLLER_CONFLICT_CORE_COLORS: dict[tuple[int, int, int, int], tuple[str, str]] = {
    (0x84, 0x12, 0x2A, 0xFF): ("background", "outline_dark"),
    (0xAA, 0x21, 0x2B, 0xFF): ("status", "offline"),
}


def rgb_hue(red: int, green: int, blue: int) -> float | None:
    maximum = max(red, green, blue)
    minimum = min(red, green, blue)
    if maximum == minimum:
        return None
    delta = maximum - minimum
    if maximum == red:
        hue = (green - blue) / delta
    elif maximum == green:
        hue = (blue - red) / delta + 2.0
    else:
        hue = (red - green) / delta + 4.0
    return (hue * 60.0) % 360.0


def pixel_luminance(red: int, green: int, blue: int) -> float:
    return (red * 299 + green * 587 + blue * 114) / 1000.0


def controller_body_palette_map(theme: Theme) -> dict[tuple[int, int, int, int], tuple[int, int, int, int]]:
    palette = {
        source: theme.color(group, key)
        for source, (group, key) in CONTROLLER_BODY_CORE_COLORS.items()
    }
    powered_glow = mix(theme.color("status", "online"), theme.color("background", "shadow"), 0.55)
    palette[(0x79, 0x7A, 0x7E, 0x28)] = (powered_glow[0], powered_glow[1], powered_glow[2], 0x28)
    palette[(0x00, 0x00, 0x00, 0x00)] = (0, 0, 0, 0)
    return palette


def controller_conflict_palette_map(theme: Theme) -> dict[tuple[int, int, int, int], tuple[int, int, int, int]]:
    offline = theme.color("status", "offline")
    outline = theme.color("background", "outline_dark")
    return {
        (0x84, 0x12, 0x2A, 0xFF): mix(outline, offline, 0.35),
        (0xAA, 0x21, 0x2B, 0xFF): offline,
        (0x00, 0x00, 0x00, 0x00): (0, 0, 0, 0),
    }


def classify_controller_light(red: int, green: int, blue: int) -> str:
    hue = rgb_hue(red, green, blue)
    if hue is None:
        return "online"
    if hue >= 330.0 or hue < 20.0:
        return "offline"
    if hue < 75.0:
        return "warning"
    if hue < 205.0:
        return "online"
    return "accent"


def themed_controller_light_color(
    red: int,
    green: int,
    blue: int,
    alpha: int,
    theme: Theme,
) -> tuple[int, int, int, int]:
    category = classify_controller_light(red, green, blue)
    luminance = pixel_luminance(red, green, blue)
    ratio = max(0.0, min(1.0, luminance / 240.0))

    if category == "offline":
        dark = theme.color("status", "offline")
        bright = mix(dark, (255, 255, 255, 255), 0.28)
    elif category == "warning":
        dark = mix(theme.color("status", "warning"), theme.color("background", "outline_dark"), 0.25)
        bright = mix(theme.color("status", "warning"), (255, 255, 255, 255), 0.35)
    elif category == "online":
        dark = mix(theme.color("status", "online"), theme.color("background", "shadow"), 0.35)
        bright = mix(theme.color("status", "online"), theme.color("help_button", "frame_light"), 0.42)
    else:
        dark = theme.color("section", "edge_light")
        bright = theme.color("help_button", "frame_light")

    result = mix(dark, bright, ratio)
    return (result[0], result[1], result[2], alpha)


def recolor_controller_lights(image: PngImage, theme: Theme) -> PngImage:
    out = bytearray(image.pixels)
    for index in range(0, len(out), 4):
        red, green, blue, alpha = out[index:index + 4]
        if alpha == 0:
            continue
        replacement = themed_controller_light_color(red, green, blue, alpha, theme)
        out[index:index + 4] = bytes(replacement)
    return PngImage(image.width, image.height, bytes(out))


def read_ae2_controller_texture(name: str) -> PngImage:
    archive_path = f"assets/ae2/textures/block/{name}"
    with zipfile.ZipFile(find_ae2_runtime_jar(), "r") as archive:
        return read_png_from_bytes(archive.read(archive_path))


def read_png_from_bytes(data: bytes) -> PngImage:
    if data[:8] != b"\x89PNG\r\n\x1a\n":
        raise ValueError("Payload is not a PNG file.")

    offset = 8
    width = height = bit_depth = color_type = interlace = None
    idat_chunks: list[bytes] = []

    while offset < len(data):
        chunk_length = struct.unpack(">I", data[offset:offset + 4])[0]
        chunk_type = data[offset + 4:offset + 8]
        chunk_data = data[offset + 8:offset + 8 + chunk_length]
        offset += 12 + chunk_length

        if chunk_type == b"IHDR":
            width, height, bit_depth, color_type, _, _, interlace = struct.unpack(">IIBBBBB", chunk_data)
        elif chunk_type == b"IDAT":
            idat_chunks.append(chunk_data)
        elif chunk_type == b"IEND":
            break

    if width is None or height is None or bit_depth is None or color_type is None or interlace is None:
        raise ValueError("PNG is missing required chunks.")
    if bit_depth != 8 or interlace != 0 or color_type not in (6, 2):
        raise ValueError(
            f"Unsupported PNG format: bit_depth={bit_depth}, color_type={color_type}, interlace={interlace}"
        )

    channels = 4 if color_type == 6 else 3
    stride = width * channels
    raw = zlib.decompress(b"".join(idat_chunks))
    pos = 0
    previous = bytearray(stride)
    rgba = bytearray(width * height * 4)

    for row in range(height):
        filter_type = raw[pos]
        pos += 1
        filtered = bytearray(raw[pos:pos + stride])
        pos += stride
        scanline = unfilter_scanline(filter_type, filtered, previous, channels)
        previous = bytearray(scanline)
        if color_type == 6:
            dst_start = row * width * 4
            rgba[dst_start:dst_start + stride] = scanline
        else:
            dst_start = row * width * 4
            for col in range(width):
                src = col * 3
                dst = dst_start + col * 4
                rgba[dst:dst + 4] = bytes((scanline[src], scanline[src + 1], scanline[src + 2], 255))

    return PngImage(width, height, bytes(rgba))


def recolor_controller_texture(name: str, image: PngImage, theme: Theme) -> PngImage:
    if name.endswith("_lights.png"):
        return recolor_controller_lights(image, theme)
    if name.endswith("_conflict.png"):
        return recolor_image_palette(image, controller_conflict_palette_map(theme))
    return recolor_image_palette(image, controller_body_palette_map(theme))


def build_themed_controller_textures(theme: Theme) -> dict[str, PngImage]:
    return {
        name: recolor_controller_texture(name, read_ae2_controller_texture(name), theme)
        for name in AE2_CONTROLLER_TEXTURES
    }


def controller_core_color_manifest(theme: Theme) -> dict:
    body = {
        "#%02X%02X%02X%02X" % source: {
            "role": f"{group}.{key}",
            "target": "#%02X%02X%02X%02X" % theme.color(group, key),
        }
        for source, (group, key) in CONTROLLER_BODY_CORE_COLORS.items()
    }
    conflict_palette = controller_conflict_palette_map(theme)
    conflict = {
        "#%02X%02X%02X%02X" % source: {
            "role": f"{group}.{key}",
            "target": "#%02X%02X%02X%02X" % conflict_palette[source],
        }
        for source, (group, key) in CONTROLLER_CONFLICT_CORE_COLORS.items()
    }
    return {
        "body_core_colors": body,
        "conflict_core_colors": conflict,
        "lights_mapping": "hue bucket -> status.online / status.warning / status.offline / section.edge_light",
    }


def build_smart_bus_texture(theme: Theme) -> PixelCanvas:
    width = 400
    height = 280
    canvas = PixelCanvas(512, 512)
    bg_outline = theme.color("background", "outline_dark")
    bg_light = theme.color("background", "edge_light")
    bg_fill = theme.color("background", "fill")
    bg_shadow = theme.color("background", "shadow")
    section_outline = theme.color("section", "outline_dark")
    section_light = theme.color("section", "edge_light")
    section_fill = theme.color("section", "fill")
    section_shadow = theme.color("section", "shadow")

    draw_panel_box(canvas, 0, 0, width, height, bg_outline, bg_light, bg_fill, bg_shadow)
    draw_panel_box(canvas, 10, 17, 380, 35, section_outline, section_light, section_fill, section_shadow)
    draw_panel_box(canvas, 10, 58, 380, 178, section_outline, section_light, section_fill, section_shadow)
    return canvas


def build_palette_swatches(theme: Theme) -> PixelCanvas:
    cell = 22
    cols = 4
    rows = (len(COLOR_SWATCH_ORDER) + cols - 1) // cols
    canvas = PixelCanvas(cols * cell + 5, rows * cell + 5)
    for index, (group, key) in enumerate(COLOR_SWATCH_ORDER):
        col = index % cols
        row = index // cols
        x = 3 + col * cell
        y = 3 + row * cell
        canvas.draw_bevel_box(
            x,
            y,
            18,
            18,
            theme.color(group, key),
            theme.color("background", "edge_light"),
            theme.color("background", "outline_dark"),
            depth=1,
        )
    return canvas


def write_manifest(path: Path, payload: dict) -> None:
    path.write_text(json.dumps(payload, indent=2, ensure_ascii=False), encoding="utf-8")

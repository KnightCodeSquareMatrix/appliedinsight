from __future__ import annotations
import re, sys, json
from collections import defaultdict
from pathlib import Path

SCRIPT_DIR = Path(__file__).resolve().parent
PROJECT_ROOT = SCRIPT_DIR.parent

def hex_to_rgb(h):
    h = h.lstrip('#')
    return (int(h[0:2],16), int(h[2:4],16), int(h[4:6],16))

def luminance(r,g,b):
    return 0.299*r + 0.587*g + 0.114*b

def parse_theme_colors(theme_entry):
    colors = {}
    for group, entries in theme_entry['colors'].items():
        colors[group] = {}
        for key, val in entries.items():
            h = val.lstrip('#')
            colors[group][key] = (int(h[0:2],16), int(h[2:4],16), int(h[4:6],16), int(h[6:8],16) if len(h)>=8 else 255)
    return colors

def load_themes(json_path):
    with open(json_path, 'r', encoding='utf-8') as f:
        data = json.load(f)
    return data['themes']

class BackgroundPixelMap:
    def __init__(self, path):
        self.path = Path(path)
        self.width = 0
        self.height = 0
        self.pixels = {}
        self.unique_colors = {}
        self.parse()
        self.analyze()

    def parse(self):
        max_x = max_y = 0
        with open(self.path, 'r', encoding='utf-8') as f:
            for line in f:
                line = line.strip()
                if line.startswith('图片尺寸'):
                    continue
                m = re.match(r'(\d+),(\d+):\s*(#[0-9A-Fa-f]{6})', line)
                if m:
                    x, y, c = int(m.group(1)), int(m.group(2)), m.group(3).upper()
                    self.pixels[(x,y)] = c
                    max_x = max(max_x, x)
                    max_y = max(max_y, y)
        self.width = max_x + 1
        self.height = max_y + 1

    def analyze(self):
        info = {}
        for (x,y), c in self.pixels.items():
            if c not in info:
                r,g,b = hex_to_rgb(c)
                info[c] = {'count':0, 'lumin':luminance(r,g,b), 'samples':[]}
            info[c]['count'] += 1
            if len(info[c]['samples']) < 3:
                info[c]['samples'].append((x,y))
        for x in range(self.width):
            for y in range(self.height):
                c = self.pixels.get((x,y))
                if not c: continue
                for dx,dy in [(-1,0),(1,0),(0,-1),(0,1)]:
                    nc = self.pixels.get((x+dx, y+dy))
                    if nc and nc != c:
                        if 'adjacents' not in info[c]:
                            info[c]['adjacents'] = set()
                        info[c]['adjacents'].add(nc)
        self.unique_colors = info

    def classify_by_luminance(self):
        sorted_colors = sorted(self.unique_colors.items(), key=lambda x: x[1]['lumin'])
        role_map = {}
        if len(sorted_colors) <= 4:
            roles = ['outline_dark', 'shadow', 'fill', 'edge_light']
            for i, (c,_) in enumerate(sorted_colors):
                role_map[c] = roles[min(i,3)]
            return role_map
        gaps = []
        for i in range(1, len(sorted_colors)):
            gap = sorted_colors[i][1]['lumin'] - sorted_colors[i-1][1]['lumin']
            gaps.append((gap, i))
        gaps.sort(reverse=True)
        splits = sorted([g[1] for g in gaps[:3]])
        roles = ['outline_dark', 'shadow', 'fill', 'edge_light']
        prev = 0
        for i, role in enumerate(roles):
            end = splits[i] if i < len(splits) else len(sorted_colors)
            for c, _ in sorted_colors[prev:end]:
                role_map[c] = role
            prev = end
        return role_map

    def count_by_luminance_bin(self):
        bins = {}
        for c, info in self.unique_colors.items():
            l = int(info['lumin'] // 20) * 20
            bins[l] = bins.get(l, 0) + info['count']
        return bins

    def generate_texture(self, theme_colors, output_path):
        role_targets = {k: theme_colors['background'][k] for k in ['outline_dark','shadow','fill','edge_light']}
        role_map = self.classify_by_luminance()
        color_map = {}
        role_colors = defaultdict(list)
        for c, info in self.unique_colors.items():
            role = role_map.get(c, 'fill')
            role_colors[role].append((c, info['lumin']))
        for role, items in role_colors.items():
            target = role_targets[role]
            items.sort(key=lambda x: x[1])
            if len(items) <= 1:
                for c, _ in items:
                    color_map[c] = target
                continue
            min_l = items[0][1]
            max_l = items[-1][1]
            l_range = max_l - min_l if max_l > min_l else 1.0
            for c, lum in items:
                ratio = (lum - min_l) / l_range
                adjusted = tuple(max(0, min(255, int(target[i] * (0.82 + 0.36 * ratio)))) for i in range(3)) + (target[3],)
                color_map[c] = adjusted
        pixels_out = bytearray(self.width * self.height * 4)
        for (x,y), old in self.pixels.items():
            rgba = color_map.get(old, role_targets.get(role_map.get(old, 'fill'), (24,28,34,255)))
            idx = (y * self.width + x) * 4
            pixels_out[idx:idx+4] = bytes(rgba)
        output_path = Path(output_path)
        output_path.parent.mkdir(parents=True, exist_ok=True)
        save_rgba_png(output_path, self.width, self.height, bytes(pixels_out))
        return output_path

def save_rgba_png(path, width, height, pixels):
    import struct, zlib
    raw = bytearray()
    stride = width * 4
    for y in range(height):
        raw.append(0)
        raw.extend(pixels[y*stride:(y+1)*stride])
    def chunk(tag, payload):
        return struct.pack('>I',len(payload)) + tag + payload + struct.pack('>I', zlib.crc32(tag+payload) & 0xFFFFFFFF)
    ihdr = struct.pack('>IIBBBBB', width, height, 8, 6, 0, 0, 0)
    idat = zlib.compress(bytes(raw), level=9)
    png = b'\x89PNG\r\n\x1a\n' + chunk(b'IHDR', ihdr) + chunk(b'IDAT', idat) + chunk(b'IEND', b'')
    path.write_bytes(png)

def generate_background_for_theme(pixel_map_path, template_path, theme_name, output_dir, resource_root=None):
    themes = load_themes(template_path)
    if theme_name not in themes:
        raise ValueError(f\"Theme '{theme_name}' not found\")
    theme = themes[theme_name]
    theme_colors = parse_theme_colors({'colors': theme['colors']})
    pixel_map = BackgroundPixelMap(pixel_map_path)
    if resource_root is None:
        resource_root = Path('assets') / 'appliedstoragesorter' / 'textures' / 'gui'
    output_path = Path(output_dir) / resource_root / 'background.png'
    pixel_map.generate_texture(theme_colors, output_path)
    print(f\"[ok] generated background for '{theme_name}' -> {output_path}\")
    return output_path

if __name__ == '__main__':
    if len(sys.argv) < 4:
        print('Usage: python background_texture.py <pixel_map.txt> <template.json> <theme_name> [output_dir]')
        sys.exit(1)
    pixel_map = sys.argv[1]
    template = sys.argv[2]
    theme_name = sys.argv[3]
    output_dir = sys.argv[4] if len(sys.argv) > 4 else 'generated_gui_textures'
    generate_background_for_theme(pixel_map, template, theme_name, output_dir)
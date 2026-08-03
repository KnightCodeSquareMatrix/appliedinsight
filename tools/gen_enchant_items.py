#!/usr/bin/env python3
"""
生成海量随机附魔物品的 Minecraft 1.21.1 give 命令（箱子填充模式）。

用法：
    python3 tools/gen_enchant_items.py --pos X Y Z [选项]

选项：
    --pos X Y Z         目标箱子坐标（必选）
    --slot-count N      箱子槽位数（默认 27，双箱子为 54）
    --count N           每种物品生成 N 份（默认 1）
    --output FILE       输出到文件（默认输出到 stdout）
    --type GROUP        只生成指定组：sword/pickaxe/armor/bow/crossbow/trident/tool/book/all（默认 all）

示例：
    python3 tools/gen_enchant_items.py --pos 100 64 100
    python3 tools/gen_enchant_items.py --pos 100 64 100 --slot-count 54 --count 64
    python3 tools/gen_enchant_items.py --pos 100 64 100 --type sword --output /tmp/swords.txt
"""

import argparse
import random
import sys

# ============================================================
# 附魔池定义
# ============================================================

# 武器附魔
SWORD_ENCHANTS = {
    "minecraft:sharpness": (1, 5),
    "minecraft:smite": (1, 5),
    "minecraft:bane_of_arthropods": (1, 5),
    "minecraft:unbreaking": (1, 3),
    "minecraft:looting": (1, 3),
    "minecraft:fire_aspect": (1, 2),
    "minecraft:knockback": (1, 2),
    "minecraft:sweeping_edge": (1, 3),
    "minecraft:mending": (1, 1),
}

# 工具附魔
TOOL_ENCHANTS = {
    "minecraft:efficiency": (1, 5),
    "minecraft:unbreaking": (1, 3),
    "minecraft:fortune": (1, 3),
    "minecraft:silk_touch": (1, 1),
    "minecraft:mending": (1, 1),
}

# 盔甲附魔
ARMOR_ENCHANTS = {
    "minecraft:protection": (1, 4),
    "minecraft:unbreaking": (1, 3),
    "minecraft:mending": (1, 1),
    "minecraft:thorns": (1, 3),
    "minecraft:respiration": (1, 3),
    "minecraft:aqua_affinity": (1, 1),
    "minecraft:feather_falling": (1, 4),
    "minecraft:depth_strider": (1, 3),
    "minecraft:frost_walker": (1, 2),
    "minecraft:soul_speed": (1, 3),
    "minecraft:swift_sneak": (1, 3),
}

# 弓附魔
BOW_ENCHANTS = {
    "minecraft:power": (1, 5),
    "minecraft:unbreaking": (1, 3),
    "minecraft:flame": (1, 1),
    "minecraft:infinity": (1, 1),
    "minecraft:punch": (1, 2),
    "minecraft:mending": (1, 1),
}

# 弩附魔
CROSSBOW_ENCHANTS = {
    "minecraft:quick_charge": (1, 3),
    "minecraft:unbreaking": (1, 3),
    "minecraft:multishot": (1, 1),
    "minecraft:piercing": (1, 4),
    "minecraft:mending": (1, 1),
}

# 三叉戟附魔
TRIDENT_ENCHANTS = {
    "minecraft:impaling": (1, 5),
    "minecraft:unbreaking": (1, 3),
    "minecraft:mending": (1, 1),
    "minecraft:loyalty": (1, 3),
    "minecraft:riptide": (1, 3),
    "minecraft:channeling": (1, 1),
}

# 互斥附魔组（不能同时出现）
MUTUALLY_EXCLUSIVE = [
    {"minecraft:sharpness", "minecraft:smite", "minecraft:bane_of_arthropods"},
    {"minecraft:silk_touch", "minecraft:fortune"},
    {"minecraft:infinity", "minecraft:mending"},
    {"minecraft:loyalty", "minecraft:riptide"},
    {"minecraft:channeling", "minecraft:riptide"},
    {"minecraft:multishot", "minecraft:piercing"},
]


def remove_conflicts(enchants: dict, selected: list[str]) -> dict:
    """从 enchants 中移除与 selected 冲突的附魔"""
    result = dict(enchants)
    for group in MUTUALLY_EXCLUSIVE:
        selected_in_group = [e for e in selected if e in group]
        if len(selected_in_group) > 0:
            for conflict in group:
                result.pop(conflict, None)
    return result


def pick_enchantments(pool: dict, min_count: int = 2, max_count: int = 6) -> dict:
    """从附魔池中随机选取一组附魔"""
    count = random.randint(min_count, min(max_count, len(pool)))
    selected = []
    available = dict(pool)
    for _ in range(count):
        if not available:
            break
        ench = random.choice(list(available.keys()))
        min_lvl, max_lvl = available[ench]
        level = random.randint(min_lvl, max_lvl)
        selected.append((ench, level))
        # 移除已选和冲突的
        available = remove_conflicts(available, [e for e, _ in selected])
    return dict(selected)


def format_enchantments(enchants: dict) -> str:
    """格式化附魔为组件语法"""
    if not enchants:
        return ""
    levels = ",".join(f'"{k}":{v}' for k, v in sorted(enchants.items()))
    return f"minecraft:enchantments={{levels:{{{levels}}}}}"


def format_damage(max_damage: int) -> str:
    """生成随机耐久值"""
    dmg = random.randint(0, max_damage)
    return f"minecraft:damage={dmg}"


# ============================================================
# 物品模板定义
# ============================================================

# (物品ID, 附魔池, 最大耐久, 最少附魔数, 最多附魔数)
ITEM_TEMPLATES = [
    # ---- 剑 ----
    ("minecraft:diamond_sword", SWORD_ENCHANTS, 1561, 2, 5),
    ("minecraft:netherite_sword", SWORD_ENCHANTS, 2031, 2, 5),
    ("minecraft:iron_sword", SWORD_ENCHANTS, 250, 1, 3),
    # ---- 镐 ----
    ("minecraft:diamond_pickaxe", TOOL_ENCHANTS, 1561, 2, 4),
    ("minecraft:netherite_pickaxe", TOOL_ENCHANTS, 2031, 2, 4),
    ("minecraft:iron_pickaxe", TOOL_ENCHANTS, 250, 1, 3),
    ("minecraft:stone_pickaxe", TOOL_ENCHANTS, 131, 1, 2),
    ("minecraft:golden_pickaxe", TOOL_ENCHANTS, 32, 2, 4),
    # ---- 其他工具 ----
    ("minecraft:diamond_axe", TOOL_ENCHANTS, 1561, 2, 4),
    ("minecraft:diamond_shovel", TOOL_ENCHANTS, 1561, 2, 3),
    ("minecraft:diamond_hoe", TOOL_ENCHANTS, 1561, 2, 3),
    ("minecraft:netherite_axe", TOOL_ENCHANTS, 2031, 2, 4),
    ("minecraft:netherite_shovel", TOOL_ENCHANTS, 2031, 2, 3),
    ("minecraft:netherite_hoe", TOOL_ENCHANTS, 2031, 2, 3),
    ("minecraft:iron_axe", TOOL_ENCHANTS, 250, 1, 3),
    ("minecraft:iron_shovel", TOOL_ENCHANTS, 250, 1, 2),
    ("minecraft:iron_hoe", TOOL_ENCHANTS, 250, 1, 2),
    # ---- 头盔 ----
    ("minecraft:diamond_helmet", ARMOR_ENCHANTS, 363, 2, 5),
    ("minecraft:netherite_helmet", ARMOR_ENCHANTS, 407, 2, 5),
    ("minecraft:iron_helmet", ARMOR_ENCHANTS, 165, 1, 3),
    # ---- 胸甲 ----
    ("minecraft:diamond_chestplate", ARMOR_ENCHANTS, 528, 2, 4),
    ("minecraft:netherite_chestplate", ARMOR_ENCHANTS, 592, 2, 4),
    ("minecraft:iron_chestplate", ARMOR_ENCHANTS, 240, 1, 3),
    # ---- 护腿 ----
    ("minecraft:diamond_leggings", ARMOR_ENCHANTS, 495, 2, 3),
    ("minecraft:netherite_leggings", ARMOR_ENCHANTS, 555, 2, 3),
    ("minecraft:iron_leggings", ARMOR_ENCHANTS, 225, 1, 2),
    # ---- 靴子 ----
    ("minecraft:diamond_boots", ARMOR_ENCHANTS, 429, 2, 5),
    ("minecraft:netherite_boots", ARMOR_ENCHANTS, 481, 2, 5),
    ("minecraft:iron_boots", ARMOR_ENCHANTS, 195, 1, 3),
    # ---- 远程武器 ----
    ("minecraft:bow", BOW_ENCHANTS, 384, 2, 5),
    ("minecraft:crossbow", CROSSBOW_ENCHANTS, 465, 2, 4),
    # ---- 三叉戟 ----
    ("minecraft:trident", TRIDENT_ENCHANTS, 250, 2, 5),
    # ---- 附魔书 ----
    ("minecraft:enchanted_book", {**SWORD_ENCHANTS, **TOOL_ENCHANTS, **ARMOR_ENCHANTS, **BOW_ENCHANTS, **CROSSBOW_ENCHANTS, **TRIDENT_ENCHANTS}, 0, 3, 6),
]

# 按类型分组
TYPE_GROUPS = {
    "sword": [t for t in ITEM_TEMPLATES if "sword" in t[0]],
    "pickaxe": [t for t in ITEM_TEMPLATES if "pickaxe" in t[0]],
    "armor": [t for t in ITEM_TEMPLATES if any(a in t[0] for a in ["helmet", "chestplate", "leggings", "boots"])],
    "bow": [t for t in ITEM_TEMPLATES if t[0] == "minecraft:bow"],
    "crossbow": [t for t in ITEM_TEMPLATES if t[0] == "minecraft:crossbow"],
    "trident": [t for t in ITEM_TEMPLATES if t[0] == "minecraft:trident"],
    "tool": [t for t in ITEM_TEMPLATES if any(a in t[0] for a in ["axe", "shovel", "hoe", "pickaxe"])],
    "book": [t for t in ITEM_TEMPLATES if t[0] == "minecraft:enchanted_book"],
}


def generate_commands(count: int = 1, pos: str = "~ ~ ~1", item_filter: str = "all") -> list[str]:
    """生成 give 命令列表"""
    templates = ITEM_TEMPLATES if item_filter == "all" else TYPE_GROUPS.get(item_filter, ITEM_TEMPLATES)
    commands = []

    for item_id, ench_pool, max_damage, min_ench, max_ench in templates:
        for _ in range(count):
            enchants = pick_enchantments(ench_pool, min_ench, max_ench)
            components = []

            ench_str = format_enchantments(enchants)
            if ench_str:
                components.append(ench_str)

            # 非附魔书才加耐久
            if "enchanted_book" not in item_id and max_damage > 0:
                components.append(format_damage(max_damage))

            if components:
                cmd = f"give @p {item_id}[{','.join(components)}]"
            else:
                cmd = f"give @p {item_id}"

            commands.append(cmd)

    return commands


def print_progress(message: str):
    """打印进度信息到 stderr"""
    print(message, file=sys.stderr)


def main():
    parser = argparse.ArgumentParser(description="生成随机附魔物品的 give 命令（箱子填充模式）")
    parser.add_argument("--pos", type=str, required=True,
                        help="目标箱子坐标（必选，格式：X Y Z）")
    parser.add_argument("--slot-count", type=int, default=27,
                        help="箱子槽位数（默认 27，双箱子为 54）")
    parser.add_argument("--count", type=int, default=1, help="每种物品生成份数（默认 1）")
    parser.add_argument("--output", type=str, help="输出文件路径（默认输出到 stdout）")
    parser.add_argument("--type", type=str, default="all",
                        choices=["all", "sword", "pickaxe", "armor", "bow", "crossbow", "trident", "tool", "book"],
                        help="物品类型（默认 all）")
    args = parser.parse_args()

    # 验证 pos 格式
    pos_parts = args.pos.split()
    if len(pos_parts) != 3:
        print("错误：--pos 需要三个坐标值，例如 --pos 100 64 100", file=sys.stderr)
        sys.exit(1)

    # 生成所有命令
    all_commands = generate_commands(args.count, args.pos, args.type)
    total_items = len(all_commands)

    # 计算箱子容量
    slot_count = args.slot_count
    # 假设玩家会先执行 data get block 命令查看当前占用
    # 我们无法知道实际占用，所以让玩家先执行检测命令
    # 这里假设已占用槽位为 0（空箱子），由玩家自行调整
    occupied_slots = 0  # 玩家需要先执行检测命令获取实际值
    available_slots = slot_count - occupied_slots

    # 计算能塞入的物品数量
    items_to_fill = min(total_items, available_slots)
    remaining_items = total_items - items_to_fill
    can_fit_all = items_to_fill >= total_items

    # 取前 items_to_fill 条命令
    output_commands = all_commands[:items_to_fill]

    # ============================================================
    # 控制台输出（到 stderr）
    # ============================================================
    print_progress("")
    print_progress("=" * 40)
    print_progress("  附魔物品生成器 - 箱子填充模式")
    print_progress("=" * 40)
    print_progress(f"目标箱子位置: {args.pos}")
    print_progress(f"箱子槽位总数: {slot_count}")
    print_progress(f"已占用槽位: {occupied_slots}")
    print_progress(f"剩余空位: {available_slots}")
    print_progress(f"待生成物品类型: {args.type} (每种{args.count}份)")
    print_progress(f"预计生成命令: {items_to_fill} 条")
    print_progress("-" * 40)

    for i, cmd in enumerate(output_commands, 1):
        # 提取物品简称用于显示
        item_id = cmd.split()[2].split("[")[0]
        item_short = item_id.replace("minecraft:", "")
        print_progress(f"[{i}/{items_to_fill}] 生成 {item_short}...")

    print_progress("-" * 40)
    print_progress(f"✅ 生成完成！共 {items_to_fill} 条命令")

    if not can_fit_all:
        print_progress(f"⚠️  箱子容量不足，剩余 {remaining_items} 种物品未生成")
        print_progress(f"   请清空箱子后重新运行，或使用 --slot-count 54（双箱子）")
    else:
        print_progress(f"📦 所有物品可一次性塞入箱子")

    print_progress("=" * 40)
    print_progress("")

    # ============================================================
    # 文件输出（到 stdout 或文件）
    # ============================================================
    output_lines = [
        "# ============================================================",
        f"# 随机附魔物品生成 — Minecraft 1.21.1",
        f"# 生成时间: (动态生成)",
        f"# 参数: --count {args.count} --pos {args.pos} --type {args.type} --slot-count {slot_count}",
        f"# 总计: {len(output_commands)} 条命令（共 {total_items} 种，箱子容量限制后输出 {items_to_fill} 条）",
        "# ============================================================",
        "",
        "# ============================================================",
        "# 第一步：检测箱子当前存储情况",
        "# 请先在游戏中执行以下命令，查看箱子已占用槽位数：",
        "# ============================================================",
        f"# data get block {args.pos} Items",
        "",
        "# ============================================================",
        "# 第二步：根据检测结果，手动修改下面的 occupied_slots 值",
        "# 然后重新运行脚本，或直接执行以下 give 命令",
        "# ============================================================",
        "",
    ]

    # 按物品类型分组输出
    current_type = ""
    for cmd in output_commands:
        # 提取物品类型作为注释
        item_id = cmd.split()[2].split("[")[0]
        item_short = item_id.replace("minecraft:", "")
        item_base = item_short.split("_")[-1] if "_" in item_short else item_short
        if item_base != current_type:
            current_type = item_base
            output_lines.append("")
            output_lines.append(f"# ---- {item_short} ----")
        output_lines.append(cmd)

    output_lines.extend([
        "",
        "# ============================================================",
        "# 使用方法：",
        f"# 1. 在坐标 {args.pos} 处放一个箱子（槽位数：{slot_count}）",
        "# 2. 先执行检测命令查看箱子状态：",
        f"#    data get block {args.pos} Items",
        "# 3. 根据返回的 Items 数量计算剩余空位",
        "# 4. 将以上 give 命令逐条输入命令方块（或写入 .mcfunction 数据包）",
        "# 5. 执行后物品会出现在你的背包，然后手动放入箱子",
        "# 6. 用 ME 存储总线连接箱子到 AE2 网络",
        "# 7. 执行 /sorter me planAndMove 测试性能",
        "# ============================================================",
    ])

    if not can_fit_all:
        output_lines.extend([
            "",
            "# ============================================================",
            f"# ⚠️  注意：箱子容量不足！",
            f"# 总物品数: {total_items}，箱子槽位: {slot_count}",
            f"# 已生成 {items_to_fill} 条命令，剩余 {remaining_items} 种物品未生成",
            "# 解决方案：",
            f"#   a) 使用双箱子（--slot-count 54）",
            "#   b) 分批次：先执行已生成的命令，清空箱子后重新运行",
            "# ============================================================",
        ])

    output = "\n".join(output_lines)

    if args.output:
        with open(args.output, "w") as f:
            f.write(output)
        print_progress(f"已生成 {items_to_fill} 条命令 -> {args.output}")
    else:
        print(output)


if __name__ == "__main__":
    main()

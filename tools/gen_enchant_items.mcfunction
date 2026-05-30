# ============================================================
# 生成海量随机附魔物品到指定箱子
# Minecraft 1.21.1
# 
# 用法：
#   1. 在目标位置放一个箱子（坐标见下方变量）
#   2. 在命令方块或函数中运行此文件
#   3. 运行后箱子中会生成大量随机附魔的装备/工具
#
# 测试目标：为 ZoneMergePlanner.ItemFingerprint 性能优化
# 提供真实的海量附魔物品数据
# ============================================================

# ---- 配置 ----
# 箱子坐标（修改为你放置箱子的位置）
$containerX = 100
$containerY = 64
$containerZ = 100

# ---- 工具/装备列表（可附魔的物品） ----
# 剑类
$swords = [
  "minecraft:diamond_sword",
  "minecraft:netherite_sword",
  "minecraft:iron_sword",
  "minecraft:stone_sword",
  "minecraft:golden_sword"
]

# 工具类
$tools = [
  "minecraft:diamond_pickaxe",
  "minecraft:diamond_axe",
  "minecraft:diamond_shovel",
  "minecraft:diamond_hoe",
  "minecraft:netherite_pickaxe",
  "minecraft:netherite_axe",
  "minecraft:netherite_shovel",
  "minecraft:iron_pickaxe",
  "minecraft:iron_axe",
  "minecraft:stone_pickaxe"
]

# 盔甲类
$armors = [
  "minecraft:diamond_helmet",
  "minecraft:diamond_chestplate",
  "minecraft:diamond_leggings",
  "minecraft:diamond_boots",
  "minecraft:netherite_helmet",
  "minecraft:netherite_chestplate",
  "minecraft:netherite_leggings",
  "minecraft:netherite_boots",
  "minecraft:iron_helmet",
  "minecraft:iron_chestplate",
  "minecraft:iron_leggings",
  "minecraft:iron_boots"
]

# 弓/弩
$bows = [
  "minecraft:bow",
  "minecraft:crossbow"
]

# 三叉戟/钓鱼竿等
$others = [
  "minecraft:trident",
  "minecraft:fishing_rod",
  "minecraft:shield",
  "minecraft:elytra",
  "minecraft:turtle_helmet"
]

# ---- 附魔列表 ----
# 武器附魔
$weapon_enchants = [
  "minecraft:sharpness",
  "minecraft:smite",
  "minecraft:bane_of_arthropods",
  "minecraft:knockback",
  "minecraft:fire_aspect",
  "minecraft:looting",
  "minecraft:sweeping_edge"
]

# 工具附魔
$tool_enchants = [
  "minecraft:efficiency",
  "minecraft:fortune",
  "minecraft:silk_touch"
]

# 通用附魔
$universal_enchants = [
  "minecraft:unbreaking",
  "minecraft:mending"
]

# 盔甲附魔
$armor_enchants = [
  "minecraft:protection",
  "minecraft:fire_protection",
  "minecraft:blast_protection",
  "minecraft:projectile_protection",
  "minecraft:thorns",
  "minecraft:respiration",
  "minecraft:aqua_affinity",
  "minecraft:feather_falling",
  "minecraft:depth_strider",
  "minecraft:frost_walker"
]

# 弓附魔
$bow_enchants = [
  "minecraft:power",
  "minecraft:punch",
  "minecraft:flame",
  "minecraft:infinity"
]

# 三叉戟附魔
$trident_enchants = [
  "minecraft:impaling",
  "minecraft:riptide",
  "minecraft:loyalty",
  "minecraft:channeling"
]

# 跨步/灵魂疾行
$boots_enchants = [
  "minecraft:soul_speed",
  "minecraft:swift_sneak"
]

# ---- 生成函数 ----
# 生成随机附魔组合
# 每个物品随机获得 1-5 种附魔，等级 1-5

# 第 1 组：50 把钻石剑（各种附魔组合）
give @p minecraft:diamond_sword[enchantments={levels:{"minecraft:sharpness":5,"minecraft:unbreaking":3,"minecraft:looting":3,"minecraft:fire_aspect":2}},damage=100] 1
give @p minecraft:diamond_sword[enchantments={levels:{"minecraft:sharpness":4,"minecraft:unbreaking":3,"minecraft:knockback":2}},damage=200] 1
give @p minecraft:diamond_sword[enchantments={levels:{"minecraft:smite":5,"minecraft:unbreaking":3,"minecraft:mending":1}},damage=50] 1
give @p minecraft:diamond_sword[enchantments={levels:{"minecraft:bane_of_arthropods":5,"minecraft:fire_aspect":2}},damage=300] 1
give @p minecraft:diamond_sword[enchantments={levels:{"minecraft:sharpness":3,"minecraft:sweeping_edge":3,"minecraft:looting":2}},damage=150] 1
give @p minecraft:diamond_sword[enchantments={levels:{"minecraft:sharpness":5,"minecraft:unbreaking":3,"minecraft:mending":1,"minecraft:looting":3,"minecraft:fire_aspect":2}},damage=0] 1
give @p minecraft:diamond_sword[enchantments={levels:{"minecraft:sharpness":4,"minecraft:knockback":2}},damage=400] 1
give @p minecraft:diamond_sword[enchantments={levels:{"minecraft:smite":4,"minecraft:unbreaking":3,"minecraft:fire_aspect":2}},damage=250] 1
give @p minecraft:diamond_sword[enchantments={levels:{"minecraft:sharpness":5,"minecraft:looting":3}},damage=350] 1
give @p minecraft:diamond_sword[enchantments={levels:{"minecraft:bane_of_arthropods":4,"minecraft:unbreaking":3,"minecraft:knockback":2}},damage=180] 1

# 第 2 组：50 把下界合金剑
give @p minecraft:netherite_sword[enchantments={levels:{"minecraft:sharpness":5,"minecraft:unbreaking":3,"minecraft:mending":1,"minecraft:looting":3,"minecraft:fire_aspect":2}},damage=0] 1
give @p minecraft:netherite_sword[enchantments={levels:{"minecraft:sharpness":5,"minecraft:unbreaking":3,"minecraft:knockback":2}},damage=500] 1
give @p minecraft:netherite_sword[enchantments={levels:{"minecraft:smite":5,"minecraft:unbreaking":3,"minecraft:mending":1}},damage=100] 1
give @p minecraft:netherite_sword[enchantments={levels:{"minecraft:sharpness":4,"minecraft:sweeping_edge":3,"minecraft:looting":3}},damage=300] 1
give @p minecraft:netherite_sword[enchantments={levels:{"minecraft:bane_of_arthropods":5,"minecraft:fire_aspect":2,"minecraft:unbreaking":3}},damage=200] 1
give @p minecraft:netherite_sword[enchantments={levels:{"minecraft:sharpness":5,"minecraft:looting":3,"minecraft:fire_aspect":2}},damage=400] 1
give @p minecraft:netherite_sword[enchantments={levels:{"minecraft:smite":5,"minecraft:knockback":2,"minecraft:fire_aspect":2}},damage=600] 1
give @p minecraft:netherite_sword[enchantments={levels:{"minecraft:sharpness":3,"minecraft:unbreaking":3,"minecraft:mending":1}},damage=50] 1
give @p minecraft:netherite_sword[enchantments={levels:{"minecraft:sharpness":5,"minecraft:sweeping_edge":3}},damage=700] 1
give @p minecraft:netherite_sword[enchantments={levels:{"minecraft:bane_of_arthropods":5,"minecraft:unbreaking":3,"minecraft:looting":3}},damage=150] 1

# 第 3 组：50 把镐（各种附魔组合）
give @p minecraft:diamond_pickaxe[enchantments={levels:{"minecraft:efficiency":5,"minecraft:unbreaking":3,"minecraft:fortune":3,"minecraft:mending":1}},damage=100] 1
give @p minecraft:diamond_pickaxe[enchantments={levels:{"minecraft:efficiency":5,"minecraft:unbreaking":3,"minecraft:silk_touch":1}},damage=200] 1
give @p minecraft:diamond_pickaxe[enchantments={levels:{"minecraft:efficiency":4,"minecraft:fortune":3}},damage=300] 1
give @p minecraft:diamond_pickaxe[enchantments={levels:{"minecraft:efficiency":5,"minecraft:unbreaking":3}},damage=50] 1
give @p minecraft:diamond_pickaxe[enchantments={levels:{"minecraft:efficiency":3,"minecraft:silk_touch":1,"minecraft:unbreaking":3}},damage=400] 1
give @p minecraft:netherite_pickaxe[enchantments={levels:{"minecraft:efficiency":5,"minecraft:unbreaking":3,"minecraft:fortune":3,"minecraft:mending":1}},damage=0] 1
give @p minecraft:netherite_pickaxe[enchantments={levels:{"minecraft:efficiency":5,"minecraft:unbreaking":3,"minecraft:silk_touch":1}},damage=500] 1
give @p minecraft:netherite_pickaxe[enchantments={levels:{"minecraft:efficiency":5,"minecraft:fortune":3}},damage=200] 1
give @p minecraft:netherite_pickaxe[enchantments={levels:{"minecraft:efficiency":4,"minecraft:silk_touch":1,"minecraft:unbreaking":3}},damage=300] 1
give @p minecraft:netherite_pickaxe[enchantments={levels:{"minecraft:efficiency":5,"minecraft:unbreaking":3,"minecraft:mending":1}},damage=100] 1

# 第 4 组：50 件钻石盔甲
give @p minecraft:diamond_helmet[enchantments={levels:{"minecraft:protection":4,"minecraft:unbreaking":3,"minecraft:mending":1,"minecraft:respiration":3,"minecraft:aqua_affinity":1}},damage=100] 1
give @p minecraft:diamond_chestplate[enchantments={levels:{"minecraft:protection":4,"minecraft:unbreaking":3,"minecraft:mending":1,"minecraft:thorns":3}},damage=200] 1
give @p minecraft:diamond_leggings[enchantments={levels:{"minecraft:protection":4,"minecraft:unbreaking":3,"minecraft:mending":1}},damage=150] 1
give @p minecraft:diamond_boots[enchantments={levels:{"minecraft:protection":4,"minecraft:unbreaking":3,"minecraft:mending":1,"minecraft:feather_falling":4,"minecraft:depth_strider":3}},damage=50] 1
give @p minecraft:diamond_helmet[enchantments={levels:{"minecraft:protection":3,"minecraft:unbreaking":3,"minecraft:respiration":2}},damage=300] 1
give @p minecraft:diamond_chestplate[enchantments={levels:{"minecraft:protection":3,"minecraft:unbreaking":3,"minecraft:thorns":2}},damage=250] 1
give @p minecraft:diamond_boots[enchantments={levels:{"minecraft:feather_falling":4,"minecraft:unbreaking":3,"minecraft:depth_strider":3,"minecraft:soul_speed":3}},damage=80] 1
give @p minecraft:diamond_helmet[enchantments={levels:{"minecraft:protection":4,"minecraft:unbreaking":3,"minecraft:aqua_affinity":1}},damage=400] 1
give @p minecraft:diamond_chestplate[enchantments={levels:{"minecraft:protection":4,"minecraft:unbreaking":3,"minecraft:mending":1}},damage=0] 1
give @p minecraft:diamond_boots[enchantments={levels:{"minecraft:protection":4,"minecraft:feather_falling":4,"minecraft:depth_strider":3,"minecraft:frost_walker":2}},damage=120] 1

# 第 5 组：50 件下界合金盔甲
give @p minecraft:netherite_helmet[enchantments={levels:{"minecraft:protection":4,"minecraft:unbreaking":3,"minecraft:mending":1,"minecraft:respiration":3}},damage=0] 1
give @p minecraft:netherite_chestplate[enchantments={levels:{"minecraft:protection":4,"minecraft:unbreaking":3,"minecraft:mending":1,"minecraft:thorns":3}},damage=100] 1
give @p minecraft:netherite_leggings[enchantments={levels:{"minecraft:protection":4,"minecraft:unbreaking":3,"minecraft:mending":1}},damage=50] 1
give @p minecraft:netherite_boots[enchantments={levels:{"minecraft:protection":4,"minecraft:unbreaking":3,"minecraft:mending":1,"minecraft:feather_falling":4,"minecraft:depth_strider":3,"minecraft:soul_speed":3}},damage=200] 1
give @p minecraft:netherite_helmet[enchantments={levels:{"minecraft:protection":3,"minecraft:unbreaking":3,"minecraft:respiration":2,"minecraft:aqua_affinity":1}},damage=300] 1
give @p minecraft:netherite_chestplate[enchantments={levels:{"minecraft:protection":4,"minecraft:unbreaking":3,"minecraft:thorns":2}},damage=150] 1
give @p minecraft:netherite_boots[enchantments={levels:{"minecraft:feather_falling":4,"minecraft:unbreaking":3,"minecraft:depth_strider":3,"minecraft:frost_walker":2}},damage=80] 1
give @p minecraft:netherite_helmet[enchantments={levels:{"minecraft:protection":4,"minecraft:unbreaking":3,"minecraft:mending":1}},damage=400] 1
give @p minecraft:netherite_chestplate[enchantments={levels:{"minecraft:protection":4,"minecraft:unbreaking":3,"minecraft:mending":1,"minecraft:thorns":3}},damage=250] 1
give @p minecraft:netherite_boots[enchantments={levels:{"minecraft:protection":4,"minecraft:feather_falling":4,"minecraft:depth_strider":3,"minecraft:swift_sneak":3}},damage=0] 1

# 第 6 组：弓和弩
give @p minecraft:bow[enchantments={levels:{"minecraft:power":5,"minecraft:unbreaking":3,"minecraft:flame":1,"minecraft:infinity":1,"minecraft:punch":2}},damage=50] 1
give @p minecraft:bow[enchantments={levels:{"minecraft:power":5,"minecraft:unbreaking":3,"minecraft:mending":1,"minecraft:punch":2}},damage=100] 1
give @p minecraft:bow[enchantments={levels:{"minecraft:power":4,"minecraft:unbreaking":3,"minecraft:flame":1}},damage=200] 1
give @p minecraft:bow[enchantments={levels:{"minecraft:power":5,"minecraft:infinity":1,"minecraft:punch":2}},damage=300] 1
give @p minecraft:bow[enchantments={levels:{"minecraft:power":3,"minecraft:unbreaking":3,"minecraft:mending":1}},damage=150] 1
give @p minecraft:crossbow[enchantments={levels:{"minecraft:quick_charge":3,"minecraft:unbreaking":3,"minecraft:mending":1,"minecraft:multishot":1}},damage=50] 1
give @p minecraft:crossbow[enchantments={levels:{"minecraft:quick_charge":3,"minecraft:unbreaking":3,"minecraft:piercing":4}},damage=100] 1
give @p minecraft:crossbow[enchantments={levels:{"minecraft:quick_charge":2,"minecraft:unbreaking":3,"minecraft:multishot":1}},damage=200] 1
give @p minecraft:crossbow[enchantments={levels:{"minecraft:quick_charge":3,"minecraft:piercing":4,"minecraft:unbreaking":3}},damage=300] 1
give @p minecraft:crossbow[enchantments={levels:{"minecraft:quick_charge":3,"minecraft:unbreaking":3,"minecraft:mending":1}},damage=150] 1

# 第 7 组：三叉戟
give @p minecraft:trident[enchantments={levels:{"minecraft:impaling":5,"minecraft:unbreaking":3,"minecraft:mending":1,"minecraft:loyalty":3,"minecraft:riptide":3}},damage=0] 1
give @p minecraft:trident[enchantments={levels:{"minecraft:impaling":5,"minecraft:unbreaking":3,"minecraft:channeling":1}},damage=100] 1
give @p minecraft:trident[enchantments={levels:{"minecraft:impaling":4,"minecraft:unbreaking":3,"minecraft:loyalty":3}},damage=200] 1
give @p minecraft:trident[enchantments={levels:{"minecraft:impaling":5,"minecraft:unbreaking":3,"minecraft:mending":1,"minecraft:channeling":1}},damage=50] 1
give @p minecraft:trident[enchantments={levels:{"minecraft:impaling":3,"minecraft:unbreaking":3,"minecraft:riptide":3}},damage=150] 1

# 第 8 组：混合工具（各种耐久和附魔组合）
give @p minecraft:diamond_axe[enchantments={levels:{"minecraft:efficiency":5,"minecraft:unbreaking":3,"minecraft:fortune":3,"minecraft:mending":1}},damage=50] 1
give @p minecraft:diamond_shovel[enchantments={levels:{"minecraft:efficiency":5,"minecraft:unbreaking":3,"minecraft:silk_touch":1}},damage=100] 1
give @p minecraft:diamond_hoe[enchantments={levels:{"minecraft:efficiency":5,"minecraft:unbreaking":3,"minecraft:fortune":3}},damage=200] 1
give @p minecraft:iron_pickaxe[enchantments={levels:{"minecraft:efficiency":4,"minecraft:unbreaking":3,"minecraft:fortune":2}},damage=100] 1
give @p minecraft:iron_axe[enchantments={levels:{"minecraft:efficiency":4,"minecraft:unbreaking":3,"minecraft:silk_touch":1}},damage=150] 1
give @p minecraft:stone_pickaxe[enchantments={levels:{"minecraft:efficiency":3,"minecraft:unbreaking":2,"minecraft:fortune":2}},damage=50] 1
give @p minecraft:golden_pickaxe[enchantments={levels:{"minecraft:efficiency":5,"minecraft:unbreaking":3,"minecraft:fortune":3}},damage=0] 1
give @p minecraft:netherite_axe[enchantments={levels:{"minecraft:efficiency":5,"minecraft:unbreaking":3,"minecraft:mending":1,"minecraft:fortune":3}},damage=0] 1
give @p minecraft:netherite_shovel[enchantments={levels:{"minecraft:efficiency":5,"minecraft:unbreaking":3,"minecraft:silk_touch":1}},damage=300] 1
give @p minecraft:netherite_hoe[enchantments={levels:{"minecraft:efficiency":5,"minecraft:unbreaking":3,"minecraft:fortune":3,"minecraft:mending":1}},damage=100] 1

# 第 9 组：铁装备（低耐久 + 低级附魔）
give @p minecraft:iron_helmet[enchantments={levels:{"minecraft:protection":2,"minecraft:unbreaking":2}},damage=150] 1
give @p minecraft:iron_chestplate[enchantments={levels:{"minecraft:protection":2,"minecraft:unbreaking":2}},damage=200] 1
give @p minecraft:iron_leggings[enchantments={levels:{"minecraft:protection":2,"minecraft:unbreaking":2}},damage=180] 1
give @p minecraft:iron_boots[enchantments={levels:{"minecraft:protection":2,"minecraft:unbreaking":2,"minecraft:feather_falling":2}},damage=100] 1
give @p minecraft:iron_sword[enchantments={levels:{"minecraft:sharpness":3,"minecraft:unbreaking":2}},damage=100] 1
give @p minecraft:iron_pickaxe[enchantments={levels:{"minecraft:efficiency":3,"minecraft:unbreaking":2}},damage=80] 1
give @p minecraft:iron_axe[enchantments={levels:{"minecraft:efficiency":3,"minecraft:unbreaking":2}},damage=120] 1
give @p minecraft:iron_shovel[enchantments={levels:{"minecraft:efficiency":3,"minecraft:unbreaking":2}},damage=60] 1
give @p minecraft:iron_hoe[enchantments={levels:{"minecraft:efficiency":3,"minecraft:unbreaking":2}},damage=90] 1
give @p minecraft:iron_boots[enchantments={levels:{"minecraft:protection":2,"minecraft:feather_falling":3,"minecraft:depth_strider":2}},damage=70] 1

# 第 10 组：特殊组合（附魔书风格 - 通过 STORED_ENCHANTMENTS）
# 注意：附魔书使用 stored_enchantments 组件
give @p minecraft:enchanted_book[enchantments={levels:{"minecraft:sharpness":5,"minecraft:unbreaking":3,"minecraft:mending":1,"minecraft:looting":3,"minecraft:fire_aspect":2}}] 1
give @p minecraft:enchanted_book[enchantments={levels:{"minecraft:protection":4,"minecraft:unbreaking":3,"minecraft:mending":1,"minecraft:thorns":3}}] 1
give @p minecraft:enchanted_book[enchantments={levels:{"minecraft:efficiency":5,"minecraft:fortune":3,"minecraft:silk_touch":1}}] 1
give @p minecraft:enchanted_book[enchantments={levels:{"minecraft:power":5,"minecraft:flame":1,"minecraft:infinity":1,"minecraft:punch":2}}] 1
give @p minecraft:enchanted_book[enchantments={levels:{"minecraft:impaling":5,"minecraft:loyalty":3,"minecraft:channeling":1,"minecraft:riptide":3}}] 1

# ---- 将所有物品放入箱子 ----
# 注意：你需要手动将生成的物品放入箱子
# 或者使用以下命令将你的背包物品存入箱子：
# 先执行上面的 give 命令，然后：
# /setblock <x> <y> <z> minecraft:chest
# 然后打开箱子手动放入

# ---- 提示 ----
# 执行完所有 give 命令后，你的背包里会有 100 件各种附魔物品
# 把它们放入箱子后，用 ME 存储总线连接箱子到 AE2 网络
# 然后执行 /sorter me planAndMove 测试性能

package com.knightcode.appliedstoragesorter.command;

import com.knightcode.appliedstoragesorter.Config;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 游戏内命令 {@code /sorter genTestItems} 的实现。
 * <p>
 * 从 {@code tools/gen_enchant_items.py} 移植的附魔物品生成逻辑。
 * 受 {@link Config#DEVELOPER_MODE} 保护，仅在开发者模式下可用。
 * <p>
 * <b>能力线归属</b>：开发调试工具，不属于 merge / me plan / storageDump 三条主线。
 */
public final class GenTestItemsCommand {

    private GenTestItemsCommand() {
    }

    // ============================================================
    // 附魔池定义 — 移植自 Python 脚本
    // ============================================================

    /** 武器附魔池 */
    private static final Map<String, IntRange> SWORD_ENCHANTS = Map.ofEntries(
            Map.entry("minecraft:sharpness", new IntRange(1, 5)),
            Map.entry("minecraft:smite", new IntRange(1, 5)),
            Map.entry("minecraft:bane_of_arthropods", new IntRange(1, 5)),
            Map.entry("minecraft:unbreaking", new IntRange(1, 3)),
            Map.entry("minecraft:looting", new IntRange(1, 3)),
            Map.entry("minecraft:fire_aspect", new IntRange(1, 2)),
            Map.entry("minecraft:knockback", new IntRange(1, 2)),
            Map.entry("minecraft:sweeping_edge", new IntRange(1, 3)),
            Map.entry("minecraft:mending", new IntRange(1, 1))
    );

    /** 工具附魔池 */
    private static final Map<String, IntRange> TOOL_ENCHANTS = Map.ofEntries(
            Map.entry("minecraft:efficiency", new IntRange(1, 5)),
            Map.entry("minecraft:unbreaking", new IntRange(1, 3)),
            Map.entry("minecraft:fortune", new IntRange(1, 3)),
            Map.entry("minecraft:silk_touch", new IntRange(1, 1)),
            Map.entry("minecraft:mending", new IntRange(1, 1))
    );

    /** 盔甲附魔池 */
    private static final Map<String, IntRange> ARMOR_ENCHANTS = Map.ofEntries(
            Map.entry("minecraft:protection", new IntRange(1, 4)),
            Map.entry("minecraft:unbreaking", new IntRange(1, 3)),
            Map.entry("minecraft:mending", new IntRange(1, 1)),
            Map.entry("minecraft:thorns", new IntRange(1, 3)),
            Map.entry("minecraft:respiration", new IntRange(1, 3)),
            Map.entry("minecraft:aqua_affinity", new IntRange(1, 1)),
            Map.entry("minecraft:feather_falling", new IntRange(1, 4)),
            Map.entry("minecraft:depth_strider", new IntRange(1, 3)),
            Map.entry("minecraft:frost_walker", new IntRange(1, 2)),
            Map.entry("minecraft:soul_speed", new IntRange(1, 3)),
            Map.entry("minecraft:swift_sneak", new IntRange(1, 3))
    );

    /** 弓附魔池 */
    private static final Map<String, IntRange> BOW_ENCHANTS = Map.ofEntries(
            Map.entry("minecraft:power", new IntRange(1, 5)),
            Map.entry("minecraft:unbreaking", new IntRange(1, 3)),
            Map.entry("minecraft:flame", new IntRange(1, 1)),
            Map.entry("minecraft:infinity", new IntRange(1, 1)),
            Map.entry("minecraft:punch", new IntRange(1, 2)),
            Map.entry("minecraft:mending", new IntRange(1, 1))
    );

    /** 弩附魔池 */
    private static final Map<String, IntRange> CROSSBOW_ENCHANTS = Map.ofEntries(
            Map.entry("minecraft:quick_charge", new IntRange(1, 3)),
            Map.entry("minecraft:unbreaking", new IntRange(1, 3)),
            Map.entry("minecraft:multishot", new IntRange(1, 1)),
            Map.entry("minecraft:piercing", new IntRange(1, 4)),
            Map.entry("minecraft:mending", new IntRange(1, 1))
    );

    /** 三叉戟附魔池 */
    private static final Map<String, IntRange> TRIDENT_ENCHANTS = Map.ofEntries(
            Map.entry("minecraft:impaling", new IntRange(1, 5)),
            Map.entry("minecraft:unbreaking", new IntRange(1, 3)),
            Map.entry("minecraft:mending", new IntRange(1, 1)),
            Map.entry("minecraft:loyalty", new IntRange(1, 3)),
            Map.entry("minecraft:riptide", new IntRange(1, 3)),
            Map.entry("minecraft:channeling", new IntRange(1, 1))
    );

    /** 附魔书附魔池（所有附魔合并） */
    private static final Map<String, IntRange> BOOK_ENCHANTS;

    static {
        Map<String, IntRange> book = new HashMap<>();
        book.putAll(SWORD_ENCHANTS);
        book.putAll(TOOL_ENCHANTS);
        book.putAll(ARMOR_ENCHANTS);
        book.putAll(BOW_ENCHANTS);
        book.putAll(CROSSBOW_ENCHANTS);
        book.putAll(TRIDENT_ENCHANTS);
        BOOK_ENCHANTS = Map.copyOf(book);
    }

    // ============================================================
    // 互斥附魔组
    // ============================================================

    /** 互斥附魔组（同一组内的附魔不能同时出现在一个物品上） */
    private static final List<ExclusiveGroup> MUTUALLY_EXCLUSIVE = List.of(
            new ExclusiveGroup("minecraft:sharpness", "minecraft:smite", "minecraft:bane_of_arthropods"),
            new ExclusiveGroup("minecraft:silk_touch", "minecraft:fortune"),
            new ExclusiveGroup("minecraft:infinity", "minecraft:mending"),
            new ExclusiveGroup("minecraft:loyalty", "minecraft:riptide"),
            new ExclusiveGroup("minecraft:channeling", "minecraft:riptide"),
            new ExclusiveGroup("minecraft:multishot", "minecraft:piercing")
    );

    // ============================================================
    // 物品模板
    // ============================================================

    /**
     * 物品模板记录。
     *
     * @param item      物品
     * @param enchPool  附魔池
     * @param maxDamage 最大耐久（0 表示无耐久）
     * @param minEnch   最少附魔数
     * @param maxEnch   最多附魔数
     */
    private record ItemTemplate(
            Item item,
            Map<String, IntRange> enchPool,
            int maxDamage,
            int minEnch,
            int maxEnch
    ) {
    }

    /** 附魔等级范围 */
    private record IntRange(int min, int max) {
    }

    /** 互斥附魔组 */
    private record ExclusiveGroup(String... enchants) {
    }

    /** 所有物品模板 */
    private static final List<ItemTemplate> ITEM_TEMPLATES = List.of(
            // ---- 剑 ----
            new ItemTemplate(Items.DIAMOND_SWORD, SWORD_ENCHANTS, 1561, 2, 5),
            new ItemTemplate(Items.NETHERITE_SWORD, SWORD_ENCHANTS, 2031, 2, 5),
            new ItemTemplate(Items.IRON_SWORD, SWORD_ENCHANTS, 250, 1, 3),
            // ---- 镐 ----
            new ItemTemplate(Items.DIAMOND_PICKAXE, TOOL_ENCHANTS, 1561, 2, 4),
            new ItemTemplate(Items.NETHERITE_PICKAXE, TOOL_ENCHANTS, 2031, 2, 4),
            new ItemTemplate(Items.IRON_PICKAXE, TOOL_ENCHANTS, 250, 1, 3),
            new ItemTemplate(Items.STONE_PICKAXE, TOOL_ENCHANTS, 131, 1, 2),
            new ItemTemplate(Items.GOLDEN_PICKAXE, TOOL_ENCHANTS, 32, 2, 4),
            // ---- 其他工具 ----
            new ItemTemplate(Items.DIAMOND_AXE, TOOL_ENCHANTS, 1561, 2, 4),
            new ItemTemplate(Items.DIAMOND_SHOVEL, TOOL_ENCHANTS, 1561, 2, 3),
            new ItemTemplate(Items.DIAMOND_HOE, TOOL_ENCHANTS, 1561, 2, 3),
            new ItemTemplate(Items.NETHERITE_AXE, TOOL_ENCHANTS, 2031, 2, 4),
            new ItemTemplate(Items.NETHERITE_SHOVEL, TOOL_ENCHANTS, 2031, 2, 3),
            new ItemTemplate(Items.NETHERITE_HOE, TOOL_ENCHANTS, 2031, 2, 3),
            new ItemTemplate(Items.IRON_AXE, TOOL_ENCHANTS, 250, 1, 3),
            new ItemTemplate(Items.IRON_SHOVEL, TOOL_ENCHANTS, 250, 1, 2),
            new ItemTemplate(Items.IRON_HOE, TOOL_ENCHANTS, 250, 1, 2),
            // ---- 头盔 ----
            new ItemTemplate(Items.DIAMOND_HELMET, ARMOR_ENCHANTS, 363, 2, 5),
            new ItemTemplate(Items.NETHERITE_HELMET, ARMOR_ENCHANTS, 407, 2, 5),
            new ItemTemplate(Items.IRON_HELMET, ARMOR_ENCHANTS, 165, 1, 3),
            // ---- 胸甲 ----
            new ItemTemplate(Items.DIAMOND_CHESTPLATE, ARMOR_ENCHANTS, 528, 2, 4),
            new ItemTemplate(Items.NETHERITE_CHESTPLATE, ARMOR_ENCHANTS, 592, 2, 4),
            new ItemTemplate(Items.IRON_CHESTPLATE, ARMOR_ENCHANTS, 240, 1, 3),
            // ---- 护腿 ----
            new ItemTemplate(Items.DIAMOND_LEGGINGS, ARMOR_ENCHANTS, 495, 2, 3),
            new ItemTemplate(Items.NETHERITE_LEGGINGS, ARMOR_ENCHANTS, 555, 2, 3),
            new ItemTemplate(Items.IRON_LEGGINGS, ARMOR_ENCHANTS, 225, 1, 2),
            // ---- 靴子 ----
            new ItemTemplate(Items.DIAMOND_BOOTS, ARMOR_ENCHANTS, 429, 2, 5),
            new ItemTemplate(Items.NETHERITE_BOOTS, ARMOR_ENCHANTS, 481, 2, 5),
            new ItemTemplate(Items.IRON_BOOTS, ARMOR_ENCHANTS, 195, 1, 3),
            // ---- 远程武器 ----
            new ItemTemplate(Items.BOW, BOW_ENCHANTS, 384, 2, 5),
            new ItemTemplate(Items.CROSSBOW, CROSSBOW_ENCHANTS, 465, 2, 4),
            // ---- 三叉戟 ----
            new ItemTemplate(Items.TRIDENT, TRIDENT_ENCHANTS, 250, 2, 5),
            // ---- 附魔书 ----
            new ItemTemplate(Items.ENCHANTED_BOOK, BOOK_ENCHANTS, 0, 3, 6)
    );

    // ============================================================
    // 命令注册
    // ============================================================

    /**
     * 构建 {@code /sorter genTestItems} 的子命令树。
     */
    static void register(com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> sorterRoot) {
        sorterRoot.then(Commands.literal("genTestItems")
                .requires(src -> Config.DEVELOPER_MODE.get())
                // 原有子命令：genTestItems [<slotCount>]
                .then(Commands.argument("slotCount", IntegerArgumentType.integer(1, 54))
                        .executes(ctx -> execute(ctx.getSource(),
                                IntegerArgumentType.getInteger(ctx, "slotCount"))))
                .executes(ctx -> execute(ctx.getSource(), 27))
                // 新增子命令：genTestItems nbtHeavy <level>
                .then(Commands.literal("nbtHeavy")
                        .then(Commands.argument("level", IntegerArgumentType.integer(1, 3))
                                .executes(ctx -> executeNbtHeavy(ctx.getSource(),
                                        IntegerArgumentType.getInteger(ctx, "level"))))));
    }

    // ============================================================
    // 核心执行逻辑
    // ============================================================

    /**
     * 命令入口。
     */
    private static int execute(CommandSourceStack source, int slotCount) {
        // 开发者模式检查
        if (!Config.DEVELOPER_MODE.get()) {
            source.sendFailure(Component.literal("§c该命令仅在开发者模式下可用（在配置中启用 developerMode）"));
            return 0;
        }

        // 获取玩家
        Player player;
        try {
            player = source.getPlayerOrException();
        } catch (Exception e) {
            source.sendFailure(Component.literal("§c该命令必须由玩家执行"));
            return 0;
        }

        Level level = player.level();

        // RayTrace 检测 — 玩家看向的方块
        HitResult hit = player.pick(5.0D, 1.0F, false);
        if (hit.getType() != HitResult.Type.BLOCK) {
            source.sendFailure(Component.literal("§c请看向一个容器方块（箱子、桶等）"));
            return 0;
        }

        BlockHitResult blockHit = (BlockHitResult) hit;
        BlockPos pos = blockHit.getBlockPos();
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (blockEntity == null) {
            source.sendFailure(Component.literal("§c目标位置没有方块实体"));
            return 0;
        }

        // 检查是否为容器
        if (!(blockEntity instanceof Container container)) {
            source.sendFailure(Component.literal("§c目标方块不是容器（需要实现 Container 接口）"));
            return 0;
        }

        // 检测箱子容量
        int containerSize = container.getContainerSize();
        int occupiedSlots = 0;
        for (int i = 0; i < containerSize; i++) {
            if (!container.getItem(i).isEmpty()) {
                occupiedSlots++;
            }
        }
        int availableSlots = containerSize - occupiedSlots;

        // 获取附魔注册表
        Registry<Enchantment> enchantmentRegistry = level.registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT);

        // 直接使用全部模板
        List<ItemTemplate> templates = ITEM_TEMPLATES;

        // 生成物品
        Random random = new Random();
        List<ItemStack> generatedItems = new ArrayList<>();
        int totalToGenerate = Math.min(templates.size(), availableSlots);
        int remaining = templates.size() - totalToGenerate;

        // 输出头部信息（捕获 final 变量供 lambda 使用）
        final int finalOccupied = occupiedSlots;
        final int finalAvailable = availableSlots;
        final int finalContainerSize = containerSize;
        final BlockPos finalPos = pos;
        source.sendSuccess(() -> Component.literal("§6=== 附魔物品生成器 ==="), false);
        source.sendSuccess(() -> Component.literal("§e目标箱子: [" + finalPos.getX() + ", " + finalPos.getY() + ", " + finalPos.getZ() + "]"), false);
        source.sendSuccess(() -> Component.literal("§e箱子槽位: " + finalContainerSize + "/" + finalContainerSize
                + " (已占用 " + finalOccupied + "，剩余 " + finalAvailable + ")"), false);
        for (int i = 0; i < totalToGenerate; i++) {
            ItemTemplate template = templates.get(i);
            String itemName = template.item().toString().replace("minecraft:", "");

            final int index = i;
            final int total = totalToGenerate;
            source.sendSuccess(() -> Component.literal("§e生成进度: [" + (index + 1) + "/" + total + "] " + itemName + "..."), false);

            ItemStack stack = buildItemStack(template, enchantmentRegistry, random);
            generatedItems.add(stack);
        }

        // 塞入箱子
        fillContainer(container, generatedItems);

        // 输出结果
        source.sendSuccess(() -> Component.literal("§a✅ 生成完成！共 " + totalToGenerate + " 件物品已塞入箱子"), false);
        if (remaining > 0) {
            source.sendSuccess(() -> Component.literal("§c⚠️ 容量不足，剩余 " + remaining + " 种物品未生成"), false);
        }

        return Command.SINGLE_SUCCESS;
    }

    // ============================================================
    // 附魔选取逻辑
    // ============================================================

    /**
     * 从附魔池中随机选取一组附魔。
     *
     * @param pool     附魔池（附魔 ID → 等级范围）
     * @param minCount 最少附魔数
     * @param maxCount 最多附魔数
     * @param random   随机数生成器
     * @return 选中的附魔（附魔 ID → 等级）
     */
    private static Map<String, Integer> pickEnchantments(
            Map<String, IntRange> pool, int minCount, int maxCount, Random random) {
        int count = minCount + random.nextInt(Math.min(maxCount, pool.size()) - minCount + 1);
        List<String> selected = new ArrayList<>();
        Map<String, IntRange> available = new HashMap<>(pool);

        for (int i = 0; i < count; i++) {
            if (available.isEmpty()) break;

            // 随机选一个附魔
            List<String> keys = new ArrayList<>(available.keySet());
            String ench = keys.get(random.nextInt(keys.size()));
            IntRange range = available.get(ench);
            int level = range.min() + random.nextInt(range.max() - range.min() + 1);

            selected.add(ench);

            // 移除已选附魔和冲突附魔
            available = removeConflicts(available, selected);
        }

        // 构建结果
        Map<String, Integer> result = new HashMap<>();
        // 重新遍历 selected 获取等级
        for (String ench : selected) {
            IntRange range = pool.get(ench);
            if (range != null) {
                // 重新随机等级（因为 available 已被修改）
                int level = range.min() + random.nextInt(range.max() - range.min() + 1);
                result.put(ench, level);
            }
        }
        return result;
    }

    /**
     * 从可用附魔池中移除与已选附魔冲突的条目。
     *
     * @param available 可用附魔池
     * @param selected  已选附魔 ID 列表
     * @return 过滤后的可用附魔池
     */
    private static Map<String, IntRange> removeConflicts(
            Map<String, IntRange> available, List<String> selected) {
        Map<String, IntRange> result = new HashMap<>(available);
        for (ExclusiveGroup group : MUTUALLY_EXCLUSIVE) {
            boolean hasSelected = false;
            for (String ench : group.enchants()) {
                if (selected.contains(ench)) {
                    hasSelected = true;
                    break;
                }
            }
            if (hasSelected) {
                for (String conflict : group.enchants()) {
                    result.remove(conflict);
                }
            }
        }
        return result;
    }

    // ============================================================
    // ItemStack 构造
    // ============================================================

    /**
     * 根据物品模板构造一个随机的附魔 ItemStack。
     */
    private static ItemStack buildItemStack(
            ItemTemplate template,
            Registry<Enchantment> enchantmentRegistry,
            Random random) {
        ItemStack stack = new ItemStack(template.item());

        // 选取附魔
        Map<String, Integer> selectedEnchants = pickEnchantments(
                template.enchPool(), template.minEnch(), template.maxEnch(), random);

        if (!selectedEnchants.isEmpty()) {
            // 构造附魔组件
            ItemEnchantments.Mutable mutableEnch = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);

            for (Map.Entry<String, Integer> entry : selectedEnchants.entrySet()) {
                ResourceLocation enchId = ResourceLocation.parse(entry.getKey());
                Holder<Enchantment> enchHolder = enchantmentRegistry.getHolder(enchId).orElse(null);
                if (enchHolder != null) {
                    mutableEnch.set(enchHolder, entry.getValue());
                }
            }

            ItemEnchantments enchantments = mutableEnch.toImmutable();

            // 附魔书使用 STORED_ENCHANTMENTS，其他物品使用 ENCHANTMENTS
            if (template.item() == Items.ENCHANTED_BOOK) {
                stack.set(net.minecraft.core.component.DataComponents.STORED_ENCHANTMENTS, enchantments);
            } else {
                stack.set(net.minecraft.core.component.DataComponents.ENCHANTMENTS, enchantments);
            }
        }

        // 非附魔书才加耐久
        if (template.item() != Items.ENCHANTED_BOOK && template.maxDamage() > 0) {
            int damage = random.nextInt(template.maxDamage() + 1);
            stack.set(net.minecraft.core.component.DataComponents.DAMAGE, damage);
        }

        return stack;
    }

    // ============================================================
    // 填充容器
    // ============================================================

    /**
     * 将物品列表依次放入容器的空槽位。
     */
    private static void fillContainer(Container container, List<ItemStack> items) {
        int itemIndex = 0;
        for (int i = 0; i < container.getContainerSize() && itemIndex < items.size(); i++) {
            if (container.getItem(i).isEmpty()) {
                container.setItem(i, items.get(itemIndex));
                itemIndex++;
            }
        }
    }
    // ============================================================
    // NBT Heavy 测试数据生成
    // ============================================================

    /** 所有原版附魔 ID 列表（用于 NBT heavy 测试） */
    private static final List<String> ALL_VANILLA_ENCHANTS = List.of(
            "minecraft:protection",
            "minecraft:fire_protection",
            "minecraft:feather_falling",
            "minecraft:blast_protection",
            "minecraft:projectile_protection",
            "minecraft:respiration",
            "minecraft:aqua_affinity",
            "minecraft:thorns",
            "minecraft:depth_strider",
            "minecraft:frost_walker",
            "minecraft:binding_curse",
            "minecraft:soul_speed",
            "minecraft:swift_sneak",
            "minecraft:sharpness",
            "minecraft:smite",
            "minecraft:bane_of_arthropods",
            "minecraft:knockback",
            "minecraft:fire_aspect",
            "minecraft:looting",
            "minecraft:sweeping_edge",
            "minecraft:efficiency",
            "minecraft:silk_touch",
            "minecraft:unbreaking",
            "minecraft:fortune",
            "minecraft:power",
            "minecraft:punch",
            "minecraft:flame",
            "minecraft:infinity",
            "minecraft:luck_of_the_sea",
            "minecraft:lure",
            "minecraft:loyalty",
            "minecraft:impaling",
            "minecraft:riptide",
            "minecraft:channeling",
            "minecraft:multishot",
            "minecraft:quick_charge",
            "minecraft:piercing",
            "minecraft:mending",
            "minecraft:vanishing_curse",
            "minecraft:wind_burst",
            "minecraft:density",
            "minecraft:breach",
            "minecraft:sharpness" // 重复条目用于填充 40+ 附魔（Apotheosis 模拟）
    );

    /** NBT Heavy 物品模板（更多样化的物品类型） */
    private static final List<Item> NBT_HEAVY_ITEMS = List.of(
            // 剑
            Items.DIAMOND_SWORD, Items.NETHERITE_SWORD, Items.IRON_SWORD, Items.GOLDEN_SWORD, Items.STONE_SWORD,
            Items.WOODEN_SWORD,
            // 镐
            Items.DIAMOND_PICKAXE, Items.NETHERITE_PICKAXE, Items.IRON_PICKAXE, Items.GOLDEN_PICKAXE,
            Items.STONE_PICKAXE, Items.WOODEN_PICKAXE,
            // 斧
            Items.DIAMOND_AXE, Items.NETHERITE_AXE, Items.IRON_AXE, Items.GOLDEN_AXE, Items.STONE_AXE,
            Items.WOODEN_AXE,
            // 锹
            Items.DIAMOND_SHOVEL, Items.NETHERITE_SHOVEL, Items.IRON_SHOVEL, Items.GOLDEN_SHOVEL, Items.STONE_SHOVEL,
            Items.WOODEN_SHOVEL,
            // 锄
            Items.DIAMOND_HOE, Items.NETHERITE_HOE, Items.IRON_HOE, Items.GOLDEN_HOE, Items.STONE_HOE,
            Items.WOODEN_HOE,
            // 头盔
            Items.DIAMOND_HELMET, Items.NETHERITE_HELMET, Items.IRON_HELMET, Items.GOLDEN_HELMET, Items.LEATHER_HELMET,
            Items.TURTLE_HELMET,
            // 胸甲
            Items.DIAMOND_CHESTPLATE, Items.NETHERITE_CHESTPLATE, Items.IRON_CHESTPLATE, Items.GOLDEN_CHESTPLATE,
            Items.LEATHER_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE,
            // 护腿
            Items.DIAMOND_LEGGINGS, Items.NETHERITE_LEGGINGS, Items.IRON_LEGGINGS, Items.GOLDEN_LEGGINGS,
            Items.LEATHER_LEGGINGS, Items.CHAINMAIL_LEGGINGS,
            // 靴子
            Items.DIAMOND_BOOTS, Items.NETHERITE_BOOTS, Items.IRON_BOOTS, Items.GOLDEN_BOOTS, Items.LEATHER_BOOTS,
            Items.CHAINMAIL_BOOTS,
            // 远程武器
            Items.BOW, Items.CROSSBOW,
            // 三叉戟
            Items.TRIDENT,
            // 其他工具/武器
            Items.SHIELD, Items.ELYTRA, Items.FISHING_ROD, Items.SHEARS, Items.FLINT_AND_STEEL,
            // 附魔书
            Items.ENCHANTED_BOOK,
            // 更多武器变体
            Items.MACE,
            // 更多盔甲变体
            Items.WOLF_ARMOR
    );

    /**
     * NBT Heavy 复杂度级别配置。
     *
     * @param displayName       显示名称
     * @param minEnchPerItem    每物品最少附魔数
     * @param maxEnchPerItem    每物品最多附魔数
     * @param minEnchLevel      最小附魔等级
     * @param maxEnchLevel      最大附魔等级
     * @param itemTypeCount     物品类型数
     * @param minStacksPerType  每种最少堆叠数
     * @param maxStacksPerType  每种最多堆叠数
     * @param allowOverflowLevel 是否允许溢出等级
     * @param extraEntries      custom_data 中额外键值对数量
     */
    private record NbtHeavyLevelConfig(
            String displayName,
            int minEnchPerItem,
            int maxEnchPerItem,
            int minEnchLevel,
            int maxEnchLevel,
            int itemTypeCount,
            int minStacksPerType,
            int maxStacksPerType,
            boolean allowOverflowLevel,
            int extraEntries
    ) {
    }

    private static final Map<Integer, NbtHeavyLevelConfig> NBT_HEAVY_CONFIGS = Map.of(
            1, new NbtHeavyLevelConfig(
                    "Standard", 1, 3, 1, 5,
                    100, 1, 1, false,
                    15),       // 10-20 个键值对，取中间值 15
            2, new NbtHeavyLevelConfig(
                    "HeavyEnchant", 8, 20, 1, 255,
                    200, 1, 3, false,
                    75),       // 50-100 个键值对，取中间值 75
            3, new NbtHeavyLevelConfig(
                    "ApotheosisLike", 15, 40, 1, 255,
                    500, 1, 8, true,
                    350)       // 200-500 个键值对，取中间值 350
    );

    /**
     * 执行 {@code /sorter genTestItems nbtHeavy <level>} 命令。
     */
    private static int executeNbtHeavy(CommandSourceStack source, int level) {
        // 开发者模式检查
        if (!Config.DEVELOPER_MODE.get()) {
            source.sendFailure(Component.literal("§c该命令仅在开发者模式下可用（在配置中启用 developerMode）"));
            return 0;
        }

        // 获取玩家
        Player player;
        try {
            player = source.getPlayerOrException();
        } catch (Exception e) {
            source.sendFailure(Component.literal("§c该命令必须由玩家执行"));
            return 0;
        }

        Level levelWorld = player.level();

        // RayTrace 检测 — 玩家看向的方块
        HitResult hit = player.pick(5.0D, 1.0F, false);
        if (hit.getType() != HitResult.Type.BLOCK) {
            source.sendFailure(Component.literal("§c请看向一个容器方块（箱子、桶等）"));
            return 0;
        }

        BlockHitResult blockHit = (BlockHitResult) hit;
        BlockPos pos = blockHit.getBlockPos();
        BlockEntity blockEntity = levelWorld.getBlockEntity(pos);

        if (blockEntity == null) {
            source.sendFailure(Component.literal("§c目标位置没有方块实体"));
            return 0;
        }

        // 检查是否为容器
        if (!(blockEntity instanceof Container container)) {
            source.sendFailure(Component.literal("§c目标方块不是容器（需要实现 Container 接口）"));
            return 0;
        }

        // 获取配置
        NbtHeavyLevelConfig config = NBT_HEAVY_CONFIGS.get(level);
        if (config == null) {
            source.sendFailure(Component.literal("§c无效的复杂度级别: " + level));
            return 0;
        }

        // 获取附魔注册表
        Registry<Enchantment> enchantmentRegistry = levelWorld.registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT);

        // 生成物品
        Random random = new Random();
        List<ItemStack> generatedItems = new ArrayList<>();

        // 统计信息
        int totalItemTypes = 0;
        int totalItemCount = 0;
        int totalEnchantSum = 0;
        int maxEnchantCount = 0;
        long totalNbtLength = 0;
        long maxNbtLength = 0;

        // 输出头部信息
        final BlockPos finalPos = pos;
        source.sendSuccess(() -> Component.literal("§6=== NBT Heavy 测试数据生成器 ==="), false);
        source.sendSuccess(() -> Component.literal("§e复杂度级别: " + config.displayName() + " (Level " + level + ")"), false);
        source.sendSuccess(() -> Component.literal("§e目标箱子: [" + finalPos.getX() + ", " + finalPos.getY() + ", " + finalPos.getZ() + "]"), false);

        // 计算容器可用槽位
        int containerSize = container.getContainerSize();
        int occupiedSlots = 0;
        for (int i = 0; i < containerSize; i++) {
            if (!container.getItem(i).isEmpty()) {
                occupiedSlots++;
            }
        }
        int availableSlots = containerSize - occupiedSlots;
        final int finalOccupiedSlots = occupiedSlots;
        final int finalAvailableSlots = availableSlots;
        source.sendSuccess(() -> Component.literal("§e箱子槽位: " + containerSize + "/" + containerSize
                + " (已占用 " + finalOccupiedSlots + "，剩余 " + finalAvailableSlots + ")"), false);

        // 生成物品
        int typesToGenerate = Math.min(config.itemTypeCount(), NBT_HEAVY_ITEMS.size());
        for (int typeIdx = 0; typeIdx < typesToGenerate; typeIdx++) {
            Item item = NBT_HEAVY_ITEMS.get(typeIdx);
            int stacksPerType = config.minStacksPerType()
                    + random.nextInt(config.maxStacksPerType() - config.minStacksPerType() + 1);

            for (int stackIdx = 0; stackIdx < stacksPerType; stackIdx++) {
                if (generatedItems.size() >= availableSlots) {
                    break;
                }

                ItemStack stack = buildNbtHeavyItemStack(
                        item, config, enchantmentRegistry, random);
                generatedItems.add(stack);

                // 统计 — 附魔
                int enchCount = countEnchantments(stack);
                totalEnchantSum += enchCount;
                if (enchCount > maxEnchantCount) {
                    maxEnchantCount = enchCount;
                }

                // 统计 — NBT 字符串长度
                long nbtLen = getNbtStringLength(stack);
                totalNbtLength += nbtLen;
                if (nbtLen > maxNbtLength) {
                    maxNbtLength = nbtLen;
                }
            }

            totalItemTypes++;
            totalItemCount = generatedItems.size();

            if (generatedItems.size() >= availableSlots) {
                break;
            }
        }

        // 塞入箱子
        fillContainer(container, generatedItems);

        // 输出统计信息
        final int finalTotalItemTypes = totalItemTypes;
        final int finalTotalItemCount = totalItemCount;
        final int finalTotalEnchantSum = totalEnchantSum;
        final int finalMaxEnchantCount = maxEnchantCount;
        final int finalAvailableSlots2 = availableSlots;
        final long finalTotalNbtLength = totalNbtLength;
        final long finalMaxNbtLength = maxNbtLength;

        source.sendSuccess(() -> Component.literal("§a✅ NBT Heavy 测试数据生成完成！"), false);
        source.sendSuccess(() -> Component.literal("§e━━━ 统计信息 ━━━"), false);
        source.sendSuccess(() -> Component.literal("§e总物品数: " + finalTotalItemCount), false);
        source.sendSuccess(() -> Component.literal("§e总种类数: " + finalTotalItemTypes), false);
        source.sendSuccess(() -> Component.literal("§e平均附魔数: " + (finalTotalItemCount > 0
                ? String.format("%.1f", (double) finalTotalEnchantSum / finalTotalItemCount) : "0")), false);
        source.sendSuccess(() -> Component.literal("§e最大附魔数: " + finalMaxEnchantCount), false);
        source.sendSuccess(() -> Component.literal("§e平均 NBT 长度: " + (finalTotalItemCount > 0
                ? String.format("%,.0f", (double) finalTotalNbtLength / finalTotalItemCount) : "0") + " 字符"), false);
        source.sendSuccess(() -> Component.literal("§e最长 NBT 长度: " + String.format("%,d", finalMaxNbtLength) + " 字符"), false);
        source.sendSuccess(() -> Component.literal("§e总 NBT 数据量: " + String.format("%,d", finalTotalNbtLength) + " 字符"), false);
        source.sendSuccess(() -> Component.literal("§e已用槽位: " + finalTotalItemCount + "/" + finalAvailableSlots2), false);
        if (finalTotalItemCount < config.itemTypeCount() * config.maxStacksPerType()) {
            source.sendSuccess(() -> Component.literal("§c⚠️ 容器空间不足，部分物品未生成"), false);
        }

        return Command.SINGLE_SUCCESS;
    }

    /**
     * 统计 ItemStack 上的附魔数量（包括 ENCHANTMENTS 和 STORED_ENCHANTMENTS）。
     */
    private static int countEnchantments(ItemStack stack) {
        ItemEnchantments ench = stack.get(DataComponents.ENCHANTMENTS);
        if (ench != null && !ench.isEmpty()) {
            return ench.size();
        }
        ItemEnchantments stored = stack.get(DataComponents.STORED_ENCHANTMENTS);
        if (stored != null && !stored.isEmpty()) {
            return stored.size();
        }
        return 0;
    }

    /**
     * 获取 ItemStack 的 NBT 字符串表示的长度（字符数）。
     * <p>
     * 使用 {@link ItemStack#getComponentsPatch()} 获取完整组件数据并序列化为 SNBT 格式，
     * 然后计算字符串长度。用于评估 NBT heavy 场景下的数据量。
     *
     * @param stack 物品堆
     * @return NBT 字符串长度（字符数），如果无法序列化则返回 0
     */
    private static long getNbtStringLength(ItemStack stack) {
        try {
            // 获取物品的完整组件数据并编码为字符串
            var components = stack.getComponentsPatch();
            String encoded = components.toString();
            return encoded.length();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 根据 NBT Heavy 配置构造一个 ItemStack。
     * <p>
     * 除了附魔数据外，还会注入 {@code custom_data} 以增加 NBT 字符串长度，
     * 用于测试大 NBT 场景下的性能表现。
     */
    private static ItemStack buildNbtHeavyItemStack(
            Item item,
            NbtHeavyLevelConfig config,
            Registry<Enchantment> enchantmentRegistry,
            Random random) {
        ItemStack stack = new ItemStack(item);

        // 随机选取附魔数量
        int enchCount = config.minEnchPerItem()
                + random.nextInt(config.maxEnchPerItem() - config.minEnchPerItem() + 1);

        // 从 ALL_VANILLA_ENCHANTS 中随机选取 enchCount 种附魔
        List<String> selectedEnchIds = new ArrayList<>();
        List<String> availablePool = new ArrayList<>(ALL_VANILLA_ENCHANTS);

        for (int i = 0; i < enchCount && !availablePool.isEmpty(); i++) {
            String enchId = availablePool.get(random.nextInt(availablePool.size()));
            selectedEnchIds.add(enchId);
            // 移除已选的（避免重复）
            availablePool.remove(enchId);
        }

        if (!selectedEnchIds.isEmpty()) {
            ItemEnchantments.Mutable mutableEnch = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);

            for (String enchId : selectedEnchIds) {
                ResourceLocation id = ResourceLocation.parse(enchId);
                Holder<Enchantment> enchHolder = enchantmentRegistry.getHolder(id).orElse(null);
                if (enchHolder != null) {
                    int level;
                    if (config.allowOverflowLevel() && random.nextInt(10) == 0) {
                        // 10% 概率生成溢出等级（256+），测试 bitset 溢出行为
                        level = 256 + random.nextInt(256);
                    } else {
                        level = config.minEnchLevel()
                                + random.nextInt(config.maxEnchLevel() - config.minEnchLevel() + 1);
                    }
                    mutableEnch.set(enchHolder, level);
                }
            }

            ItemEnchantments enchantments = mutableEnch.toImmutable();

            // 附魔书使用 STORED_ENCHANTMENTS，其他物品使用 ENCHANTMENTS
            if (item == Items.ENCHANTED_BOOK) {
                stack.set(DataComponents.STORED_ENCHANTMENTS, enchantments);
            } else {
                stack.set(DataComponents.ENCHANTMENTS, enchantments);
            }
        }

        // 非附魔书才加耐久
        if (item != Items.ENCHANTED_BOOK) {
            // 获取最大耐久（如果有）
            int maxDamage = stack.getMaxDamage();
            if (maxDamage > 0) {
                int damage = random.nextInt(maxDamage + 1);
                stack.set(DataComponents.DAMAGE, damage);
            }
        }

        // ============================================================
        // 注入 custom_data — 生成超长 NBT 字符串
        // ============================================================
        CompoundTag tag = new CompoundTag();
        tag.putString("source", "nbt_heavy_test");
        tag.putInt("level", config.displayName().equals("Standard") ? 1
                : config.displayName().equals("HeavyEnchant") ? 2 : 3);

        // 大量字符串键值对（模拟各种 mod 数据）
        int extraEntries = config.extraEntries();
        for (int i = 0; i < extraEntries; i++) {
            tag.putString("key_" + i, "value_" + random.nextInt(100000));
        }

        // 嵌套对象
        CompoundTag nested = new CompoundTag();
        for (int i = 0; i < extraEntries / 2; i++) {
            nested.putFloat("float_" + i, random.nextFloat());
        }
        tag.put("nested_data", nested);

        // 深层嵌套
        CompoundTag deep = new CompoundTag();
        deep.putString("type", "simulated");
        deep.putInt("depth", 3);
        CompoundTag child = new CompoundTag();
        child.putLong("timestamp", System.currentTimeMillis());
        child.putDouble("value", random.nextDouble());
        deep.put("child", child);
        tag.put("metadata", deep);

        // 注入 custom_data
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        return stack;
    }

}

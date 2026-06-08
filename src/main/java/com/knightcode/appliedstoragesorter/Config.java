package com.knightcode.appliedstoragesorter;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLE_SORTER = BUILDER
            .comment("Whether Applied Energistics: Insight is enabled.")
            .define("enableSorter", true);

    public static final ModConfigSpec.IntValue SCAN_INTERVAL_TICKS = BUILDER
            .comment("How often the sorter should scan AE2 drives, in ticks.")
            .defineInRange("scanIntervalTicks", 200, 20, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue MAX_TRANSFERS_PER_OPERATION = BUILDER
            .comment("Maximum transfer steps performed during one sort operation.")
            .defineInRange("maxTransfersPerOperation", 64, 1, Integer.MAX_VALUE);

    public static final ModConfigSpec.BooleanValue VERBOSE_LOGGING = BUILDER
            .comment("Whether to emit verbose debug logging for sorter behavior.")
            .define("verboseLogging", false);

    public static final ModConfigSpec.BooleanValue DEVELOPER_MODE = BUILDER
            .comment("Enable developer mode. When disabled, /sorter commands are restricted.")
            .define("developerMode", false);

    public static final ModConfigSpec.EnumValue<DebugSmartBusUvMode> DEBUG_SMART_BUS_UV = BUILDER
            .comment("Smart Bus UV debug overlay (temporary dev aid). "
                    + "OFF = normal mode textures; "
                    + "LETTERBOX = per-face debug textures (digits 1-6), full stretch UV [0,0,16,16]; "
                    + "SQUARE = same textures, centered [4,4,12,12]. "
                    + "Set back to OFF before release.")
            .defineEnum("debugSmartBusUv", DebugSmartBusUvMode.OFF);

    public static final ModConfigSpec.ConfigValue<String> FILTER_EDITOR_URL = BUILDER
            .comment("Optional override URL for the Smart Bus filter editor. "
                    + "Leave blank to use the bundled offline editor (extracted under config/AppliedStorageSorter/filter-editor/, "
                    + "opened via file:// — no network required).")
            .define("filterEditorUrl", "");

    public static final ModConfigSpec.IntValue SMART_BUS_STACK_TRANSFERS_PER_TICK = BUILDER
            .comment("Maximum stack-sized transfers Smart Bus performs per tick (import or export). "
                    + "Default 12 matches ExtendedAE extended I/O bus at 4 acceleration cards (96 x 8 items).")
            .defineInRange("smartBusStackTransfersPerTick", 12, 1, 64);

    // ── 电量消耗（Energy Cost）配置 ────────────────────────────────

    public static final ModConfigSpec.BooleanValue ENERGY_COST_ENABLED = BUILDER
            .comment("Enable energy cost estimation for sorter operations. "
                    + "When enabled, /sorter merge and /sorter me planAndMove will display "
                    + "an estimated energy cost based on move count, item amount, item types, "
                    + "and Manhattan distance. Default: false (backward compatible).")
            .define("energyCostEnabled", false);

    public static final ModConfigSpec.DoubleValue ENERGY_COST_BASE_FEE = BUILDER
            .comment("Base energy cost per move operation (coefficient α). "
                    + "Formula: totalCost = α×N + β×log₂(1+A) + γ×log₂(1+T) + δ×log₂(1+avgDist).")
            .defineInRange("energyCostBaseFee", 2.0, 0.0, 100.0);

    public static final ModConfigSpec.DoubleValue ENERGY_COST_AMOUNT_COEFF = BUILDER
            .comment("Energy cost coefficient for moved item amount (coefficient β). "
                    + "Applied to log₂(1 + total moved amount).")
            .defineInRange("energyCostAmountCoeff", 20.0, 0.0, 1000.0);

    public static final ModConfigSpec.DoubleValue ENERGY_COST_TYPE_COEFF = BUILDER
            .comment("Energy cost coefficient for distinct item types (coefficient γ). "
                    + "Applied to log₂(1 + distinct item types).")
            .defineInRange("energyCostTypeCoeff", 30.0, 0.0, 1000.0);

    public static final ModConfigSpec.DoubleValue ENERGY_COST_DISTANCE_COEFF = BUILDER
            .comment("Energy cost coefficient for average Manhattan distance (coefficient δ). "
                    + "Applied to log₂(1 + average Manhattan distance) between drives.")
            .defineInRange("energyCostDistanceCoeff", 15.0, 0.0, 1000.0);

    // ── DAV 自动扩容配置 ────────────────────────────────────────────

    public static final ModConfigSpec.BooleanValue DAV_AUTO_EXPAND_ENABLED = BUILDER
            .comment("Whether DAV auto-expansion is globally enabled. When disabled, "
                    + "per-DAV auto-expand toggles are ignored.")
            .define("davAutoExpandEnabled", true);

    public static final ModConfigSpec.DoubleValue DAV_AUTO_EXPAND_BYTES_THRESHOLD = BUILDER
            .comment("Byte usage ratio (0.0–1.0) that triggers DAV auto-expansion. "
                    + "Default 0.80 means expansion triggers at 80%% byte usage.")
            .defineInRange("davAutoExpandBytesThreshold", 0.80, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue DAV_AUTO_EXPAND_TYPES_THRESHOLD = BUILDER
            .comment("Type slot usage ratio (0.0–1.0) that triggers DAV auto-expansion. "
                    + "Default 0.80 means expansion triggers at 80%% type usage.")
            .defineInRange("davAutoExpandTypesThreshold", 0.80, 0.0, 1.0);

    public static final ModConfigSpec.IntValue DAV_AUTO_EXPAND_COOLDOWN_TICKS = BUILDER
            .comment("Minimum ticks between two DAV auto-expansion attempts. Default 200 (10 seconds).")
            .defineInRange("davAutoExpandCooldownTicks", 200, 20, 72000);

    // ── 配置规格 ────────────────────────────────────────────────────

    public static final ModConfigSpec SPEC = BUILDER.build();

    /** Temporary Smart Bus UV debug modes; remove after UV investigation. */
    public enum DebugSmartBusUvMode {
        OFF,
        LETTERBOX,
        SQUARE
    }

    private Config() {
    }
}

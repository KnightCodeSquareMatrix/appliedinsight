package com.knightcode.appliedstoragesorter;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLE_SORTER = BUILDER
            .comment("Whether Applied Storage Sorter is enabled.")
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

    // ── 配置规格 ────────────────────────────────────────────────────

    public static final ModConfigSpec SPEC = BUILDER.build();

    private Config() {
    }
}

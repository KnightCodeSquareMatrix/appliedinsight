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

    public static final ModConfigSpec SPEC = BUILDER.build();

    private Config() {
    }
}

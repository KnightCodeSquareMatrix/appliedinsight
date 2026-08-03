package com.knightcode.appliedstoragesorter.application.result;

import java.util.List;

import net.minecraft.network.chat.Component;

public record SorterStorageAnalysisCommandResult(
        boolean success,
        List<Component> lines,
        String dumpFilePath,
        int storageLocationCount,
        int uniqueKeyCount) {
    public SorterStorageAnalysisCommandResult {
        lines = List.copyOf(lines);
    }

    public static SorterStorageAnalysisCommandResult failure(String message) {
        return new SorterStorageAnalysisCommandResult(false, List.of(Component.literal(message)), null, 0, 0);
    }

    public static SorterStorageAnalysisCommandResult failure(Component message) {
        return new SorterStorageAnalysisCommandResult(false, List.of(message), null, 0, 0);
    }

    public static SorterStorageAnalysisCommandResult success(List<Component> lines, String dumpFilePath, int storageLocationCount,
            int uniqueKeyCount) {
        return new SorterStorageAnalysisCommandResult(true, lines, dumpFilePath, storageLocationCount,
                uniqueKeyCount);
    }
}

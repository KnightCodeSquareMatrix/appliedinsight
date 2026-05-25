package com.knightcode.appliedstoragesorter.ae2.dump;

public record SorterNetworkDumpResult(
        String dumpFilePath,
        int driveCount,
        int mountedCellCount,
        int uniqueItemKeyCount,
        int itemOccurrenceCount) {
}

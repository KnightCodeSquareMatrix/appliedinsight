package com.knightcode.appliedstoragesorter.application.result;

public record SorterDumpCommandResult(
        boolean success,
        String message,
        String dumpFilePath,
        int mountedCellCount,
        int uniqueItemKeyCount) {
    public static SorterDumpCommandResult failure(String message) {
        return new SorterDumpCommandResult(false, message, null, 0, 0);
    }

    public static SorterDumpCommandResult success(String message, String dumpFilePath, int mountedCellCount,
            int uniqueItemKeyCount) {
        return new SorterDumpCommandResult(true, message, dumpFilePath, mountedCellCount, uniqueItemKeyCount);
    }
}

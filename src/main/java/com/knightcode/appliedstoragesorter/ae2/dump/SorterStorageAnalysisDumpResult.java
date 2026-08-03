package com.knightcode.appliedstoragesorter.ae2.dump;

import java.util.List;

public record SorterStorageAnalysisDumpResult(
        String dumpFilePath,
        int storageLocationCount,
        int nonEmptyStorageLocationCount,
        int internalStorageLocationCount,
        int externalStorageLocationCount,
        int uniqueKeyCount,
        int duplicatedKeyCount,
        long totalAmount,
        long externalTotalAmount,
        String fragmentationLevel,
        List<String> healthFlags) {
}

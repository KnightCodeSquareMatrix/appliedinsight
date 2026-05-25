package com.knightcode.appliedstoragesorter.ae2.scan;

public record Ae2DriveScanSummary(
        int driveCount,
        int scannedCellSlotCount,
        int mountedCellCount,
        int uniqueItemKeyCount,
        int duplicatedItemKeyCount,
        int duplicatedCellReferenceCount) {
}

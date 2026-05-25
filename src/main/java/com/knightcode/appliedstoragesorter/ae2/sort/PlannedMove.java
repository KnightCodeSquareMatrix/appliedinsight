package com.knightcode.appliedstoragesorter.ae2.sort;

import appeng.api.stacks.AEItemKey;

import com.knightcode.appliedstoragesorter.ae2.scan.DriveCellReference;

public record PlannedMove(AEItemKey key, long amount, DriveCellReference source, DriveCellReference destination) {
}

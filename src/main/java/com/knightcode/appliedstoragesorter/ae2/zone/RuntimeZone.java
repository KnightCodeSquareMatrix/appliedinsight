package com.knightcode.appliedstoragesorter.ae2.zone;

import java.util.List;

import com.knightcode.appliedstoragesorter.ae2.scan.DriveCellReference;

import appeng.api.stacks.AEItemKey;

public final class RuntimeZone extends ZoneManager {
    public RuntimeZone(String zoneId, String zoneName) {
        super(zoneId, zoneName);
    }

    @Override
    protected ZonePlacementDecision doPlanPlacement(
            DriveCellReference sourceReference,
            AEItemKey itemKey,
            long amount,
            List<RuntimeCell> cells) {
        MoveCandidate bestCandidate = null;

        for (RuntimeCell cell : cells) {
            if (cell.isSameCell(sourceReference)) {
                continue;
            }

            long acceptedAmount = cell.simulateAcceptedAmount(itemKey, amount);
            if (acceptedAmount <= 0) {
                continue;
            }

            MoveCandidate candidate = new MoveCandidate(
                    cell,
                    acceptedAmount,
                    cell.containsExactItem(itemKey),
                    cell.distinctItemKeyCount());

            if (bestCandidate == null || candidate.isBetterThan(bestCandidate)) {
                bestCandidate = candidate;
            }
        }

        if (bestCandidate == null) {
            return ZonePlacementDecision.rejected(zoneId(), "zone has no writable target cell for item");
        }

        return ZonePlacementDecision.accepted(zoneId(), bestCandidate.cell(), bestCandidate.acceptedAmount());
    }

    private record MoveCandidate(
            RuntimeCell cell,
            long acceptedAmount,
            boolean alreadyContainsItem,
            int distinctItemKeyCount) {
        private boolean isBetterThan(MoveCandidate other) {
            if (alreadyContainsItem != other.alreadyContainsItem) {
                return alreadyContainsItem;
            }
            if (acceptedAmount != other.acceptedAmount) {
                return acceptedAmount > other.acceptedAmount;
            }
            if (distinctItemKeyCount != other.distinctItemKeyCount) {
                return distinctItemKeyCount < other.distinctItemKeyCount;
            }
            return cell.reference().slot() < other.cell.reference().slot();
        }
    }
}

package com.knightcode.appliedstoragesorter.ae2.zone;

import java.util.List;

import com.knightcode.appliedstoragesorter.ae2.scan.CellInfo;
import com.knightcode.appliedstoragesorter.ae2.scan.DriveCellReference;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;

public final class RuntimeZone extends ZoneManager {
    public RuntimeZone(String zoneId, String zoneName) {
        super(zoneId, zoneName);
    }

    /**
     * ADR-012: public zone-level API. Finds the best cell in this zone
     * for the given item and simulates acceptance. Consumers no longer
     * iterate cells directly.
     */
    public ZonePlacementDecision acceptItem(DriveCellReference sourceRef, AEItemKey itemKey, long amount) {
        return planPlacement(sourceRef, itemKey, amount);
    }

    @Override
    protected ZonePlacementDecision doPlanPlacement(
            DriveCellReference sourceReference,
            AEItemKey itemKey,
            long amount,
            List<CellInfo> cells) {

        CellInfo bestCell = null;
        long bestAccepted = 0L;
        boolean bestContainsItem = false;
        int bestDistinctCount = Integer.MAX_VALUE;

        for (CellInfo cell : cells) {
            if (cell.reference().equals(sourceReference)) {
                continue;
            }

            // ADR-012: use MEStorage directly, not deprecated CellInfo methods
            long accepted = cell.storage().insert(
                    itemKey, amount, Actionable.SIMULATE,
                    IActionSource.ofMachine(cell.actionHost()));
            if (accepted <= 0) {
                continue;
            }

            boolean containsItem = containsItem(cell, itemKey);

            // Prefer: already contains item > higher accepted > fewer types > lower slot
            boolean better = false;
            if (bestCell == null) {
                better = true;
            } else if (containsItem && !bestContainsItem) {
                better = true;
            } else if (!containsItem && bestContainsItem) {
                better = false;
            } else if (accepted > bestAccepted) {
                better = true;
            } else if (accepted == bestAccepted) {
                int distinct = distinctCount(cell);
                if (distinct < bestDistinctCount) {
                    better = true;
                } else if (distinct == bestDistinctCount
                        && cell.reference().slot() < bestCell.reference().slot()) {
                    better = true;
                }
            }

            if (better) {
                bestCell = cell;
                bestAccepted = accepted;
                bestContainsItem = containsItem;
                bestDistinctCount = distinctCount(cell);
            }
        }

        if (bestCell == null) {
            return ZonePlacementDecision.rejected(zoneId(), "zone has no writable target cell for item");
        }

        return ZonePlacementDecision.accepted(
                zoneId(), bestCell.reference(), bestCell.actionHost(), bestCell.storage(), bestAccepted);
    }

    // ADR-012: direct MEStorage queries, replacing deprecated CellInfo methods

    private static boolean containsItem(CellInfo cell, AEItemKey itemKey) {
        for (var entry : cell.storage().getAvailableStacks()) {
            if (entry.getLongValue() > 0 && itemKey.equals(entry.getKey())) {
                return true;
            }
        }
        return false;
    }

    private static int distinctCount(CellInfo cell) {
        int count = 0;
        for (var entry : cell.storage().getAvailableStacks()) {
            if (entry.getLongValue() > 0) {
                count++;
            }
        }
        return count;
    }
}

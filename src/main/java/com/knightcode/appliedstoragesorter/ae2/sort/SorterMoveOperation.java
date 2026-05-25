package com.knightcode.appliedstoragesorter.ae2.sort;

import java.util.ArrayList;
import java.util.List;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.MEStorage;

public final class SorterMoveOperation {
    private final List<PlannedMove> plannedMoves;
    private final List<ExecutableMove> executableMoves;
    private final long totalPlannedAmount;

    SorterMoveOperation(List<PlannedMove> plannedMoves, List<ExecutableMove> executableMoves, long totalPlannedAmount) {
        this.plannedMoves = List.copyOf(plannedMoves);
        this.executableMoves = List.copyOf(executableMoves);
        this.totalPlannedAmount = totalPlannedAmount;
    }

    public List<PlannedMove> plannedMoves() {
        return plannedMoves;
    }

    public int plannedMoveCount() {
        return plannedMoves.size();
    }

    public long totalPlannedAmount() {
        return totalPlannedAmount;
    }

    public boolean isEmpty() {
        return plannedMoves.isEmpty();
    }

    public SorterMoveExecutionResult execute() {
        int completedMoveCount = 0;
        int failedMoveCount = 0;
        long movedAmount = 0;
        List<SorterMoveExecutionResult.MoveResult> moveResults = new ArrayList<>(executableMoves.size());

        for (ExecutableMove executableMove : executableMoves) {
            long extracted = executableMove.sourceStorage.extract(
                    executableMove.plannedMove.key(),
                    executableMove.plannedMove.amount(),
                    Actionable.MODULATE,
                    IActionSource.ofMachine(executableMove.sourceDrive));

            if (extracted <= 0) {
                failedMoveCount++;
                moveResults.add(new SorterMoveExecutionResult.MoveResult(executableMove.plannedMove, 0L, 0L));
                continue;
            }

            long inserted = executableMove.destinationStorage.insert(
                    executableMove.plannedMove.key(),
                    extracted,
                    Actionable.MODULATE,
                    IActionSource.ofMachine(executableMove.destinationDrive));

            if (inserted < extracted) {
                executableMove.sourceStorage.insert(
                        executableMove.plannedMove.key(),
                        extracted - inserted,
                        Actionable.MODULATE,
                        IActionSource.ofMachine(executableMove.sourceDrive));
            }

            if (inserted == executableMove.plannedMove.amount()) {
                completedMoveCount++;
            } else {
                failedMoveCount++;
            }

            movedAmount += inserted;
            moveResults.add(new SorterMoveExecutionResult.MoveResult(executableMove.plannedMove, extracted, inserted));
        }

        return new SorterMoveExecutionResult(
                executableMoves.size(),
                completedMoveCount,
                failedMoveCount,
                totalPlannedAmount,
                movedAmount,
                List.copyOf(moveResults));
    }

    public static SorterMoveOperation empty() {
        return new SorterMoveOperation(List.of(), List.of(), 0);
    }

    public static SorterMoveOperation of(List<PlannedMove> plannedMoves, List<ExecutableMove> executableMoves,
            long totalPlannedAmount) {
        return new SorterMoveOperation(plannedMoves, executableMoves, totalPlannedAmount);
    }

    public static final class ExecutableMove {
        private final PlannedMove plannedMove;
        private final IActionHost sourceDrive;
        private final IActionHost destinationDrive;
        private final MEStorage sourceStorage;
        private final MEStorage destinationStorage;

        public ExecutableMove(PlannedMove plannedMove, IActionHost sourceDrive, IActionHost destinationDrive,
                MEStorage sourceStorage, MEStorage destinationStorage) {
            this.plannedMove = plannedMove;
            this.sourceDrive = sourceDrive;
            this.destinationDrive = destinationDrive;
            this.sourceStorage = sourceStorage;
            this.destinationStorage = destinationStorage;
        }

        public PlannedMove plannedMove() {
            return plannedMove;
        }

        public IActionHost sourceDrive() {
            return sourceDrive;
        }

        public IActionHost destinationDrive() {
            return destinationDrive;
        }

        public MEStorage sourceStorage() {
            return sourceStorage;
        }

        public MEStorage destinationStorage() {
            return destinationStorage;
        }
    }

    static ExecutableMove executableMove(PlannedMove plannedMove, IActionHost sourceDrive,
            IActionHost destinationDrive, MEStorage sourceStorage, MEStorage destinationStorage) {
        return new ExecutableMove(plannedMove, sourceDrive, destinationDrive, sourceStorage, destinationStorage);
    }
}

package com.knightcode.appliedstoragesorter.ae2.sort;

import com.knightcode.appliedstoragesorter.ae2.scan.DriveCellReference;
import com.knightcode.appliedstoragesorter.ae2.scan.DriveMachineAccessor.DriveMachine;

/**
 * Merge move endpoint rules aligned with AE2 storage semantics.
 *
 * <ul>
 *   <li>Allowed: internal cell → internal cell</li>
 *   <li>Allowed: internal cell → external storage bus</li>
 *   <li>Forbidden: external storage → anywhere (no pulling from drawers/chests via merge)</li>
 * </ul>
 */
public final class MergeEndpointPolicy {
    private MergeEndpointPolicy() {
    }

    public static boolean isMergeSource(DriveMachine drive) {
        return drive != null && !drive.isExternalStorageBus();
    }

    public static boolean isInternalDestination(DriveMachine drive) {
        return drive != null && !drive.isExternalStorageBus();
    }

    public static boolean isExternalDestination(DriveMachine drive) {
        return drive != null && drive.isExternalStorageBus();
    }

    public static boolean isExternalStorage(DriveCellReference reference) {
        return reference != null && reference.isExternalStorage();
    }
}

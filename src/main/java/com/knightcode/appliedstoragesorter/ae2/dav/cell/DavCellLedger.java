package com.knightcode.appliedstoragesorter.ae2.dav.cell;

public record DavCellLedger(long absorbedCellCount, long absorbedBytes, long absorbedTypeCapacity) {
    public static final DavCellLedger EMPTY = new DavCellLedger(0L, 0L, 0L);

    public DavCellLedger addAbsorbed(long addedBytes, long addedTypes) {
        return new DavCellLedger(
                Math.addExact(absorbedCellCount, 1L),
                Math.addExact(absorbedBytes, addedBytes),
                Math.addExact(absorbedTypeCapacity, addedTypes));
    }

    public long totalBytes() {
        return DavCellConstants.totalBytes(this);
    }

    public long totalTypeCapacity() {
        return DavCellConstants.totalTypeCapacity(this);
    }
}

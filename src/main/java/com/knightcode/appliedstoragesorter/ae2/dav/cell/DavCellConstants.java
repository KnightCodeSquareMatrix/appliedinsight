package com.knightcode.appliedstoragesorter.ae2.dav.cell;

public final class DavCellConstants {
    /** Built-in capacity equivalent to two empty AE2 1k item storage cells. */
    public static final long BUILTIN_BYTES = 2048L;
    public static final long BUILTIN_TYPE_CAPACITY = 126L;

    public static final String CUSTOM_DATA_CELL_ID = "dav_cell_id";

    public static final String SAVED_DATA_ID = "appliedinsight_dav_cells";

    private DavCellConstants() {
    }

    public static long totalBytes(DavCellLedger ledger) {
        return BUILTIN_BYTES + ledger.absorbedBytes();
    }

    public static long totalTypeCapacity(DavCellLedger ledger) {
        return BUILTIN_TYPE_CAPACITY + ledger.absorbedTypeCapacity();
    }
}

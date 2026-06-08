package com.knightcode.appliedstoragesorter.client.format;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ByteUnitFormatterTest {

    @Test
    void formatBytesBelowKilobyte() {
        assertEquals("0 B", ByteUnitFormatter.format(0));
        assertEquals("512 B", ByteUnitFormatter.format(512));
        assertEquals("1023 B", ByteUnitFormatter.format(1023));
    }

    @Test
    void formatKilobyteBoundaries() {
        assertEquals("1 KB", ByteUnitFormatter.format(1024));
        assertEquals("1.5 KB", ByteUnitFormatter.format(1536));
        assertEquals("10 KB", ByteUnitFormatter.format(10 * 1024));
    }

    @Test
    void formatHigherUnits() {
        assertEquals("1 MB", ByteUnitFormatter.format(1024L * 1024));
        assertEquals("1 GB", ByteUnitFormatter.format(1024L * 1024 * 1024));
        assertEquals("1 TB", ByteUnitFormatter.format(1024L * 1024 * 1024 * 1024));
        assertEquals("1 PB", ByteUnitFormatter.format(1024L * 1024 * 1024 * 1024 * 1024));
    }

    @Test
    void formatPairUsesIndependentUnits() {
        assertEquals("512 B / 4 KB", ByteUnitFormatter.formatPair(512, 4096));
        assertEquals("1.2 MB / 4.5 MB", ByteUnitFormatter.formatPair(1_258_291, 4_718_592));
    }
}

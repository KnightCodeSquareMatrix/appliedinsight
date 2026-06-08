package com.knightcode.appliedstoragesorter.client.format;

import java.util.Locale;

public final class ByteUnitFormatter {
    private static final long KB = 1L << 10;
    private static final long MB = KB << 10;
    private static final long GB = MB << 10;
    private static final long TB = GB << 10;
    private static final long PB = TB << 10;

    private ByteUnitFormatter() {
    }

    public static String format(long bytes) {
        long absValue = Math.abs(bytes);
        if (absValue < KB) {
            return bytes + " B";
        }
        if (absValue < MB) {
            return formatScaled(bytes, KB, "KB");
        }
        if (absValue < GB) {
            return formatScaled(bytes, MB, "MB");
        }
        if (absValue < TB) {
            return formatScaled(bytes, GB, "GB");
        }
        if (absValue < PB) {
            return formatScaled(bytes, TB, "TB");
        }
        return formatScaled(bytes, PB, "PB");
    }

    public static String formatPair(long used, long total) {
        return format(used) + " / " + format(total);
    }

    private static String formatScaled(long value, long divisor, String suffix) {
        double scaled = (double) value / (double) divisor;
        String pattern = Math.abs(scaled) >= 10.0D ? "%.0f %s" : "%.1f %s";
        String formatted = String.format(Locale.ROOT, pattern, scaled, suffix);
        return formatted.replace(".0 ", " ");
    }
}

package com.knightcode.appliedstoragesorter.client.format;

import java.util.Locale;

public final class CompactNumberFormatter {
    private static final long THOUSAND = 1_000L;
    private static final long MILLION = 1_000_000L;
    private static final long BILLION = 1_000_000_000L;
    private static final long TRILLION = 1_000_000_000_000L;

    private CompactNumberFormatter() {
    }

    public static String format(long value) {
        long absValue = Math.abs(value);
        if (absValue < THOUSAND) {
            return Long.toString(value);
        }
        if (absValue < MILLION) {
            return formatScaled(value, THOUSAND, "k");
        }
        if (absValue < BILLION) {
            return formatScaled(value, MILLION, "m");
        }
        if (absValue < TRILLION) {
            return formatScaled(value, BILLION, "B");
        }
        return formatScaled(value, TRILLION, "T");
    }

    private static String formatScaled(long value, long divisor, String suffix) {
        double scaled = (double) value / (double) divisor;
        String pattern = Math.abs(scaled) >= 10.0D ? "%.0f%s" : "%.1f%s";
        String formatted = String.format(Locale.ROOT, pattern, scaled, suffix);
        return formatted.replace(".0", "");
    }
}

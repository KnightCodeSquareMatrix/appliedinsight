package com.knightcode.appliedstoragesorter.ae2;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Set;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appeng.api.storage.cells.StorageCell;

/**
 * Shared utility for inspecting AE2 StorageCell capacity via reflection.
 *
 * <p>AE2's {@link StorageCell} interface does not expose byte/type capacity directly at compile time;
 * the actual methods ({@code getTotalBytes}, {@code getUsedBytes}, etc.) are on subinterfaces like
 * {@code ICellInventory}. This inspector uses reflection to access them safely, with fallback names
 * for cross-version compatibility.
 *
 * <p>Also provides heuristic detection of infinite/creative cells (e.g. from {@code allthetweaks})
 * that don't expose standard capacity properties.
 *
 * <p>Used by both {@code SorterNetworkDumpWriter} (me-dump) and {@code Ae2StorageAnalyzer}
 * (storage-analysis dump).
 */
public final class CellCapacityInspector {
    private static final Logger log = LoggerFactory.getLogger(CellCapacityInspector.class);

    // Known mod IDs that provide infinite/creative storage cells
    private static final Set<String> INFINITE_CELL_MOD_IDS = Set.of(
            "allthetweaks",
            "kubejs" // KubeJS can register creative-tier cells in modpacks
    );

    // Item path substrings that indicate infinite/creative cells
    private static final Set<String> INFINITE_CELL_PATH_PATTERNS = Set.of(
            "infinite", "creative", "无穷", "创造"
    );

    private CellCapacityInspector() {
    }

    /**
     * Read capacity info from an AE2 StorageCell. Returns null if the cell is null or
     * the reflection fails (e.g. external storage buses).
     */
    public static CellCapacity inspect(StorageCell storageCell) {
        if (storageCell == null) {
            return null;
        }

        Long totalBytes = invokeLong(storageCell, "getTotalBytes", "getBytes");
        Long usedBytes = invokeLong(storageCell, "getUsedBytes");
        Integer totalItemTypes = invokeInt(storageCell, "getTotalTypes", "getTotalItemTypes");
        Integer remainingItemTypes = invokeInt(storageCell, "getRemainingItemTypes");

        if (totalBytes == null || usedBytes == null) {
            return null;
        }

        return new CellCapacity(totalBytes, usedBytes, totalItemTypes, remainingItemTypes);
    }

    /**
     * Derive a human-readable cell kind from totalBytes using known AE2 cell sizes.
     */
    public static String describeCellKind(long totalBytes) {
        if (totalBytes <= 0) {
            return null;
        }
        if (totalBytes <= 1024) return "1k_cell";
        if (totalBytes <= 4096) return "4k_cell";
        if (totalBytes <= 16384) return "16k_cell";
        if (totalBytes <= 65536) return "64k_cell";
        if (totalBytes <= 262144) return "256k_cell";
        return totalBytes + "b_cell";
    }

    // ── Infinite cell detection ──

    /**
     * Determine if a cell is likely an infinite/creative cell based on available signals.
     *
     * <p>Signals used (in order):
     * <ol>
     *   <li>No capacity info ({@link #inspect} returned null)</li>
     *   <li>Cell item mod ID matches known infinite-cell mods</li>
     *   <li>Cell item path contains infinite/creative keywords</li>
     * </ol>
     *
     * @param capacity  result of {@link #inspect}, null means no capacity info
     * @param cellItemId registry name of the cell item, or null if unknown
     * @return true if the cell is highly likely to be infinite/creative
     */
    public static boolean isLikelyInfiniteCell(@Nullable CellCapacity capacity, @Nullable String cellItemId) {
        if (capacity != null) {
            return false; // Has real capacity info → standard cell
        }
        if (cellItemId == null) {
            return false;
        }
        var parts = cellItemId.split(":");
        // Signal 1: mod ID is a known source of infinite cells
        if (parts.length >= 1 && INFINITE_CELL_MOD_IDS.contains(parts[0])) {
            return true;
        }
        // Signal 2: item path contains infinite/creative keywords
        var path = parts.length >= 2 ? parts[1] : parts[0];
        var lower = path.toLowerCase(Locale.ROOT);
        for (var pattern : INFINITE_CELL_PATH_PATTERNS) {
            if (lower.contains(pattern)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Resolve the best cellKind string from capacity info and cell item ID.
     *
     * <p>Priority:
     * <ol>
     *   <li>Standard cell with capacity → AE2 cell kind (e.g. "256k_cell")</li>
     *   <li>No capacity + likely infinite → "infinite:" prefix + item ID</li>
     *   <li>Cell item ID available but unknown type → raw item ID</li>
     *   <li>Nothing available → null</li>
     * </ol>
     *
     * @param capacity  result of {@link #inspect}, may be null
     * @param cellItemId registry name of the cell item, may be null
     * @return best cell kind string, or null if completely unknown
     */
    @Nullable
    public static String resolveCellKind(@Nullable CellCapacity capacity, @Nullable String cellItemId) {
        if (capacity != null) {
            return describeCellKind(capacity.totalBytes());
        }
        if (cellItemId != null) {
            if (isLikelyInfiniteCell(null, cellItemId)) {
                return "infinite:" + cellItemId;
            }
            return cellItemId;
        }
        return null;
    }

    // ── reflection helpers with fallback names ──

    private static Long invokeLong(Object target, String primaryName, String... fallbackNames) {
        Long result = tryInvokeLong(target, primaryName);
        if (result != null) return result;
        for (String name : fallbackNames) {
            result = tryInvokeLong(target, name);
            if (result != null) return result;
        }
        return null;
    }

    private static Long tryInvokeLong(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            Object value = method.invoke(target);
            if (value instanceof Number number) {
                return number.longValue();
            }
            log.debug("Cell capacity method {} returned non-numeric value: {}", methodName, value);
        } catch (NoSuchMethodException e) {
            log.debug("Cell capacity method {} not available on {}", methodName, target.getClass().getSimpleName());
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.warn("Cell capacity method {} invoked failed on {}: {}", methodName, target.getClass().getSimpleName(), e.getMessage());
        }
        return null;
    }

    private static Integer invokeInt(Object target, String primaryName, String... fallbackNames) {
        Integer result = tryInvokeInt(target, primaryName);
        if (result != null) return result;
        for (String name : fallbackNames) {
            result = tryInvokeInt(target, name);
            if (result != null) return result;
        }
        return null;
    }

    private static Integer tryInvokeInt(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            Object value = method.invoke(target);
            if (value instanceof Number number) {
                return number.intValue();
            }
            log.debug("Cell capacity method {} returned non-numeric value: {}", methodName, value);
        } catch (NoSuchMethodException e) {
            log.debug("Cell capacity method {} not available on {}", methodName, target.getClass().getSimpleName());
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.warn("Cell capacity method {} invoked failed on {}: {}", methodName, target.getClass().getSimpleName(), e.getMessage());
        }
        return null;
    }

    /**
     * Raw capacity data for a single AE2 StorageCell.
     */
    public record CellCapacity(
            long totalBytes,
            long usedBytes,
            Integer totalItemTypes,
            Integer remainingItemTypes) {

        public long freeBytes() {
            return totalBytes - usedBytes;
        }

        public double byteUsageRatio() {
            if (totalBytes <= 0) return 0.0;
            return (double) usedBytes / (double) totalBytes;
        }
    }
}

package com.knightcode.appliedstoragesorter.client.format;

import java.util.Map;

/**
 * Parses AE2 storage location IDs (pipe-delimited technical strings) into
 * player-friendly location descriptions.
 *
 * <p>Input format produced by {@code Ae2StorageAnalyzer.buildLocationId()}:
 * {@code dimension|blockId|blockPos|attachedPos|slot=N}
 *
 * <p>Example transformations:
 * <ul>
 *   <li>{@code minecraft:overworld|ae2:drive|x=100,y=64,z=200|<none>|slot=0}
 *       → {@code "ME Drive @ (100, 64, 200)"}</li>
 *   <li>{@code minecraft:the_nether|extendedae:ex_drive|x=50,y=32,z=10|<none>|slot=2}
 *       → {@code "Extended Drive @ (50, 32, 10)"}</li>
 *   <li>{@code minecraft:overworld|ae2:drive|x=10,y=20,z=30|x=40,y=50,z=60|slot=1}
 *       → {@code "ME Drive @ (10, 20, 30) slot 1"}</li>
 * </ul>
 */
public final class LocationIdFormatter {

    private static final String SEPARATOR = "\\|";
    private static final Map<String, String> BLOCK_DISPLAY_NAMES = Map.of(
            "ae2:drive", "ME Drive",
            "extendedae:ex_drive", "Extended Drive",
            "appliedinsight:digital_asset_vault", "DAV");

    private LocationIdFormatter() {
    }

    /**
     * Converts a technical location ID into a short, player-readable label.
     *
     * @param locationId the pipe-delimited location ID from
     *                   {@code StorageLocationSummary.locationId()}
     * @return a friendly string like {@code "ME Drive @ (100, 64, 200)"},
     *         or the original input if it cannot be parsed
     */
    public static String toFriendlyLocation(String locationId) {
        if (locationId == null || locationId.isBlank()) {
            return "<unknown>";
        }

        String[] parts = locationId.split(SEPARATOR, 6);
        if (parts.length < 3) {
            return locationId;
        }

        String blockId = parts[1];
        String blockPos = parts[2];
        String slotPart = parts.length >= 6 ? parts[5] : "";

        String friendlyBlock = BLOCK_DISPLAY_NAMES.getOrDefault(blockId, blockId);
        String friendlyPos = simplifyPos(blockPos);
        String friendlySlot = extractSlotLabel(slotPart);

        if (friendlySlot.isEmpty()) {
            return friendlyBlock + " @ " + friendlyPos;
        }
        return friendlyBlock + " @ " + friendlyPos + " " + friendlySlot;
    }

    /**
     * Converts a dimension-prefixed location ID into a longer form that
     * includes the dimension name.
     *
     * @param locationId the pipe-delimited location ID
     * @return a string like {@code "Overworld / ME Drive @ (100, 64, 200)"},
     *         or the original input if it cannot be parsed
     */
    public static String toDetailedLocation(String locationId) {
        if (locationId == null || locationId.isBlank()) {
            return "<unknown>";
        }

        String[] parts = locationId.split(SEPARATOR, 6);
        if (parts.length < 3) {
            return locationId;
        }

        String dimension = simplifyDimension(parts[0]);
        String blockId = parts[1];
        String blockPos = parts[2];
        String slotPart = parts.length >= 6 ? parts[5] : "";

        String friendlyBlock = BLOCK_DISPLAY_NAMES.getOrDefault(blockId, blockId);
        String friendlyPos = simplifyPos(blockPos);
        String friendlySlot = extractSlotLabel(slotPart);

        StringBuilder sb = new StringBuilder();
        sb.append(dimension).append(" / ").append(friendlyBlock)
                .append(" @ ").append(friendlyPos);
        if (!friendlySlot.isEmpty()) {
            sb.append(" ").append(friendlySlot);
        }
        return sb.toString();
    }

    private static String simplifyPos(String pos) {
        if (pos == null || pos.isBlank() || "<none>".equals(pos)) {
            return "?";
        }
        // Input: "x=100,y=64,z=200"
        // Output: "(100, 64, 200)"
        String cleaned = pos.replace("x=", "")
                .replace("y=", "")
                .replace("z=", "")
                .replace(",", ", ");
        return "(" + cleaned + ")";
    }

    private static String extractSlotLabel(String slotPart) {
        if (slotPart == null || slotPart.isBlank()) {
            return "";
        }
        if (slotPart.startsWith("slot=")) {
            String num = slotPart.substring("slot=".length());
            if (num.matches("\\d+")) {
                int slotNum = Integer.parseInt(num) + 1; // 0-indexed → 1-indexed for players
                return "slot " + slotNum;
            }
        }
        return "";
    }

    private static String simplifyDimension(String dimensionId) {
        if (dimensionId == null || dimensionId.isBlank()) {
            return "Unknown";
        }
        return switch (dimensionId) {
            case "minecraft:overworld" -> "Overworld";
            case "minecraft:the_nether" -> "Nether";
            case "minecraft:the_end" -> "End";
            default -> {
                String[] parts = dimensionId.split(":");
                yield parts.length >= 2 ? parts[1] : dimensionId;
            }
        };
    }
}

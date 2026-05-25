package com.knightcode.appliedstoragesorter.ae2.dump;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.knightcode.appliedstoragesorter.ae2.CellCapacityInspector;
import com.knightcode.appliedstoragesorter.ae2.Ae2ControllerTargetResolver;
import com.knightcode.appliedstoragesorter.ae2.Ae2GridTargetResult;
import com.knightcode.appliedstoragesorter.ae2.scan.Ae2DriveScanSummary;
import com.knightcode.appliedstoragesorter.ae2.scan.DriveMachineAccessor;
import com.knightcode.appliedstoragesorter.logging.ReportFileSupport;

import appeng.api.stacks.AEItemKey;
import appeng.api.storage.cells.StorageCell;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.Tag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;

public final class SorterNetworkDumpWriter {
    private static final Logger log = LoggerFactory.getLogger(SorterNetworkDumpWriter.class);
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().serializeNulls().create();
    private static final Path DUMP_DIR = ReportFileSupport.resolveDumpDir("appliedstoragesorter");

    private SorterNetworkDumpWriter() {
    }

    public static SorterNetworkDumpResult dump(CommandSourceStack source, Ae2GridTargetResult targetResult,
            Ae2DriveScanSummary scanSummary) throws IOException {
        var grid = targetResult.grid();
        var level = source.getLevel();
        var registries = level.registryAccess();

        Map<AEItemKey, AggregatedItemBuilder> itemsByKey = new LinkedHashMap<>();
        List<CellDump> cells = new ArrayList<>();
        int itemOccurrenceCount = 0;

        for (var drive : DriveMachineAccessor.findSupportedDrives(grid)) {
            for (int slot = 0; slot < drive.cellCount(); slot++) {
                var storage = drive.getCellInventory(slot);
                if (storage == null) {
                    continue;
                }

                // Extract cell capacity info from original StorageCell (null for external buses)
                CellCapacityInspector.CellCapacity capacity = null;
                String cellKind = null;
                String zoneId = drive.getDeclaredZoneId().orElse(null);
                if (!drive.isExternalStorageBus()) {
                    StorageCell originalCell = drive.getOriginalCellInventory(slot);
                    if (originalCell != null) {
                        capacity = CellCapacityInspector.inspect(originalCell);
                    }
                    cellKind = CellCapacityInspector.resolveCellKind(
                            capacity, drive.getCellItemId(slot).orElse(null));
                }

                List<CellEntryDump> entries = new ArrayList<>();
                long cellTotalAmount = 0;

                for (var entry : storage.getAvailableStacks()) {
                    if (entry.getLongValue() <= 0 || !(entry.getKey() instanceof AEItemKey itemKey)) {
                        continue;
                    }

                    long amount = entry.getLongValue();
                    cellTotalAmount += amount;
                    itemOccurrenceCount++;

                    var readOnlyStack = itemKey.getReadOnlyStack();
                    var itemId = BuiltInRegistries.ITEM.getKey(readOnlyStack.getItem()).toString();

                    entries.add(new CellEntryDump(
                            itemId,
                            itemKey.getDisplayName().getString(),
                            amount,
                            readOnlyStack.isComponentsPatchEmpty()));

                    itemsByKey.computeIfAbsent(itemKey, key -> AggregatedItemBuilder.fromKey(key, registries))
                            .addOccurrence(drive, slot, amount);
                }

                cells.add(new CellDump(
                        drive.blockId(),
                        Ae2ControllerTargetResolver.formatBlockPos(drive.blockPos()),
                        drive.attachedStoragePos().map(Ae2ControllerTargetResolver::formatBlockPos).orElse(null),
                        drive.isExternalStorageBus(),
                        slot,
                        drive.cellCount(),
                        entries.size(),
                        cellTotalAmount,
                        entries,
                        capacity != null ? capacity.totalBytes() : null,
                        capacity != null ? capacity.usedBytes() : null,
                        capacity != null ? capacity.totalItemTypes() : null,
                        capacity != null ? capacity.remainingItemTypes() : null,
                        cellKind,
                        zoneId));
            }
        }

        var dump = new NetworkDump(
                "1",
                LocalDateTime.now().toString(),
                new CommandDump(
                        source.getTextName(),
                        level.dimension().location().toString(),
                        source.getPosition().x,
                        source.getPosition().y,
                        source.getPosition().z,
                        level.getServer() != null ? level.getServer().getTickCount() : -1),
                new TargetDump(
                        targetResult.targetBlockId(),
                        targetResult.targetPos() != null
                                ? Ae2ControllerTargetResolver.formatBlockPos(targetResult.targetPos())
                                : null,
                        targetResult.controllerPos() != null
                                ? Ae2ControllerTargetResolver.formatBlockPos(targetResult.controllerPos())
                                : null),
                new SummaryDump(
                        grid != null ? grid.size() : 0,
                        scanSummary.driveCount(),
                        scanSummary.scannedCellSlotCount(),
                        scanSummary.mountedCellCount(),
                        scanSummary.uniqueItemKeyCount(),
                        scanSummary.duplicatedItemKeyCount(),
                        scanSummary.duplicatedCellReferenceCount(),
                        itemOccurrenceCount),
                cells,
                itemsByKey.values().stream().map(AggregatedItemBuilder::build).toList());

        var json = GSON.toJson(dump);

        // 文件名中的网络标识，使不同 ME 网络文件可区分
        var slug = ReportFileSupport.networkSlug(targetResult.dimensionId(), targetResult.controllerPos());

        // 时间戳版本（历史存档）
        var fileName = ReportFileSupport.timestampedFileName("me-dump-" + slug + "-", ".json");
        var dumpPath = DUMP_DIR.resolve(fileName);
        ReportFileSupport.writeTextFile(
                DUMP_DIR,
                fileName,
                json,
                log,
                "Failed to write sorter network dump {}"
        );

        // latest 版本（始终覆盖，供前端动态读取）
        var latestName = ReportFileSupport.latestFileName("me-dump-" + slug + "-", ".json");
        ReportFileSupport.writeTextFile(
                DUMP_DIR,
                latestName,
                json,
                log,
                "Failed to write latest network dump {}"
        );

        return new SorterNetworkDumpResult(
                ReportFileSupport.relativizeGamePath(dumpPath),
                scanSummary.driveCount(),
                scanSummary.mountedCellCount(),
                scanSummary.uniqueItemKeyCount(),
                itemOccurrenceCount);
    }

    private static final class AggregatedItemBuilder {
        private final String itemId;
        private final String modId;
        private final String displayName;
        private final boolean componentsPatchEmpty;
        private final int maxStackSize;
        private final List<String> tags;
        private final String serializedStackNbt;
        private long totalAmount;
        private final List<ItemOccurrenceDump> occurrences = new ArrayList<>();

        private AggregatedItemBuilder(String itemId, String modId, String displayName, boolean componentsPatchEmpty,
                int maxStackSize, List<String> tags, String serializedStackNbt) {
            this.itemId = itemId;
            this.modId = modId;
            this.displayName = displayName;
            this.componentsPatchEmpty = componentsPatchEmpty;
            this.maxStackSize = maxStackSize;
            this.tags = tags;
            this.serializedStackNbt = serializedStackNbt;
        }

        private static AggregatedItemBuilder fromKey(AEItemKey key, net.minecraft.core.HolderLookup.Provider registries) {
            ItemStack stack = key.getReadOnlyStack();
            ItemStack normalizedStack = normalizeForDump(stack);
            String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            String modId = stack.getItem().builtInRegistryHolder().key().location().getNamespace();
            List<String> tags = normalizedStack.getTags().map(TagKey::location).map(Object::toString).sorted().toList();
            String serializedStackNbt = serializeStackForDump(normalizedStack, registries, itemId);

            return new AggregatedItemBuilder(
                    itemId,
                    modId,
                    key.getDisplayName().getString(),
                    stack.isComponentsPatchEmpty(),
                    stack.getMaxStackSize(),
                    tags,
                    serializedStackNbt);
        }

        private void addOccurrence(DriveMachineAccessor.DriveMachine drive, int slot, long amount) {
            totalAmount += amount;
            occurrences.add(new ItemOccurrenceDump(
                    drive.blockId(),
                    Ae2ControllerTargetResolver.formatBlockPos(drive.blockPos()),
                    drive.attachedStoragePos().map(Ae2ControllerTargetResolver::formatBlockPos).orElse(null),
                    drive.isExternalStorageBus(),
                    slot,
                    amount));
        }

        private ItemDump build() {
            return new ItemDump(itemId, modId, displayName, totalAmount, occurrences.size(), componentsPatchEmpty,
                    maxStackSize, tags, serializedStackNbt, occurrences);
        }
    }

    private static ItemStack normalizeForDump(ItemStack stack) {
        ItemStack normalizedStack = stack.copy();
        if (!normalizedStack.isEmpty()) {
            normalizedStack.setCount(1);
        }
        return normalizedStack;
    }

    private static String serializeStackForDump(ItemStack stack,
            net.minecraft.core.HolderLookup.Provider registries,
            String itemId) {
        try {
            return toSnbt(stack.saveOptional(registries));
        } catch (RuntimeException exception) {
            log.warn(
                    "Failed to serialize stack for sorter dump, falling back to item id only: {}",
                    itemId,
                    exception);
            return "{id:\"" + itemId + "\"}";
        }
    }

    private static String toSnbt(Tag tag) {
        return tag.toString();
    }

    private record NetworkDump(
            String formatVersion,
            String generatedAt,
            CommandDump command,
            TargetDump target,
            SummaryDump summary,
            List<CellDump> cells,
            List<ItemDump> items) {
    }

    private record CommandDump(
            String executor,
            String dimension,
            double x,
            double y,
            double z,
            int serverTime) {
    }

    private record TargetDump(
            String targetBlockId,
            String targetPos,
            String controllerPos) {
    }

    private record SummaryDump(
            int networkNodeCount,
            int driveCount,
            int scannedCellSlotCount,
            int mountedCellCount,
            int uniqueItemKeyCount,
            int duplicatedItemKeyCount,
            int duplicatedCellReferenceCount,
            int itemOccurrenceCount) {
    }

    private record CellDump(
            String sourceBlockId,
            String drivePos,
            String attachedStoragePos,
            boolean externalStorageBus,
            int slot,
            int driveCellCount,
            int distinctItemKeyCount,
            long totalAmount,
            List<CellEntryDump> entries,
            Long totalBytes,
            Long usedBytes,
            Integer totalItemTypes,
            Integer remainingItemTypes,
            String cellKind,
            String zoneId) {
    }

    private record CellEntryDump(
            String itemId,
            String displayName,
            long amount,
            boolean componentsPatchEmpty) {
    }

    private record ItemDump(
            String itemId,
            String modId,
            String displayName,
            long totalAmount,
            int occurrenceCount,
            boolean componentsPatchEmpty,
            int maxStackSize,
            List<String> tags,
            String serializedStackNbt,
            List<ItemOccurrenceDump> occurrences) {
    }

    private record ItemOccurrenceDump(
            String sourceBlockId,
            String drivePos,
            String attachedStoragePos,
            boolean externalStorageBus,
            int slot,
            long amount) {
    }
}

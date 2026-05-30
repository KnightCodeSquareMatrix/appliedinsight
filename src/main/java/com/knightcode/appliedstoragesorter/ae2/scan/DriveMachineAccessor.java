package com.knightcode.appliedstoragesorter.ae2.scan;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.knightcode.appliedstoragesorter.blockentity.DigitalAssetVaultBlockEntity;

import appeng.api.implementations.blockentities.IChestOrDrive;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionHost;
import appeng.api.storage.MEStorage;
import appeng.api.storage.cells.StorageCell;
import appeng.parts.storagebus.StorageBusPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class DriveMachineAccessor {
    private static final Set<String> SUPPORTED_DRIVE_BLOCK_IDS = Set.of(
            "ae2:drive",
            "extendedae:ex_drive",
            "appliedstoragesorter:digital_asset_vault");
    private static final String STORAGE_BUS_BLOCK_ID = "ae2:storage_bus";

    private DriveMachineAccessor() {
    }

    public static List<DriveMachine> findSupportedDrives(IGrid grid) {
        Map<String, DriveMachine> drivesByKey = new LinkedHashMap<>();

        for (Class<?> machineClass : grid.getMachineClasses()) {
            for (Object machine : getMachinesUnchecked(grid, machineClass)) {
                var drive = tryCreate(machine);
                if (drive != null) {
                    drivesByKey.putIfAbsent(drive.uniqueKey(), drive);
                }
            }
        }

        return List.copyOf(drivesByKey.values());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static Set<?> getMachinesUnchecked(IGrid grid, Class<?> machineClass) {
        return grid.getMachines((Class) machineClass);
    }

    private static DriveMachine tryCreate(Object machine) {
        if (machine instanceof StorageBusPart storageBusPart) {
            return tryCreateStorageBus(storageBusPart);
        }
        if (machine instanceof BlockEntity blockEntity) {
            return tryCreateDrive(blockEntity);
        }
        return null;
    }

    /**
     * Creates a {@link DriveMachine} from a drive block entity.
     * <p>
     * Prefers the compile-time {@link IChestOrDrive} interface when available.
     * Falls back to reflection for block entities that do not implement the
     * interface.
     */
    private static DriveMachine tryCreateDrive(BlockEntity blockEntity) {
        if (!(blockEntity instanceof IActionHost actionHost)) {
            return null;
        }

        var blockId = BuiltInRegistries.BLOCK.getKey(blockEntity.getBlockState().getBlock()).toString();
        if (!SUPPORTED_DRIVE_BLOCK_IDS.contains(blockId)) {
            return null;
        }

        // Priority path: IChestOrDrive interface (compile-time safe)
        if (blockEntity instanceof IChestOrDrive chestOrDrive) {
            int cellCount = chestOrDrive.getCellCount();
            return new DriveMachine(
                    blockEntity,
                    actionHost,
                    blockId,
                    cellCount,
                    null,
                    null,
                    chestOrDrive,
                    false);
        }

        // Fallback path: reflection for block entities that don't implement IChestOrDrive
        var getCellCount = findZeroArgMethod(blockEntity.getClass(), "getCellCount");
        var getOriginalCellInventory = findSingleIntMethod(blockEntity.getClass(), "getOriginalCellInventory");
        var getCellInventory = findSingleIntMethod(blockEntity.getClass(), "getCellInventory");
        if (getCellCount == null || getOriginalCellInventory == null || getCellInventory == null) {
            return null;
        }

        try {
            int cellCount = ((Number) getCellCount.invoke(blockEntity)).intValue();
            return new DriveMachine(
                    blockEntity,
                    actionHost,
                    blockId,
                    cellCount,
                    null,
                    null,
                    getOriginalCellInventory,
                    getCellInventory,
                    false);
        } catch (IllegalAccessException | InvocationTargetException | ClassCastException exception) {
            return null;
        }
    }

    private static DriveMachine tryCreateStorageBus(StorageBusPart storageBusPart) {
        BlockEntity hostBlockEntity = storageBusPart.getBlockEntity();
        Direction side = storageBusPart.getSide();
        if (hostBlockEntity == null || side == null || !(storageBusPart instanceof IActionHost actionHost)) {
            return null;
        }

        return new DriveMachine(
                hostBlockEntity,
                actionHost,
                STORAGE_BUS_BLOCK_ID,
                1,
                hostBlockEntity.getBlockPos().relative(side),
                side,
                null,
                null,
                true) {
            @Override
            public MEStorage getCellInventory(int slot) {
                return slot == 0 ? storageBusPart.getInternalHandler() : null;
            }
        };
    }

    // ── Reflection fallback helpers ──────────────────────────────────────────

    private static Method findZeroArgMethod(Class<?> type, String name) {
        try {
            var method = type.getMethod(name);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException exception) {
            return null;
        }
    }

    private static Method findSingleIntMethod(Class<?> type, String name) {
        try {
            var method = type.getMethod(name, int.class);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException exception) {
            return null;
        }
    }

    public static class DriveMachine {
        private final BlockEntity blockEntity;
        private final IActionHost actionHost;
        private final String blockId;
        private final int cellCount;
        private final @Nullable BlockPos attachedStoragePos;
        private final @Nullable Direction attachmentSide;
        // Non-null when using IChestOrDrive API path
        private final @Nullable IChestOrDrive chestOrDrive;
        // Non-null when using reflection fallback path
        private final @Nullable Method getOriginalCellInventory;
        private final @Nullable Method getCellInventory;
        private final boolean externalStorageBus;

        /**
         * Constructor for the IChestOrDrive API path.
         */
        private DriveMachine(BlockEntity blockEntity, IActionHost actionHost, String blockId, int cellCount,
                @Nullable BlockPos attachedStoragePos, @Nullable Direction attachmentSide,
                @Nullable IChestOrDrive chestOrDrive,
                boolean externalStorageBus) {
            this.blockEntity = blockEntity;
            this.actionHost = actionHost;
            this.blockId = blockId;
            this.cellCount = cellCount;
            this.attachedStoragePos = attachedStoragePos;
            this.attachmentSide = attachmentSide;
            this.chestOrDrive = chestOrDrive;
            this.getOriginalCellInventory = null;
            this.getCellInventory = null;
            this.externalStorageBus = externalStorageBus;
        }

        /**
         * Constructor for the reflection fallback path.
         */
        private DriveMachine(BlockEntity blockEntity, IActionHost actionHost, String blockId, int cellCount,
                @Nullable BlockPos attachedStoragePos, @Nullable Direction attachmentSide,
                @Nullable Method getOriginalCellInventory,
                @Nullable Method getCellInventory,
                boolean externalStorageBus) {
            this.blockEntity = blockEntity;
            this.actionHost = actionHost;
            this.blockId = blockId;
            this.cellCount = cellCount;
            this.attachedStoragePos = attachedStoragePos;
            this.attachmentSide = attachmentSide;
            this.chestOrDrive = null;
            this.getOriginalCellInventory = getOriginalCellInventory;
            this.getCellInventory = getCellInventory;
            this.externalStorageBus = externalStorageBus;
        }

        public String uniqueKey() {
            List<String> parts = new ArrayList<>();
            parts.add(blockPos().toShortString());
            parts.add(blockId);
            attachedStoragePos().ifPresent(pos -> parts.add(pos.toShortString()));
            attachmentSide().ifPresent(side -> parts.add(side.getSerializedName()));
            return String.join("|", parts);
        }

        public BlockEntity blockEntity() {
            return blockEntity;
        }

        public BlockPos blockPos() {
            return blockEntity.getBlockPos();
        }

        public IActionHost actionHost() {
            return actionHost;
        }

        public int cellCount() {
            return cellCount;
        }

        public String blockId() {
            return blockId;
        }

        public boolean isExternalStorageBus() {
            return externalStorageBus;
        }

        public Optional<BlockPos> attachedStoragePos() {
            return Optional.ofNullable(attachedStoragePos);
        }

        public Optional<Direction> attachmentSide() {
            return Optional.ofNullable(attachmentSide);
        }

        public Optional<String> attachedStorageBlockId() {
            if (attachedStoragePos == null || blockEntity.getLevel() == null) {
                return Optional.empty();
            }
            var state = blockEntity.getLevel().getBlockState(attachedStoragePos);
            return Optional.of(BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
        }

        public Optional<String> getDeclaredZoneId() {
            if (blockEntity instanceof DigitalAssetVaultBlockEntity vault) {
                return vault.getDeclaredZoneId();
            }

            return Optional.empty();
        }

        /**
         * Get the registry name of the cell item in the given slot.
         * Uses {@link IChestOrDrive#getCellItem(int)} when available,
         * falls back to reflection.
         * Returns empty for external storage buses or if the method is unavailable.
         */
        public Optional<String> getCellItemId(int slot) {
            if (externalStorageBus) {
                return Optional.empty();
            }

            // Priority path: IChestOrDrive API
            if (chestOrDrive != null) {
                var item = chestOrDrive.getCellItem(slot);
                if (item != null) {
                    return Optional.of(BuiltInRegistries.ITEM.getKey(item).toString());
                }
                return Optional.empty();
            }

            // Fallback path: reflection
            try {
                var method = blockEntity.getClass().getMethod("getCellItem", int.class);
                method.setAccessible(true);
                var item = (Item) method.invoke(blockEntity, slot);
                if (item != null) {
                    return Optional.of(BuiltInRegistries.ITEM.getKey(item).toString());
                }
            } catch (Exception e) {
                // Method not available or invocation failed
            }
            return Optional.empty();
        }

        public @Nullable StorageCell getOriginalCellInventory(int slot) {
            // Priority path: IChestOrDrive API
            if (chestOrDrive != null) {
                return chestOrDrive.getOriginalCellInventory(slot);
            }

            // Fallback path: reflection
            if (getOriginalCellInventory == null) {
                return null;
            }
            try {
                return (StorageCell) getOriginalCellInventory.invoke(blockEntity, slot);
            } catch (IllegalAccessException | InvocationTargetException | ClassCastException exception) {
                return null;
            }
        }

        public @Nullable MEStorage getCellInventory(int slot) {
            // Priority path: IChestOrDrive API
            if (chestOrDrive != null) {
                return chestOrDrive.getCellInventory(slot);
            }

            // Fallback path: reflection
            if (getCellInventory == null) {
                return null;
            }
            try {
                return (MEStorage) getCellInventory.invoke(blockEntity, slot);
            } catch (IllegalAccessException | InvocationTargetException | ClassCastException exception) {
                return null;
            }
        }
    }
}

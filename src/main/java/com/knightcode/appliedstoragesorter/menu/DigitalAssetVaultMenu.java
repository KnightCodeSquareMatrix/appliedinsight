package com.knightcode.appliedstoragesorter.menu;

import appeng.api.storage.StorageCells;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantic;
import appeng.menu.SlotSemantics;
import com.knightcode.appliedstoragesorter.blockentity.DigitalAssetVaultBlockEntity;
import com.knightcode.appliedstoragesorter.menu.slot.StorageCellSlot;
import com.knightcode.appliedstoragesorter.registry.SorterBlocks;
import com.knightcode.appliedstoragesorter.registry.SorterMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DigitalAssetVaultMenu extends AEBaseMenu {
    public static final SlotSemantic INPUT_CELL = SlotSemantics.register(
            "AppliedStorageSorter_INPUT_CELL", true, 501);

    public static final int RIGHT_SECTION_X = 134;
    public static final int RIGHT_COL_X = RIGHT_SECTION_X + 6;
    public static final int GUI_WIDTH = 226;
    public static final int RIGHT_COL_WIDTH = GUI_WIDTH - RIGHT_COL_X - 8;

    public static final int SLOT_LABEL_X = RIGHT_SECTION_X + 8;
    public static final int SLOT_LABEL_Y = 28;
    public static final int INPUT_SLOT_X = 167;
    public static final int INPUT_SLOT_Y = 44;

    public static final int ABSORPTION_TEXT_X = RIGHT_COL_X;
    public static final int STAT_TEXT_MAX_WIDTH = RIGHT_COL_WIDTH;
    public static final int ABSORPTION_STAT_1_Y = 66;
    public static final int ABSORPTION_BYTES_VALUE_Y = 76;
    public static final int ABSORPTION_TYPES_TOTAL_Y = 86;
    public static final int ABSORPTION_TYPES_USED_Y = 96;

    public static final int EXPANSION_LABEL_Y = 110;
    public static final int EXPANSION_SLOT_X = INPUT_SLOT_X;
    public static final int EXPANSION_SLOT_Y = 120;
    public static final int EXPANSION_SLOT_SIZE = 18;

    public static final int INDICATOR_Y = 140;
    public static final int STATUS_Y = 150;
    public static final int PLAYER_INV_Y = 162;

    private static final int PLAYER_INV_START = 1;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 27;
    private static final int HOTBAR_START = PLAYER_INV_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;

    private final DigitalAssetVaultBlockEntity blockEntity;
    private final ContainerLevelAccess access;
    private long absorbedCellCount;
    private long absorbedBytes;
    private long absorbedTypeCapacity;
    private long usedBytes;
    private long usedTypeCapacity;
    private boolean migrateExistingItems;
    private boolean autoAcceptIncoming;
    private boolean autoExpandEnabled;
    private String expansionCellId = "";
    private boolean expansionCellValid;
    private boolean expansionCellCraftable;
    private int statusOrdinal;

    public DigitalAssetVaultMenu(int containerId, Inventory playerInventory,
            DigitalAssetVaultBlockEntity blockEntity) {
        super(SorterMenus.DIGITAL_ASSET_VAULT_MENU.get(), containerId, playerInventory, blockEntity);
        this.blockEntity = blockEntity;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());

        addSlot(new StorageCellSlot(blockEntity.getInputInventory().toItemHandler(), 0, 0, 0), INPUT_CELL);
        createPlayerInventorySlots(playerInventory);
        addCounterDataSlots();
        updateLocalDataFromBlockEntity();
    }

    public static DigitalAssetVaultMenu fromNetwork(int containerId, Inventory playerInventory, BlockPos pos,
            boolean autoExpandEnabled, String expansionCellId,
            boolean expansionCellValid, boolean expansionCellCraftable) {
        Level level = playerInventory.player.level();
        if (!(level.getBlockEntity(pos) instanceof DigitalAssetVaultBlockEntity blockEntity)) {
            throw new IllegalStateException("Expected DigitalAssetVaultBlockEntity at " + pos);
        }
        var menu = new DigitalAssetVaultMenu(containerId, playerInventory, blockEntity);
        menu.autoExpandEnabled = autoExpandEnabled;
        menu.expansionCellId = expansionCellId;
        menu.expansionCellValid = expansionCellValid;
        menu.expansionCellCraftable = expansionCellCraftable;
        return menu;
    }

    public DigitalAssetVaultBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public long getAbsorbedCellCount() {
        return absorbedCellCount;
    }

    public long getAbsorbedBytes() {
        return absorbedBytes;
    }

    public long getAbsorbedTypeCapacity() {
        return absorbedTypeCapacity;
    }

    public long getUsedBytes() {
        return usedBytes;
    }

    public long getUsedTypeCapacity() {
        return usedTypeCapacity;
    }

    public boolean isMigrateExistingItems() {
        return migrateExistingItems;
    }

    public boolean isAutoAcceptIncoming() {
        return autoAcceptIncoming;
    }

    public boolean isAutoExpandEnabled() {
        return autoExpandEnabled;
    }

    public String getExpansionCellId() {
        return expansionCellId;
    }

    public boolean isExpansionCellValid() {
        return expansionCellValid;
    }

    public boolean isExpansionCellCraftable() {
        return expansionCellCraftable;
    }

    public DigitalAssetVaultBlockEntity.Status getStatus() {
        return DigitalAssetVaultBlockEntity.Status.fromOrdinal(statusOrdinal);
    }

    @Override
    public void broadcastChanges() {
        updateLocalDataFromBlockEntity();
        super.broadcastChanges();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack moved = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        moved = stack.copy();

        if (index == 0) {
            if (!moveItemStackTo(stack, PLAYER_INV_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (StorageCells.isCellHandled(stack)) {
            if (!moveItemStackTo(stack, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index >= PLAYER_INV_START && index < PLAYER_INV_END) {
            if (!moveItemStackTo(stack, HOTBAR_START, HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index >= HOTBAR_START && index < HOTBAR_END) {
            if (!moveItemStackTo(stack, PLAYER_INV_START, PLAYER_INV_END, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == moved.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stack);
        return moved;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, SorterBlocks.DIGITAL_ASSET_VAULT.get());
    }

    private void addCounterDataSlots() {
        addLongDataSlots(() -> absorbedCellCount, value -> absorbedCellCount = value);
        addLongDataSlots(() -> absorbedBytes, value -> absorbedBytes = value);
        addLongDataSlots(() -> absorbedTypeCapacity, value -> absorbedTypeCapacity = value);
        addLongDataSlots(() -> usedBytes, value -> usedBytes = value);
        addLongDataSlots(() -> usedTypeCapacity, value -> usedTypeCapacity = value);
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return migrateExistingItems ? 1 : 0;
            }

            @Override
            public void set(int value) {
                migrateExistingItems = value != 0;
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return autoAcceptIncoming ? 1 : 0;
            }

            @Override
            public void set(int value) {
                autoAcceptIncoming = value != 0;
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return autoExpandEnabled ? 1 : 0;
            }

            @Override
            public void set(int value) {
                autoExpandEnabled = value != 0;
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return expansionCellValid ? 1 : 0;
            }

            @Override
            public void set(int value) {
                expansionCellValid = value != 0;
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return expansionCellCraftable ? 1 : 0;
            }

            @Override
            public void set(int value) {
                expansionCellCraftable = value != 0;
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return statusOrdinal;
            }

            @Override
            public void set(int value) {
                statusOrdinal = value;
            }
        });
    }

    private void addLongDataSlots(LongGetter getter, LongSetter setter) {
        for (int part = 0; part < 4; part++) {
            final int shift = part * 16;
            addDataSlot(new DataSlot() {
                @Override
                public int get() {
                    return (int) ((getter.get() >> shift) & 0xFFFFL);
                }

                @Override
                public void set(int value) {
                    long mask = 0xFFFFL << shift;
                    long current = getter.get();
                    long updated = (current & ~mask) | ((long) (value & 0xFFFF) << shift);
                    setter.set(updated);
                }
            });
        }
    }

    private void updateLocalDataFromBlockEntity() {
        absorbedCellCount = blockEntity.getAbsorbedCellCount();
        absorbedBytes = blockEntity.getAbsorbedBytes();
        absorbedTypeCapacity = blockEntity.getAbsorbedTypeCapacity();
        usedBytes = blockEntity.getUsedBytes();
        usedTypeCapacity = blockEntity.getUsedTypeCapacity();
        migrateExistingItems = blockEntity.isMigrateExistingItems();
        autoAcceptIncoming = blockEntity.isAutoAcceptIncoming();
        autoExpandEnabled = blockEntity.isAutoExpandEnabled();
        expansionCellId = blockEntity.getExpansionCellId();
        expansionCellValid = blockEntity.isExpansionCellValid();
        expansionCellCraftable = blockEntity.isExpansionCellCraftable();
        statusOrdinal = blockEntity.getLastStatus().ordinal();
    }

    @FunctionalInterface
    private interface LongGetter {
        long get();
    }

    @FunctionalInterface
    private interface LongSetter {
        void set(long value);
    }
}

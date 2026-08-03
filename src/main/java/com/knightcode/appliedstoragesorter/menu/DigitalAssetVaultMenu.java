package com.knightcode.appliedstoragesorter.menu;

import appeng.api.storage.StorageCells;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantic;
import appeng.menu.SlotSemantics;
import com.knightcode.appliedstoragesorter.blockentity.DigitalAssetVaultBlockEntity;
import com.knightcode.appliedstoragesorter.ae2.dav.cell.DavCellStack;
import com.knightcode.appliedstoragesorter.menu.slot.DavCellSlot;
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
            "appliedinsight_INPUT_CELL", true, 501);
    public static final SlotSemantic BUILT_IN_DAV_CELL = SlotSemantics.register(
            "appliedinsight_BUILT_IN_DAV_CELL", true, 502);

    public static final int RIGHT_SECTION_X = 182;
    public static final int RIGHT_COL_X = RIGHT_SECTION_X + 6;
    public static final int GUI_WIDTH = 360;
    public static final int RIGHT_COL_WIDTH = GUI_WIDTH - RIGHT_COL_X - 8;

    public static final int SLOT_LABEL_X = RIGHT_SECTION_X + 8;
    public static final int SLOT_LABEL_Y = 28;
    public static final int ABSORPTION_HINT_Y = 38;
    public static final int BUILT_IN_DAV_CELL_SLOT_X = 230;
    public static final int BUILT_IN_DAV_CELL_SLOT_Y = 46;
    public static final int INPUT_SLOT_X = 258;
    public static final int INPUT_SLOT_Y = 46;

    public static final int ABSORPTION_TEXT_X = RIGHT_COL_X;
    public static final int STAT_TEXT_MAX_WIDTH = RIGHT_COL_WIDTH;
    public static final int ABSORPTION_STAT_1_Y = 70;
    public static final int ABSORPTION_BYTES_VALUE_Y = 83;
    public static final int ABSORPTION_TYPES_TOTAL_Y = 96;
    public static final int ABSORPTION_TYPES_USED_Y = 109;

    public static final int EXPANSION_LABEL_Y = 124;
    public static final int EXPANSION_SLOT_X = INPUT_SLOT_X;
    public static final int EXPANSION_SLOT_Y = 136;
    public static final int EXPANSION_SLOT_SIZE = 18;

    public static final int LEFT_COL_X = 14;
    public static final int LEFT_COL_WIDTH = 158;
    public static final int TOGGLE_HEIGHT = 18;
    public static final int EXPAND_ONCE_BUTTON_X = LEFT_COL_X;
    public static final int EXPAND_ONCE_BUTTON_Y = 92;
    public static final int EXPAND_ONCE_BUTTON_WIDTH = LEFT_COL_WIDTH;
    public static final int EXPAND_ONCE_BUTTON_HEIGHT = TOGGLE_HEIGHT;
    public static final int MIGRATE_TO_SQL_BUTTON_X = LEFT_COL_X;
    public static final int MIGRATE_TO_SQL_BUTTON_Y = EXPAND_ONCE_BUTTON_Y + EXPAND_ONCE_BUTTON_HEIGHT + 4;
    public static final int MIGRATE_TO_SQL_BUTTON_WIDTH = LEFT_COL_WIDTH;
    public static final int MIGRATE_TO_SQL_BUTTON_HEIGHT = TOGGLE_HEIGHT;

    public static final int INDICATOR_Y = 158;
    public static final int STATUS_X = 14;
    public static final int STATUS_Y = 162;
    public static final int STATUS_WIDTH = GUI_WIDTH - 28;
    public static final int STATUS_MAX_LINES = 3;
    public static final int STATUS_LINE_HEIGHT = 9;
    public static final int PLAYER_INV_Y = 184;

    private static final int INPUT_CELL_SLOT_INDEX = 0;
    private static final int BUILT_IN_DAV_CELL_SLOT_INDEX = 1;
    private static final int PLAYER_INV_START = 2;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 27;
    private static final int HOTBAR_START = PLAYER_INV_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;
    /** Max synced chars for expansion cell item id (e.g. ae2:item_storage_cell_64k). */
    private static final int EXPANSION_CELL_ID_MAX_LENGTH = 64;
    private static final int EXPANSION_CELL_ID_CHAR_SLOTS = 32;

    /** Max synced chars for expand-once detail (missing ingredients summary). */
    private static final int EXPAND_ONCE_DETAIL_MAX_LENGTH = 64;
    private static final int EXPAND_ONCE_DETAIL_CHAR_SLOTS = 32;

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
    private String expandOnceDetail = "";
    private int syncedExpandOnceDetailLength;
    private final int[] syncedExpandOnceDetailChars = new int[EXPAND_ONCE_DETAIL_CHAR_SLOTS];
    private int syncedExpansionCellIdLength;
    private final int[] syncedExpansionCellIdChars = new int[EXPANSION_CELL_ID_CHAR_SLOTS];

    public DigitalAssetVaultMenu(int containerId, Inventory playerInventory,
            DigitalAssetVaultBlockEntity blockEntity) {
        super(SorterMenus.DIGITAL_ASSET_VAULT_MENU.get(), containerId, playerInventory, blockEntity);
        this.blockEntity = blockEntity;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());

        addSlot(new StorageCellSlot(blockEntity.getInputInventory().toItemHandler(), 0, 0, 0), INPUT_CELL);
        addSlot(new DavCellSlot(blockEntity.getBuiltInCellInventory().toItemHandler(), 0, 0, 0),
                BUILT_IN_DAV_CELL);
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

    /** Immediate client preview after JEI/EMI drop; server state follows via data slots. */
    public void clientPreviewExpansionCell(String cellId, boolean valid) {
        expansionCellId = cellId != null ? cellId : "";
        expansionCellValid = valid;
    }

    public void refreshFromBlockEntity() {
        updateLocalDataFromBlockEntity();
    }

    public DigitalAssetVaultBlockEntity.Status getStatus() {
        return DigitalAssetVaultBlockEntity.Status.fromOrdinal(statusOrdinal);
    }

    public String getExpandOnceDetail() {
        return expandOnceDetail;
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

        if (index == BUILT_IN_DAV_CELL_SLOT_INDEX || index == INPUT_CELL_SLOT_INDEX) {
            if (!moveItemStackTo(stack, PLAYER_INV_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (DavCellStack.isDavCell(stack)) {
            if (!moveItemStackTo(stack, BUILT_IN_DAV_CELL_SLOT_INDEX, BUILT_IN_DAV_CELL_SLOT_INDEX + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (StorageCells.isCellHandled(stack)) {
            if (!moveItemStackTo(stack, INPUT_CELL_SLOT_INDEX, INPUT_CELL_SLOT_INDEX + 1, false)) {
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
        addExpansionCellIdDataSlots();
        addExpandOnceDetailDataSlots();
    }

    private void addExpansionCellIdDataSlots() {
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return Math.min(expansionCellId.length(), EXPANSION_CELL_ID_MAX_LENGTH);
            }

            @Override
            public void set(int value) {
                syncedExpansionCellIdLength = Math.max(0, Math.min(value, EXPANSION_CELL_ID_MAX_LENGTH));
                applySyncedExpansionCellId();
            }
        });
        for (int i = 0; i < EXPANSION_CELL_ID_CHAR_SLOTS; i++) {
            final int index = i;
            addDataSlot(new DataSlot() {
                @Override
                public int get() {
                    if (index >= expansionCellId.length()) {
                        return 0;
                    }
                    return expansionCellId.charAt(index);
                }

                @Override
                public void set(int value) {
                    syncedExpansionCellIdChars[index] = value & 0xFFFF;
                    applySyncedExpansionCellId();
                }
            });
        }
    }

    private void applySyncedExpansionCellId() {
        if (syncedExpansionCellIdLength <= 0) {
            expansionCellId = "";
            return;
        }
        var builder = new StringBuilder(syncedExpansionCellIdLength);
        for (int i = 0; i < syncedExpansionCellIdLength && i < EXPANSION_CELL_ID_CHAR_SLOTS; i++) {
            char c = (char) syncedExpansionCellIdChars[i];
            if (c == 0) {
                break;
            }
            builder.append(c);
        }
        expansionCellId = builder.toString();
    }

    private void addExpandOnceDetailDataSlots() {
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return Math.min(expandOnceDetail.length(), EXPAND_ONCE_DETAIL_MAX_LENGTH);
            }

            @Override
            public void set(int value) {
                syncedExpandOnceDetailLength = Math.max(0, Math.min(value, EXPAND_ONCE_DETAIL_MAX_LENGTH));
                applySyncedExpandOnceDetail();
            }
        });
        for (int i = 0; i < EXPAND_ONCE_DETAIL_CHAR_SLOTS; i++) {
            final int index = i;
            addDataSlot(new DataSlot() {
                @Override
                public int get() {
                    if (index >= expandOnceDetail.length()) {
                        return 0;
                    }
                    return expandOnceDetail.charAt(index);
                }

                @Override
                public void set(int value) {
                    syncedExpandOnceDetailChars[index] = value & 0xFFFF;
                    applySyncedExpandOnceDetail();
                }
            });
        }
    }

    private void applySyncedExpandOnceDetail() {
        if (syncedExpandOnceDetailLength <= 0) {
            expandOnceDetail = "";
            return;
        }
        var builder = new StringBuilder(syncedExpandOnceDetailLength);
        for (int i = 0; i < syncedExpandOnceDetailLength && i < EXPAND_ONCE_DETAIL_CHAR_SLOTS; i++) {
            char c = (char) syncedExpandOnceDetailChars[i];
            if (c == 0) {
                break;
            }
            builder.append(c);
        }
        expandOnceDetail = builder.toString();
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
        expandOnceDetail = blockEntity.getExpandOnceDetail();
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

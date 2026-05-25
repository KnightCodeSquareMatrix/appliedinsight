package com.knightcode.appliedstoragesorter.menu;

import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantic;
import appeng.menu.SlotSemantics;
import com.knightcode.appliedstoragesorter.blockentity.DigitalAssetVaultBlockEntity;
import com.knightcode.appliedstoragesorter.menu.slot.DigitalAssetManagementCardSlot;
import com.knightcode.appliedstoragesorter.menu.slot.StorageCellSlot;
import com.knightcode.appliedstoragesorter.registry.SorterMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class DigitalAssetVaultMenu extends AEBaseMenu {
    // --- Slot semantic for the management card (registered at class load) ---
    public static final SlotSemantic MANAGEMENT_CARD = SlotSemantics.register(
            "APPLIEDSTORAGESORTER_MANAGEMENT_CARD", false, 500);

    // --- GUI layout constants ---
    private static final int SLOT_SIZE = 18;
    private static final int CELL_COLS = 5;
    private static final int CELL_ROWS = 2;

    // Cell grid: top-left at (26, 18), 5 cols x 2 rows
    private static final int CELL_AREA_X = 26;
    private static final int CELL_AREA_Y = 18;

    // Management card: single slot at (134, 18)
    private static final int CARD_SLOT_X = 134;
    private static final int CARD_SLOT_Y = 18;

    // Player inventory: starts at (8, 67) for 3x9 grid, hotbar at (8, 125)
    private static final int PLAYER_INV_X = 8;
    private static final int PLAYER_INV_Y = 67;
    private static final int PLAYER_HOTBAR_Y = 125;

    public DigitalAssetVaultMenu(int id, Inventory playerInventory, DigitalAssetVaultBlockEntity blockEntity) {
        super(SorterMenus.DIGITAL_ASSET_VAULT_MENU.get(), id, playerInventory, blockEntity);

        addCellSlots(blockEntity);
        addManagementCardSlot(blockEntity);
        addPlayerInventorySlots(playerInventory);
    }

    public static DigitalAssetVaultMenu fromNetwork(int id, Inventory playerInventory, BlockPos pos) {
        var level = playerInventory.player.level();
        if (!(level.getBlockEntity(pos) instanceof DigitalAssetVaultBlockEntity blockEntity)) {
            throw new IllegalStateException("Expected Digital Asset Vault block entity at " + pos);
        }
        return new DigitalAssetVaultMenu(id, playerInventory, blockEntity);
    }

    private void addCellSlots(DigitalAssetVaultBlockEntity blockEntity) {
        var cellHandler = blockEntity.getInternalInventory().toItemHandler();
        for (int row = 0; row < CELL_ROWS; row++) {
            for (int col = 0; col < CELL_COLS; col++) {
                int slotIndex = row * CELL_COLS + col;
                addSlot(new StorageCellSlot(cellHandler, slotIndex,
                        CELL_AREA_X + col * SLOT_SIZE,
                        CELL_AREA_Y + row * SLOT_SIZE),
                        SlotSemantics.STORAGE_CELL);
            }
        }
    }

    private void addManagementCardSlot(DigitalAssetVaultBlockEntity blockEntity) {
        addSlot(new DigitalAssetManagementCardSlot(blockEntity.getCardInventory().toItemHandler(), 0,
                CARD_SLOT_X, CARD_SLOT_Y),
                MANAGEMENT_CARD);
    }

    private void addPlayerInventorySlots(Inventory playerInventory) {
        // Main inventory: 3 rows x 9 cols (player inventory slots 9-35)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int index = col + row * 9 + 9;
                addSlot(new Slot(playerInventory, index,
                        PLAYER_INV_X + col * SLOT_SIZE,
                        PLAYER_INV_Y + row * SLOT_SIZE),
                        SlotSemantics.PLAYER_INVENTORY);
            }
        }
        // Hotbar: 1 row x 9 cols (player inventory slots 0-8)
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col,
                    PLAYER_INV_X + col * SLOT_SIZE,
                    PLAYER_HOTBAR_Y),
                    SlotSemantics.PLAYER_HOTBAR);
        }
    }
}

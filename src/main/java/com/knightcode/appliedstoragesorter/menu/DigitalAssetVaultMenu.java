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
            "APPLIEDSTORAGESORTER_MANAGEMENT_CARD", true, 500);

    // --- GUI layout constants ---
    private static final int SLOT_SIZE = 18;
    private static final int CELL_COLS = 2;
    private static final int CELL_ROWS = 5;

    /**
     * Cell 槽位布局：垂直排列，从 (71, 8) 开始，2 列 x 5 行。
     * DAV 使用垂直布局（5行2列），与 AE2 Drive 的水平布局（2行5列）不同。
     */
    private static final int CELL_AREA_X = 71;
    private static final int CELL_AREA_Y = 8;

    /**
     * 管理卡槽位：位于卡槽面板内（纹理 y=201..222），与 Drive 主体分离。
     * x=152 在卡槽面板内的管理卡槽专属位置，y=203 对应纹理中槽位边框顶部。
     */
    private static final int CARD_SLOT_X = 152;
    private static final int CARD_SLOT_Y = 203;

    /**
     * 玩家背包：与 AE2 Drive 的 common/player_inventory.json 布局一致。
     * 从 (8, 84) 开始，3 行 x 9 列，快捷栏在 (8, 142)。
     */
    private static final int PLAYER_INV_X = 8;
    private static final int PLAYER_INV_Y = 84;
    private static final int PLAYER_HOTBAR_Y = 142;

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

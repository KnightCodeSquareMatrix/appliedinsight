package com.knightcode.appliedstoragesorter.client.screen;

import com.knightcode.appliedstoragesorter.menu.DigitalAssetVaultMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class DigitalAssetVaultScreen extends AbstractContainerScreen<DigitalAssetVaultMenu> {
    /**
     * AE2 Drive 纹理，尺寸 256x256，有效区域 176x201。
     * DAV 继承自 DriveBlockEntity，UI 复用 Drive 的纹理以保持视觉一致性。
     */
    private static final ResourceLocation CONTAINER_TEXTURE = ResourceLocation.parse(
            "appliedstoragesorter:textures/gui/digital_asset_vault.png");

    /** DAV 纹理中 cell 槽区域的列数（2 列 x 5 行，DAV 使用垂直布局） */
    private static final int CELL_ROWS = 5;

    /** Drive 纹理中玩家背包区域的起始 Y（从 common/player_inventory.json 继承） */
    private static final int PLAYER_INV_SECTION_Y = 84;

    /** Drive 纹理尺寸常量（176x201 主体 + 176x22 卡槽面板 = 总高 223） */
    private static final int TEXTURE_WIDTH = 176;
    private static final int TEXTURE_HEIGHT = 223;

    /** 卡槽面板在纹理中的起始 Y 和高度 */
    private static final int CARD_PANEL_START_Y = 201;
    private static final int CARD_PANEL_HEIGHT = 22;

    public DigitalAssetVaultScreen(DigitalAssetVaultMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = TEXTURE_WIDTH;
        this.imageHeight = TEXTURE_HEIGHT;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // 1. 绘制 Drive 上半部分（标题 + cell 槽区域）：y=0..84
        guiGraphics.blit(CONTAINER_TEXTURE, leftPos, topPos, 0, 0, TEXTURE_WIDTH, PLAYER_INV_SECTION_Y);
        // 2. 绘制 Drive 下半部分（玩家背包区域）：y=84..201
        guiGraphics.blit(CONTAINER_TEXTURE, leftPos, topPos + PLAYER_INV_SECTION_Y, 0, PLAYER_INV_SECTION_Y,
                TEXTURE_WIDTH, CARD_PANEL_START_Y - PLAYER_INV_SECTION_Y);
        // 3. 绘制卡槽面板（管理卡槽）：y=201..223
        guiGraphics.blit(CONTAINER_TEXTURE, leftPos, topPos + CARD_PANEL_START_Y, 0, CARD_PANEL_START_Y,
                TEXTURE_WIDTH, CARD_PANEL_HEIGHT);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}

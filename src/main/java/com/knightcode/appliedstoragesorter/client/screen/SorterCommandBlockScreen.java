package com.knightcode.appliedstoragesorter.client.screen;

import com.knightcode.appliedstoragesorter.menu.SorterCommandBlockMenu;
import com.knightcode.appliedstoragesorter.network.SorterCommandPayload;

import appeng.client.gui.widgets.AE2Button;
import appeng.client.gui.widgets.OpenGuideButton;
import appeng.items.tools.GuideItem;
import guideme.GuidesCommon;
import guideme.PageAnchor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class SorterCommandBlockScreen extends AbstractContainerScreen<SorterCommandBlockMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse(
            "appliedstoragesorter:textures/gui/sorter_command_block.png");
    private static final int PANEL_HEIGHT = 145;
    private static final int PANEL_WIDTH = 176;

    public SorterCommandBlockScreen(SorterCommandBlockMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = PANEL_WIDTH;
        this.imageHeight = PANEL_HEIGHT;
        this.inventoryLabelY = 10000; // 移出屏幕，隐藏"Inventory"标签
    }

    @Override
    protected void init() {
        super.init();

        // 按钮坐标相对于面板左上角 (leftPos, topPos)
        addRenderableWidget(new AE2Button(
                leftPos + 28, topPos + 20, 120, 20,
                Component.literal("§e[ME] 扫描网络"),
                btn -> PacketDistributor.sendToServer(new SorterCommandPayload(SorterCommandPayload.CMD_ME_DUMP))));

        addRenderableWidget(new AE2Button(
                leftPos + 28, topPos + 44, 120, 20,
                Component.literal("§e[ME] 存储分析"),
                btn -> PacketDistributor.sendToServer(new SorterCommandPayload(SorterCommandPayload.CMD_ME_STORAGE_DUMP))));

        addRenderableWidget(new AE2Button(
                leftPos + 28, topPos + 68, 120, 20,
                Component.literal("§b[规划] 生成计划"),
                btn -> PacketDistributor.sendToServer(new SorterCommandPayload(SorterCommandPayload.CMD_ME_PLAN))));

        addRenderableWidget(new AE2Button(
                leftPos + 28, topPos + 92, 120, 20,
                Component.literal("§b[执行] 规划并搬运"),
                btn -> PacketDistributor.sendToServer(new SorterCommandPayload(SorterCommandPayload.CMD_ME_PLAN_AND_MOVE))));

        addRenderableWidget(new AE2Button(
                leftPos + 28, topPos + 116, 120, 20,
                Component.literal("§a[Merge] 整理合并"),
                btn -> PacketDistributor.sendToServer(new SorterCommandPayload(SorterCommandPayload.CMD_MERGE))));

        // GuideME "?" 按钮 —— 右上角，点击打开教程首页
        addRenderableWidget(new OpenGuideButton(btn -> {
            var player = getMinecraft().player;
            if (player != null) {
                GuidesCommon.openGuide(player, GuideItem.GUIDE_ID,
                        PageAnchor.page(ResourceLocation.parse("appliedstoragesorter:index.md")));
            }
        }));
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, PANEL_WIDTH, PANEL_HEIGHT);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // 只绘制标题，不绘制玩家背包标签
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
    }
}

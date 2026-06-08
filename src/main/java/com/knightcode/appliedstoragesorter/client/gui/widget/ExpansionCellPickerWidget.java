package com.knightcode.appliedstoragesorter.client.gui.widget;

import com.knightcode.appliedstoragesorter.client.screen.ExpansionCellOperations;
import com.knightcode.appliedstoragesorter.menu.DigitalAssetVaultMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class ExpansionCellPickerWidget extends AbstractWidget {
    private final DigitalAssetVaultMenu menu;

    public ExpansionCellPickerWidget(int x, int y, DigitalAssetVaultMenu menu) {
        super(x, y, DigitalAssetVaultMenu.EXPANSION_SLOT_SIZE, DigitalAssetVaultMenu.EXPANSION_SLOT_SIZE,
                Component.translatable("screen.appliedinsight.digital_asset_vault.expansion_cell"));
        this.menu = menu;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(getX(), getY(), getX() + width, getY() + height, 0xFF101010);
        guiGraphics.fill(getX(), getY(), getX() + width, getY() + 1, 0xFF4A5A66);
        guiGraphics.fill(getX(), getY() + height - 1, getX() + width, getY() + height, 0xFF4A5A66);
        guiGraphics.fill(getX(), getY(), getX() + 1, getY() + height, 0xFF4A5A66);
        guiGraphics.fill(getX() + width - 1, getY(), getX() + width, getY() + height, 0xFF4A5A66);

        ItemStack displayStack = resolveDisplayStack();
        if (!displayStack.isEmpty()) {
            guiGraphics.renderItem(displayStack, getX() + 1, getY() + 1);
            guiGraphics.renderItemDecorations(Minecraft.getInstance().font, displayStack, getX() + 1, getY() + 1);
        }
    }

    private ItemStack resolveDisplayStack() {
        String cellId = menu.getExpansionCellId();
        if (cellId.isEmpty()) {
            return ItemStack.EMPTY;
        }
        try {
            var item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(cellId));
            return item != null ? new ItemStack(item) : ItemStack.EMPTY;
        } catch (Exception ignored) {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered && active && visible && button == 1) {
            ExpansionCellOperations.clear(menu);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }
}

package com.knightcode.appliedstoragesorter.integration.emi;

import com.knightcode.appliedstoragesorter.client.screen.ExpansionCellOperations;
import com.knightcode.appliedstoragesorter.client.screen.DigitalAssetVaultScreen;
import dev.emi.emi.api.EmiDragDropHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ExpansionCellEmiDragDropHandler implements EmiDragDropHandler<Screen> {
    private static final int DROP_HIGHLIGHT_COLOR = 0x8822BB33;

    @Override
    public boolean dropStack(Screen screen, EmiIngredient emiIngredient, int x, int y) {
        if (!(screen instanceof DigitalAssetVaultScreen davScreen)) {
            return false;
        }
        var bounds = ExpansionCellOperations.dropBounds(davScreen);
        if (!bounds.contains(x, y)) {
            return false;
        }
        List<ItemStack> stacks = new ArrayList<>();
        for (EmiStack emiStack : emiIngredient.getEmiStacks()) {
            stacks.add(emiStack.getItemStack());
        }
        return ExpansionCellOperations.firstAcceptedStack(stacks)
                .map(stack -> {
                    ExpansionCellOperations.apply(davScreen.getMenu(), stack);
                    return true;
                })
                .orElse(false);
    }

    @Override
    public void render(Screen screen, EmiIngredient dragged, GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        if (!(screen instanceof DigitalAssetVaultScreen davScreen)) {
            return;
        }
        var ghostStack = dragged.getEmiStacks().stream()
                .map(EmiStack::getItemStack)
                .filter(ExpansionCellOperations::accepts)
                .findFirst();
        if (ghostStack.isEmpty()) {
            return;
        }
        var bounds = ExpansionCellOperations.dropBounds(davScreen);
        graphics.fill(bounds.getX(), bounds.getY(), bounds.getX() + bounds.getWidth(),
                bounds.getY() + bounds.getHeight(), DROP_HIGHLIGHT_COLOR);
        graphics.renderFakeItem(ghostStack.get(), bounds.getX() + 1, bounds.getY() + 1);
    }
}

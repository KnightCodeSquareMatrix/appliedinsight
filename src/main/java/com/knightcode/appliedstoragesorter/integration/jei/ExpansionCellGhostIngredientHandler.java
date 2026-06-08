package com.knightcode.appliedstoragesorter.integration.jei;

import com.knightcode.appliedstoragesorter.client.screen.ExpansionCellOperations;
import com.knightcode.appliedstoragesorter.client.screen.DigitalAssetVaultScreen;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ExpansionCellGhostIngredientHandler implements IGhostIngredientHandler<DigitalAssetVaultScreen> {
    @Override
    public <I> List<Target<I>> getTargetsTyped(DigitalAssetVaultScreen gui, ITypedIngredient<I> ingredient,
            boolean doStart) {
        if (ingredient.getType() != VanillaTypes.ITEM_STACK) {
            return List.of();
        }
        ItemStack stack = ingredient.getIngredient(VanillaTypes.ITEM_STACK).orElse(ItemStack.EMPTY);
        if (!ExpansionCellOperations.accepts(stack)) {
            return List.of();
        }

        Rect2i area = ExpansionCellOperations.dropBounds(gui);
        if (area.getWidth() <= 0 || area.getHeight() <= 0) {
            return List.of();
        }

        return List.of(new Target<I>() {
            @Override
            public Rect2i getArea() {
                return area;
            }

            @Override
            public void accept(I accepted) {
                if (accepted instanceof ItemStack acceptedStack) {
                    ExpansionCellOperations.apply(gui.getMenu(), acceptedStack);
                }
            }
        });
    }

    @Override
    public void onComplete() {
    }
}

package com.knightcode.appliedstoragesorter.recipe;

import com.knightcode.appliedstoragesorter.item.DigitalAssetManagementCardItem;
import com.knightcode.appliedstoragesorter.registry.SorterItems;
import com.knightcode.appliedstoragesorter.registry.SorterRecipeSerializers;

import appeng.api.ids.AEComponents;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public final class ZoneStampedManagementCardRecipe extends CustomRecipe {
    private static final String NAME_PRESS_ITEM_ID = "ae2:name_press";

    public ZoneStampedManagementCardRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return findInputs(input) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        RecipeInputs recipeInputs = findInputs(input);
        if (recipeInputs == null) {
            return ItemStack.EMPTY;
        }

        String zoneText = recipeInputs.zoneText();
        if (zoneText == null) {
            return ItemStack.EMPTY;
        }

        ItemStack result = recipeInputs.blankCard().copyWithCount(1);
        DigitalAssetManagementCardItem.setZoneData(result, zoneText, zoneText);
        return result;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> remainingItems = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        RecipeInputs recipeInputs = findInputs(input);
        if (recipeInputs != null) {
            remainingItems.set(recipeInputs.namePressSlot(), recipeInputs.namePress().copyWithCount(1));
        }
        return remainingItems;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SorterRecipeSerializers.ZONE_STAMPED_MANAGEMENT_CARD.get();
    }

    private static RecipeInputs findInputs(CraftingInput input) {
        ItemStack blankCard = ItemStack.EMPTY;
        ItemStack namePress = ItemStack.EMPTY;
        int namePressSlot = -1;

        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }

            if (stack.is(SorterItems.DIGITAL_ASSET_MANAGEMENT_CARD.get())
                    && !DigitalAssetManagementCardItem.hasZoneData(stack)) {
                if (!blankCard.isEmpty()) {
                    return null;
                }
                blankCard = stack;
                continue;
            }

            if (isInscribedNamePress(stack)) {
                if (!namePress.isEmpty()) {
                    return null;
                }
                namePress = stack;
                namePressSlot = slot;
                continue;
            }

            return null;
        }

        if (blankCard.isEmpty() || namePress.isEmpty() || namePressSlot < 0) {
            return null;
        }

        String zoneText = readZoneText(namePress);
        if (zoneText == null) {
            return null;
        }

        return new RecipeInputs(blankCard, namePress, namePressSlot, zoneText);
    }

    private static boolean isInscribedNamePress(ItemStack stack) {
        ResourceLocation itemId = stack.getItemHolder().unwrapKey().orElseThrow().location();
        if (!NAME_PRESS_ITEM_ID.equals(itemId.toString())) {
            return false;
        }

        return readZoneText(stack) != null;
    }

    private static String readZoneText(ItemStack namePress) {
        var component = namePress.get(AEComponents.NAME_PRESS_NAME);
        if (component == null) {
            return null;
        }

        String value = component.getString().trim();
        return value.isEmpty() ? null : value;
    }

    private record RecipeInputs(
            ItemStack blankCard,
            ItemStack namePress,
            int namePressSlot,
            String zoneText) {
    }
}

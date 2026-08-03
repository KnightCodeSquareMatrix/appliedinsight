package com.knightcode.appliedstoragesorter.recipe;

import com.knightcode.appliedstoragesorter.ae2.dav.cell.DavCellStack;
import com.knightcode.appliedstoragesorter.registry.SorterRecipeSerializers;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Blank DAV Cell + identified DAV Cell → two DAV Cells sharing the source cell ID.
 */
public class DavCellCopyRecipe extends CustomRecipe {
    public DavCellCopyRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return resolveIdentifiedCellId(input) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        UUID cellId = resolveIdentifiedCellId(input);
        if (cellId == null) {
            return ItemStack.EMPTY;
        }
        var result = DavCellStack.createWithCellId(cellId);
        result.setCount(2);
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SorterRecipeSerializers.DAV_CELL_COPY.get();
    }

    @Nullable
    private static UUID resolveIdentifiedCellId(CraftingInput input) {
        boolean hasBlank = false;
        UUID identifiedId = null;
        int davCellCount = 0;

        for (int slot = 0; slot < input.size(); slot++) {
            var stack = input.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (!DavCellStack.isDavCell(stack)) {
                return null;
            }
            davCellCount++;
            if (DavCellStack.isBlank(stack)) {
                hasBlank = true;
                continue;
            }
            UUID cellId = DavCellStack.getCellId(stack);
            if (cellId == null) {
                return null;
            }
            if (identifiedId != null && !identifiedId.equals(cellId)) {
                return null;
            }
            identifiedId = cellId;
        }

        if (davCellCount != 2 || !hasBlank || identifiedId == null) {
            return null;
        }
        return identifiedId;
    }
}

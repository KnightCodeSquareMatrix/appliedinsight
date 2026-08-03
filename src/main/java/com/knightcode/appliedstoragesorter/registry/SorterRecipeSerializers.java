package com.knightcode.appliedstoragesorter.registry;

import com.knightcode.appliedstoragesorter.AppliedStorageSorter;
import com.knightcode.appliedstoragesorter.recipe.DavCellCopyRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class SorterRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister
            .create(Registries.RECIPE_SERIALIZER, AppliedStorageSorter.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<DavCellCopyRecipe>> DAV_CELL_COPY =
            RECIPE_SERIALIZERS.register("dav_cell_copy",
                    () -> new SimpleCraftingRecipeSerializer<>(DavCellCopyRecipe::new));

    private SorterRecipeSerializers() {
    }

    public static void register(IEventBus eventBus) {
        RECIPE_SERIALIZERS.register(eventBus);
    }
}

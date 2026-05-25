package com.knightcode.appliedstoragesorter.registry;

import com.knightcode.appliedstoragesorter.AppliedStorageSorter;
import com.knightcode.appliedstoragesorter.recipe.ZoneStampedManagementCardRecipe;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class SorterRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister
            .create(Registries.RECIPE_SERIALIZER, AppliedStorageSorter.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ZoneStampedManagementCardRecipe>> ZONE_STAMPED_MANAGEMENT_CARD = RECIPE_SERIALIZERS
            .register(
                    "zone_stamped_management_card",
                    () -> new net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer<>(
                            ZoneStampedManagementCardRecipe::new));

    private SorterRecipeSerializers() {
    }

    public static void register(IEventBus eventBus) {
        RECIPE_SERIALIZERS.register(eventBus);
    }
}

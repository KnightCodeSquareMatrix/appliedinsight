package com.knightcode.appliedstoragesorter.registry;

import com.knightcode.appliedstoragesorter.AppliedStorageSorter;
import com.knightcode.appliedstoragesorter.blockentity.SorterCommandBlockEntity;
import com.knightcode.appliedstoragesorter.menu.DigitalAssetVaultMenu;
import com.knightcode.appliedstoragesorter.menu.SorterCommandBlockMenu;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class SorterMenus {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU,
            AppliedStorageSorter.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<DigitalAssetVaultMenu>> DIGITAL_ASSET_VAULT_MENU = MENU_TYPES
            .register("digital_asset_vault",
                    () -> IMenuTypeExtension.create((containerId, playerInventory, buffer) -> DigitalAssetVaultMenu
                            .fromNetwork(containerId, playerInventory, buffer.readBlockPos())));

    public static final DeferredHolder<MenuType<?>, MenuType<SorterCommandBlockMenu>> SORTER_COMMAND_BLOCK_MENU = MENU_TYPES
            .register("sorter_command_block",
                    () -> IMenuTypeExtension.create((containerId, playerInventory, buffer) -> {
                        BlockPos pos = buffer.readBlockPos();
                        Level level = playerInventory.player.level();
                        if (level.getBlockEntity(pos) instanceof SorterCommandBlockEntity be) {
                            return new SorterCommandBlockMenu(containerId, playerInventory, be);
                        }
                        throw new IllegalStateException("Expected SorterCommandBlockEntity at " + pos);
                    }));

    private SorterMenus() {
    }

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }
}

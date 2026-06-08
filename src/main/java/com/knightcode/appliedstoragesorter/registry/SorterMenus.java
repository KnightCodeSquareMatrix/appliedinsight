package com.knightcode.appliedstoragesorter.registry;

import com.knightcode.appliedstoragesorter.AppliedStorageSorter;
import com.knightcode.appliedstoragesorter.blockentity.SorterCommandBlockEntity;
import com.knightcode.appliedstoragesorter.menu.DigitalAssetVaultMenu;
import com.knightcode.appliedstoragesorter.menu.SmartBusMenu;
import com.knightcode.appliedstoragesorter.menu.SorterCommandBlockMenu;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class SorterMenus {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU,
            AppliedStorageSorter.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<DigitalAssetVaultMenu>> DIGITAL_ASSET_VAULT_MENU = MENU_TYPES
            .register("digital_asset_vault",
                    () -> IMenuTypeExtension.create((containerId, playerInventory, buffer) -> {
                        var pos = buffer.readBlockPos();
                        var autoExpand = buffer.readBoolean();
                        var cellId = buffer.readUtf();
                        var cellValid = buffer.readBoolean();
                        var cellCraftable = buffer.readBoolean();
                        return DigitalAssetVaultMenu.fromNetwork(containerId, playerInventory, pos,
                                autoExpand, cellId, cellValid, cellCraftable);
                    }));

    public static final DeferredHolder<MenuType<?>, MenuType<SmartBusMenu>> SMART_BUS = MENU_TYPES
            .register("smart_bus",
                    () -> IMenuTypeExtension.create((containerId, playerInventory, buffer) ->
                            SmartBusMenu.fromNetwork(containerId, playerInventory, buffer.readBlockPos(),
                                    Direction.values()[buffer.readByte()])));

    public static final DeferredHolder<MenuType<?>, MenuType<SorterCommandBlockMenu>> SORTER_COMMAND_BLOCK_MENU = MENU_TYPES
            .register("sorter_command_block", () -> IMenuTypeExtension.create(
                    (containerId, playerInventory, buffer) -> SorterCommandBlockMenu.fromNetwork(
                            containerId,
                            playerInventory,
                            buffer.readBlockPos(),
                            SorterCommandBlockEntity.NetworkStatus.values()[buffer.readVarInt()])));

    private SorterMenus() {
    }

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }
}

package com.knightcode.appliedstoragesorter.client;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.menu.AEBaseMenu;
import com.knightcode.appliedstoragesorter.client.gui.style.SorterStyleManager;
import com.knightcode.appliedstoragesorter.client.screen.DigitalAssetVaultScreen;
import com.knightcode.appliedstoragesorter.client.screen.SmartBusScreen;
import com.knightcode.appliedstoragesorter.client.screen.SorterCommandBlockScreen;
import com.knightcode.appliedstoragesorter.registry.SorterMenus;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public final class SorterInitScreens {
    private SorterInitScreens() {
    }

    public static void register(RegisterMenuScreensEvent event) {
        register(event, SorterMenus.DIGITAL_ASSET_VAULT_MENU.get(), DigitalAssetVaultScreen::new,
                "/screens/digital_asset_vault.json");
        register(event, SorterMenus.SMART_BUS.get(), SmartBusScreen::new, "/screens/smart_bus.json");
        register(event, SorterMenus.SORTER_COMMAND_BLOCK_MENU.get(), SorterCommandBlockScreen::new,
                "/screens/sorter_command_block.json");
    }

    public static <M extends AEBaseMenu, U extends AEBaseScreen<M>> void register(
            RegisterMenuScreensEvent event,
            net.minecraft.world.inventory.MenuType<M> type,
            StyledScreenFactory<M, U> factory,
            String stylePath) {
        event.<M, U>register(type, (menu, playerInv, title) -> {
            ScreenStyle style = SorterStyleManager.loadStyleDoc(stylePath);
            return factory.create(menu, playerInv, title, style);
        });
    }

    @FunctionalInterface
    public interface StyledScreenFactory<T extends AEBaseMenu, U extends AEBaseScreen<T>> {
        U create(T menu, Inventory playerInventory, Component title, ScreenStyle style);
    }
}

package com.knightcode.appliedstoragesorter.client;

import com.knightcode.appliedstoragesorter.client.screen.DigitalAssetVaultScreen;
import com.knightcode.appliedstoragesorter.client.screen.SorterCommandBlockScreen;
import com.knightcode.appliedstoragesorter.registry.SorterMenus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public final class SorterClientRegistrations {
    private SorterClientRegistrations() {
    }

    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(SorterMenus.DIGITAL_ASSET_VAULT_MENU.get(), DigitalAssetVaultScreen::new);
        event.register(SorterMenus.SORTER_COMMAND_BLOCK_MENU.get(), SorterCommandBlockScreen::new);
    }
}

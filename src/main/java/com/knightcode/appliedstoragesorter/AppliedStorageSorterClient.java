package com.knightcode.appliedstoragesorter;

import com.knightcode.appliedstoragesorter.client.SorterClientRegistrations;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = AppliedStorageSorter.MODID, dist = Dist.CLIENT)
public class AppliedStorageSorterClient {
    public AppliedStorageSorterClient(IEventBus modEventBus, ModContainer container) {
        modEventBus.addListener(SorterClientRegistrations::registerScreens);
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}

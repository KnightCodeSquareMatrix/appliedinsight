package com.knightcode.appliedstoragesorter;



import com.knightcode.appliedstoragesorter.client.SorterClientRegistrations;

import com.knightcode.appliedstoragesorter.client.gui.style.SorterStyleManager;

import net.minecraft.client.Minecraft;

import net.neoforged.api.distmarker.Dist;

import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import net.neoforged.bus.api.IEventBus;

import net.neoforged.fml.ModContainer;

import net.neoforged.fml.common.Mod;

import net.neoforged.neoforge.client.gui.ConfigurationScreen;

import net.neoforged.neoforge.client.gui.IConfigScreenFactory;



@Mod(value = AppliedStorageSorter.MODID, dist = Dist.CLIENT)

public class AppliedStorageSorterClient {

    public AppliedStorageSorterClient(IEventBus modEventBus, ModContainer container) {

        modEventBus.addListener(SorterClientRegistrations::registerScreens);

        modEventBus.addListener(this::onClientSetup);

        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

    }



    private void onClientSetup(FMLClientSetupEvent event) {

        event.enqueueWork(() -> {

            SorterStyleManager.initialize(Minecraft.getInstance().getResourceManager());

            for (String stylePath : new String[] {

                    "/screens/digital_asset_vault.json",

                    "/screens/smart_bus.json",

                    "/screens/sorter_command_block.json"

            }) {

                try {

                    SorterStyleManager.loadStyleDoc(stylePath);

                    AppliedStorageSorter.LOGGER.debug("Loaded screen style {}", stylePath);

                } catch (RuntimeException e) {

                    AppliedStorageSorter.LOGGER.error("Failed to preload screen style {}", stylePath, e);

                }

            }

        });

    }

}



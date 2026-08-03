package com.knightcode.appliedstoragesorter;

import org.slf4j.Logger;

import com.knightcode.appliedstoragesorter.command.SorterCommands;
import com.knightcode.appliedstoragesorter.network.FileChunkPayload;
import com.knightcode.appliedstoragesorter.network.FileChunkPayloadHandler;
import com.knightcode.appliedstoragesorter.network.NewDavExpandOncePayload;
import com.knightcode.appliedstoragesorter.network.NewDavExpandOncePayloadHandler;
import com.knightcode.appliedstoragesorter.network.NewDavMigrateToSqlPayload;
import com.knightcode.appliedstoragesorter.network.NewDavMigrateToSqlPayloadHandler;
import com.knightcode.appliedstoragesorter.network.NewDavSetExpansionCellPayload;
import com.knightcode.appliedstoragesorter.network.NewDavSetExpansionCellPayloadHandler;
import com.knightcode.appliedstoragesorter.network.NewDavTogglePayload;
import com.knightcode.appliedstoragesorter.network.NewDavTogglePayloadHandler;
import com.knightcode.appliedstoragesorter.network.SmartBusFilterPayload;
import com.knightcode.appliedstoragesorter.network.SmartBusModePayload;
import com.knightcode.appliedstoragesorter.network.SorterAnalysisPayload;
import com.knightcode.appliedstoragesorter.network.SorterAnalysisPayloadHandler;
import com.knightcode.appliedstoragesorter.network.SorterCommandPayload;
import com.knightcode.appliedstoragesorter.network.SorterCommandPayloadHandler;
import com.knightcode.appliedstoragesorter.registry.SorterBlockEntities;
import com.knightcode.appliedstoragesorter.registry.SorterBlocks;
import com.knightcode.appliedstoragesorter.registry.SorterCapabilities;
import com.knightcode.appliedstoragesorter.registry.SorterCreativeTabs;
import com.knightcode.appliedstoragesorter.registry.SorterItems;
import com.knightcode.appliedstoragesorter.registry.SorterMenus;
import com.knightcode.appliedstoragesorter.registry.SorterRecipeSerializers;
import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(AppliedStorageSorter.MODID)
public class AppliedStorageSorter {
    public static final String MODID = "appliedinsight";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AppliedStorageSorter(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerPayloads);
        modEventBus.addListener(SorterCapabilities::register);
        modEventBus.addListener(SorterCreativeTabs::buildCreativeTabContents);
        SorterBlocks.register(modEventBus);
        SorterItems.register(modEventBus);
        SorterBlockEntities.register(modEventBus);
        SorterMenus.register(modEventBus);
        SorterRecipeSerializers.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        NeoForge.EVENT_BUS.addListener(SorterCommands::register);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Applied Energistics: Insight initialized.");
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(AppliedStorageSorter.MODID);
        registrar.playToServer(
                SorterCommandPayload.TYPE,
                SorterCommandPayload.CODEC,
                SorterCommandPayloadHandler::handle);
        registrar.playToServer(
                NewDavTogglePayload.TYPE,
                NewDavTogglePayload.CODEC,
                NewDavTogglePayloadHandler::handle);
        registrar.playToServer(
                NewDavSetExpansionCellPayload.TYPE,
                NewDavSetExpansionCellPayload.CODEC,
                NewDavSetExpansionCellPayloadHandler::handle);
        registrar.playToServer(
                NewDavExpandOncePayload.TYPE,
                NewDavExpandOncePayload.CODEC,
                NewDavExpandOncePayloadHandler::handle);
        registrar.playToServer(
                NewDavMigrateToSqlPayload.TYPE,
                NewDavMigrateToSqlPayload.CODEC,
                NewDavMigrateToSqlPayloadHandler::handle);
        registrar.playToServer(
                SmartBusModePayload.TYPE,
                SmartBusModePayload.CODEC,
                SmartBusModePayload::handle);
        registrar.playToServer(
                SmartBusFilterPayload.TYPE,
                SmartBusFilterPayload.CODEC,
                SmartBusFilterPayload::handle);
        registrar.playToClient(
                SorterAnalysisPayload.TYPE,
                SorterAnalysisPayload.CODEC,
                SorterAnalysisPayloadHandler::handle);
        registrar.playToClient(
                FileChunkPayload.TYPE,
                FileChunkPayload.CODEC,
                FileChunkPayloadHandler::handle);
    }
}

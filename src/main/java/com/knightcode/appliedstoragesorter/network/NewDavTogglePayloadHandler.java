package com.knightcode.appliedstoragesorter.network;

import com.knightcode.appliedstoragesorter.AppliedStorageSorter;
import com.knightcode.appliedstoragesorter.blockentity.DigitalAssetVaultBlockEntity;
import com.knightcode.appliedstoragesorter.menu.DigitalAssetVaultMenu;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class NewDavTogglePayloadHandler {
    private NewDavTogglePayloadHandler() {
    }

    public static void handle(final NewDavTogglePayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            if (!(player.containerMenu instanceof DigitalAssetVaultMenu)) {
                return;
            }
            var level = player.serverLevel();
            if (level == null) {
                return;
            }
            if (!(level.getBlockEntity(payload.pos()) instanceof DigitalAssetVaultBlockEntity blockEntity)) {
                return;
            }

            switch (payload.settingId()) {
                case NewDavTogglePayload.SETTING_MIGRATE_EXISTING -> {
                    boolean current = blockEntity.isMigrateExistingItems();
                    boolean next = !current;
                    AppliedStorageSorter.LOGGER.info(
                            "[DAV-Migrate] player={} pos={} toggle import-existing: {} -> {}",
                            player.getGameProfile().getName(),
                            payload.pos(),
                            current,
                            next);
                    blockEntity.setMigrateExistingItems(next);
                }
                case NewDavTogglePayload.SETTING_AUTO_ACCEPT -> {
                    boolean current = blockEntity.isAutoAcceptIncoming();
                    blockEntity.setAutoAcceptIncoming(!current);
                }
                case NewDavTogglePayload.SETTING_AUTO_EXPAND -> {
                    boolean current = blockEntity.isAutoExpandEnabled();
                    blockEntity.setAutoExpandEnabled(!current);
                }
                default -> {
                }
            }
        });
    }
}

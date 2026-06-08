package com.knightcode.appliedstoragesorter.network;

import com.knightcode.appliedstoragesorter.blockentity.DigitalAssetVaultBlockEntity;
import com.knightcode.appliedstoragesorter.menu.DigitalAssetVaultMenu;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class NewDavMigrateToSqlPayloadHandler {
    private NewDavMigrateToSqlPayloadHandler() {
    }

    public static void handle(final NewDavMigrateToSqlPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            if (!(player.containerMenu instanceof DigitalAssetVaultMenu menu)) {
                return;
            }
            var level = player.serverLevel();
            if (level == null) {
                return;
            }
            if (!(level.getBlockEntity(payload.pos()) instanceof DigitalAssetVaultBlockEntity blockEntity)) {
                return;
            }
            blockEntity.showMigrateToSqlTbd();
            menu.refreshFromBlockEntity();
            menu.broadcastChanges();
        });
    }
}

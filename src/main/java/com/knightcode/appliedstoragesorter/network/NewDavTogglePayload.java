package com.knightcode.appliedstoragesorter.network;

import com.knightcode.appliedstoragesorter.AppliedStorageSorter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record NewDavTogglePayload(BlockPos pos, int settingId) implements CustomPacketPayload {
    public static final int SETTING_MIGRATE_EXISTING = 1;
    public static final int SETTING_AUTO_ACCEPT = 2;
    public static final int SETTING_AUTO_EXPAND = 3;

    public static final Type<NewDavTogglePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AppliedStorageSorter.MODID, "new_dav_toggle"));

    public static final StreamCodec<FriendlyByteBuf, NewDavTogglePayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            NewDavTogglePayload::pos,
            ByteBufCodecs.VAR_INT,
            NewDavTogglePayload::settingId,
            NewDavTogglePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

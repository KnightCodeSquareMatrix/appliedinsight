package com.knightcode.appliedstoragesorter.network;

import com.knightcode.appliedstoragesorter.AppliedStorageSorter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record NewDavExpandOncePayload(BlockPos pos) implements CustomPacketPayload {
    public static final Type<NewDavExpandOncePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AppliedStorageSorter.MODID, "new_dav_expand_once"));

    public static final StreamCodec<FriendlyByteBuf, NewDavExpandOncePayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            NewDavExpandOncePayload::pos,
            NewDavExpandOncePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

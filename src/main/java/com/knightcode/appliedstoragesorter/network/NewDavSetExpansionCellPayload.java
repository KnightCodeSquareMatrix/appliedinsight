package com.knightcode.appliedstoragesorter.network;

import com.knightcode.appliedstoragesorter.AppliedStorageSorter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record NewDavSetExpansionCellPayload(BlockPos pos, String cellId) implements CustomPacketPayload {
    public static final Type<NewDavSetExpansionCellPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AppliedStorageSorter.MODID, "new_dav_set_expansion_cell"));

    public static final StreamCodec<FriendlyByteBuf, NewDavSetExpansionCellPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            NewDavSetExpansionCellPayload::pos,
            ByteBufCodecs.STRING_UTF8,
            NewDavSetExpansionCellPayload::cellId,
            NewDavSetExpansionCellPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

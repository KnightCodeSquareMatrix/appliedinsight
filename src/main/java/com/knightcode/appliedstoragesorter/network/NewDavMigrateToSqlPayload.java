package com.knightcode.appliedstoragesorter.network;

import com.knightcode.appliedstoragesorter.AppliedStorageSorter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record NewDavMigrateToSqlPayload(BlockPos pos) implements CustomPacketPayload {
    public static final Type<NewDavMigrateToSqlPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AppliedStorageSorter.MODID, "new_dav_migrate_to_sql"));

    public static final StreamCodec<FriendlyByteBuf, NewDavMigrateToSqlPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            NewDavMigrateToSqlPayload::pos,
            NewDavMigrateToSqlPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

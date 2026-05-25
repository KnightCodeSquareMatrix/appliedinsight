package com.knightcode.appliedstoragesorter.network;

import com.knightcode.appliedstoragesorter.AppliedStorageSorter;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SorterCommandPayload(int buttonId) implements CustomPacketPayload {

    // Button ID constants
    public static final int CMD_ME_DUMP = 1;
    public static final int CMD_ME_STORAGE_DUMP = 2;
    public static final int CMD_ME_PLAN = 3;
    public static final int CMD_ME_PLAN_AND_MOVE = 4;
    public static final int CMD_MERGE = 5;

    public static final Type<SorterCommandPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AppliedStorageSorter.MODID, "sorter_command"));

    public static final StreamCodec<FriendlyByteBuf, SorterCommandPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            SorterCommandPayload::buttonId,
            SorterCommandPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

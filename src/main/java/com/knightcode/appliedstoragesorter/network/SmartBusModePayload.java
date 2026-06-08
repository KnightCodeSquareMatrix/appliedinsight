package com.knightcode.appliedstoragesorter.network;

import appeng.api.parts.IPartHost;
import com.knightcode.appliedstoragesorter.AppliedStorageSorter;
import com.knightcode.appliedstoragesorter.ae2.part.SmartBusPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SmartBusModePayload(BlockPos pos, Direction side) implements CustomPacketPayload {

    public static final Type<SmartBusModePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AppliedStorageSorter.MODID, "smart_bus_mode"));

    public static final StreamCodec<FriendlyByteBuf, SmartBusModePayload> CODEC = StreamCodec.of(
            (buffer, payload) -> {
                BlockPos.STREAM_CODEC.encode(buffer, payload.pos());
                buffer.writeByte(payload.side().ordinal());
            },
            buffer -> new SmartBusModePayload(
                    BlockPos.STREAM_CODEC.decode(buffer),
                    Direction.values()[buffer.readByte()]));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SmartBusModePayload payload,
            net.neoforged.neoforge.network.handling.IPayloadContext context) {
        context.enqueueWork(() -> {
            var blockEntity = context.player().level().getBlockEntity(payload.pos());
            if (!(blockEntity instanceof IPartHost partHost)) {
                return;
            }
            if (partHost.getPart(payload.side()) instanceof SmartBusPart smartBusPart) {
                smartBusPart.cycleMode();
            }
        });
    }
}

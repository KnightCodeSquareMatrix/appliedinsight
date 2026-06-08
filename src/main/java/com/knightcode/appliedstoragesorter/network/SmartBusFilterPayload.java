package com.knightcode.appliedstoragesorter.network;

import com.knightcode.appliedstoragesorter.AppliedStorageSorter;
import com.knightcode.appliedstoragesorter.ae2.part.SmartBusPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SmartBusFilterPayload(BlockPos pos, Direction side, String filterJson) implements CustomPacketPayload {

    public static final Type<SmartBusFilterPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AppliedStorageSorter.MODID, "smart_bus_filter"));

    public static final StreamCodec<FriendlyByteBuf, SmartBusFilterPayload> CODEC = StreamCodec.of(
            (buffer, payload) -> {
                BlockPos.STREAM_CODEC.encode(buffer, payload.pos());
                buffer.writeByte(payload.side().ordinal());
                buffer.writeUtf(payload.filterJson() == null ? "" : payload.filterJson());
            },
            buffer -> {
                BlockPos pos = BlockPos.STREAM_CODEC.decode(buffer);
                Direction side = Direction.values()[buffer.readByte()];
                String filterJson = buffer.readUtf();
                return new SmartBusFilterPayload(pos, side, filterJson.isBlank() ? null : filterJson);
            });

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SmartBusFilterPayload payload,
            net.neoforged.neoforge.network.handling.IPayloadContext context) {
        context.enqueueWork(() -> {
            var blockEntity = context.player().level().getBlockEntity(payload.pos());
            if (!(blockEntity instanceof appeng.api.parts.IPartHost partHost)) {
                return;
            }
            if (partHost.getPart(payload.side()) instanceof SmartBusPart smartBusPart) {
                smartBusPart.setFilterJson(payload.filterJson());
            }
        });
    }
}

package com.knightcode.appliedstoragesorter.network;

import com.knightcode.appliedstoragesorter.AppliedStorageSorter;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 大文件分块传输网络包。
 * 服务端将大文件切分为 128KB 的小块，依次发送。
 * 客户端根据 fileId + chunkIndex 重组。
 */
public record FileChunkPayload(
        ResourceLocation fileId,
        int chunkIndex,
        int totalChunks,
        byte[] data) implements CustomPacketPayload {

    public static final Type<FileChunkPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AppliedStorageSorter.MODID, "file_chunk"));

    public static final StreamCodec<FriendlyByteBuf, FileChunkPayload> CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeResourceLocation(payload.fileId);
                buf.writeInt(payload.chunkIndex);
                buf.writeInt(payload.totalChunks);
                buf.writeByteArray(payload.data);
            },
            buf -> new FileChunkPayload(
                    buf.readResourceLocation(),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readByteArray()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

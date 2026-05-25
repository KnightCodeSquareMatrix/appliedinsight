package com.knightcode.appliedstoragesorter.network;

import com.knightcode.appliedstoragesorter.AppliedStorageSorter;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 通知客户端有新的存储分析报告可用，报告内容通过 FileChunkedSender 分块传输。
 * 客户端收到此通知后应注册 FileCompleteCallback 以接收完整数据。
 */
public record SorterAnalysisPayload(ResourceLocation fileId) implements CustomPacketPayload {

    public static final Type<SorterAnalysisPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AppliedStorageSorter.MODID, "analysis_notify"));

    public static final StreamCodec<FriendlyByteBuf, SorterAnalysisPayload> CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC,
            SorterAnalysisPayload::fileId,
            SorterAnalysisPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

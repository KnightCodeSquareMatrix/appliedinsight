package com.knightcode.appliedstoragesorter.network;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.knightcode.appliedstoragesorter.ae2.analysis.StorageAnalyzerReport;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 客户端侧处理器。
 * 接收轻量通知 {@link SorterAnalysisPayload}（仅携带 fileId），
 * 然后通过 {@link FileChunkPayloadHandler} 的分块回调接收完整 JSON。
 */
public final class SorterAnalysisPayloadHandler {
    private static final Logger log = LoggerFactory.getLogger(SorterAnalysisPayloadHandler.class);
    private static final Gson GSON = new GsonBuilder().create();

    private static volatile StorageAnalyzerReport cachedReport;
    private static volatile String cachedJson;

    private SorterAnalysisPayloadHandler() {
    }

    public static void handle(final SorterAnalysisPayload payload, final IPayloadContext context) {
        var fileId = payload.fileId();
        log.info("Storage analysis notification received, expecting file: {}", fileId);

        // 注册分块传输完成回调——当所有块到齐后解析 JSON 并缓存
        FileChunkPayloadHandler.registerCallback(fileId, (fileName, data) -> {
            context.enqueueWork(() -> {
                String json = new String(data, StandardCharsets.UTF_8);
                cachedJson = json;
                cachedReport = GSON.fromJson(json, StorageAnalyzerReport.class);
                log.info("Storage analysis reassembled: {} locations, {} unique keys",
                        cachedReport != null ? cachedReport.summary().storageLocationCount() : 0,
                        cachedReport != null ? cachedReport.summary().uniqueKeyCount() : 0);
            });
        });
    }

    /**
     * 获取缓存的报告对象。可能为 null（尚未收到数据）。
     */
    public static StorageAnalyzerReport getReport() {
        return cachedReport;
    }

    /**
     * 获取缓存的原始 JSON 字符串。可能为 null。
     */
    public static String getRawJson() {
        return cachedJson;
    }

    /**
     * 是否有缓存数据。
     */
    public static boolean hasData() {
        return cachedReport != null;
    }

    /**
     * 清空缓存。
     */
    public static void clear() {
        cachedReport = null;
        cachedJson = null;
    }
}

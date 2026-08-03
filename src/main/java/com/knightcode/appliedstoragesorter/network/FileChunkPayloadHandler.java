package com.knightcode.appliedstoragesorter.network;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 客户端分块重组处理器。
 * 接收 FileChunkPayload，按 fileId 缓存分块，全部到达后重组为完整字节数组。
 */
public final class FileChunkPayloadHandler {
    private static final Logger log = LoggerFactory.getLogger(FileChunkPayloadHandler.class);

    // 传输会话缓存：fileId → 分块数组
    private static final Map<ResourceLocation, ChunkSession> sessions = new ConcurrentHashMap<>();

    private FileChunkPayloadHandler() {
    }

    /**
     * 注册一个文件完成时的回调。
     * key: fileId, value: (fileName, bytes) → void
     */
    private static final Map<ResourceLocation, FileCompleteCallback> callbacks = new ConcurrentHashMap<>();

    @FunctionalInterface
    public interface FileCompleteCallback {
        void onComplete(String fileName, byte[] data);
    }

    /**
     * 注册文件传输完成回调。
     */
    public static void registerCallback(ResourceLocation fileId, FileCompleteCallback callback) {
        callbacks.put(fileId, callback);
    }

    /**
     * 取消回调注册。
     */
    public static void unregisterCallback(ResourceLocation fileId) {
        callbacks.remove(fileId);
    }

    public static void handle(final FileChunkPayload payload, final IPayloadContext context) {
        var session = sessions.computeIfAbsent(payload.fileId(), id -> new ChunkSession(payload.totalChunks()));

        synchronized (session) {
            session.chunks[payload.chunkIndex()] = payload.data();
            session.receivedCount++;

            if (session.receivedCount == payload.totalChunks()) {
                // 所有分块到齐，重组
                sessions.remove(payload.fileId());
                int totalSize = Arrays.stream(session.chunks).mapToInt(c -> c.length).sum();
                byte[] fullData = new byte[totalSize];
                int offset = 0;
                for (var chunk : session.chunks) {
                    System.arraycopy(chunk, 0, fullData, offset, chunk.length);
                    offset += chunk.length;
                }

                log.info("File '{}' reassembled: {} bytes in {} chunks",
                        payload.fileId(), totalSize, payload.totalChunks());

                // 触发回调
                var callback = callbacks.remove(payload.fileId());
                if (callback != null) {
                    callback.onComplete(payload.fileId().getPath(), fullData);
                }
            }
        }
    }

    private static class ChunkSession {
        final byte[][] chunks;
        int receivedCount;

        ChunkSession(int totalChunks) {
            this.chunks = new byte[totalChunks][];
            this.receivedCount = 0;
        }
    }
}

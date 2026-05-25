package com.knightcode.appliedstoragesorter.network;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 服务端大文件分块发送工具。
 * 将文件切分为 128KB 的小块，通过 FileChunkPayload 依次发送给指定玩家。
 */
public final class FileChunkedSender {
    private static final Logger log = LoggerFactory.getLogger(FileChunkedSender.class);
    private static final int CHUNK_SIZE = 128 * 1024; // 128 KB

    private FileChunkedSender() {
    }

    /**
     * 将文件分块发送给玩家。
     *
     * @param player 目标玩家
     * @param fileId 文件唯一标识（接收方用此标识重组）
     * @param filePath 要发送的文件路径
     */
    public static void sendFile(ServerPlayer player, ResourceLocation fileId, Path filePath) {
        try {
            byte[] allBytes = Files.readAllBytes(filePath);
            int totalChunks = (int) Math.ceil((double) allBytes.length / CHUNK_SIZE);

            log.info("Sending file '{}' ({} bytes, {} chunks) to player {}",
                    fileId, allBytes.length, totalChunks, player.getScoreboardName());

            for (int i = 0; i < totalChunks; i++) {
                int start = i * CHUNK_SIZE;
                int end = Math.min(allBytes.length, start + CHUNK_SIZE);
                byte[] chunkData = Arrays.copyOfRange(allBytes, start, end);

                var payload = new FileChunkPayload(fileId, i, totalChunks, chunkData);
                PacketDistributor.sendToPlayer(player, payload);
            }

            log.info("File '{}' sent successfully to player {}", fileId, player.getScoreboardName());
        } catch (IOException e) {
            log.error("Failed to send file '{}' to player {}", fileId, player.getScoreboardName(), e);
        }
    }
}

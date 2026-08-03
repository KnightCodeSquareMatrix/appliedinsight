package com.knightcode.appliedstoragesorter.application;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.knightcode.appliedstoragesorter.AppliedStorageSorter;
import com.knightcode.appliedstoragesorter.Config;
import com.knightcode.appliedstoragesorter.ae2.Ae2ControllerTargetResolver;
import com.knightcode.appliedstoragesorter.ae2.GridTargetResolver;
import com.knightcode.appliedstoragesorter.ae2.analysis.Ae2StorageAnalyzer;
import com.knightcode.appliedstoragesorter.ae2.dump.SorterStorageAnalysisDumpWriter;
import com.knightcode.appliedstoragesorter.application.result.SorterStorageAnalysisCommandResult;
import com.knightcode.appliedstoragesorter.logging.SorterFileLogger;
import com.knightcode.appliedstoragesorter.network.FileChunkedSender;
import com.knightcode.appliedstoragesorter.network.SorterAnalysisPayload;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SorterStorageAnalysisService {
    private static final Logger log = LoggerFactory.getLogger(SorterStorageAnalysisService.class);
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private SorterStorageAnalysisService() {
    }

    public static SorterStorageAnalysisCommandResult execute(CommandSourceStack source) {
        return execute(source, Ae2ControllerTargetResolver::resolveGridTarget);
    }

    public static SorterStorageAnalysisCommandResult execute(CommandSourceStack source, GridTargetResolver resolver) {
        if (!Config.ENABLE_SORTER.get()) {
            log.debug("Skip sorter storage analysis because sorter is disabled.");
            return SorterStorageAnalysisCommandResult.failure(
                    Component.translatable("sorter.command.error.disabled"));
        }
        if (source.getEntity() == null) {
            log.debug("Reject sorter storage analysis because command source has no entity.");
            return SorterStorageAnalysisCommandResult.failure(Component.translatable("sorter.command.error.player_only"));
        }
        var gridTarget = resolver.resolve(source);
        if (!gridTarget.success()) {
            log.warn("Sorter storage analysis target resolution failed: {}", gridTarget.userMessage());
            SorterFileLogger.logSorterMeStorageAnalysisTargetFailure(source, gridTarget);
            return SorterStorageAnalysisCommandResult.failure(gridTarget.userMessage());
        }

        try {
            var dumpResult = SorterStorageAnalysisDumpWriter.dump(gridTarget);
            log.info("Sorter storage analysis dump written to {} with {} storage location(s) and {} unique key(s).",
                    dumpResult.dumpFilePath(), dumpResult.storageLocationCount(), dumpResult.uniqueKeyCount());
            SorterFileLogger.logSorterMeStorageAnalysis(source, gridTarget, dumpResult);

            // 通过网络包将分析报告推送给客户端：分块传输，无大小限制
            if (source.getEntity() instanceof ServerPlayer serverPlayer) {
                var report = Ae2StorageAnalyzer.analyze(gridTarget.grid());
                byte[] jsonBytes = GSON.toJson(report).getBytes(StandardCharsets.UTF_8);

                // 写入临时文件，由 FileChunkedSender 分块发送
                Path tempFile = Files.createTempFile("storage-analysis-", ".json");
                try {
                    Files.write(tempFile, jsonBytes);
                    ResourceLocation fileId = ResourceLocation.fromNamespaceAndPath(
                            AppliedStorageSorter.MODID, "analysis_report");

                    // 先发通知（客户端据此注册回调），然后立即发分块
                    PacketDistributor.sendToPlayer(serverPlayer, new SorterAnalysisPayload(fileId));
                    FileChunkedSender.sendFile(serverPlayer, fileId, tempFile);
                } finally {
                    Files.deleteIfExists(tempFile);
                }
            }
            return SorterStorageAnalysisCommandResult.success(
                    buildSummaryLines(dumpResult),
                    dumpResult.dumpFilePath(),
                    dumpResult.storageLocationCount(),
                    dumpResult.uniqueKeyCount());
        } catch (IOException exception) {
            log.error("Failed to write sorter storage analysis dump.", exception);
            SorterFileLogger.logSorterMeStorageAnalysisFailure(source, gridTarget, exception);
            return SorterStorageAnalysisCommandResult.failure(
                    Component.translatable("sorter.command.storageanalysis.error.write_failed"));
        }
    }

    private static List<Component> buildSummaryLines(
            com.knightcode.appliedstoragesorter.ae2.dump.SorterStorageAnalysisDumpResult dumpResult) {
        List<Component> lines = new ArrayList<>();
        lines.add(SorterComponentHelper.clickableFile("dumpFile", dumpResult.dumpFilePath()));
        lines.add(SorterComponentHelper.line("storageLocations=%d (nonEmpty=%d, internal=%d, external=%d)"
                .formatted(
                        dumpResult.storageLocationCount(),
                        dumpResult.nonEmptyStorageLocationCount(),
                        dumpResult.internalStorageLocationCount(),
                        dumpResult.externalStorageLocationCount())));
        lines.add(SorterComponentHelper.line("uniqueKeys=%d, duplicatedKeys=%d, totalAmount=%d"
                .formatted(
                        dumpResult.uniqueKeyCount(),
                        dumpResult.duplicatedKeyCount(),
                        dumpResult.totalAmount())));
        lines.add(SorterComponentHelper.line("fragmentation=%s, externalAmountRatio=%s"
                .formatted(
                        dumpResult.fragmentationLevel(),
                        formatRatioPercent(dumpResult.externalTotalAmount(), dumpResult.totalAmount()))));
        lines.add(SorterComponentHelper.line("health=" + (dumpResult.healthFlags().isEmpty() ? "<none>" : String.join(", ", dumpResult.healthFlags()))));
        return List.copyOf(lines);
    }

    private static String formatRatioPercent(long numerator, long denominator) {
        if (numerator <= 0 || denominator <= 0) {
            return "0.00%";
        }
        return String.format(Locale.ROOT, "%.2f%%", (double) numerator * 100.0D / (double) denominator);
    }
}

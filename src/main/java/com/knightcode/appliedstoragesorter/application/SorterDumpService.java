package com.knightcode.appliedstoragesorter.application;

import java.io.IOException;
import java.util.List;

import com.knightcode.appliedstoragesorter.Config;
import com.knightcode.appliedstoragesorter.ae2.Ae2ControllerTargetResolver;
import com.knightcode.appliedstoragesorter.ae2.Ae2GridTargetResult;
import com.knightcode.appliedstoragesorter.ae2.GridTargetResolver;
import com.knightcode.appliedstoragesorter.ae2.dump.SorterNetworkDumpWriter;
import com.knightcode.appliedstoragesorter.ae2.scan.Ae2DriveScanner;
import com.knightcode.appliedstoragesorter.application.result.SorterFeedbackResult;
import com.knightcode.appliedstoragesorter.logging.ReportFileSupport;
import com.knightcode.appliedstoragesorter.logging.SorterFileLogger;
import com.knightcode.appliedstoragesorter.network.FileChunkedSender;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.loading.FMLPaths;

public final class SorterDumpService {
    private SorterDumpService() {
    }

    public static SorterFeedbackResult execute(CommandSourceStack source) {
        return execute(source, Ae2ControllerTargetResolver::resolveGridTarget);
    }

    public static SorterFeedbackResult execute(CommandSourceStack source, GridTargetResolver resolver) {
        if (!Config.ENABLE_SORTER.get()) {
            return SorterFeedbackResult.failure(Component.translatable("sorter.command.error.disabled"));
        }
        if (source.getEntity() == null) {
            return SorterFeedbackResult.failure(Component.translatable("sorter.command.error.player_only"));
        }
        var gridTarget = resolver.resolve(source);
        if (!gridTarget.success()) {
            SorterFileLogger.logSorterMeDumpTargetFailure(source, gridTarget);
            return SorterFeedbackResult.failure(gridTarget.userMessage());
        }

        var scanSummary = Ae2DriveScanner.scan(gridTarget.grid());
        try {
            var dumpResult = SorterNetworkDumpWriter.dump(source, gridTarget, scanSummary);
            SorterFileLogger.logSorterMeDump(source, gridTarget, scanSummary, dumpResult);
            // 通过分块网络包将 me-dump 推送给客户端
            if (source.getEntity() instanceof ServerPlayer serverPlayer) {
                var dumpFile = FMLPaths.GAMEDIR.get().resolve(dumpResult.dumpFilePath());
                var fileId = ResourceLocation.fromNamespaceAndPath(
                        "AppliedStorageSorter", "me-dump-" + System.currentTimeMillis());
                FileChunkedSender.sendFile(serverPlayer, fileId, dumpFile);
            }
            return SorterFeedbackResult.success(List.of(
                    Component.translatable("sorter.command.dump.success",
                            dumpResult.uniqueItemKeyCount(), dumpResult.mountedCellCount()),
                    SorterComponentHelper.clickableFile("dumpFile", dumpResult.dumpFilePath())));
        } catch (IOException exception) {
            SorterFileLogger.logSorterMeDumpFailure(source, gridTarget, scanSummary, exception);
            return SorterFeedbackResult.failure(
                    Component.translatable("sorter.command.dump.error.write_failed"));
        }
    }
}

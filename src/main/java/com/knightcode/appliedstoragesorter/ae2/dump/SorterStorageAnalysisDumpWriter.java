package com.knightcode.appliedstoragesorter.ae2.dump;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;

import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.knightcode.appliedstoragesorter.ae2.Ae2GridTargetResult;
import com.knightcode.appliedstoragesorter.ae2.analysis.Ae2StorageAnalyzer;
import com.knightcode.appliedstoragesorter.ae2.analysis.StorageAnalyzerReport;
import com.knightcode.appliedstoragesorter.logging.ReportFileSupport;

public final class SorterStorageAnalysisDumpWriter {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().serializeNulls().create();
    private static final Path DUMP_DIR = ReportFileSupport.resolveDumpDir("AppliedStorageSorter");

    private SorterStorageAnalysisDumpWriter() {
    }

    public static SorterStorageAnalysisDumpResult dump(Ae2GridTargetResult targetResult) throws IOException {
        StorageAnalyzerReport report = Ae2StorageAnalyzer.analyze(targetResult.grid());

        var dump = new StorageAnalysisDump(
                "1",
                LocalDateTime.now().toString(),
                targetResult.dimensionId(),
                targetResult.targetBlockId(),
                targetResult.targetPos() != null ? targetResult.targetPos().toShortString() : null,
                targetResult.controllerPos() != null ? targetResult.controllerPos().toShortString() : null,
                report);

        var json = GSON.toJson(dump);

        // 文件名中的网络标识，使不同 ME 网络文件可区分
        var slug = ReportFileSupport.networkSlug(targetResult.dimensionId(), targetResult.controllerPos());

        // 时间戳版本（历史存档）
        var fileName = ReportFileSupport.timestampedFileName("storage-analysis-" + slug + "-", ".json");
        var dumpPath = DUMP_DIR.resolve(fileName);
        ReportFileSupport.writeTextFile(
                DUMP_DIR,
                fileName,
                json,
                LoggerFactory.getLogger(SorterStorageAnalysisDumpWriter.class),
                "Failed to write sorter storage analysis dump {}"
        );

        // latest 版本（始终覆盖，供前端动态读取）
        var latestName = ReportFileSupport.latestFileName("storage-analysis-" + slug + "-", ".json");
        ReportFileSupport.writeTextFile(
                DUMP_DIR,
                latestName,
                json,
                LoggerFactory.getLogger(SorterStorageAnalysisDumpWriter.class),
                "Failed to write latest storage analysis dump {}"
        );

        return new SorterStorageAnalysisDumpResult(
                ReportFileSupport.relativizeGamePath(dumpPath),
                report.summary().storageLocationCount(),
                report.summary().nonEmptyStorageLocationCount(),
                report.summary().internalStorageLocationCount(),
                report.summary().externalStorageLocationCount(),
                report.summary().uniqueKeyCount(),
                report.summary().duplicatedKeyCount(),
                report.summary().totalAmount(),
                report.summary().externalTotalAmount(),
                report.summary().fragmentationLevel(),
                report.healthFlags());
    }

    private record StorageAnalysisDump(
            String formatVersion,
            String generatedAt,
            String dimensionId,
            String targetBlockId,
            String targetPos,
            String controllerPos,
            StorageAnalyzerReport report) {
    }
}

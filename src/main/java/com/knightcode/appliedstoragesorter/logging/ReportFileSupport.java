package com.knightcode.appliedstoragesorter.logging;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.slf4j.Logger;

import com.knightcode.appliedstoragesorter.ae2.scan.DriveCellReference;

import appeng.api.stacks.AEItemKey;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import net.neoforged.fml.loading.FMLPaths;

public final class ReportFileSupport {
    private static final DateTimeFormatter FILE_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final DateTimeFormatter HEADER_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private ReportFileSupport() {
    }

    public static Path resolveLogDir(String namespace) {
        return FMLPaths.GAMEDIR.get().resolve("logs").resolve(namespace);
    }

    public static Path resolveDumpDir(String namespace) {
        return FMLPaths.GAMEDIR.get().resolve("dumps").resolve(namespace);
    }

    public static String timestampedFileName(String prefix, String suffix) {
        return prefix + LocalDateTime.now().format(FILE_TIMESTAMP_FORMAT) + suffix;
    }

    /**
     * 文件名中的 "latest" 后缀，用于前端动态读取（始终指向最新版本）。
     */
    public static String latestFileName(String prefix, String suffix) {
        return prefix + "latest" + suffix;
    }

    /**
     * 构建网络标识文件名片段，格式：{dimension}-{x}_{y}_{z}
     * 使不同 ME 网络的 dump 文件名可区分。
     * 当 pos 为 null 时返回 "unknown"。
     */
    public static String networkSlug(@Nullable String dimensionId, @Nullable BlockPos pos) {
        var dim = dimensionId != null ? dimensionId.replace(':', '_') : "unknown";
        if (pos == null) {
            return dim + "-unknown";
        }
        return dim + "_" + pos.getX() + "_" + pos.getY() + "_" + pos.getZ();
    }

    public static String writeTextFile(
            Path directory,
            String fileName,
            String content,
            Logger logger,
            String failureMessage) {
        Path path = directory.resolve(fileName);
        try {
            Files.createDirectories(directory);
            Files.writeString(path, content);
        } catch (IOException exception) {
            logger.error(failureMessage, path, exception);
            return relativizeGamePath(path) + " (write failed)";
        }
        return relativizeGamePath(path);
    }

    /**
     * 追加写入到单一日志文件（如 appliedstoragesorter.log）。
     * 与 writeTextFile 不同，此方法始终追加而非覆盖。
     */
    public static void appendToLogFile(Path logPath, String content, Logger logger, String failureMessage) {
        try {
            Files.createDirectories(logPath.getParent());
            Files.writeString(logPath, content, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException exception) {
            logger.error(failureMessage, logPath, exception);
        }
    }

    public static String relativizeGamePath(Path path) {
        return FMLPaths.GAMEDIR.get().relativize(path).toString().replace('\\', '/');
    }

    public static StringBuilder buildCommandHeader(CommandSourceStack source, String commandName) {
        var position = source.getPosition();
        var level = source.getLevel();
        var entity = source.getEntity();

        return new StringBuilder()
                .append("==================================================\n")
                .append('[')
                .append(LocalDateTime.now().format(HEADER_TIMESTAMP_FORMAT))
                .append("] ")
                .append(commandName)
                .append('\n')
                .append("executor=")
                .append(source.getTextName())
                .append('\n')
                .append("dimension=")
                .append(level.dimension().location())
                .append('\n')
                .append("position=")
                .append(formatCoordinate(position.x))
                .append(", ")
                .append(formatCoordinate(position.y))
                .append(", ")
                .append(formatCoordinate(position.z))
                .append('\n')
                .append("server_time=")
                .append(level.getServer() != null ? level.getServer().getTickCount() : -1)
                .append('\n')
                .append("entity=")
                .append(formatEntity(entity))
                .append("\n\n");
    }

    public static String formatCoordinate(double coordinate) {
        return String.format(Locale.ROOT, "%.3f", coordinate);
    }

    public static String formatEntity(Entity entity) {
        if (entity == null) {
            return "<none>";
        }
        return entity.getType().toString();
    }

    public static String formatBlockPos(BlockPos pos) {
        if (pos == null) {
            return "<none>";
        }
        return pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
    }

    public static String nullToPlaceholder(String value) {
        return value != null ? value : "<none>";
    }

    /**
     * 与 nullToPlaceholder 类似，但额外将空白字符串也映射为 {@code "<none>"}。
     */
    public static String valueOrPlaceholder(String value) {
        return value == null || value.isBlank() ? "<none>" : value;
    }

    /**
     * 格式化 DriveCellReference 为可读字符串，例如 "12, 64, 45#slot=3"
     * 或 "12, 64, 45#slot=3->attached=12, 64, 46@west"。
     */
    public static String formatDriveCellReference(DriveCellReference reference) {
        String base = formatBlockPos(reference.drivePos()) + "#slot=" + reference.slot();
        if (!reference.hasAttachedStorage()) {
            return base;
        }
        return base + "->attached=" + formatBlockPos(reference.attachedStoragePos())
                + "@" + nullToPlaceholder(reference.attachmentDescription());
    }

    /**
     * 从 AEItemKey 提取物品 ID 字符串。
     */
    public static String itemId(AEItemKey key) {
        return BuiltInRegistries.ITEM.getKey(key.getReadOnlyStack().getItem()).toString();
    }

    /**
     * 描述 AE2 grid pivot 的持有者。
     */
    public static String describeOwner(Object owner) {
        if (owner instanceof net.minecraft.world.level.block.entity.BlockEntity blockEntity) {
            return owner.getClass().getName() + "@" + formatBlockPos(blockEntity.getBlockPos());
        }
        return owner.getClass().getName();
    }
}
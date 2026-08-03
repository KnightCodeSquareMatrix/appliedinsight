package com.knightcode.appliedstoragesorter.network;

import com.knightcode.appliedstoragesorter.ae2.Ae2GridTargetResult;
import com.knightcode.appliedstoragesorter.ae2.GridTargetResolver;
import com.knightcode.appliedstoragesorter.application.SorterDumpService;
import com.knightcode.appliedstoragesorter.application.SorterMergeService;
import com.knightcode.appliedstoragesorter.application.SorterPlanService;
import com.knightcode.appliedstoragesorter.application.SorterStorageAnalysisService;
import com.knightcode.appliedstoragesorter.menu.SorterCommandBlockMenu;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class SorterCommandPayloadHandler {
    private SorterCommandPayloadHandler() {
    }

    public static void handle(final SorterCommandPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();

            if (!(player.containerMenu instanceof SorterCommandBlockMenu menu)) {
                player.sendSystemMessage(Component.literal("§c请先打开命令执行方块 GUI"));
                return;
            }

            var be = menu.getBlockEntity();
            if (!be.isNodeOnline()) {
                player.sendSystemMessage(Component.literal("§c[错误] 方块未接入 ME 网络或频道不足！"));
                return;
            }
            if (!(player instanceof ServerPlayer serverPlayer)) return;

            var level = be.getLevel();
            if (!(level instanceof ServerLevel serverLevel)) return;

            // 构建命令源：以玩家为执行者，定位到方块位置
            CommandSourceStack source = new CommandSourceStack(
                    serverPlayer,
                    be.getBlockPos().getCenter(),
                    serverPlayer.getRotationVector(),
                    serverLevel,
                    2,
                    serverPlayer.getDisplayName().getString(),
                    serverPlayer.getDisplayName(),
                    serverPlayer.getServer(),
                    serverPlayer);

            // 基于方块节点的网格解析策略（绕过射线检测）
            String dimensionId = serverLevel.dimension().location().toString();
            GridTargetResolver nodeResolver = src -> Ae2GridTargetResult.fromNode(
                    be.getMainNode(), be.getBlockPos(),
                    "appliedinsight:sorter_command_block", dimensionId);

            switch (payload.buttonId()) {
                case SorterCommandPayload.CMD_ME_DUMP -> {
                    var result = SorterDumpService.execute(source, nodeResolver);
                    sendFeedback(player, result.success(), result.lines());
                }
                case SorterCommandPayload.CMD_ME_STORAGE_DUMP -> {
                    var result = SorterStorageAnalysisService.execute(source, nodeResolver);
                    sendFeedback(player, result.success(), result.lines());
                }
                case SorterCommandPayload.CMD_ME_PLAN -> {
                    var result = SorterPlanService.execute(source, false, nodeResolver);
                    sendFeedback(player, result.success(), result.lines());
                }
                case SorterCommandPayload.CMD_ME_PLAN_AND_MOVE -> {
                    var result = SorterPlanService.execute(source, true, nodeResolver);
                    sendFeedback(player, result.success(), result.lines());
                }
                case SorterCommandPayload.CMD_MERGE -> {
                    var result = SorterMergeService.execute(source, nodeResolver);
                    sendFeedback(player, result.success(), result.lines());
                }
                default -> player.sendSystemMessage(Component.literal("§c未知操作码: " + payload.buttonId()));
            }
        });
    }

    private static void sendFeedback(Player player, boolean success, java.util.List<net.minecraft.network.chat.Component> lines) {
        if (success) {
            lines.forEach(player::sendSystemMessage);
        } else {
            lines.forEach(line -> player.sendSystemMessage(
                    Component.literal("§c").append(line)));
        }
    }
}


package com.knightcode.appliedstoragesorter.command;

import com.knightcode.appliedstoragesorter.Config;
import com.knightcode.appliedstoragesorter.application.SorterDumpService;
import com.knightcode.appliedstoragesorter.application.SorterMergeService;
import com.knightcode.appliedstoragesorter.application.SorterPlanService;
import com.knightcode.appliedstoragesorter.application.SorterProfileBindingService;
import com.knightcode.appliedstoragesorter.application.SorterStorageAnalysisService;
import com.knightcode.appliedstoragesorter.application.result.SorterFeedbackResult;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class SorterCommands {
    private SorterCommands() {
    }

    public static void register(RegisterCommandsEvent event) {
        var sorterRoot = Commands.literal("sorter")
                .then(Commands.literal("merge")
                        .executes(SorterCommands::runMerge))
                .then(Commands.literal("me")
                        .then(Commands.literal("dump")
                                .executes(SorterCommands::runMeDump))
                        .then(Commands.literal("storageDump")
                                .executes(SorterCommands::runMeStorageDump))
                        .then(Commands.literal("listProfiles")
                                .executes(SorterCommands::runListProfiles))
                        .then(Commands.literal("bindProfile")
                                .then(Commands.argument("number", IntegerArgumentType.integer(1))
                                        .executes(SorterCommands::runBindProfile)))
                        .then(Commands.literal("showProfile")
                                .executes(SorterCommands::runShowProfile))
                        .then(Commands.literal("plan")
                                .executes(SorterCommands::runPlan))
                        .then(Commands.literal("planAndMove")
                                .executes(SorterCommands::runPlanAndMove)));

        // 注册 genTestItems 子命令（开发调试工具，不属于三条主线）
        GenTestItemsCommand.register(sorterRoot);

        event.getDispatcher().register(sorterRoot);
    }

    private static int runMerge(CommandContext<CommandSourceStack> context) {
        return sendFeedback(context.getSource(), SorterMergeService.execute(context.getSource()));
    }

    private static int runMeDump(CommandContext<CommandSourceStack> context) {
        if (!Config.DEVELOPER_MODE.get()) {
            context.getSource().sendFailure(Component.translatable("sorter.command.error.developer_mode").withStyle(net.minecraft.ChatFormatting.RED));
            return 0;
        }
        return sendFeedback(context.getSource(), SorterDumpService.execute(context.getSource()));
    }

    private static int runMeStorageDump(CommandContext<CommandSourceStack> context) {
        if (!Config.DEVELOPER_MODE.get()) {
            context.getSource().sendFailure(Component.translatable("sorter.command.error.developer_mode").withStyle(net.minecraft.ChatFormatting.RED));
            return 0;
        }
        var source = context.getSource();
        var result = SorterStorageAnalysisService.execute(source);
        if (!result.success()) {
            result.lines().forEach(source::sendFailure);
            return 0;
        }

        result.lines().forEach(line -> source.sendSuccess(() -> line, false));
        return Command.SINGLE_SUCCESS;
    }

    private static int runListProfiles(CommandContext<CommandSourceStack> context) {
        return sendFeedback(context.getSource(), SorterProfileBindingService.listProfiles());
    }

    private static int runBindProfile(CommandContext<CommandSourceStack> context) {
        int number = IntegerArgumentType.getInteger(context, "number");
        return sendFeedback(context.getSource(), SorterProfileBindingService.bindProfile(context.getSource(), number));
    }

    private static int runShowProfile(CommandContext<CommandSourceStack> context) {
        return sendFeedback(context.getSource(), SorterProfileBindingService.showBoundProfile(context.getSource()));
    }

    private static int runPlan(CommandContext<CommandSourceStack> context) {
        return sendFeedback(context.getSource(), SorterPlanService.plan(context.getSource()));
    }

    private static int runPlanAndMove(CommandContext<CommandSourceStack> context) {
        return sendFeedback(context.getSource(), SorterPlanService.planAndMove(context.getSource()));
    }

    private static int sendFeedback(CommandSourceStack source, SorterFeedbackResult result) {
        if (!result.success()) {
            result.lines().forEach(source::sendFailure);
            return 0;
        }

        result.lines().forEach(line -> source.sendSuccess(() -> line, false));
        return Command.SINGLE_SUCCESS;
    }
}

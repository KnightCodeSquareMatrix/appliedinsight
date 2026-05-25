package com.knightcode.appliedstoragesorter.application;

import java.nio.file.Path;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.neoforged.fml.loading.FMLPaths;

/**
 * 用于生成 Minecraft 聊天栏可点击组件的工具方法。
 */
public final class SorterComponentHelper {
    private SorterComponentHelper() {
    }

    /**
     * 创建一个可点击的文件路径组件。
     * 玩家点击后在系统默认程序中打开该文件。
     *
     * @param label  显示文本（如 "detailedLog"）
     * @param path   相对 game 目录的文件路径（如 "logs/appliedstoragesorter/plan-xxx.log"）
     * @return 带点击事件和悬浮提示的组件
     */
    public static Component clickableFile(String label, String path) {
        Path absolute = FMLPaths.GAMEDIR.get().resolve(path).normalize();
        var fileStyle = Style.EMPTY
                .withColor(ChatFormatting.GREEN)
                .withUnderlined(true)
                .withClickEvent(new ClickEvent(
                        ClickEvent.Action.OPEN_FILE,
                        absolute.toAbsolutePath().toString()))
                .withHoverEvent(new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        Component.literal("Click to open: " + path)));
        return Component.literal(label + "=")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(path).withStyle(fileStyle));
    }

    /**
     * 创建一个带颜色的 key=value 文本组件。
     */
    public static Component keyValue(String key, String value) {
        return Component.literal(key + "=")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(value)
                        .withStyle(ChatFormatting.WHITE));
    }

    /**
     * 创建一个纯色文本行。
     */
    public static Component line(String text) {
        return Component.literal(text);
    }
}

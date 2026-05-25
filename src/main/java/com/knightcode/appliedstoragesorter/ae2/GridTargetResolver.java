package com.knightcode.appliedstoragesorter.ae2;

import net.minecraft.commands.CommandSourceStack;

/**
 * 网格目标解析策略抽象。
 * <p>
 * service 层通过此接口获取 {@link Ae2GridTargetResult}，不再关心具体解析方式。
 * 默认实现为 {@link Ae2ControllerTargetResolver#resolveGridTarget(CommandSourceStack)}（射线检测），
 * 已接网方块可提供基于 {@link appeng.api.networking.IManagedGridNode} 的实现。
 */
@FunctionalInterface
public interface GridTargetResolver {
    Ae2GridTargetResult resolve(CommandSourceStack source);
}

package com.knightcode.appliedstoragesorter.ae2;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import appeng.api.networking.IGrid;
import appeng.blockentity.networking.ControllerBlockEntity;
import net.minecraft.core.BlockPos;

/**
 * 从 AE2 网格中计算稳定的网络标识。
 * 扫描所有控制器，按东南西北优先级取第一个作为标识坐标。
 * 这样无论从哪个入口（玩家命令/命令方块）触发，同一网络产生相同的标识。
 */
public final class GridNetworkLocator {
    private GridNetworkLocator() {
    }

    /**
     * 从网格中找出一个稳定的标识坐标。
     * 优先使用控制器坐标，若网格中无控制器则返回 null。
     * 扫描优先级：东优先于西，南优先于北。
     */
    @Nullable
    public static BlockPos locateAnchor(IGrid grid) {
        var controllers = grid.getMachines(ControllerBlockEntity.class);
        if (controllers.isEmpty()) {
            return null;
        }

        // 按东南西北优先级排序：东(高X) > 南(高Z) > 西(低X) > 北(低Z)
        List<ControllerBlockEntity> sorted = new ArrayList<>(controllers);
        sorted.sort(Comparator
                .<ControllerBlockEntity, Integer>comparing(
                        c -> c.getBlockPos().getX(), Comparator.reverseOrder())
                .thenComparing(
                        c -> c.getBlockPos().getZ(), Comparator.reverseOrder()));
        return sorted.getFirst().getBlockPos();
    }
}

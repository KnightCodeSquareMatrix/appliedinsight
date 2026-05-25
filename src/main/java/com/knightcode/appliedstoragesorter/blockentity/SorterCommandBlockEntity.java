package com.knightcode.appliedstoragesorter.blockentity;

import java.util.Set;

import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.networking.IManagedGridNode;
import appeng.api.util.AECableType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * AE2 网络命令执行方块的方块实体。
 * <p>
 * 遵循 AE2 官方 {@code AENetworkedBlockEntity} 的生命周期模式：
 * <ul>
 *   <li>{@code create()} 延迟到第一 tick 的 {@code onReady()} 中调用</li>
 *   <li>{@code destroy()} 在 {@code setRemoved()} 和 {@code onChunkUnloaded()} 中调用</li>
 *   <li>NBT 通过 {@code loadFromNBT/saveToNBT} 读写，在 {@code create()} 之前加载</li>
 * </ul>
 */
public class SorterCommandBlockEntity extends BlockEntity implements IInWorldGridNodeHost {
    private static final Component TITLE = Component.translatable("block.appliedstoragesorter.sorter_command_block");
    private static final int NODE_IDLE_POWER = 1;

    private final IManagedGridNode mainNode;

    public SorterCommandBlockEntity(BlockPos pos, BlockState state) {
        super(com.knightcode.appliedstoragesorter.registry.SorterBlockEntities.SORTER_COMMAND_BLOCK.get(), pos, state);
        this.mainNode = GridHelper.createManagedNode(this, new NodeListener())
                .setInWorldNode(true)
                .setExposedOnSides(Set.of(Direction.values()))
                .setFlags(GridFlags.REQUIRE_CHANNEL)
                .setIdlePowerUsage(NODE_IDLE_POWER);
    }

    public IManagedGridNode getMainNode() {
        return mainNode;
    }

    public boolean isNodeOnline() {
        return mainNode != null && mainNode.isOnline();
    }

    public void openMenu(Player player) {
        if (!isNodeOnline()) {
            player.sendSystemMessage(Component.literal("§c[错误] 方块未接入 ME 网络或频道不足！"));
            return;
        }
        player.openMenu(new SimpleMenuProvider(
                (containerId, playerInventory, menuPlayer) -> new com.knightcode.appliedstoragesorter.menu.SorterCommandBlockMenu(
                        containerId, playerInventory, this),
                TITLE),
                buf -> buf.writeBlockPos(getBlockPos()));
    }

    // --- AE2 Grid Node Lifecycle ---

    /**
     * 方块首次加载或放置时，注册第一 tick 回调。
     * 不在 onLoad 中直接 create，而是等 chunk 完全就绪后创建节点。
     */
    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide()) {
            GridHelper.onFirstTick(this, SorterCommandBlockEntity::onReady);
        }
    }

    /**
     * 第一 tick 回调，此时 chunk 已就绪，创建网格节点。
     */
    private static void onReady(SorterCommandBlockEntity self) {
        self.mainNode.create(self.level, self.worldPosition);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        mainNode.destroy();
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        mainNode.destroy();
    }

    // 注意：不需要覆写 clearRemoved()，onLoad() 已注册 first-tick 回调。
    // 同时覆写两者会导致 create() 被调用两次而崩溃。

    // --- NBT ---

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        mainNode.saveToNBT(tag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        mainNode.loadFromNBT(tag);
    }

    // --- IInWorldGridNodeHost ---

    @Override
    public IGridNode getGridNode(Direction dir) {
        return mainNode.getNode();
    }

    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.SMART;
    }

    // --- Node Listener ---

    private static final class NodeListener implements IGridNodeListener<SorterCommandBlockEntity> {
        @Override
        public void onSaveChanges(SorterCommandBlockEntity nodeOwner, IGridNode node) {
            nodeOwner.setChanged();
        }
    }
}

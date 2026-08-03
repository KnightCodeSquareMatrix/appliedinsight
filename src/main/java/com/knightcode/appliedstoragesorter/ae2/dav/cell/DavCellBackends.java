package com.knightcode.appliedstoragesorter.ae2.dav.cell;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class DavCellBackends {
    private DavCellBackends() {
    }

    @Nullable
    public static DavCellBackend resolve(Level level, ItemStack cellStack) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return null;
        }
        UUID cellId = DavCellStack.getCellId(cellStack);
        if (cellId == null) {
            return null;
        }
        return DavCellSavedData.get(serverLevel).openBackend(cellId);
    }

    public static Optional<DavCellBackend> resolveOptional(Level level, ItemStack cellStack) {
        return Optional.ofNullable(resolve(level, cellStack));
    }

    public static DavCellBackend getOrCreate(ServerLevel level, UUID cellId) {
        return DavCellSavedData.get(level).openBackend(cellId);
    }
}

package com.knightcode.appliedstoragesorter.ae2.dav.cell;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.Nullable;

public final class DavCellSavedData extends SavedData {
    private static final String CELLS_TAG = "Cells";
    private static final SavedData.Factory<DavCellSavedData> FACTORY = new SavedData.Factory<>(
            DavCellSavedData::new,
            DavCellSavedData::load,
            null);

    private final Map<UUID, DavCellState> cells = new HashMap<>();

    public DavCellSavedData() {
    }

    public static DavCellSavedData get(ServerLevel level) {
        DimensionDataStorage storage = level.getDataStorage();
        return storage.computeIfAbsent(FACTORY, DavCellConstants.SAVED_DATA_ID);
    }

    public DavCellState getOrCreate(UUID cellId) {
        return cells.computeIfAbsent(cellId, DavCellState::new);
    }

    @Nullable
    public DavCellState get(UUID cellId) {
        return cells.get(cellId);
    }

    public WorldDavCellBackend openBackend(UUID cellId) {
        var state = getOrCreate(cellId);
        return new WorldDavCellBackend(state, this::setDirty);
    }

    private static DavCellSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        var data = new DavCellSavedData();
        var list = tag.getList(CELLS_TAG, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            var state = DavCellState.load(list.getCompound(i), registries);
            data.cells.put(state.cellId(), state);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        var list = new ListTag();
        for (var state : cells.values()) {
            list.add(state.save(registries));
        }
        tag.put(CELLS_TAG, list);
        return tag;
    }
}

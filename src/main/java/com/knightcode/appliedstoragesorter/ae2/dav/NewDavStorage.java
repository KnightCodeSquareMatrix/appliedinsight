package com.knightcode.appliedstoragesorter.ae2.dav;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import com.knightcode.appliedstoragesorter.blockentity.DigitalAssetVaultBlockEntity;
import java.util.Objects;
import net.minecraft.network.chat.Component;

public class NewDavStorage implements MEStorage {
    private final DigitalAssetVaultBlockEntity owner;

    public NewDavStorage(DigitalAssetVaultBlockEntity owner) {
        this.owner = Objects.requireNonNull(owner, "owner");
    }

    @Override
    public boolean isPreferredStorageFor(AEKey key, IActionSource source) {
        if (!(key instanceof AEItemKey itemKey)) {
            return false;
        }
        if (!owner.isAutoAcceptIncoming()) {
            return false;
        }
        long freeBytes = Math.max(0, owner.getAbsorbedBytes() - owner.getUsedBytes());
        long freeTypes = Math.max(0, owner.getAbsorbedTypeCapacity() - owner.getUsedTypeCapacity());
        boolean newType = owner.wouldInsertNewType(itemKey);
        if (freeBytes <= 0) {
            return false;
        }
        return !newType || freeTypes > 0;
    }

    @Override
    public long insert(AEKey key, long amount, Actionable mode, IActionSource source) {
        MEStorage.checkPreconditions(key, amount, mode, source);
        if (!(key instanceof AEItemKey itemKey)) {
            return 0;
        }
        return owner.insertStoredItem(itemKey, amount, mode);
    }

    @Override
    public long extract(AEKey key, long amount, Actionable mode, IActionSource source) {
        MEStorage.checkPreconditions(key, amount, mode, source);
        if (!(key instanceof AEItemKey itemKey)) {
            return 0;
        }
        return owner.extractStoredItem(itemKey, amount, mode);
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        owner.writeStoredItemsTo(out);
    }

    @Override
    public Component getDescription() {
        return Component.translatable("block.appliedinsight.digital_asset_vault");
    }
}

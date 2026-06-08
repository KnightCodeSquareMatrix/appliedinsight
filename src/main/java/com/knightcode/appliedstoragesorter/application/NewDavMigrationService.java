package com.knightcode.appliedstoragesorter.application;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import com.knightcode.appliedstoragesorter.AppliedStorageSorter;
import com.knightcode.appliedstoragesorter.Config;
import com.knightcode.appliedstoragesorter.application.result.NewDavMigrationResult;
import com.knightcode.appliedstoragesorter.blockentity.DigitalAssetVaultBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;

public final class NewDavMigrationService {
    private static final String STATUS_NO_GRID =
            "screen.appliedinsight.digital_asset_vault.status.migration_no_grid";
    private static final String STATUS_NO_WORK =
            "screen.appliedinsight.digital_asset_vault.status.migration_no_work";
    private static final String STATUS_PENDING =
            "screen.appliedinsight.digital_asset_vault.status.migration_pending";

    private NewDavMigrationService() {
    }

    public static NewDavMigrationResult migrateOnce(DigitalAssetVaultBlockEntity owner, int maxTransfers) {
        var pos = owner != null ? owner.getBlockPos() : null;
        if (owner == null) {
            logIdle(pos, STATUS_NO_GRID, "owner is null");
            return NewDavMigrationResult.idle(STATUS_NO_GRID);
        }

        var mainNode = owner.getMainNode();
        if (mainNode == null || !mainNode.isActive()) {
            logIdle(pos, STATUS_NO_GRID,
                    "mainNode missing or inactive [nodeNull=%s, active=%s]"
                            .formatted(mainNode == null, mainNode != null && mainNode.isActive()));
            return NewDavMigrationResult.idle(STATUS_NO_GRID);
        }

        IGrid grid = mainNode.getGrid();
        if (grid == null) {
            logIdle(pos, STATUS_NO_GRID, "grid is null");
            return NewDavMigrationResult.idle(STATUS_NO_GRID);
        }

        if (owner.getAbsorbedBytes() <= 0) {
            logIdle(pos, STATUS_NO_WORK, "no absorbed capacity  [absorbedBytes=0]");
            return NewDavMigrationResult.idle(STATUS_NO_WORK);
        }

        var storageService = grid.getStorageService();
        if (storageService == null) {
            logIdle(pos, STATUS_NO_GRID, "storageService is null");
            return NewDavMigrationResult.idle(STATUS_NO_GRID);
        }

        MEStorage meStorage = storageService.getInventory();
        var availableStacks = meStorage.getAvailableStacks();
        if (availableStacks.isEmpty()) {
            logIdle(pos, STATUS_NO_WORK, "ME network inventory is empty");
            return NewDavMigrationResult.idle(STATUS_NO_WORK);
        }

        KeyCounter davStacks = new KeyCounter();
        owner.writeStoredItemsTo(davStacks);

        long freeBytes = Math.max(0, owner.getAbsorbedBytes() - owner.getUsedBytes());
        long freeTypes = Math.max(0, owner.getAbsorbedTypeCapacity() - owner.getUsedTypeCapacity());
        if (freeBytes <= 0 && freeTypes <= 0) {
            verbose(pos,
                    "skip scan  [reason=no-dav-capacity, networkStacks={}, davStacks={}, freeBytes={}, freeTypes={}]",
                    availableStacks.size(),
                    davStacks.size(),
                    freeBytes,
                    freeTypes);
            return NewDavMigrationResult.idle(STATUS_NO_WORK);
        }
        verbose(pos,
                "scanning ME inventory  [networkStacks={}, davStacks={}, freeBytes={}, freeTypes={}, maxTransfers={}]",
                availableStacks.size(),
                davStacks.size(),
                freeBytes,
                freeTypes,
                maxTransfers);

        IActionSource source = IActionSource.empty();
        int attemptedMoveCount = 0;
        int completedMoveCount = 0;
        long movedAmount = 0;
        int skippedAlreadyInDav = 0;
        int skippedNoCapacity = 0;
        int skippedExtractFailed = 0;
        int skippedNonItem = 0;

        for (var entry : availableStacks) {
            if (completedMoveCount >= maxTransfers) {
                verbose(pos, "reached maxTransfers={} for this cycle", maxTransfers);
                break;
            }
            if (!(entry.getKey() instanceof AEItemKey itemKey)) {
                skippedNonItem++;
                continue;
            }

            String itemId = describeItem(itemKey);
            long networkAmount = entry.getLongValue();
            long davAmount = davStacks.get(itemKey);
            long migratable = networkAmount - davAmount;
            if (migratable <= 0) {
                skippedAlreadyInDav++;
                verbose(pos, "skip {}  [reason=already-only-in-dav, network={}, dav={}]",
                        itemId, networkAmount, davAmount);
                continue;
            }

            if (freeTypes <= 0 && davAmount <= 0) {
                skippedNoCapacity++;
                verbose(pos, "skip {} x{}  [reason=no-type-slots, migratable={}]",
                        itemId, migratable, migratable);
                continue;
            }

            long insertable = owner.insertStoredItemForMigration(itemKey, migratable, Actionable.SIMULATE);
            if (insertable <= 0) {
                skippedNoCapacity++;
                verbose(pos, "skip {} x{}  [reason=no-dav-capacity, migratable={}]",
                        itemId, migratable, migratable);
                continue;
            }

            attemptedMoveCount++;
            AppliedStorageSorter.LOGGER.info(
                    "[DAV-Migrate] pos={} attempting {} x{}  [network={}, dav={}, insertable={}]",
                    pos,
                    itemId,
                    migratable,
                    networkAmount,
                    davAmount,
                    insertable);

            long extracted = meStorage.extract(itemKey, insertable, Actionable.MODULATE, source);
            if (extracted <= 0) {
                skippedExtractFailed++;
                AppliedStorageSorter.LOGGER.info(
                        "[DAV-Migrate] pos={} extract failed for {}  [requested={}]",
                        pos,
                        itemId,
                        insertable);
                continue;
            }

            long inserted = owner.insertStoredItemForMigration(itemKey, extracted, Actionable.MODULATE);
            if (inserted < extracted) {
                long rollback = extracted - inserted;
                long rolledBack = meStorage.insert(itemKey, rollback, Actionable.MODULATE, source);
                AppliedStorageSorter.LOGGER.warn(
                        "[DAV-Migrate] pos={} partial insert for {}  [extracted={}, inserted={}, rollback={}, rolledBack={}]",
                        pos,
                        itemId,
                        extracted,
                        inserted,
                        rollback,
                        rolledBack);
            }

            if (inserted > 0) {
                completedMoveCount++;
                movedAmount += inserted;
                davStacks.add(itemKey, inserted);
                AppliedStorageSorter.LOGGER.info(
                        "[DAV-Migrate] pos={} moved {} x{}  [extracted={}, inserted={}]",
                        pos,
                        itemId,
                        inserted,
                        extracted,
                        inserted);
            }
        }

        if (completedMoveCount > 0) {
            AppliedStorageSorter.LOGGER.info(
                    "[DAV-Migrate] pos={} cycle complete with transfers  [completed={}, attempted={}, movedAmount={}, skippedAlreadyInDav={}, skippedNoCapacity={}, skippedExtractFailed={}, skippedNonItem={}]",
                    pos,
                    completedMoveCount,
                    attemptedMoveCount,
                    movedAmount,
                    skippedAlreadyInDav,
                    skippedNoCapacity,
                    skippedExtractFailed,
                    skippedNonItem);
            return new NewDavMigrationResult(
                    completedMoveCount,
                    attemptedMoveCount,
                    completedMoveCount,
                    movedAmount,
                    true,
                    STATUS_PENDING);
        }

        verbose(pos,
                "cycle complete with no transfers  [attempted={}, skippedAlreadyInDav={}, skippedNoCapacity={}, skippedExtractFailed={}, skippedNonItem={}]",
                attemptedMoveCount,
                skippedAlreadyInDav,
                skippedNoCapacity,
                skippedExtractFailed,
                skippedNonItem);
        return NewDavMigrationResult.idle(STATUS_NO_WORK);
    }

    private static void logIdle(Object pos, String statusKey, String reason) {
        AppliedStorageSorter.LOGGER.info(
                "[DAV-Migrate] pos={} idle  [status={}, reason={}]",
                pos,
                statusKey,
                reason);
    }

    private static void verbose(Object pos, String fmt, Object... args) {
        if (Config.VERBOSE_LOGGING.get()) {
            AppliedStorageSorter.LOGGER.info("[DAV-Migrate] pos={} {}", pos, fmt.formatted(args));
        }
    }

    private static String describeItem(AEItemKey itemKey) {
        return BuiltInRegistries.ITEM.getKey(itemKey.getItem()).toString();
    }
}

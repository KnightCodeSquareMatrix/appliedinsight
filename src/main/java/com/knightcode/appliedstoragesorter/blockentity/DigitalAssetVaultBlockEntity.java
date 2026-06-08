package com.knightcode.appliedstoragesorter.blockentity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import com.google.common.collect.ImmutableSet;

import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.IStorageProvider;
import appeng.api.storage.StorageCells;
import appeng.api.storage.StorageHelper;
import appeng.api.storage.cells.ISaveProvider;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.me.helpers.MachineSource;
import appeng.util.SettingsFrom;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import appeng.util.inv.filter.IAEItemFilter;
import com.knightcode.appliedstoragesorter.AppliedStorageSorter;
import com.knightcode.appliedstoragesorter.Config;
import com.knightcode.appliedstoragesorter.ae2.CellCapacityInspector;
import com.knightcode.appliedstoragesorter.ae2.ExpansionCellCrafting;
import com.knightcode.appliedstoragesorter.application.NewDavMigrationService;
import com.knightcode.appliedstoragesorter.application.result.NewDavMigrationResult;
import com.knightcode.appliedstoragesorter.ae2.dav.NewDavStorage;
import com.knightcode.appliedstoragesorter.ae2.dav.NewDavStorageProvider;
import com.knightcode.appliedstoragesorter.menu.DigitalAssetVaultMenu;
import com.knightcode.appliedstoragesorter.registry.SorterBlockEntities;
import com.knightcode.appliedstoragesorter.registry.SorterItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class DigitalAssetVaultBlockEntity extends AEBaseBlockEntity
        implements InternalInventoryHost, ISaveProvider, IInWorldGridNodeHost, IActionHost, ICraftingRequester,
        ItemPersistable {
    private static final Component TITLE = Component.translatable("block.appliedinsight.digital_asset_vault");
    private static final String INVENTORY_TAG = "InputInventory";
    private static final String ABSORBED_CELL_COUNT_TAG = "AbsorbedCellCount";
    private static final String ABSORBED_BYTES_TAG = "AbsorbedBytes";
    private static final String ABSORBED_TYPE_CAPACITY_TAG = "AbsorbedTypeCapacity";
    private static final String LAST_STATUS_CODE_TAG = "LastStatusCode";
    private static final String GRID_NODE_TAG = "GridNode";
    private static final String STORED_ITEMS_TAG = "StoredItems";
    private static final String STORED_KEY_TAG = "Key";
    private static final String STORED_AMOUNT_TAG = "Amount";
    private static final String EXPANSION_CRAFTING_LINK_TAG = "ExpansionCraftingLink";
    private static final int NODE_IDLE_POWER = 1;
    private static final int DEFAULT_MIGRATION_TICK_INTERVAL = 20;
    private static final int MAX_MIGRATION_IDLE_BACKOFF_STREAK = 9;
    private static final int AUTO_EXPAND_BLOCKED_LOG_INTERVAL = 200;
    private static final int CRAFTABILITY_REFRESH_INTERVAL = 20;

    private final AppEngInternalInventory inputInventory = new AppEngInternalInventory(this, 1, 1);
    private final IActionSource actionSource;
    private final Map<AEItemKey, Long> storedItems = new LinkedHashMap<>();
    private final NewDavStorage storage = new NewDavStorage(this);
    private final NewDavStorageProvider storageProvider = new NewDavStorageProvider(storage);
    private final IManagedGridNode mainNode;
    private long absorbedCellCount;
    private long absorbedBytes;
    private long absorbedTypeCapacity;
    private boolean migrateExistingItems;
    private boolean autoAcceptIncoming = true;
    private int migrationTickInterval = DEFAULT_MIGRATION_TICK_INTERVAL;
    private int migrationCooldownTicks;
    private int migrationIdleStreak;
    private int autoExpandBlockedLogCooldown;
    private int autoExpandBusyLogCooldown;
    private NewDavMigrationResult lastMigrationResult = NewDavMigrationResult.idle("screen.appliedinsight.digital_asset_vault.status.migration_idle");
    private Status lastStatus = Status.IDLE;
    private boolean autoExpandEnabled;
    private String expansionCellId = "";
    private boolean expansionCellValid;
    private boolean expansionCellCraftable;
    private int craftabilityRefreshCooldown;
    private Future<ICraftingPlan> craftingFuture;
    private ICraftingLink expansionCraftingLink;

    public DigitalAssetVaultBlockEntity(BlockPos pos, BlockState state) {
        super(SorterBlockEntities.DIGITAL_ASSET_VAULT.get(), pos, state);
        inputInventory.setFilter(new CellOnlyFilter());
        actionSource = new MachineSource(this);
        mainNode = GridHelper.createManagedNode(this, new NodeListener())
                .setInWorldNode(true)
                .setExposedOnSides(Set.of(Direction.values()))
                .setFlags(GridFlags.REQUIRE_CHANNEL)
                .setIdlePowerUsage(NODE_IDLE_POWER)
                .setVisualRepresentation(SorterItems.DIGITAL_ASSET_VAULT.get())
                .addService(IStorageProvider.class, storageProvider)
                .addService(ICraftingRequester.class, this);
    }

    public AppEngInternalInventory getInputInventory() {
        return inputInventory;
    }

    public IManagedGridNode getMainNode() {
        return mainNode;
    }

    public long getAbsorbedCellCount() {
        return absorbedCellCount;
    }

    public long getAbsorbedBytes() {
        return absorbedBytes;
    }

    public long getAbsorbedTypeCapacity() {
        return absorbedTypeCapacity;
    }

    public long getUsedBytes() {
        return storedItems.values().stream().mapToLong(Long::longValue).sum();
    }

    public long getUsedTypeCapacity() {
        return storedItems.size();
    }

    public boolean isMigrateExistingItems() {
        return migrateExistingItems;
    }

    public void setMigrateExistingItems(boolean migrateExistingItems) {
        if (this.migrateExistingItems == migrateExistingItems) {
            return;
        }
        this.migrateExistingItems = migrateExistingItems;
        var node = mainNode;
        AppliedStorageSorter.LOGGER.info(
                "[DAV-Migrate] pos={} import-existing enabled={} gridActive={} absorbedBytes={}/{} absorbedTypes={}/{} storedTypes={} cooldownTicks={}",
                worldPosition,
                migrateExistingItems,
                node != null && node.isActive(),
                getUsedBytes(),
                absorbedBytes,
                getUsedTypeCapacity(),
                absorbedTypeCapacity,
                storedItems.size(),
                migrationCooldownTicks);
        setChanged();
    }

    public boolean isAutoAcceptIncoming() {
        return autoAcceptIncoming;
    }

    public boolean wouldInsertNewType(AEItemKey key) {
        return !storedItems.containsKey(key);
    }

    public void setAutoAcceptIncoming(boolean autoAcceptIncoming) {
        this.autoAcceptIncoming = autoAcceptIncoming;
        requestStorageUpdate();
        setChanged();
    }

    public boolean isAutoExpandEnabled() {
        return autoExpandEnabled;
    }

    public void setAutoExpandEnabled(boolean autoExpandEnabled) {
        if (this.autoExpandEnabled == autoExpandEnabled) {
            return;
        }
        this.autoExpandEnabled = autoExpandEnabled;
        if (!autoExpandEnabled) {
            cancelExpansionCrafting();
        } else {
            clearExpansionJobState();
        }
        validateExpansionCell();
        AppliedStorageSorter.LOGGER.info(
                "[DAV-Expand] pos={} auto-expand enabled={} cell={} craftable={} bytesUsed={}/{} typesUsed={}/{}",
                worldPosition,
                autoExpandEnabled,
                expansionCellId.isEmpty() ? "<none>" : expansionCellId,
                expansionCellCraftable,
                getUsedBytes(),
                absorbedBytes,
                getUsedTypeCapacity(),
                absorbedTypeCapacity);
        setChanged();
        if (autoExpandEnabled) {
            checkAutoExpandTrigger();
        }
    }

    public String getExpansionCellId() {
        return expansionCellId;
    }

    public void setExpansionCellId(String expansionCellId) {
        var nextId = expansionCellId != null ? expansionCellId : "";
        if (this.expansionCellId.equals(nextId)) {
            return;
        }
        this.expansionCellId = nextId;
        cancelExpansionCrafting();
        validateExpansionCell();
        AppliedStorageSorter.LOGGER.info(
                "[DAV-Expand] pos={} expansion cell set to {}  [valid={}, craftable={}]",
                worldPosition,
                nextId.isEmpty() ? "<none>" : nextId,
                expansionCellValid,
                expansionCellCraftable);
        setChanged();
        if (autoExpandEnabled) {
            checkAutoExpandTrigger();
        }
    }

    private void validateExpansionCell() {
        if (expansionCellId.isEmpty()) {
            expansionCellValid = false;
            expansionCellCraftable = false;
            return;
        }
        ResourceLocation rl;
        try {
            rl = ResourceLocation.parse(expansionCellId);
        } catch (Exception e) {
            expansionCellValid = false;
            expansionCellCraftable = false;
            return;
        }
        var item = BuiltInRegistries.ITEM.get(rl);
        if (item == null) {
            expansionCellValid = false;
            expansionCellCraftable = false;
            return;
        }
        var stack = new ItemStack(item);
        expansionCellValid = StorageCells.isCellHandled(stack);
        if (expansionCellValid) {
            var grid = mainNode.getGrid();
            if (grid != null && mainNode.isActive()) {
                var key = AEItemKey.of(item);
                var craftingService = grid.getCraftingService();
                boolean craftable = key != null && ExpansionCellCrafting.hasCraftingPattern(craftingService, key);
                if (craftable != expansionCellCraftable) {
                    logAutoExpandVerbose("craftability changed to {}  [cell={}, gridActive={}]",
                            craftable,
                            expansionCellId,
                            mainNode.isActive());
                }
                expansionCellCraftable = craftable;
            } else {
                expansionCellCraftable = false;
            }
        } else {
            expansionCellCraftable = false;
        }
    }

    public boolean isActiveExpansionJob() {
        return isExpansionJobActive();
    }

    private boolean isExpansionJobActive() {
        return craftingFuture != null || expansionCraftingLink != null;
    }

    @Override
    public IGridNode getActionableNode() {
        return mainNode.getNode();
    }

    @Override
    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        return expansionCraftingLink != null ? ImmutableSet.of(expansionCraftingLink) : ImmutableSet.of();
    }

    @Override
    public long insertCraftedItems(ICraftingLink link, AEKey what, long amount, Actionable mode) {
        if (level == null || level.isClientSide() || link != expansionCraftingLink || !autoExpandEnabled
                || expansionCellId.isEmpty() || amount <= 0) {
            return 0;
        }
        if (!(what instanceof AEItemKey itemKey)) {
            return 0;
        }
        Item expectedItem;
        try {
            expectedItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(expansionCellId));
        } catch (Exception e) {
            return 0;
        }
        if (expectedItem == null || itemKey.getItem() != expectedItem) {
            logAutoExpandVerbose("ignoring crafted item {} for expansion cell {}", itemKey, expansionCellId);
            return 0;
        }
        long toAccept = Math.min(amount, 1L);
        if (mode == Actionable.SIMULATE) {
            return toAccept;
        }
        var stack = itemKey.toStack((int) toAccept);
        if (completeExpansionCellAbsorb(stack, "crafting-output")) {
            return toAccept;
        }
        return 0;
    }

    @Override
    public void jobStateChange(ICraftingLink link) {
        if (link != expansionCraftingLink) {
            return;
        }
        expansionCraftingLink = null;
        craftingFuture = null;
        if (link.isCanceled()) {
            logAutoExpand("crafting job canceled  [cell={}]", expansionCellId);
            setStatus(Status.AUTO_EXPAND_FAILED);
        }
        setChanged();
    }

    public boolean isExpansionCellValid() {
        return expansionCellValid;
    }

    public boolean isExpansionCellCraftable() {
        return expansionCellCraftable;
    }

    public int getMigrationTickInterval() {
        return migrationTickInterval;
    }

    public NewDavMigrationResult getLastMigrationResult() {
        return lastMigrationResult;
    }

    public Status getLastStatus() {
        return lastStatus;
    }

    public void openMenu(Player player) {
        player.openMenu(new SimpleMenuProvider(
                (containerId, playerInventory, menuPlayer) -> new DigitalAssetVaultMenu(containerId, playerInventory,
                        this),
                TITLE),
                buffer -> {
                    buffer.writeBlockPos(getBlockPos());
                    buffer.writeBoolean(autoExpandEnabled);
                    buffer.writeUtf(expansionCellId);
                    buffer.writeBoolean(expansionCellValid);
                    buffer.writeBoolean(expansionCellCraftable);
                });
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DigitalAssetVaultBlockEntity blockEntity) {
        blockEntity.tryAbsorbInputCell();
        blockEntity.tickMigrationLoop();
        blockEntity.tickCraftabilityRefresh();
        blockEntity.tickAutoExpandPoll();
        blockEntity.checkAutoExpandTrigger();
    }

    private void tickCraftabilityRefresh() {
        if (level == null || level.isClientSide() || expansionCellId.isEmpty()) {
            return;
        }
        if (--craftabilityRefreshCooldown > 0) {
            return;
        }
        craftabilityRefreshCooldown = CRAFTABILITY_REFRESH_INTERVAL;
        boolean previous = expansionCellCraftable;
        validateExpansionCell();
        if (previous != expansionCellCraftable) {
            setChanged();
        }
    }

    @Override
    public void saveToItemStack(ItemStack stack, HolderLookup.Provider registries) {
        var tag = new CompoundTag();
        writeBreakPersistedData(tag, registries);
        if (tag.isEmpty()) {
            return;
        }
        BlockEntity.addEntityType(tag, getType());
        stack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(tag));
    }

    @Override
    public void loadFromItemStack(ItemStack stack, HolderLookup.Provider registries) {
        var blockEntityData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (blockEntityData == null) {
            return;
        }
        readBreakPersistedData(blockEntityData.copyTag(), registries);
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        // Inventory is persisted on the block item stack.
    }

    @Override
    public void exportSettings(SettingsFrom mode, DataComponentMap.Builder builder, @Nullable Player player) {
        super.exportSettings(mode, builder, player);
        if (mode != SettingsFrom.DISMANTLE_ITEM || level == null) {
            return;
        }
        var stack = new ItemStack(getItemFromBlockEntity());
        saveToItemStack(stack, level.registryAccess());
        var blockEntityData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (blockEntityData != null) {
            builder.set(DataComponents.BLOCK_ENTITY_DATA, blockEntityData);
        }
    }

    @Override
    public void clearContent() {
        inputInventory.clear();
        storedItems.clear();
    }

    public long insertStoredItem(AEItemKey key, long amount, Actionable mode) {
        if (!autoAcceptIncoming && mode == Actionable.MODULATE) {
            return 0;
        }
        return insertStoredItemUnchecked(key, amount, mode);
    }

    public long insertStoredItemForMigration(AEItemKey key, long amount, Actionable mode) {
        return insertStoredItemUnchecked(key, amount, mode);
    }

    private long insertStoredItemUnchecked(AEItemKey key, long amount, Actionable mode) {
        if (amount <= 0) {
            return 0;
        }
        long freeBytes = Math.max(0, absorbedBytes - getUsedBytes());
        long freeTypes = Math.max(0, absorbedTypeCapacity - getUsedTypeCapacity());
        boolean newType = !storedItems.containsKey(key);
        if (newType && freeTypes <= 0) {
            return 0;
        }
        long accepted = Math.min(amount, freeBytes);
        if (accepted <= 0) {
            return 0;
        }
        if (mode == Actionable.MODULATE) {
            storedItems.merge(key, accepted, Math::addExact);
            markStorageChanged();
        }
        return accepted;
    }

    public long extractStoredItem(AEItemKey key, long amount, Actionable mode) {
        if (amount <= 0) {
            return 0;
        }
        long stored = storedItems.getOrDefault(key, 0L);
        long extracted = Math.min(amount, stored);
        if (extracted <= 0) {
            return 0;
        }
        if (mode == Actionable.MODULATE) {
            long remaining = stored - extracted;
            if (remaining > 0) {
                storedItems.put(key, remaining);
            } else {
                storedItems.remove(key);
            }
            markStorageChanged();
        }
        return extracted;
    }

    public void writeStoredItemsTo(KeyCounter out) {
        storedItems.forEach(out::add);
    }

    private void tickMigrationLoop() {
        if (level == null || level.isClientSide() || !migrateExistingItems) {
            return;
        }
        if (migrationCooldownTicks > 0) {
            migrationCooldownTicks--;
            if (Config.VERBOSE_LOGGING.get() && migrationCooldownTicks == 0) {
                AppliedStorageSorter.LOGGER.info(
                        "[DAV-Migrate] pos={} cooldown finished, next migration cycle starting",
                        worldPosition);
            }
            return;
        }
        logMigrationVerbose("starting migration cycle  [intervalTicks={}, idleStreak={}, maxTransfers=16]",
                migrationTickInterval,
                migrationIdleStreak);
        lastMigrationResult = NewDavMigrationService.migrateOnce(this, 16);
        if (lastMigrationResult.foundWork()) {
            migrationIdleStreak = 0;
            migrationCooldownTicks = migrationTickInterval;
            AppliedStorageSorter.LOGGER.info(
                    "[DAV-Migrate] pos={} cycle result  [foundWork=true, completed={}, movedAmount={}, status={}]",
                    worldPosition,
                    lastMigrationResult.completedMoveCount(),
                    lastMigrationResult.movedAmount(),
                    lastMigrationResult.statusKey());
            setStatus(Status.MIGRATION_PENDING);
        } else {
            migrationIdleStreak = Math.min(migrationIdleStreak + 1, MAX_MIGRATION_IDLE_BACKOFF_STREAK);
            migrationCooldownTicks = migrationTickInterval * (1 + migrationIdleStreak);
            logMigrationVerbose(
                    "cycle result  [foundWork=false, completed=0, movedAmount=0, status={}, nextCooldownTicks={}]",
                    lastMigrationResult.statusKey(),
                    migrationCooldownTicks);
            if (lastMigrationResult.statusKey().equals(
                    "screen.appliedinsight.digital_asset_vault.status.migration_no_work")) {
                setStatus(Status.IDLE);
            }
        }
    }

    private void checkAutoExpandTrigger() {
        if (autoExpandBlockedLogCooldown > 0) {
            autoExpandBlockedLogCooldown--;
        }
        if (autoExpandBusyLogCooldown > 0) {
            autoExpandBusyLogCooldown--;
        }
        long usedB = getUsedBytes();
        long usedT = getUsedTypeCapacity();
        double bytesThreshold = Config.DAV_AUTO_EXPAND_BYTES_THRESHOLD.get();
        double typesThreshold = Config.DAV_AUTO_EXPAND_TYPES_THRESHOLD.get();
        boolean bytesTriggered = absorbedBytes > 0 && (double) usedB / (double) absorbedBytes >= bytesThreshold;
        boolean typesTriggered = absorbedTypeCapacity > 0 && (double) usedT / (double) absorbedTypeCapacity >= typesThreshold;
        if (!bytesTriggered && !typesTriggered) {
            return;
        }

        if (!autoExpandEnabled || expansionCellId.isEmpty()) {
            if (autoExpandBlockedLogCooldown <= 0) {
                autoExpandBlockedLogCooldown = AUTO_EXPAND_BLOCKED_LOG_INTERVAL;
                AppliedStorageSorter.LOGGER.info(
                        "[DAV-Expand] pos={} threshold exceeded but auto-expand is not ready  [autoExpandEnabled={}, cell={}, types={}/{} ({}%), bytes={}/{} ({}%)]",
                        worldPosition,
                        autoExpandEnabled,
                        expansionCellId.isEmpty() ? "<none>" : expansionCellId,
                        usedT,
                        absorbedTypeCapacity,
                        formatPercent(absorbedTypeCapacity > 0 ? (double) usedT / absorbedTypeCapacity : 0.0),
                        usedB,
                        absorbedBytes,
                        formatPercent(absorbedBytes > 0 ? (double) usedB / absorbedBytes : 0.0));
            }
            return;
        }
        if (isExpansionJobActive()) {
            if (autoExpandBusyLogCooldown <= 0) {
                autoExpandBusyLogCooldown = AUTO_EXPAND_BLOCKED_LOG_INTERVAL;
                logAutoExpandVerbose(
                        "threshold exceeded but expansion job is busy  [craftingFuture={}, craftingLink={}]",
                        craftingFuture != null,
                        expansionCraftingLink != null);
            }
            return;
        }

        var grid = mainNode.getGrid();
        if (grid == null) {
            logAutoExpandVerbose("threshold hit but ME grid is not connected  [bytesTriggered={}, typesTriggered={}]",
                    bytesTriggered, typesTriggered);
            return;
        }
        var item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(expansionCellId));
        if (item == null) {
            logAutoExpand("threshold hit but expansion cell item is unknown  [cell={}]", expansionCellId);
            return;
        }
        var key = AEItemKey.of(item);
        if (key == null) {
            logAutoExpand("threshold hit but expansion cell key is invalid  [cell={}]", expansionCellId);
            return;
        }

        var craftingService = grid.getCraftingService();
        if (craftingService == null) {
            logAutoExpand("threshold hit but crafting service is unavailable  [cell={}]", expansionCellId);
            return;
        }
        if (tryAbsorbExpansionCellFromNetwork(grid, item, "network-storage")) {
            return;
        }

        var craftKey = ExpansionCellCrafting.resolveCraftingKey(craftingService, key);
        if (craftKey == null) {
            logAutoExpand("threshold hit but no crafting pattern found  [cell={}, exactPatterns={}, craftableItems={}]",
                    expansionCellId,
                    craftingService.getCraftingFor(key).size(),
                    craftingService.getCraftables(AEItemKey.filter()).size());
            setStatus(Status.AUTO_EXPAND_NO_PATTERN);
            return;
        }

        double bytesUsage = absorbedBytes > 0 ? (double) usedB / (double) absorbedBytes : 0.0;
        double typesUsage = absorbedTypeCapacity > 0 ? (double) usedT / (double) absorbedTypeCapacity : 0.0;
        AppliedStorageSorter.LOGGER.info(
                "[DAV-Expand] pos={} threshold hit, starting craft  [cell={}, bytes={}/{} ({}% >= {}%), types={}/{} ({}% >= {}%), triggeredBy={}]",
                worldPosition,
                expansionCellId,
                usedB,
                absorbedBytes,
                formatPercent(bytesUsage),
                formatPercent(bytesThreshold),
                usedT,
                absorbedTypeCapacity,
                formatPercent(typesUsage),
                formatPercent(typesThreshold),
                bytesTriggered ? (typesTriggered ? "bytes+types" : "bytes") : "types");

        setStatus(Status.AUTO_EXPAND_CRAFTING);

        if (!craftKey.equals(key)) {
            logAutoExpandVerbose("using fuzzy crafting key  [cell={}, craftKey={}]", expansionCellId, craftKey);
        }
        craftingFuture = craftingService.beginCraftingCalculation(
                level, () -> actionSource, craftKey, 1, CalculationStrategy.CRAFT_LESS);
        setChanged();
    }

    private void tickAutoExpandPoll() {
        if (craftingFuture == null || level == null || level.isClientSide()) {
            return;
        }
        if (!craftingFuture.isDone()) {
            return;
        }

        ICraftingPlan plan;
        try {
            plan = craftingFuture.get();
        } catch (Exception e) {
            clearExpansionJobState();
            logAutoExpand("craft calculation failed  [cell={}, error={}]", expansionCellId, e.toString());
            setStatus(Status.AUTO_EXPAND_FAILED);
            return;
        }
        craftingFuture = null;

        if (plan == null) {
            clearExpansionJobState();
            logAutoExpand("craft calculation returned no plan  [cell={}]", expansionCellId);
            setStatus(Status.AUTO_EXPAND_FAILED);
            return;
        }
        if (plan.simulation()) {
            clearExpansionJobState();
            logAutoExpand("craft plan is simulation only (missing ingredients?)  [cell={}, missingItems={}]",
                    expansionCellId,
                    plan.missingItems().size());
            setStatus(Status.AUTO_EXPAND_FAILED);
            return;
        }

        var grid = mainNode.getGrid();
        if (grid == null) {
            clearExpansionJobState();
            logAutoExpand("craft plan ready but ME grid disconnected  [cell={}]", expansionCellId);
            return;
        }
        var craftingService = grid.getCraftingService();
        if (craftingService == null) {
            clearExpansionJobState();
            logAutoExpand("craft plan ready but crafting service unavailable  [cell={}]", expansionCellId);
            return;
        }

        var result = craftingService.submitJob(plan, this, null, false, actionSource);
        if (!result.successful()) {
            clearExpansionJobState();
            logAutoExpand("craft job submission failed  [cell={}, error={}, detail={}]",
                    expansionCellId,
                    result.errorCode(),
                    result.errorDetail());
            setStatus(Status.AUTO_EXPAND_FAILED);
            return;
        }
        expansionCraftingLink = result.link();
        logAutoExpand("craft job submitted, waiting for crafted cell  [cell={}, link={}]",
                expansionCellId,
                expansionCraftingLink != null ? expansionCraftingLink.getCraftingID() : null);
        setChanged();
    }

    private void cancelExpansionCrafting() {
        if (expansionCraftingLink != null) {
            expansionCraftingLink.cancel();
        }
        if (craftingFuture != null) {
            craftingFuture.cancel(true);
        }
        clearExpansionJobState();
    }

    private void clearExpansionJobState() {
        craftingFuture = null;
        expansionCraftingLink = null;
    }

    private boolean tryAbsorbExpansionCellFromNetwork(IGrid grid, Item item, String source) {
        var stack = ExpansionCellCrafting.extractOneStackFromNetwork(grid, item);
        if (stack.isEmpty()) {
            return false;
        }
        if (!completeExpansionCellAbsorb(stack, source)) {
            return false;
        }
        logAutoExpand("absorbed cell from {} without crafting  [cell={}]", source, expansionCellId);
        return true;
    }

    private boolean completeExpansionCellAbsorb(ItemStack stack, String source) {
        if (stack.isEmpty() || !StorageCells.isCellHandled(stack)) {
            logAutoExpand("extracted stack is not a storage cell  [cell={}, source={}]", expansionCellId, source);
            return false;
        }
        var cell = StorageCells.getCellInventory(stack, this);
        if (cell == null) {
            logAutoExpand("failed to open extracted cell inventory  [cell={}, source={}]", expansionCellId, source);
            return false;
        }
        var cellItemId = stack.getItemHolder().unwrapKey().map(k -> k.location().toString()).orElse(null);
        var capacity = CellCapacityInspector.inspect(cell);
        if (CellCapacityInspector.isLikelyInfiniteCell(capacity, cellItemId)
                || capacity == null || capacity.totalBytes() <= 0
                || capacity.totalItemTypes() == null || capacity.totalItemTypes() <= 0) {
            logAutoExpand("extracted cell has invalid capacity  [cell={}, source={}, capacity={}]",
                    expansionCellId,
                    source,
                    capacity);
            return false;
        }
        if (capacity.usedBytes() > 0 || !cell.getAvailableStacks().isEmpty()) {
            logAutoExpand("extracted cell is not empty  [cell={}, source={}, usedBytes={}]",
                    expansionCellId,
                    source,
                    capacity.usedBytes());
            return false;
        }
        long previousCellCount = absorbedCellCount;
        long previousBytes = absorbedBytes;
        long previousTypeCapacity = absorbedTypeCapacity;
        absorbedCellCount = Math.addExact(absorbedCellCount, 1L);
        absorbedBytes = Math.addExact(absorbedBytes, capacity.totalBytes());
        absorbedTypeCapacity = Math.addExact(absorbedTypeCapacity, capacity.totalItemTypes().longValue());
        migrationIdleStreak = 0;
        clearExpansionJobState();
        requestStorageUpdate();
        setStatus(Status.AUTO_EXPAND_COMPLETED);
        AppliedStorageSorter.LOGGER.info(
                "[DAV-Expand] pos={} cell absorbed  [source={}, cell={}, addedBytes={}, addedTypes={}, cells={} -> {}, bytes={} -> {}, types={} -> {}, usedBytes={}/{}, usedTypes={}/{}]",
                worldPosition,
                source,
                expansionCellId,
                capacity.totalBytes(),
                capacity.totalItemTypes(),
                previousCellCount,
                absorbedCellCount,
                previousBytes,
                absorbedBytes,
                previousTypeCapacity,
                absorbedTypeCapacity,
                getUsedBytes(),
                absorbedBytes,
                getUsedTypeCapacity(),
                absorbedTypeCapacity);
        setChanged();
        return true;
    }

    private static String formatPercent(double ratio) {
        return String.format(Locale.ROOT, "%.1f", ratio * 100.0);
    }

    private void logAutoExpand(String message, Object... args) {
        AppliedStorageSorter.LOGGER.info("[DAV-Expand] pos={} " + message, prependLogArgs(args));
    }

    private void logAutoExpandVerbose(String message, Object... args) {
        if (Config.VERBOSE_LOGGING.get()) {
            logAutoExpand(message, args);
        }
    }

    private void logMigrationVerbose(String message, Object... args) {
        if (Config.VERBOSE_LOGGING.get()) {
            AppliedStorageSorter.LOGGER.info("[DAV-Migrate] pos={} " + message, prependLogArgs(args));
        }
    }

    private Object[] prependLogArgs(Object... args) {
        var merged = new Object[args.length + 1];
        merged[0] = worldPosition;
        System.arraycopy(args, 0, merged, 1, args.length);
        return merged;
    }

    private void tryAbsorbInputCell() {
        if (level == null || level.isClientSide()) {
            return;
        }

        var stack = inputInventory.getStackInSlot(0);
        if (stack.isEmpty()) {
            setStatus(Status.IDLE);
            return;
        }
        if (!StorageCells.isCellHandled(stack)) {
            setStatus(Status.NOT_STORAGE_CELL);
            return;
        }
        if (stack.getCount() != 1) {
            setStatus(Status.STACK_COUNT_NOT_ONE);
            return;
        }

        var cell = StorageCells.getCellInventory(stack, this);
        if (cell == null) {
            setStatus(Status.UNKNOWN_CAPACITY);
            return;
        }

        var cellItemId = stack.getItemHolder().unwrapKey()
                .map(key -> key.location().toString())
                .orElse(null);
        var capacity = CellCapacityInspector.inspect(cell);
        if (CellCapacityInspector.isLikelyInfiniteCell(capacity, cellItemId)) {
            setStatus(Status.INFINITE_CELL);
            return;
        }
        if (capacity == null || capacity.totalBytes() <= 0) {
            setStatus(Status.UNKNOWN_CAPACITY);
            return;
        }
        if (capacity.totalItemTypes() == null || capacity.totalItemTypes() <= 0) {
            setStatus(Status.UNKNOWN_TYPE_CAPACITY);
            return;
        }
        if (capacity.usedBytes() > 0 || !cell.getAvailableStacks().isEmpty()) {
            setStatus(Status.NON_EMPTY_CELL);
            return;
        }

        absorbedCellCount = Math.addExact(absorbedCellCount, 1L);
        absorbedBytes = Math.addExact(absorbedBytes, capacity.totalBytes());
        absorbedTypeCapacity = Math.addExact(absorbedTypeCapacity, capacity.totalItemTypes().longValue());
        migrationIdleStreak = 0;
        inputInventory.setItemDirect(0, ItemStack.EMPTY);
        requestStorageUpdate();
        setStatus(Status.ABSORBED);
    }

    private void markStorageChanged() {
        setChanged();
        requestStorageUpdate();
    }

    private void requestStorageUpdate() {
        if (level != null && !level.isClientSide()) {
            IStorageProvider.requestUpdate(mainNode);
        }
    }

    private void setStatus(Status status) {
        if (lastStatus != status) {
            lastStatus = status;
            setChanged();
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        writeBreakPersistedData(tag, registries);
        var nodeTag = new CompoundTag();
        mainNode.saveToNBT(nodeTag);
        tag.put(GRID_NODE_TAG, nodeTag);
        tag.putInt("MigrationCooldownTicks", migrationCooldownTicks);
        if (expansionCraftingLink != null) {
            var linkTag = new CompoundTag();
            expansionCraftingLink.writeToNBT(linkTag);
            tag.put(EXPANSION_CRAFTING_LINK_TAG, linkTag);
        }
    }

    @Override
    public void loadTag(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadTag(tag, registries);
        readBreakPersistedData(tag, registries);
        if (tag.contains(GRID_NODE_TAG, Tag.TAG_COMPOUND)) {
            mainNode.loadFromNBT(tag.getCompound(GRID_NODE_TAG));
        }
        migrationCooldownTicks = Math.max(0, tag.getInt("MigrationCooldownTicks"));
        expansionCraftingLink = null;
        craftingFuture = null;
        if (tag.contains(EXPANSION_CRAFTING_LINK_TAG, Tag.TAG_COMPOUND)) {
            expansionCraftingLink = StorageHelper.loadCraftingLink(tag.getCompound(EXPANSION_CRAFTING_LINK_TAG), this);
            if (expansionCraftingLink != null && lastStatus != Status.AUTO_EXPAND_COMPLETED) {
                lastStatus = Status.AUTO_EXPAND_CRAFTING;
            }
        }
    }

    private void writeBreakPersistedData(CompoundTag tag, HolderLookup.Provider registries) {
        inputInventory.writeToNBT(tag, INVENTORY_TAG, registries);
        saveStoredItems(tag, registries);
        tag.putLong(ABSORBED_CELL_COUNT_TAG, absorbedCellCount);
        tag.putLong(ABSORBED_BYTES_TAG, absorbedBytes);
        tag.putLong(ABSORBED_TYPE_CAPACITY_TAG, absorbedTypeCapacity);
        tag.putBoolean("MigrateExistingItems", migrateExistingItems);
        tag.putBoolean("AutoAcceptIncoming", autoAcceptIncoming);
        tag.putInt("MigrationTickInterval", migrationTickInterval);
        tag.putInt(LAST_STATUS_CODE_TAG, lastStatus.ordinal());
        tag.putBoolean("AutoExpandEnabled", autoExpandEnabled);
        tag.putString("ExpansionCellId", expansionCellId);
        tag.putBoolean("ExpansionCellValid", expansionCellValid);
        tag.putBoolean("ExpansionCellCraftable", expansionCellCraftable);
    }

    private void readBreakPersistedData(CompoundTag tag, HolderLookup.Provider registries) {
        inputInventory.readFromNBT(tag, INVENTORY_TAG, registries);
        loadStoredItems(tag, registries);
        absorbedCellCount = tag.getLong(ABSORBED_CELL_COUNT_TAG);
        absorbedBytes = tag.getLong(ABSORBED_BYTES_TAG);
        absorbedTypeCapacity = tag.getLong(ABSORBED_TYPE_CAPACITY_TAG);
        migrateExistingItems = tag.getBoolean("MigrateExistingItems");
        autoAcceptIncoming = !tag.contains("AutoAcceptIncoming") || tag.getBoolean("AutoAcceptIncoming");
        migrationTickInterval = tag.contains("MigrationTickInterval")
                ? Math.max(1, tag.getInt("MigrationTickInterval"))
                : DEFAULT_MIGRATION_TICK_INTERVAL;
        migrationCooldownTicks = 0;
        migrationIdleStreak = 0;
        lastStatus = Status.fromOrdinal(tag.getInt(LAST_STATUS_CODE_TAG));
        autoExpandEnabled = tag.getBoolean("AutoExpandEnabled");
        expansionCellId = tag.contains("ExpansionCellId") ? tag.getString("ExpansionCellId") : "";
        expansionCellValid = tag.getBoolean("ExpansionCellValid");
        expansionCellCraftable = tag.getBoolean("ExpansionCellCraftable");
        craftingFuture = null;
        expansionCraftingLink = null;
    }

    private void saveStoredItems(CompoundTag tag, HolderLookup.Provider registries) {
        var list = new ListTag();
        storedItems.forEach((key, amount) -> {
            var entry = new CompoundTag();
            entry.put(STORED_KEY_TAG, key.toTagGeneric(registries));
            entry.putLong(STORED_AMOUNT_TAG, amount);
            list.add(entry);
        });
        tag.put(STORED_ITEMS_TAG, list);
    }

    private void loadStoredItems(CompoundTag tag, HolderLookup.Provider registries) {
        storedItems.clear();
        var list = tag.getList(STORED_ITEMS_TAG, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            var entry = list.getCompound(i);
            var key = AEKey.fromTagGeneric(registries, entry.getCompound(STORED_KEY_TAG));
            long amount = entry.getLong(STORED_AMOUNT_TAG);
            if (key instanceof AEItemKey itemKey && amount > 0) {
                storedItems.put(itemKey, amount);
            }
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide()) {
            GridHelper.onFirstTick(this, DigitalAssetVaultBlockEntity::onFirstTickReady);
        }
    }

    private static void onFirstTickReady(DigitalAssetVaultBlockEntity self) {
        self.mainNode.create(self.level, self.worldPosition);
        self.validateExpansionCell();
    }

    @Override
    public void setRemoved() {
        if (level != null && !level.isClientSide()) {
            cancelExpansionCrafting();
        }
        super.setRemoved();
        mainNode.destroy();
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        mainNode.destroy();
    }

    @Override
    public IGridNode getGridNode(Direction dir) {
        return mainNode.getNode();
    }

    @Override
    public void saveChangedInventory(AppEngInternalInventory inventory) {
        setChanged();
    }

    @Override
    public void onChangeInventory(AppEngInternalInventory inventory, int slot) {
        setChanged();
    }

    @Override
    public boolean isClientSide() {
        return level != null && level.isClientSide();
    }

    @Override
    public void saveChanges() {
        setChanged();
    }

    public enum Status {
        IDLE("idle"),
        ABSORBED("absorbed"),
        NOT_STORAGE_CELL("not_storage_cell"),
        STACK_COUNT_NOT_ONE("stack_count_not_one"),
        NON_EMPTY_CELL("non_empty_cell"),
        UNKNOWN_CAPACITY("unknown_capacity"),
        UNKNOWN_TYPE_CAPACITY("unknown_type_capacity"),
        INFINITE_CELL("infinite_cell"),
        MIGRATION_PENDING("migration_pending"),
        AUTO_EXPAND_CRAFTING("auto_expand_crafting"),
        AUTO_EXPAND_COMPLETED("auto_expand_completed"),
        AUTO_EXPAND_NO_PATTERN("auto_expand_no_pattern"),
        AUTO_EXPAND_FAILED("auto_expand_failed");

        private final String translationKeySuffix;

        Status(String translationKeySuffix) {
            this.translationKeySuffix = translationKeySuffix;
        }

        public String translationKey() {
            return "screen.appliedinsight.digital_asset_vault.status." + translationKeySuffix;
        }

        public static Status fromOrdinal(int ordinal) {
            var values = values();
            if (ordinal < 0 || ordinal >= values.length) {
                return IDLE;
            }
            return values[ordinal];
        }
    }

    private static class CellOnlyFilter implements IAEItemFilter {
        @Override
        public boolean allowExtract(appeng.api.inventories.InternalInventory inv, int slot, int amount) {
            return true;
        }

        @Override
        public boolean allowInsert(appeng.api.inventories.InternalInventory inv, int slot, ItemStack stack) {
            return StorageCells.isCellHandled(stack);
        }
    }

    private static final class NodeListener implements IGridNodeListener<DigitalAssetVaultBlockEntity> {
        @Override
        public void onSaveChanges(DigitalAssetVaultBlockEntity nodeOwner, IGridNode node) {
            nodeOwner.setChanged();
        }
    }
}

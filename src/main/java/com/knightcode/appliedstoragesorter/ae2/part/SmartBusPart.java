package com.knightcode.appliedstoragesorter.ae2.part;

import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.MEStorage;
import appeng.core.settings.TickRates;
import appeng.me.helpers.MachineSource;
import appeng.parts.AEBasePart;
import appeng.parts.PartAdjacentApi;
import appeng.parts.PartModel;
import com.knightcode.appliedstoragesorter.AppliedStorageSorter;
import com.knightcode.appliedstoragesorter.Config;
import com.knightcode.appliedstoragesorter.block.SmartBusMode;
import com.knightcode.appliedstoragesorter.menu.SmartBusMenu;
import com.knightcode.appliedstoragesorter.rule.filter.FilterExpression;
import com.knightcode.appliedstoragesorter.rule.filter.FilterExpressionJsonCodec;
import com.knightcode.appliedstoragesorter.rule.filter.ItemFilter;
import com.knightcode.appliedstoragesorter.rule.filter.ItemFilterMatcher;
import com.knightcode.appliedstoragesorter.rule.filter.ItemMatchContext;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.capabilities.Capabilities;

public class SmartBusPart extends AEBasePart implements IGridTickable {

    private static final String TAG_MODE = "smart_mode";
    private static final String TAG_FILTER = "smart_filter";
    private static final ResourceLocation WRENCH_TAG_ID = ResourceLocation.fromNamespaceAndPath("c", "tools/wrench");
    private static final TickRates IMPORT_TICK_RATES = TickRates.ImportBus;

    private int stackTransfersPerTick() {
        return Config.SMART_BUS_STACK_TRANSFERS_PER_TICK.get();
    }

    @appeng.items.parts.PartModels
    public static final IPartModel MODEL_EMPTY = new PartModel(
            ResourceLocation.fromNamespaceAndPath(AppliedStorageSorter.MODID, "part/smart_bus_empty"));
    @appeng.items.parts.PartModels
    public static final IPartModel MODEL_IMPORT = new PartModel(
            ResourceLocation.fromNamespaceAndPath(AppliedStorageSorter.MODID, "part/smart_bus_import"));
    @appeng.items.parts.PartModels
    public static final IPartModel MODEL_EXPORT = new PartModel(
            ResourceLocation.fromNamespaceAndPath(AppliedStorageSorter.MODID, "part/smart_bus_export"));
    @appeng.items.parts.PartModels
    public static final IPartModel MODEL_DEBUG_LETTERBOX = new PartModel(
            ResourceLocation.fromNamespaceAndPath(AppliedStorageSorter.MODID, "part/smart_bus_debug"));
    @appeng.items.parts.PartModels
    public static final IPartModel MODEL_DEBUG_SQUARE = new PartModel(
            ResourceLocation.fromNamespaceAndPath(AppliedStorageSorter.MODID, "part/smart_bus_debug_square"));

    private final IActionSource actionSource = new MachineSource(this);
    private final PartAdjacentApi<IItemHandler> adjacentItemHandler = new PartAdjacentApi<>(this, Capabilities.ItemHandler.BLOCK,
            this::wakeDevice);

    private SmartBusMode mode = SmartBusMode.EMPTY;
    private String filterJson;

    public SmartBusPart(IPartItem<?> partItem) {
        super(partItem);
        getMainNode().setFlags(GridFlags.REQUIRE_CHANNEL);
        getMainNode().addService(IGridTickable.class, this);
    }

    // ─── Mode ───────────────────────────────────────────────────

    public SmartBusMode getMode() {
        return mode;
    }

    public Component getModeDisplayName() {
        return mode.getDisplayName();
    }

    public void cycleMode() {
        setMode(mode.next());
    }

    public void cycleModeAndNotify(Player player) {
        setMode(mode.next());
        player.displayClientMessage(
                Component.translatable("msg.appliedinsight.smart_bus.mode_switched", getModeDisplayName()),
                true);
    }

    public void setMode(SmartBusMode mode) {
        SmartBusMode previous = this.mode;
        this.mode = mode == null ? SmartBusMode.EMPTY : mode;
        if (getHost() != null) {
            getHost().markForSave();
            getHost().markForUpdate();
        }
        syncTickState();
        alwaysLog("SmartBus mode changed: {} -> {}  [side={}, pos={}]",
                previous.name(), this.mode.name(),
                getSide(), getBlockEntity() != null ? getBlockEntity().getBlockPos() : "?");
    }

    // ─── Filter ─────────────────────────────────────────────────

    public String getFilterJson() {
        return filterJson;
    }

    public boolean hasFilter() {
        return filterJson != null && !filterJson.isBlank();
    }

    public boolean hasValidFilter() {
        if (!hasFilter()) {
            return false;
        }
        try {
            FilterExpressionJsonCodec.parse(filterJson);
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    public void setFilterJson(String json) {
        if (json == null || json.isBlank()) {
            this.filterJson = null;
        } else {
            this.filterJson = json.trim();
        }
        if (getHost() != null) {
            getHost().markForSave();
            getHost().markForUpdate();
        }
        syncTickState();
        alwaysLog("SmartBus filter updated  [hasFilter={}, hasValidFilter={}]", hasFilter(), hasValidFilter());
    }

    // ─── Menu ───────────────────────────────────────────────────

    public void openMenu(Player player) {
        if (player.level().isClientSide() || getSide() == null || getBlockEntity() == null) {
            return;
        }
        player.openMenu(new SimpleMenuProvider(
                (containerId, playerInventory, menuPlayer) -> new SmartBusMenu(containerId, playerInventory, this),
                Component.translatable("item.appliedinsight.smart_bus")), buffer -> {
                    buffer.writeBlockPos(getBlockEntity().getBlockPos());
                    buffer.writeByte(getSide().ordinal());
                });
    }

    // ─── Interaction ────────────────────────────────────────────

    @Override
    public boolean onUseWithoutItem(Player player, Vec3 pos) {
        if (player.isShiftKeyDown()) {
            cycleModeOnShiftClick(player);
            return true;
        }

        if (!isClientSide()) {
            openMenu(player);
        }
        return true;
    }

    @Override
    public boolean onUseItemOn(ItemStack heldItem, Player player, InteractionHand hand, Vec3 pos) {
        if (player.isShiftKeyDown()) {
            if (heldItem.isEmpty()) {
                cycleModeOnShiftClick(player);
                return true;
            }
            if (heldItem.is(ItemTags.create(WRENCH_TAG_ID))) {
                return false;
            }
        }
        if (super.onUseItemOn(heldItem, player, hand, pos)) {
            return true;
        }
        if (!isClientSide()) {
            openMenu(player);
        }
        return true;
    }

    private void cycleModeOnShiftClick(Player player) {
        if (!isClientSide()) {
            cycleModeAndNotify(player);
        }
    }

    // ─── Appearance ─────────────────────────────────────────────

    @Override
    public void getBoxes(IPartCollisionHelper bch) {
        bch.addBox(2, 2, 10, 14, 14, 16);
    }

    @Override
    public IPartModel getStaticModels() {
        return switch (Config.DEBUG_SMART_BUS_UV.get()) {
            case LETTERBOX -> MODEL_DEBUG_LETTERBOX;
            case SQUARE -> MODEL_DEBUG_SQUARE;
            case OFF -> switch (mode) {
                case EMPTY -> MODEL_EMPTY;
                case IMPORT -> MODEL_IMPORT;
                case EXPORT -> MODEL_EXPORT;
            };
        };
    }

    // ─── Persistence ────────────────────────────────────────────

    @Override
    public void readFromNBT(CompoundTag data, HolderLookup.Provider registries) {
        super.readFromNBT(data, registries);
        mode = SmartBusMode.fromStoredName(data.getString(TAG_MODE));
        if (data.contains(TAG_FILTER)) {
            filterJson = data.getString(TAG_FILTER);
            if (filterJson.isBlank()) {
                filterJson = null;
            }
        } else {
            filterJson = null;
        }
    }

    @Override
    public void writeToNBT(CompoundTag data, HolderLookup.Provider registries) {
        super.writeToNBT(data, registries);
        data.putString(TAG_MODE, mode.name());
        if (filterJson != null && !filterJson.isBlank()) {
            data.putString(TAG_FILTER, filterJson);
        }
    }

    @Override
    public void writeToStream(RegistryFriendlyByteBuf data) {
        super.writeToStream(data);
        data.writeByte(mode.ordinal());
    }

    @Override
    public boolean readFromStream(RegistryFriendlyByteBuf data) {
        boolean changed = super.readFromStream(data);
        SmartBusMode previous = mode;
        mode = SmartBusMode.fromNetworkOrdinal(data.readByte());
        boolean anyChange = changed || previous != mode;
        if (anyChange && getHost() != null) {
            getHost().markForUpdate();
        }
        return anyChange;
    }

    @Override
    public void addAdditionalDrops(List<ItemStack> drops, boolean wrenched) {
    }

    @Override
    public void clearContent() {
        filterJson = null;
    }

    // ─── Lifecycle ──────────────────────────────────────────────

    @Override
    public void addToWorld() {
        super.addToWorld();
        syncTickState();
        alwaysLog("SmartBus added to world  [mode={}, side={}, hasFilter={}, hasValidFilter={}, pos={}]",
                mode.name(), getSide(), hasFilter(), hasValidFilter(),
                getBlockEntity() != null ? getBlockEntity().getBlockPos() : "?");
    }

    @Override
    protected void onMainNodeStateChanged(IGridNodeListener.State reason) {
        super.onMainNodeStateChanged(reason);
        syncTickState();
    }

    // ─── Ticking ────────────────────────────────────────────────

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(IMPORT_TICK_RATES, isSleeping());
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (isSleeping()) {
            verboseLog("tickingRequest -> SLEEP  [mode={}, hasFilter={}, hasValidFilter={}]",
                    mode.name(), hasFilter(), hasValidFilter());
            return TickRateModulation.SLEEP;
        }
        boolean canWork = canDoBusWork();
        if (!canWork) {
            verboseLog("tickingRequest -> IDLE  [mode={}, mainActive={}, adjHandler={}]",
                    mode.name(), getMainNode().isActive(), getAdjacentItemHandler());
            return TickRateModulation.IDLE;
        }
        boolean didWork = switch (mode) {
            case IMPORT -> doImportWork(node);
            case EXPORT -> doExportWork(node);
            default -> false;
        };
        verboseLog("tickingRequest -> {}  [mode={}, ticksSinceLast={}]",
                didWork ? "FASTER" : "SLOWER", mode.name(), ticksSinceLastCall);
        return didWork ? TickRateModulation.FASTER : TickRateModulation.SLOWER;
    }

    protected boolean isSleeping() {
        boolean isActiveMode = mode == SmartBusMode.IMPORT || mode == SmartBusMode.EXPORT;
        boolean sleeping = mode == SmartBusMode.EMPTY || !isActiveMode || !hasValidFilter();
        if (sleeping && Config.VERBOSE_LOGGING.get()) {
            verboseLog("isSleeping=true  [mode={}, hasValidFilter={}]", mode.name(), hasValidFilter());
        }
        return sleeping;
    }

    private boolean canDoBusWork() {
        if (!getMainNode().isActive()) {
            verboseLog("canDoBusWork=false  [reason=mainNode not active]");
            return false;
        }
        IItemHandler handler = getAdjacentItemHandler();
        if (handler == null) {
            verboseLog("canDoBusWork=false  [reason=no adjacent IItemHandler]");
            return false;
        }
        return true;
    }

    private boolean doImportWork(IGridNode node) {
        IItemHandler itemHandler = getAdjacentItemHandler();
        if (itemHandler == null) {
            return false;
        }

        ItemFilter filter = parseRuntimeFilter();
        if (filter == null) {
            verboseLog("doImportWork=false  [reason=filter parse failed, hasFilterJson={}]", hasFilter());
            return false;
        }

        int slots = itemHandler.getSlots();
        verboseLog("doImportWork scanning {} slots  [filterId={}]", slots, filter.id());

        MEStorage meStorage = node.getGrid().getStorageService().getInventory();
        int skippedEmpty = 0;
        int skippedFilter = 0;
        int transfersRemaining = stackTransfersPerTick();
        boolean didWork = false;
        int slot = 0;
        while (transfersRemaining > 0 && slot < slots) {
            ItemStack simulatedStack = itemHandler.extractItem(slot, Integer.MAX_VALUE, true);
            if (simulatedStack.isEmpty()) {
                skippedEmpty++;
                slot++;
                continue;
            }

            String itemId = BuiltInRegistries.ITEM.getKey(simulatedStack.getItem()).toString();
            if (!matchesFilter(filter, simulatedStack)) {
                skippedFilter++;
                verboseLog("  slot {} skipped (filter)  [item={} x{}]", slot, itemId, simulatedStack.getCount());
                slot++;
                continue;
            }

            AEItemKey itemKey = AEItemKey.of(simulatedStack);
            if (itemKey == null) {
                verboseLog("  slot {} skipped (AEItemKey.of null)  [item={}]", slot, itemId);
                slot++;
                continue;
            }

            long acceptedAmount = meStorage.insert(itemKey, simulatedStack.getCount(), Actionable.SIMULATE, actionSource);
            if (acceptedAmount <= 0) {
                verboseLog("  slot {} skipped (ME refused)  [item={} x{}]", slot, itemId, simulatedStack.getCount());
                slot++;
                continue;
            }

            int transferAmount = (int) Math.min(simulatedStack.getCount(), acceptedAmount);
            ItemStack extractedStack = itemHandler.extractItem(slot, transferAmount, false);
            if (extractedStack.isEmpty()) {
                verboseLog("  slot {} skipped (extract returned empty)  [item={} want={}]",
                        slot, itemId, transferAmount);
                slot++;
                continue;
            }

            AEItemKey extractedKey = AEItemKey.of(extractedStack);
            if (extractedKey == null) {
                slot++;
                continue;
            }

            long inserted = meStorage.insert(extractedKey, extractedStack.getCount(), Actionable.MODULATE, actionSource);
            if (inserted < extractedStack.getCount()) {
                ItemStack leftover = extractedStack.copyWithCount((int) (extractedStack.getCount() - inserted));
                ItemStack remainder = insertIntoAdjacent(itemHandler, leftover);
                if (!remainder.isEmpty()) {
                    AppliedStorageSorter.LOGGER.warn(
                            "[SmartBus] voided {}x{} after ME network under-accepted adjacent import.",
                            remainder.getCount(),
                            BuiltInRegistries.ITEM.getKey(remainder.getItem()));
                }
            }

            if (inserted > 0) {
                alwaysLog("IMPORT {}x{}  [fromSlot={}, accepted={}, extracted={}, inserted={}, remainingBudget={}]",
                        itemId, extractedStack.getCount(), slot, acceptedAmount,
                        extractedStack.getCount(), inserted, transfersRemaining - 1);
                transfersRemaining--;
                didWork = true;
                if (itemHandler.extractItem(slot, 1, true).isEmpty()) {
                    slot++;
                }
            } else {
                slot++;
            }
        }

        if (!didWork) {
            verboseLog("doImportWork: scanned {} slots, {} empty, {} filtered out, no import this tick",
                    slots, skippedEmpty, skippedFilter);
        }
        return didWork;
    }

    // ─── Import helpers ─────────────────────────────────────────

    private boolean doExportWork(IGridNode node) {
        IItemHandler itemHandler = getAdjacentItemHandler();
        if (itemHandler == null) {
            return false;
        }

        ItemFilter filter = parseRuntimeFilter();
        if (filter == null) {
            verboseLog("doExportWork=false  [reason=filter parse failed, hasFilterJson={}]", hasFilter());
            return false;
        }

        MEStorage meStorage = node.getGrid().getStorageService().getInventory();
        var availableStacks = meStorage.getAvailableStacks();
        if (availableStacks.isEmpty()) {
            verboseLog("doExportWork: ME network is empty, nothing to export");
            return false;
        }

        verboseLog("doExportWork scanning {} ME stacks  [filterId={}]", availableStacks.size(), filter.id());

        int transfersRemaining = stackTransfersPerTick();
        boolean didWork = false;
        for (var entry : availableStacks) {
            if (transfersRemaining <= 0) {
                break;
            }
            if (!(entry.getKey() instanceof AEItemKey itemKey)) {
                continue;
            }

            ItemStack refStack = itemKey.getReadOnlyStack();
            if (!matchesFilter(filter, refStack)) {
                verboseLog("  stack skipped (filter)  [item={} x{}]",
                        BuiltInRegistries.ITEM.getKey(refStack.getItem()), entry.getLongValue());
                continue;
            }

            long meAmount = entry.getLongValue();
            int transferAmount = (int) Math.min(meAmount, refStack.getMaxStackSize());
            ItemStack toExport = itemKey.toStack(transferAmount);

            // Simulate insert into adjacent
            ItemStack remainder = ItemStack.EMPTY;
            for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
                remainder = itemHandler.insertItem(slot, toExport, true);
                if (remainder.getCount() < toExport.getCount()) {
                    break;
                }
            }
            long acceptedAmount = toExport.getCount() - remainder.getCount();
            if (acceptedAmount <= 0) {
                verboseLog("  stack skipped (adjacent full)  [item={}]",
                        BuiltInRegistries.ITEM.getKey(refStack.getItem()));
                continue;
            }

            // Real extract from ME
            long extracted = meStorage.extract(itemKey, acceptedAmount, Actionable.MODULATE, actionSource);
            if (extracted <= 0) {
                verboseLog("  stack skipped (ME extract failed)  [item={}]",
                        BuiltInRegistries.ITEM.getKey(refStack.getItem()));
                continue;
            }

            ItemStack extractedStack = itemKey.toStack((int) extracted);

            // Real insert into adjacent
            ItemStack leftover = extractedStack.copy();
            for (int slot = 0; slot < itemHandler.getSlots() && !leftover.isEmpty(); slot++) {
                leftover = itemHandler.insertItem(slot, leftover, false);
            }

            long inserted = extracted - leftover.getCount();
            if (leftover.getCount() > 0) {
                // Return overflow to ME
                meStorage.insert(itemKey, leftover.getCount(), Actionable.MODULATE, actionSource);
            }

            if (inserted > 0) {
                String itemId = BuiltInRegistries.ITEM.getKey(refStack.getItem()).toString();
                alwaysLog("EXPORT {}x{}  [extracted={}, inserted={}, returned={}, remainingBudget={}]",
                        itemId, extracted, extracted, inserted, leftover.getCount(), transfersRemaining - 1);
                transfersRemaining--;
                didWork = true;
            }
        }

        return didWork;
    }

    private IItemHandler getAdjacentItemHandler() {
        return adjacentItemHandler.find();
    }

    private ItemFilter parseRuntimeFilter() {
        if (!hasFilter()) {
            verboseLog("parseRuntimeFilter=null  [reason=no filter configured]");
            return null;
        }

        try {
            FilterExpression expression = FilterExpressionJsonCodec.parse(filterJson);
            ItemFilter filter = new ItemFilter("smart_bus", "Smart Bus", "", true, expression);
            verboseLog("parseRuntimeFilter ok  [conditionCount={}]", filter.allConditions().size());
            return filter;
        } catch (RuntimeException ex) {
            alwaysLog("parseRuntimeFilter failed  [error={}]", ex.getMessage());
            return null;
        }
    }

    private boolean matchesFilter(ItemFilter filter, ItemStack stack) {
        return ItemFilterMatcher.matches(filter, buildMatchContext(stack));
    }

    private ItemMatchContext buildMatchContext(ItemStack stack) {
        Level level = getLevel();
        return new ItemMatchContext(
                BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(),
                stack.getItem().builtInRegistryHolder().key().location().getNamespace(),
                stack.getHoverName().getString(),
                stack.getTags().map(TagKey::location).map(Object::toString).collect(java.util.stream.Collectors.toSet()),
                !stack.isComponentsPatchEmpty(),
                stack.getCount(),
                stack.saveOptional(level.registryAccess()).toString());
    }

    private ItemStack insertIntoAdjacent(IItemHandler itemHandler, ItemStack stack) {
        ItemStack remaining = stack;
        for (int slot = 0; slot < itemHandler.getSlots() && !remaining.isEmpty(); slot++) {
            remaining = itemHandler.insertItem(slot, remaining, false);
        }
        return remaining;
    }

    // ─── Tick sync ──────────────────────────────────────────────

    private void syncTickState() {
        getMainNode().ifPresent((grid, node) -> {
            if (isSleeping()) {
                grid.getTickManager().sleepDevice(node);
            } else {
                grid.getTickManager().wakeDevice(node);
                grid.getTickManager().alertDevice(node);
            }
        });
    }

    private void wakeDevice() {
        if (!isSleeping()) {
            getMainNode().ifPresent((grid, node) -> grid.getTickManager().alertDevice(node));
        }
    }

    // ─── Logging helpers ─────────────────────────────────────────

    private void alwaysLog(String fmt, Object... args) {
        AppliedStorageSorter.LOGGER.info("[SmartBus] " + fmt, args);
    }

    private void verboseLog(String fmt, Object... args) {
        if (Config.VERBOSE_LOGGING.get()) {
            AppliedStorageSorter.LOGGER.info("[SmartBus] " + fmt, args);
        }
    }
}

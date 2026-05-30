package com.knightcode.appliedstoragesorter.item;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.Nullable;

public class DigitalAssetManagementCardItem extends Item {
    private static final String ZONE_ID_TAG = "zoneId";
    private static final String ZONE_NAME_TAG = "zoneName";

    public DigitalAssetManagementCardItem(Properties properties) {
        super(properties);
    }

    public static boolean hasZoneData(ItemStack stack) {
        return getZoneId(stack).isPresent();
    }

    /**
     * 获取区域 ID。优先级：
     * <ol>
     *   <li>CUSTOM_DATA 中持久化的 zoneId</li>
     *   <li>DataComponents.ITEM_NAME（Inscriber 重命名）</li>
     *   <li>DataComponents.CUSTOM_NAME（铁砧重命名）</li>
     * </ol>
     */
    public static Optional<String> getZoneId(ItemStack stack) {
        // 1. 优先读取持久化的 zoneId
        var fromCustomData = getCustomDataTag(stack)
                .map(tag -> normalize(tag.getString(ZONE_ID_TAG)));
        if (fromCustomData.isPresent()) return fromCustomData;

        // 2. 从物品名称读取（铁砧/Inscriber 重命名）
        var itemName = stack.get(DataComponents.ITEM_NAME);
        if (itemName != null) {
            String text = normalize(itemName.getString());
            if (text != null) return Optional.of(text);
        }

        // 3. 从 custom_name 读取（兼容旧版铁砧）
        var customName = stack.get(DataComponents.CUSTOM_NAME);
        if (customName != null) {
            String text = normalize(customName.getString());
            if (text != null) return Optional.of(text);
        }

        return Optional.empty();
    }

    /**
     * 获取区域名称。如果物品有自定义名称且未持久化 zoneName，则使用名称作为 zoneName。
     */
    public static Optional<String> getZoneName(ItemStack stack) {
        // 1. 优先读取持久化的 zoneName
        var fromCustomData = getCustomDataTag(stack)
                .map(tag -> normalize(tag.getString(ZONE_NAME_TAG)));
        if (fromCustomData.isPresent()) return fromCustomData;

        // 2. 回退到物品名称
        return getZoneId(stack);
    }

    /**
     * 将区域数据持久化到卡片的 CUSTOM_DATA 中。
     * 当 DAV 绑定卡片时调用此方法，将运行时识别的区域写入持久数据。
     */
    public static void setZoneData(ItemStack stack, String zoneId, @Nullable String zoneName) {
        Objects.requireNonNull(stack, "stack");
        String normalizedZoneId = Objects.requireNonNull(normalize(zoneId), "zoneId must not be blank");
        String normalizedZoneName = normalize(zoneName);

        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putString(ZONE_ID_TAG, normalizedZoneId);
            if (normalizedZoneName != null) {
                tag.putString(ZONE_NAME_TAG, normalizedZoneName);
            } else {
                tag.remove(ZONE_NAME_TAG);
            }
        });
    }

    public static void clearZoneData(ItemStack stack) {
        Objects.requireNonNull(stack, "stack");

        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.remove(ZONE_ID_TAG);
            tag.remove(ZONE_NAME_TAG);
        });
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents,
            TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        String zoneId = getZoneId(stack).orElse(null);
        if (zoneId == null) {
            tooltipComponents.add(Component.translatable(
                    "item.appliedstoragesorter.digital_asset_management_card.zone.unassigned")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }

        String zoneName = getZoneName(stack).orElse(null);
        if (zoneName != null && !zoneName.equals(zoneId)) {
            tooltipComponents.add(Component.translatable(
                    "item.appliedstoragesorter.digital_asset_management_card.zone.named",
                    zoneName,
                    zoneId).withStyle(ChatFormatting.AQUA));
        } else {
            tooltipComponents.add(Component.translatable(
                    "item.appliedstoragesorter.digital_asset_management_card.zone.id",
                    zoneId).withStyle(ChatFormatting.AQUA));
        }
    }

    private static Optional<CompoundTag> getCustomDataTag(ItemStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }

        var customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null || customData.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(customData.copyTag());
    }

    @Nullable
    private static String normalize(@Nullable String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

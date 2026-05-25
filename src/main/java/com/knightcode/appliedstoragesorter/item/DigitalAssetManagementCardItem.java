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

    public static Optional<String> getZoneId(ItemStack stack) {
        return getCustomDataTag(stack)
                .map(tag -> normalize(tag.getString(ZONE_ID_TAG)));
    }

    public static Optional<String> getZoneName(ItemStack stack) {
        return getCustomDataTag(stack)
                .map(tag -> normalize(tag.getString(ZONE_NAME_TAG)));
    }

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
            tooltipComponents.add(Component.translatable("item.appliedstoragesorter.digital_asset_management_card.zone.unassigned")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }

        String zoneName = getZoneName(stack).orElse(null);
        if (zoneName != null) {
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

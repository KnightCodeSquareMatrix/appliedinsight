package com.knightcode.appliedstoragesorter.item;

import com.knightcode.appliedstoragesorter.ae2.dav.cell.DavCellStack;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * Visual stand-in for a drive cell. Not handled by AE2 {@code StorageCells}; not insertable into ME drives.
 * Custom data is identity-only — see {@link com.knightcode.appliedstoragesorter.ae2.dav.cell.DavCellStack}.
 */
public class DavCellItem extends Item {
    public DavCellItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.appliedinsight.dav_cell.tooltip.summary")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.appliedinsight.dav_cell.tooltip.panel_hint")
                .withStyle(ChatFormatting.DARK_GRAY));
        var cellId = DavCellStack.getCellId(stack);
        if (cellId != null) {
            if (flag.isAdvanced()) {
                tooltip.add(Component.translatable(
                                "item.appliedinsight.dav_cell.tooltip.cell_id",
                                cellId)
                        .withStyle(ChatFormatting.DARK_GRAY));
            }
        } else {
            tooltip.add(Component.translatable("item.appliedinsight.dav_cell.tooltip.blank")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}

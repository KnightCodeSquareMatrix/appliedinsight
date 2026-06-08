package com.knightcode.appliedstoragesorter.client.screen;

import appeng.api.storage.StorageCells;
import com.knightcode.appliedstoragesorter.menu.DigitalAssetVaultMenu;
import com.knightcode.appliedstoragesorter.network.NewDavSetExpansionCellPayload;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Optional;

public final class ExpansionCellOperations {
    private ExpansionCellOperations() {
    }

    public static boolean accepts(ItemStack stack) {
        return !stack.isEmpty() && StorageCells.isCellHandled(stack);
    }

    public static Optional<ItemStack> firstAcceptedStack(Iterable<ItemStack> stacks) {
        for (ItemStack stack : stacks) {
            if (accepts(stack)) {
                return Optional.of(stack);
            }
        }
        return Optional.empty();
    }

    public static void apply(DigitalAssetVaultMenu menu, ItemStack stack) {
        if (!accepts(stack)) {
            return;
        }
        var itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        sendCellId(menu, itemId.toString());
    }

    public static void clear(DigitalAssetVaultMenu menu) {
        sendCellId(menu, "");
    }

    private static void sendCellId(DigitalAssetVaultMenu menu, String cellId) {
        PacketDistributor.sendToServer(
                new NewDavSetExpansionCellPayload(menu.getBlockEntity().getBlockPos(), cellId));
    }

    public static Rect2i dropBounds(DigitalAssetVaultScreen screen) {
        var picker = screen.getExpansionCellPicker();
        if (picker == null) {
            return new Rect2i(0, 0, 0, 0);
        }
        return new Rect2i(picker.getX(), picker.getY(), picker.getWidth(), picker.getHeight());
    }
}

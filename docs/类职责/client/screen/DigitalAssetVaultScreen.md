# DigitalAssetVaultScreen
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/client/screen/DigitalAssetVaultScreen.java`
- **包**: `com.knightcode.appliedstoragesorter.client.screen`
- **类型**: `class`
- **所属层**: 客户端展示层

## 职责
DAV 客户端 GUI，负责背景纹理绘制和 tooltip 渲染。

## 边界检查
边界健康。

## 抽象检查
没有过度抽象。

## 主要协作者
- `com.knightcode.appliedstoragesorter.menu.DigitalAssetVaultMenu`
- `net.minecraft.client.gui.GuiGraphics`
- `net.minecraft.client.gui.screens.inventory.AbstractContainerScreen`
- `net.minecraft.network.chat.Component`
- `net.minecraft.resources.ResourceLocation`
- `net.minecraft.world.entity.player.Inventory`

## 维护备注
- 当前实现简洁，优先保持单一职责，不要为了未来猜想提前拆分。

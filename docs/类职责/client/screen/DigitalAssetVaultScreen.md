# DigitalAssetVaultScreen
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/client/screen/DigitalAssetVaultScreen.java`
- **包**: `com.knightcode.appliedstoragesorter.client.screen`
- **类型**: `class`
- **所属层**: 客户端展示层

## 职责
DAV 客户端 GUI，负责使用模组自有 GUI 纹理绘制背景和 tooltip 渲染。DAV 继承自 `DriveBlockEntity`，UI 使用独立纹理以摆脱对 AE2 内部资源的依赖。

## 边界检查
边界健康。

## 抽象检查
没有过度抽象。

## 关键设计
- **纹理来源**: 使用模组自有纹理 `appliedstoragesorter:textures/gui/digital_asset_vault.png`（256x256，有效区域 176x223），替代原来的 AE2 Drive 纹理依赖
- **纹理布局**（三段 blit）：
  - Drive 主体上半（cell 槽区域）：y=0~84
  - Drive 主体下半（玩家背包区域）：y=84~201
  - 卡槽面板（管理卡槽区域）：y=201~223
- **imageHeight**: 223px（Drive 主体 201px + 卡槽面板 22px）
- **常量**: `CARD_PANEL_START_Y = 201`、`CARD_PANEL_HEIGHT = 22`

## 主要协作者
- `com.knightcode.appliedstoragesorter.menu.DigitalAssetVaultMenu`
- `net.minecraft.client.gui.GuiGraphics`
- `net.minecraft.client.gui.screens.inventory.AbstractContainerScreen`
- `net.minecraft.network.chat.Component`
- `net.minecraft.resources.ResourceLocation`
- `net.minecraft.world.entity.player.Inventory`

## 维护备注
- 纹理引用 `appliedstoragesorter:textures/gui/digital_asset_vault.png` 是模组自包含资源，随模组打包部署
- 如果 DAV GUI 布局需要调整，需同步修改纹理 PNG 和 `TEXTURE_WIDTH`/`TEXTURE_HEIGHT`/`PLAYER_INV_SECTION_Y` 常量
- 保持单一职责，不要为了未来猜想提前拆分

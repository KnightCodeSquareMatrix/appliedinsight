# NewDavToggleButton
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/client/gui/widget/NewDavToggleButton.java`
- **包**: `com.knightcode.appliedstoragesorter.client.gui.widget`
- **类型**: `class`
- **所属层**: 客户端展示层

## 职责
DAV GUI 三态开关控件。继承 `ThemedAE2Button`，根据布尔状态切换 lang key 后缀 `.on` / `.off`，点击时回调 `Consumer<Boolean>`。

## 边界检查
边界健康。不直接发包；`DigitalAssetVaultScreen` 在回调中发送 `NewDavTogglePayload`。

## 主要协作者
- `com.knightcode.appliedstoragesorter.client.gui.widget.ThemedAE2Button`
- `com.knightcode.appliedstoragesorter.client.screen.DigitalAssetVaultScreen`

## 维护备注
- `translationKey` 应为完整前缀（如 `screen.appliedinsight.digital_asset_vault.toggle.migrate`），不含 `.on`/`.off`。

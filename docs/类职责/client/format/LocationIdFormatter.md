# LocationIdFormatter
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/client/format/LocationIdFormatter.java`
- **包**: `com.knightcode.appliedstoragesorter.client.format`
- **类型**: `final class`（静态工具）
- **所属层**: 客户端展示层

## 职责
将 `Ae2StorageAnalyzer.buildLocationId()` 产出的**管道分隔技术 ID** 转为玩家可读短标签。

**输入格式**：`dimension|blockId|blockPos|attachedPos|slot=N`

**示例**：
- `minecraft:overworld|ae2:drive|x=100,y=64,z=200|<none>|slot=0` → `ME Drive @ (100, 64, 200)`
- `…|appliedinsight:digital_asset_vault|…` → `DAV @ (…)`

## API
- `toFriendlyLocation(locationId)` — 短标签（GUI 列表）
- `toDetailedLocation(locationId)` — 含维度名（如 `Overworld / ME Drive @ …`）

## 边界检查
边界健康。纯字符串解析；解析失败时原样返回输入。

## 主要协作者
- `com.knightcode.appliedstoragesorter.client.analysis.AnalysisPresenter`
- `com.knightcode.appliedstoragesorter.ae2.analysis.Ae2StorageAnalyzer`

## 维护备注
- 新 storage 方块类型时在 `BLOCK_DISPLAY_NAMES` 增加友好名映射。

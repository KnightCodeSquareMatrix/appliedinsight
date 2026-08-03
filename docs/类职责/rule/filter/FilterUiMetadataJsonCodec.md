# FilterUiMetadataJsonCodec
- **源码路径**: `src/main/java/com/knightcode/appliedinsight/rule/filter/FilterUiMetadataJsonCodec.java`
- **包**: `com.knightcode.appliedstoragesorter.rule.filter`
- **类型**: `class`
- **所属层**: 纯规则模型层

## 职责
FilterUiMetadata 的 JSON 读写器。

## 边界检查
边界健康。

## 抽象检查
没有过度抽象。

## 主要协作者
- `com.google.gson.Gson`
- `com.google.gson.GsonBuilder`
- `com.google.gson.JsonObject`
- `com.google.gson.JsonParser`

## 维护备注
- 当前实现简洁，优先保持单一职责，不要为了未来猜想提前拆分。

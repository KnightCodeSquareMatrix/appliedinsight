# NbtPathExtractor

| 维度 | 说明 |
|------|------|
| **包路径** | `rule/filter/NbtPathExtractor.java` |
| **定位** | NBT JSON 路径提取器 |
| **职责** | 从序列化 NBT JSON 字符串中按点分隔路径提取值，支持嵌套对象字段和数组索引访问 |
| **关键方法** | `extract(String nbtJson, String path)` → `String` |
| **路径格式** | `components.apotheosis:rarity.value`（嵌套对象）；`items[0].id`（数组索引） |
| **依赖** | Gson `JsonParser`（兼容 Smart Bus / dump 使用的 SNBT 字符串） |
| **被谁使用** | `ItemFilterMatcher`（NBT_PATH 操作符的匹配实现） |
| **边界** | 纯规则层，无 Minecraft/AE2 运行时依赖；路径不存在返回 null |

# SorterDumpAnalyzer
- **源码路径**: `src/main/java/com/knightcode/appliedinsight/analysis/SorterDumpAnalyzer.java`
- **包**: `com.knightcode.appliedstoragesorter.analysis`
- **类型**: `class`
- **所属层**: 离线分析层

## 职责
离线分析 dump 的总体结构、数量分布、模组聚合、tag 聚合和组件物品热点。

## 边界检查
边界健康。它位于 analysis 包，只依赖 dump JSON，而不依赖 Minecraft/AE2 运行时。

## 抽象检查
没有过度抽象；作为 CLI 工具把解析和报告写在一个类里是可以接受的。

## 主要协作者
- `com.google.gson.stream.JsonReader`
- `com.google.gson.stream.JsonToken`

## 维护备注
- 如果后续报表格式增多，可把读取器和文本渲染器拆开。

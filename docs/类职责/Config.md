# Config
- **源码路径**: `src/main/java/com/knightcode/appliedinsight/Config.java`
- **包**: `com.knightcode.appliedstoragesorter`
- **类型**: `class`
- **所属层**: 入口/装配层

## 职责
集中定义通用配置项，如开关、扫描间隔、单次最大搬运步数和详细日志开关。

## 边界检查
边界健康。配置值没有掺杂业务推导，只是参数源。

## 抽象检查
没有过度抽象。

## 主要协作者
- `net.neoforged.neoforge.common.ModConfigSpec`

## 维护备注
- SCAN_INTERVAL_TICKS 当前尚未看到完整消费链路，后续如果长期不用可以清理。

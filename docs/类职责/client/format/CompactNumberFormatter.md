# CompactNumberFormatter

> **最后更新**: 2026-05-30

| 维度 | 说明 |
|------|------|
| **包路径** | `client/format/CompactNumberFormatter.java` |
| **定位** | 客户端展示层数字缩写格式化工具 |
| **职责** | 将较大的整数压缩为短格式字符串，例如 `100k`、`10m`、`20B`，供 GUI 概览面板和列表展示复用 |
| **关键方法** | `format(long)` 根据数量级选择 `k / m / B / T` 后缀；内部 `formatScaled(...)` 负责控制 1 位或 0 位小数输出 |
| **依赖** | `Locale`（JDK） |
| **被谁使用** | 当前由 `SorterCommandBlockScreen` 复用；后续可用于其他客户端结果面板 |
| **边界** | 仅服务于客户端展示语义，不承担业务聚合、日志输出或网络序列化职责 |

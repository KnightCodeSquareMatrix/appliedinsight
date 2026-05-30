---
navigation:
  title: 命令参考
  icon: sorter_command_block
  parent: index.md
  position: 40
---

# 命令参考

模组提供 `/sorter me <子命令>` 系列命令，主要用于管理员调试和开发者测试。

## 命令树

```
/sorter me dump           — 扫描网络并导出 JSON
/sorter me storageDump    — 扫描 + 分析存储健康
/sorter me plan           — 生成搬运计划
/sorter me planAndMove    — 生成计划并执行搬运
/sorter merge             — 合并同类物品
```

## 输出文件

所有命令的输出文件都保存在游戏根目录下的 `dumps/appliedstoragesorter/` 里：

| 命令 | 输出文件 |
| :--- | :--- |
| `dump` | `dump_<网络ID>.json` |
| `storageDump` | `storage-analysis_<网络ID>.json` |
| `plan` | `plan_<网络ID>.json` |
| `planAndMove` | 计划文件 + 执行报告 |
| `merge` | `merge-report_<时间戳>.json` |

## 配置项

模组的配置文件在 `config/appliedstoragesorter-server.toml`，可以调整以下参数：

| 配置项 | 默认值 | 说明 |
| :--- | :--- | :--- |
| `enableSorter` | `true` | 是否启动物品整理功能 |
| `developerMode` | `false` | 开发者模式（开启后会输出更多调试信息） |
| `maxTransfers` | `100` | 每次操作最多处理多少个物品 |

## 配置文件管理

命令执行块 GUI 里的按钮已经覆盖了大部分日常操作。大多数玩家不需要直接使用这些命令。

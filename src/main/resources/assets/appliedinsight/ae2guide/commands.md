---
navigation:
  title: 命令参考
  icon: sorter_command_block
  parent: index.md
  position: 40
---

# 命令参考

大多数玩家通过[命令执行块](sorter-command-block.md) GUI 完成日常操作。本节列出 `/sorter` 命令与服务器配置，供服主与调试使用。

## 命令树

```
/sorter merge                          — 合并同类物品（等同 GUI「碎片物品合并」）
/sorter me plan                        — 生成搬运计划（不执行）
/sorter me planAndMove                 — 生成计划并执行（等同 GUI「以绑定配置整理」）
/sorter me listProfiles                — 列出 profiles 目录下的路由配置
/sorter me bindProfile <编号>          — 将当前 ME 网络绑定到指定配置
/sorter me showProfile                 — 查看当前网络的绑定配置
/sorter me dump                        — 导出网络清单 JSON（需 developerMode）
/sorter me storageDump                 — 导出存储分析 JSON（需 developerMode）
/sorter genTestItems [选项...]         — 生成测试物品（需 developerMode）
```

路由配置工作流详见[路由配置（服主）](routing-profiles.md)。

## 输出文件

命令与 GUI 分析产生的文件保存在游戏根目录 `dumps/appliedinsight/`：

| 操作 | 典型输出 |
| :--- | :--- |
| 分析 / storageDump | `storage-analysis_<网络ID>.json` |
| dump | `dump_<网络ID>.json` |
| plan / planAndMove | `plan_<网络ID>.json` 及执行报告 |
| merge | `merge-report_<时间戳>.json` |

## 服务器配置

配置文件：`config/appliedinsight-server.toml`

### 通用

| 配置项 | 默认值 | 说明 |
| :--- | :--- | :--- |
| `enableSorter` | `true` | 是否启用整理功能 |
| `developerMode` | `false` | 开启后允许 `dump` / `storageDump` / `genTestItems` |
| `verboseLogging` | `false` | 输出更多调试日志 |
| `scanIntervalTicks` | `200` | 扫描间隔（tick） |
| `maxTransfersPerOperation` | `64` | 单次操作最大搬运步数 |

### 能量估算（可选）

| 配置项 | 默认值 | 说明 |
| :--- | :--- | :--- |
| `energyCostEnabled` | `false` | 是否在 merge / planAndMove 摘要中显示估算 AE 能耗 |
| `energyCostBaseFee` | `2.0` | 每次搬运基础系数 α |
| `energyCostAmountCoeff` | `20.0` | 搬运数量系数 β |
| `energyCostTypeCoeff` | `30.0` | 物品种类系数 γ |
| `energyCostDistanceCoeff` | `15.0` | 平均曼哈顿距离系数 δ |

### DAV 自动扩容

| 配置项 | 默认值 | 说明 |
| :--- | :--- | :--- |
| `davAutoExpandEnabled` | `true` | 全局开关；为 `false` 时忽略各 DAV 的自动扩容开关 |
| `davAutoExpandBytesThreshold` | `0.80` | 字节占用达此比例时触发扩容 |
| `davAutoExpandTypesThreshold` | `0.80` | 种类占用达此比例时触发扩容 |
| `davAutoExpandCooldownTicks` | `200` | 两次自动扩容尝试的最小间隔（tick） |

## GUI 与命令的关系

| GUI 按钮 | 对应能力 |
| :--- | :--- |
| 分析当前网络 | 存储分析服务 + 刷新 GUI（无需 developerMode） |
| 碎片物品合并 | `/sorter merge` |
| 以绑定配置整理 | `/sorter me planAndMove`（需先 bindProfile） |

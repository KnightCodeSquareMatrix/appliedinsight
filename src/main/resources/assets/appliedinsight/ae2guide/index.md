---
navigation:
  title: 应用能源：洞察
  icon: sorter_command_block
  position: 999
---

# 什么是应用能源：洞察？

**应用能源：洞察**（Applied Energistics: Insight）是 Applied Energistics 2 的附属模组，帮助你**看清、整理、扩展** ME 网络的存储能力。社区里常简称为 **AE 洞察**。

面向玩家的核心能力：

- **网络分析** — 在[命令执行块](sorter-command-block.md)中查看存储健康、碎片化与内外部分布。
- **碎片物品合并** — 把同种物品归并到更少的位置，释放物品种类槽位。
- **数字资产库（DAV）** — 自带 2k 字节 / 126 种初始容量，可吸收 Storage Cell 继续扩容，支持自动导入与自动扩容。详见[数字资产库](digital-asset-vault.md)。
- **智能总线** — 带 JSON 过滤器的输入/输出总线，用于相邻库存的条件搬运。详见[智能总线](smart-bus.md)。

服主可选能力：

- **规则搬运** — 通过 `config/appliedinsight/profiles/` 下的路由配置 + 网络绑定，在命令执行块触发分区整理。详见[路由配置（服主）](routing-profiles.md)。

# 页面导航

- [快速上手](getting-started.md) — 分析 → 合并 → 接入 DAV 的推荐流程。
- [命令执行块](sorter-command-block.md) — 主操作终端与三栏分析界面。
- [数字资产库](digital-asset-vault.md) — Cell 吸收扩容与自动扩容。
- [智能总线](smart-bus.md) — 过滤式输入/输出总线。
- [路由配置（服主）](routing-profiles.md) — 服主启用规则搬运。
- [命令参考](commands.md) — 命令与服务器配置速查。

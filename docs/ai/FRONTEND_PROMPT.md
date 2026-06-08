# Applied Energistics: Insight — 前端开发 Prompt

> Minecraft AE2 模组 `appliedinsight` 的前端控制台。后端生成 JSON dump，前端读取展示。

完整版（TypeScript 类型、组件树、开发步骤）见 [`frontend-vibe-coding-prompt.md`](frontend-vibe-coding-prompt.md)。

## 核心数据契约

| 数据源 | 命令 | 文件 |
|--------|------|------|
| 主要 | `/sorter me dump` | `me-dump-*.json` — Cell 快照、物品聚合、容量字段 |
| 补充 | `/sorter me storageDump` | `storage-analysis-*.json` — 健康指标、语义候选 |

## MVP 页面结构

1. **Dashboard**（首页）— Health Summary Cards + 四图 + Health Flags
2. **Storages**（存储列表）— 表格 + Drawer 详情（容量仪表盘 + entries）
3. **Items**（物品浏览）— 表格 + Drawer 详情（occurrence 列表 + 分布）

## 给 AI 的指令

技术栈：React 18 + TypeScript + Ant Design 5 + Zustand + Vite。先给结果再纠偏，宏观调控优先。容量数据前端自己算（`byteUsageRatio`、`typeUsageRatio`、`healthLevel`）。完整类型定义见 [`frontend-vibe-coding-prompt.md`](frontend-vibe-coding-prompt.md#32-json-结构)。

> 完整版 prompt：[`frontend-vibe-coding-prompt.md`](frontend-vibe-coding-prompt.md) | 前端对接说明：[`../前端对接说明.md`](../前端对接说明.md) | 术语表：[`../架构/FACTS.md`](../架构/FACTS.md)

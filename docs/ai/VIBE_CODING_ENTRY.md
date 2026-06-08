# Applied Energistics: Insight — Vibe-Coding 入口

> Minecraft AE2 模组 `appliedinsight` 的前端控制台。后端生成 JSON dump，前端读取展示。

## 三条能力线

- **自动整理**：`/sorter merge` — 同类物品归并压实，无需配置
- **规则路由**：`/sorter me planAndMove` — 玩家定义存储区+过滤器，规则化整理
- **宏观观测**：`/sorter me storageDump` — 存储健康诊断，dashboard 快照

## 核心数据契约（精简版）

| 数据源 | 命令 | 文件 |
|--------|------|------|
| 主要 | `/sorter me dump` | `me-dump-*.json` — Cell 快照、物品聚合、容量字段 |
| 补充 | `/sorter me storageDump` | `storage-analysis-*.json` — 健康指标、语义候选 |

关键 TypeScript 类型见 [`frontend-vibe-coding-prompt.md`](frontend-vibe-coding-prompt.md#32-json-结构)。

## MVP 页面结构

1. **Dashboard**（首页）— Health Summary Cards + 四图 + Health Flags
2. **Storages**（存储列表）— 表格 + Drawer 详情（容量仪表盘 + entries）
3. **Items**（物品浏览）— 表格 + Drawer 详情（occurrence 列表 + 分布）
4. **Rules**（规则编辑，后续）— Filter Tree Editor

## 给 AI 的 Prompt 模板

```
你是一个 React + TypeScript 前端开发者。项目是 Minecraft AE2 模组 appliedinsight 的前端控制台。
数据来自 me-dump-*.json（/sorter me dump 产出），TypeScript 类型定义见 frontend-vibe-coding-prompt.md。
MVP 三页面：Dashboard / Storages / Items。技术栈：React 18 + TypeScript + Ant Design 5 + Zustand + Vite。
先给结果再纠偏，宏观调控优先。开始实现 [页面名]。
```

> 完整版 prompt 见 [`frontend-vibe-coding-prompt.md`](frontend-vibe-coding-prompt.md)

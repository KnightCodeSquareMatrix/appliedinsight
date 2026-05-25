# AI Development Guide

本文件是给 AI 协作者的**最小入口**，只说明高优先级规则。需要更详细背景时，优先继续阅读：

**新开发者/AI 首次接入（推荐顺序）**:
1. `README.md`
2. `docs/GLOSSARY.md` — 术语对齐
3. `docs/ARCHITECTURE_REFERENCE.md` — 架构全景
4. `docs/ai/AI_ENTRY.md` — AI 协作者完整入口
5. `docs/整体逻辑.md` — 项目主骨架
6. `docs/类职责总览.md` — 类审查结论

**按需查阅**:
- `docs/下一步计划.md` — 当前阶段方向
- `docs/前端对接说明.md` — 前端边界
- `docs/类职责/索引.md` — 99 个类职责索引
- `docs/DEVELOPER_GUIDE.md` — 开发者环境与工作流

---

## 1. 先理解两条能力线

### `/sorter merge`
- 这是**快速 merge 模式**
- 只负责把同类物品尽量合并到更少的 cell
- 不要把 profile / zone / route 逻辑塞进这条链路

### `/sorter me ...`
- 这是**ME 网络规则治理主线**
- 当前重点是：`dump`、`bindProfile`、`plan`、`planAndMove`
- 主线语义是：`item -> zone -> runtime zone -> execute`

---

## 2. 必须守住的边界

- `rule/**` 保持纯规则层，不要引入 Minecraft / AE2 运行时依赖
- `ae2/**` 负责 AE2 集成、扫描、执行、运行时映射
- `plan/**` 负责规划结果，不提前承担执行职责
- `logging/**` 负责可观测性，不参与业务决策
- DAV / 管理卡是玩家输入入口，不是 route 引擎本身

如果新增代码会把 `route`、`zone`、`cell`、`execute` 混在一起，先停下重想。

---

## 3. 当前命名与语义约定

- `merge` 只用于快速合并路径
- `plan` / `planAndMove` 只用于 profile/zone 主线
- 不要把 `/sorter merge` 再写成通用 sorter 主入口
- 不要把 zone 主线错误表述成“只是 merge”

文案、日志、命令说明都应尽量和这个语义保持一致。

---

## 4. 当前不建议做的事

- 不要为了未来猜想引入大框架
- 不要强行抽象一整套 node / endpoint / provider 体系
- 不要把简单 DTO 拆成多层空壳 service
- 不要随意重写 `DriveMachineAccessor`，除非出现新的兼容需求或上游变化

---

## 5. 修改代码前的建议动作

1. 先读相关类职责文档
2. 再读对应源码
3. 判断改动属于哪条能力线：`merge` 还是 `me plan` 主线
4. 若涉及架构判断，优先同步更新：
   - `docs/整体逻辑.md`
   - `docs/类职责总览.md`
   - 必要时更新对应类职责文档

---

## 6. 一句话原则

> 优先维护现有清晰边界；小步收口，少做幻想式重构；详细背景去看上面的文档，而不是在这里重复展开。

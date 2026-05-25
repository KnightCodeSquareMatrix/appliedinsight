# 重构计划 Part 1.5：运行时主干收敛

## 0. 开始前必须阅读
在开始本阶段重构前，必须先阅读：

1. `docs/AI_DEVELOPMENT_GUIDE.md`
2. `docs/整体逻辑.md`
3. `docs/类职责总览.md`
4. `docs/边界收敛重构计划.md`
5. `docs/重构计划-part1.md`
6. `docs/RuntimeTopology-设计草案.md`
7. `docs/Analyzer-阶段定位草案.md`

阅读目标：
- 明确这不是扩张式重构，而是收敛式重构
- 明确 `application` / `infrastructure` / `rule` 的边界
- 明确 `RuntimeTopology` 是运行时主干
- 明确 `RuntimeZone` 具有内部自治
- 明确 `Analyzer` 未来潜力很大，但本阶段只做最小搬运可行性解释

---

## 1. 本阶段目标
本阶段只解决四个问题：

1. 确立 `RuntimeTopology` 作为运行时主干
2. 保留 `ZoneManager`，但将其收敛为 topology 内部的 zone 子域管理器
3. 让 `RuntimeZone` 开始承接 zone 内部自治能力
4. 正式引入 `Analyzer`，但本阶段只落地“能不能搬进去”

一句话：

> 先把运行时世界、zone 内自治、最小分析解释权三件事定住，再继续后续大阶段。

---

## 2. 本阶段明确不做
本阶段**不要**做以下事情：

- 不做完整 `StorageAnalyzer`
- 不做 bus / machine 级完整分析系统
- 不做 `Ae2ZoneMoveExecutor` 大拆解
- 不做完整 source hierarchy 设计
- 不做通用 AE2 网络平台化
- 不做“未来全能 mod”级别扩张设计

本阶段原则：

> **只做主干收敛，不做体系扩张。**

---

## 3. 已确认的架构结论

### 3.1 `RuntimeTopology`
`RuntimeTopology` 是：
- 一次执行中当前 ME 网络的运行时世界模型
- planner / executor / analyzer 共用事实中心
- `RuntimeZoneRegistry` 的上位替代方向

### 3.2 `ZoneManager`
`ZoneManager` 保留，但定位变为：
- `RuntimeTopology` 内部的 zone 子域管理器
- 负责 zone 集合、查询、分类、跨 zone 视图
- 不再承担世界中心角色

### 3.3 `RuntimeZone`
`RuntimeZone` 是：
- 有内部自治能力的运行时治理单元
- 持有 zone 内 members / capabilities / zone 内诊断
- 不负责全局拓扑关系

### 3.4 `Analyzer`
`Analyzer` 应正式存在。

但本阶段只负责：
- 判断能不能把物品搬进去
- 提供最小阻塞原因
- 承接 merge / writable / placement 等能力解释权

Executor 现阶段只要求：
- **能搬就搬**

---

## 4. 本阶段要保留的稳定核心
本阶段不要重写以下能力主线，只允许做边界收敛：

- `/sorter merge`
- `/sorter me plan`
- `/sorter me planAndMove`
- 现有 dump / storageDump / bindProfile 能力

本阶段不要改变：
- 命令语义
- 对外 JSON 契约
- 现有 use-case 入口

---

## 5. 本阶段推荐改动顺序

## Step 1：建立运行时主干命名与职责
目标：
- 在代码中接受 `RuntimeTopology` 方向
- 明确 `ZoneManager` 是 topology 内部组件
- 明确 `RuntimeZone` 承接内部自治

验收：
- 新老命名关系清楚
- 不再把 registry 当作最终运行时中心

---

## Step 2：让 `RuntimeZoneRegistryBuilder` 朝 topology service 方向演化
目标：
- 把“运行时世界解释权”开始集中
- 为后续 `TopologyService` 打基础

注意：
- 本阶段可以保留过渡命名
- 重点是职责收敛，不是一次性改完全部实现

验收：
- builder 不再只是 registry 拼装器思路
- topology 语义开始成立

---

## Step 3：把 zone 内能力判断收回 `RuntimeZone`
优先收回：
- writable
- mergeable
- placement / admission
- zone 内基本诊断

验收：
- zone 是否可接收 / 可合并，不再主要由 executor 隐式解释
- `RuntimeZone` 开始具备内部自治语义

---

## Step 4：正式引入最小 `Analyzer`
本阶段只做一个最小分析器，职责是：
- 判断是否能把一批物品搬进去
- 判断哪些 zone 当前不可用
- 给出最小阻塞原因

不要扩张为完整分析系统。

验收：
- executor 不再独占可用性解释权
- analyzer 已正式进入主干，但仍保持极小

---

## 6. 本阶段完成标准
完成后，应满足：

### 6.1 认知完成标准
阅读代码时应能快速回答：
- 运行时主干是什么？
  - `RuntimeTopology`
- zone 集合由谁组织？
  - `ZoneManager`
- zone 内能力由谁解释？
  - `RuntimeZone`
- 搬运可行性解释权初步在谁？
  - 最小 `Analyzer`

### 6.2 结构完成标准
- `RuntimeZoneRegistry` 不再被继续强化为最终中心模型
- `ZoneManager` 已被收敛为 topology 内部组件
- `RuntimeZone` 已开始承接 zone 内能力判断
- 最小 analyzer 已有正式位置

### 6.3 行为完成标准
以下行为仍应保持可用：
- 现有 merge 行为
- 现有 plan / planAndMove 行为
- 现有 dump / storageDump 行为

---

## 7. 给 Cline 的执行提醒
执行本阶段时请始终遵守：

1. 先读本文第 0 节列出的文档，再改代码
2. 优先做职责收敛，不要顺手扩张体系
3. 任何新对象都先回答：它服务于哪条现有能力线
4. 本阶段的 analyzer 只能做最小搬运可行性判断
5. 如改动影响架构理解，同步更新相关文档

一句话提醒：

> **本阶段不是做未来全能系统，而是给未来全能系统打一个不混乱的运行时主干。**

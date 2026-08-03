# RuntimeTopology 设计草案

## 1. 目标
`RuntimeTopology` 是一次 use-case 执行中，**当前 ME 网络的运行时世界模型**。

它负责统一承接：
- 配置快照
- network / machine / zone / cell 的运行时事实
- planner / executor / analyzer 共用查询

---

## 2. 定位
它是：
- `ConfigSnapshot + IGrid -> RuntimeTopology` 的产物
- `RuntimeZoneRegistry` 的上位替代
- 允许适度偏胖的运行时核心对象

它不是：
- JSON 真相源
- 规则定义层
- use-case 编排层
- move 执行器
- 用户反馈拼装器

---

## 3. 最小组成

### 3.1 上下文
至少包含：
- 当前 grid / network 标识
- 本次使用的 config snapshot
- 构建时间 / 构建上下文
- 是否 degraded
- 网络级 warnings / diagnostics

### 3.2 核心对象
至少包含：
- `RuntimeZone`
- `RuntimeMachine`
- `RuntimeCell`
- `SourceRuntimeZone` 或等价 source 视图

### 3.3 核心关系
至少维护：
- `zoneId -> RuntimeZone`
- `machineId -> RuntimeMachine`
- `cellId -> RuntimeCell`
- `zone -> cells`
- `machine -> zone`
- `machine -> cells`
- `source -> candidate targets`（可后置，但要预留）

---

## 4. 关键原则

### 4.1 Topology 持有“已解释后的世界”
不是原始扫描仓库。

也就是说，它里面的对象已经完成：
- 配置归属
- zone 解释
- machine 角色解释
- 基本可用性判断

### 4.2 Topology 是 network 级事实中心
凡是“当前 ME 网络运行时事实”，优先考虑归入 `RuntimeTopology`。

### 4.3 允许 Topology 胖，换取其他对象变薄
重点是：
- planner 变薄
- executor 变薄
- `ZoneManager` 成为 topology 内部的 zone 子域组件
- registry builder 演化为 topology service

### 4.4 诊断分层
诊断应当分层放置：
- `RuntimeTopology` 持有整个网络级诊断
- `RuntimeZone` 持有 zone 内部诊断与分析

不要把 zone 内部问题全部抬升到 topology。
也不要把网络级问题下沉到 zone。

---

## 5. RuntimeZone 的职责
`RuntimeZone` 是 **具有内部自治能力的运行时治理单元**。

它负责：
- 持有 declared zone 的运行时投影
- 持有本 zone 的 cells / machines 视图
- 维护本 zone 的内部成员状态
- 判断是否可接收 / 可合并 / 可放置
- 提供 zone 内部能力查询
- 持有 zone 内部诊断与分析结果

它不负责：
- 全局索引
- 全局拓扑关系
- 跨 zone 协调
- 配置读取
- 执行编排

---

## 6. ZoneManager 的职责
`ZoneManager` 是 `RuntimeTopology` 内部的 **zone 子域管理器**。

它负责：
- 持有所有 `RuntimeZone`
- 提供 `zoneId -> RuntimeZone` 查询
- 提供 source / target / capability 视图
- 提供跨 zone 索引
- 汇总 zone 集合视图

它不负责：
- 构建整个 topology
- 扫描 machine
- 读取 JSON
- 执行 move
- 规划 route

---

## 7. RuntimeTopology 的职责
`RuntimeTopology` 负责 **全局统一视图**：
- 持有 context / network diagnostics
- 持有 `ZoneManager`
- 持有 machine / cell 级对象与索引
- 提供全局查询
- 持有 unresolved / degraded / warning 等网络级信息
- 为 planner / executor / analyzer 提供共同语言

---

## 8. 关于 machine 级对象
`RuntimeTopology` 必须正式纳入 machine 级对象。

原因：
- 未来不只是 zone 操作
- 还会有 machine 级诊断、控制、观察
- 只有 zone + cell 不足以描述真实网络结构

因此推荐保留三层视角：
- zone：治理视角
- machine：节点视角
- cell：存储载体视角

---

## 9. 关于 source-only zone
建议将 source-only zone 作为**正式一等模型**。

意义：
- merge 与 move 的执行语义可收敛
- planner / executor 可以共享统一语言
- “来源物品”不再是特殊散落逻辑

当前不必先做复杂继承设计，但应先承认其模型地位。

---

## 10. 对外服务对象

### planner 需要
- 所有 target zones
- source zones / source 视图
- zone 能力
- machine / cell 归属与可用性
- 网络级诊断
- 必要的 zone 内部诊断

### executor 需要
- plan 引用的 zones / machines / cells
- 实际可写性 / 可达性 / merge 能力
- 执行所需索引

### analyzer / dump 需要
- topology 的完整运行时结构
- 网络级诊断与降级信息
- zone 内部分析结果
- zone / machine / cell 的映射关系

---

## 11. 不放进 Topology 的内容
以下内容不应进入 `RuntimeTopology`：
- JSON 文件读写
- CLI
- 规则定义本身
- 路由规划算法本身
- move 执行过程状态
- command feedback 文案

---

## 12. 下一步落点
下一步只需继续确定三件事：
1. `RuntimeMachine` 的最小职责
2. `SourceRuntimeZone` 是否独立成类
3. `TopologyService` 的输入输出边界
4. `ZoneManager` 与 `RuntimeZone` 的最小接口

---

## 13. 一句话结论
`RuntimeTopology` 应成为：

> **一次执行中，对当前 ME 网络的统一、已解释、可执行、可观察的运行时世界模型。**

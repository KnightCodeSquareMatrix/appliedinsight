# 过滤器、zone、router 的存储管理

> 当前阶段的落地计划详见：
> `docs/存储管理/dump-resolver-executor-闭环计划.md`

## 1. 定位

这一条能力线解决的是：

> **根据玩家配置的 profile，把物品路由到指定 zone，再落到声明该 zone 的 cell 集合中。**

它不是简单的“合并同类”，而是：

- 有过滤器
- 有 zone
- 有 route rule
- 有 default zone
- 有前端生成的完整 profile
- 最终运行时管理对象是 cell

一句话：

- **存储管理 = 玩家显式配置的规则路由与 cell 级物理落点管理**

---

## 2. 当前已有基础

当前项目已经具备以下基础设施：

### 规则与配置侧

- `RoutingProfileJsonCodec`
- `RoutingProfile`
- `StorageZone`
- `ItemFilter`
- `RouteRule`
- `RoutingEngine`
- `ItemMatchContext`

### 前端协作侧

- React Query Builder 对齐的 filter tree
- `routing-profile.schema.json`
- `filter-ui-metadata.json`
- 前端可生成完整 profile JSON

### 物理宿主侧

- `DigitalAssetManagementCardItem`
  - 已可承载 `zoneId` / `zoneName`
- `DigitalAssetVaultBlockEntity`
  - 已可读取卡上声明的 `zoneId` / `zoneName`

### 网络移动侧

- `SorterMoveOperation`
- `PlannedMove`
- `MEStorage` 的 extract / insert / rollback 方法已验证可用

---

## 3. 当前版本的最小运行时目标

当前版本不追求完整的大型拓扑模型，也不追求执行时强一致的在线重规划。

这一步的目标收敛为：

1. 运行时先导出网络 dump
2. 在子线程中解析 dump 与 profile
3. 生成一整串搬运指令
4. 在执行阶段按指令 best-effort 搬运
5. 对于执行期的网络变化：
   - 不重新规划
   - 不补偿外部自动化
   - 不追求强一致
6. 通过 threshold 与跳过策略保证本 mod 的调控闭环稳定

也就是最短闭环：

- **dump -> resolver -> instructions -> executor**

---

## 4. 当前建议的最小组件形态

这一阶段只落地两个核心组件：

### 4.1 `ZoneAllocationResolver`

定位：

> **运行在子线程中的 dump 解析器 / 路由指令生成器。**

职责：

- 读取 dump 文件
- 读取前端生成的完整 profile
- 根据 item、zone、filter、threshold 生成搬运指令序列
- 为每条指令附带 route 决策信息与目标落点

不负责：

- 实时网络一致性修正
- 执行期重新规划
- 与外部自动化做补偿协同

### 4.2 `Ae2ItemMoveExecutor`

定位：

> **按 resolver / planner 生成的 `item -> zone` 指令，把物品送入目标 zone 的 cell。**

职责：

- 根据指令定位 source
- 根据 `zoneId` 找到目标 `RuntimeZone`
- 在该 zone 的 cells 中选择一个可接收目标
- 执行 `extract -> insert -> rollback` 或 skip
- 输出执行结果，供日志与下一轮闭环使用

不负责：

- zone 内部深度整理
- filter / route 判定
- dump 解析
- threshold 判定
- 重新生成规划

### 4.3 `ZoneSorter`

定位：

> **针对某个 zone 内部的 cells 做 consolidation / internal organization 的 sorter。**

职责：

- 只处理某个 `RuntimeZone` 内部 cells
- 对 zone 内同类 item 做归并
- 负责 DAV 插入 Zone Card 后的内部整理

这意味着：

- executor 负责“送进 zone”
- `ZoneSorter` 负责“zone 内整理”

---

## 5. 当前版本明确不做的事

这一版先不做：

- 完整的 storage topology 抽象层
- 大而全的 endpoint / node / sink 模型
- 高级 destination scoring
- 复杂 zone 内再平衡
- 多 zone 复合声明
- profile 编辑 GUI 本体

这一步只追求：

> **前端 profile 能被 runtime 消费，并真的驱动一次按 zone 的分发。**

---

## 6. 与自动合并同类 sorter 的边界

这一条能力线和自动整理要明确区分：

### 自动合并同类 sorter
- 不读 profile
- 不看 zone route
- 不关心语义
- 只做归并
- 后续会拆成：
  - `MESorter`
  - `ZoneSorter`

### 存储管理 / zone 路由
- 必须读 profile
- 必须跑 filter / route
- 必须识别 DAV 与 zone 声明
- executor 必须把物品送到目标 zone 的 cell

后续运行时建议明确分两步：

1. 先做按 zone 分发
2. 再在 zone 内由 `ZoneSorter` 做自动归并

这样“分发”和“整理”仍保持实现上的分离。

---

## 7. 当前近期计划

### 阶段 A
先建立统一运行时模型：

- `RuntimeCell`
- `RuntimeZone`

### 阶段 B
重构 sorter 体系：

- `AbstractSorter`
- `MESorter`
- `ZoneSorter`

### 阶段 C
收缩 executor 职责：

- 只负责把物品送到目标 zone 的 cell
- zone 内部整理交给 `ZoneSorter`

### 阶段 D
继续让 profile / planner / executor / sorter 共享同一套 cell / zone 运行时模型。

---

## 8. 一句话结论

当前这条能力线的最近目标不是“再做一个复杂 planner 框架”，而是：

> **先以 cell 为中心建立统一运行时模型，再把 executor 和 `ZoneSorter` 分层：executor 负责把物品送到目标 zone 的 cell，`ZoneSorter` 负责 zone 内部整理。**

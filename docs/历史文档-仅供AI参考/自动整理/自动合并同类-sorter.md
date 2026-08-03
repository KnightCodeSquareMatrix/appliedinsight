# 自动合并同类 sorter

## 1. 定位

这一条能力线只解决一件事：

> **在同一个 AE2 网络内部，把已经存在于多个 cell 中的同类物品进行归并。**

它不负责：

- 判断物品属于哪个 zone
- 读取 filter / route / profile
- 理解玩家的存储语义
- 做跨 zone 的治理分发

一句话：

- **自动合并同类 = 网络内部压实与归并**

---

## 2. 当前已有成果

当前项目已经完成：

- 可从 `ME Controller` 解析 `IGrid`
- 可扫描 AE2 / ExtendedAE 的 drive-like machine
- 可遍历 cell 与 `AEItemKey`
- 可生成第一版 `plan -> execute` move 流程
- 可在 `MEStorage` 上完成：
  - `insert(..., SIMULATE, ...)`
  - `extract(..., MODULATE, ...)`
  - `insert(..., MODULATE, ...)`
  - rollback

当前第一版 `SorterMovePlanner` 的价值，不在于策略先进，而在于它已经证明了：

> **AE2 网络内物品移动的方法本身是成立的。**

---

## 3. 当前运行时边界

当前自动整理能力主要涉及：

- `Ae2ControllerTargetResolver`
- `Ae2DriveScanner`
- `DriveMachineAccessor`
- `SorterMovePlanner`
- `SorterMoveOperation`
- `PlannedMove`
- `SorterMoveExecutionResult`

当前这条能力线已经明确会往新的 sorter 分层演进：

- `AbstractSorter`
- `MESorter`
- `ZoneSorter`

其中：

### `SorterMovePlanner`
当前仅实现最朴素的规则：

- 第一次看到某个 `AEItemKey` 的 cell，记为 destination
- 后续再看到同 key 的 source cell，就尝试往第一个 destination 塞
- 若 `SIMULATE insert` 成功，则生成 move

它的当前价值主要是：

- 验证 AE2 内部搬运方法
- 为后续 `AbstractSorter` 提供 consolidation 算法基础

### `SorterMoveOperation`
负责真正执行：

1. 从 source 抽取
2. 向 destination 插入
3. 插入不足时 rollback

这部分会继续作为后续 sorter 的公共执行基础保留。

---

## 4. 当前不纳入这条能力线的内容

以下内容不属于“自动合并同类 sorter”本身：

- `RoutingProfileJsonCodec`
- `RoutingEngine`
- `StorageZone`
- `RouteRule`
- `Digital Asset Vault` 的 `zoneId`
- 前端生成 profile
- 玩家指定 profile 文件路径

这些都属于另一条能力线：**存储管理 / zone 路由**。

---

## 5. 后续演进方向

这一条能力线后续仍然保持“无语义、只归并”的边界，但会引入明确的 sorter 分层。

### 近期可做

- 提炼 `AbstractSorter`
- 新增 `MESorter`
- 新增 `ZoneSorter`
- 引入 `RuntimeCell` / `RuntimeZone`
- 让有 Zone Card 的 DAV 内部整理由 `ZoneSorter` 接管

### 中期可做

- destination 选择比“第一个见到的 cell”更合理
- 优先合并到已经包含该 key 的目标 cell
- 减少来回波动
- 限制每次操作规模
- 更稳的压实顺序
- 容量预留策略
- 更好的执行 explain / log

---

## 6. 当前建议

未来运行时建议明确拆成两条路径：

1. **自动合并同类**
   - 不读 profile
   - 不看 zone route
   - 只做归并
   - 但未来会细分为：
     - `MESorter`：未声明 zone 的 cells
     - `ZoneSorter`：某个 zone 内部 cells

2. **存储管理 / 按 zone 分发**
   - 读取 profile
   - 跑 filter / route
   - 根据 DAV 的 zone 声明决定去向
   - executor 负责把物品送到目标 zone 的 cell 中

这样两条能力线职责清晰，不会把简单 sorter 和存储治理逻辑搅在一起。

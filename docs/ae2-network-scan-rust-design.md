# AE2 网络扫描 Rust 实现设计方案

> 状态：设计阶段  
> 日期：2026-06-02  
> 关联：[[动车上的头脑风暴]] | DAV 自定义存储引擎 | RuntimeTopology 抽象层

---

## 1. 目标与范围

### 1.1 解决什么问题

AE2 存储网络的全量扫描（遍历 drive → cell → slot → `getAvailableStacks()`）是 O(全部物品条目)。当网络增长到 100+ cell、数万种物品时：

- 单次扫描耗时从 5ms 膨胀到 500ms+
- 扫描产生的临时对象（`AEItemKey`、`GenericStack`、`HashMap` entry）触发 GC
- 如果 on-tick 执行（自动排序/碎片整理/健康检查），每次 tick 都可能卡顿

### 1.2 不在范围

- 不负责 AE2 的 extract/insert 执行（必须 Java 调 AE2 API）
- 不负责 Minecraft 存档持久化（Rust 有独立存储，Java 侧 NBT 保存元数据引用）
- 不替代现有的离线分析工具（SorterDumpAnalyzer 等仍可用于离线场景）

### 1.3 设计原则

1. **Java 主线程只读结果**：每次 tick 从共享内存读一个版本号 + 指针，O(1)
2. **Rust 做所有计算**：扫描数据聚合、碎片分析、排序规划、健康检查
3. **崩溃不影响 JVM**：Rust 进程崩溃 → Java 检测健康标志 → 降级使用上次有效数据
4. **渐进落地**：先并行验证 → 配置开关切流 → 稳定后移除旧路径

---

## 2. 架构总览

```
┌──────────────────────────────────────────────────────────────────┐
│  Minecraft Server (JVM)                                          │
│                                                                  │
│  ┌─ 主 tick (每 50ms) ──────────────────────────────────────┐   │
│  │  version = shm.readVersion()        // O(1) 原子读       │   │
│  │  if (version != lastVersion) {                            │   │
│  │      result = shm.readResultPtr()   // O(1) 指针读       │   │
│  │      topology = decode(result)      // 零拷贝 MemorySegment│   │
│  │      lastVersion = version                                │   │
│  │  }                                                        │   │
│  │  executePendingMoves(topology)       // 执行已规划的移动   │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                  │
│  ┌─ 推送线程 (每 200 tick / 10s) ──────────────────────────┐   │
│  │  snapshot = scanAllDrives(grid)     // Java 调 AE2 API   │   │
│  │  shm.writePushBuffer(snapshot)      // 写入推送缓冲区     │   │
│  │  shm.signalPushReady()              // 原子标记推送就绪   │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                  │
└──────────────────────────┬───────────────────────────────────────┘
                           │ FFM MemorySegment (mmap)
                           │
┌──────────────────────────┴───────────────────────────────────────┐
│  Rust 独立进程 (长期运行)                                         │
│                                                                  │
│  ┌─ 主循环 (事件驱动) ──────────────────────────────────────┐   │
│  │  loop {                                                   │   │
│  │      wait_for_push_signal()                                │   │
│  │      snapshot = decode_push_buffer()                       │   │
│  │      model.apply_delta(snapshot)  // 更新内部网络模型      │   │
│  │      report = analyze(&model)     // 碎片/健康/容量        │   │
│  │      plans = plan_moves(&model)   // 排序/迁移计划         │   │
│  │      write_result_double_buffer(report, plans)             │   │
│  │      atomic_swap_version()         // 切换活跃指针         │   │
│  │  }                                                         │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                  │
│  ┌─ 内部数据结构 ───────────────────────────────────────────┐   │
│  │  NetworkModel {                                            │   │
│  │      cells: HashMap<CellId, CellState>                     │   │
│  │      items: HashMap<ItemFingerprint, GlobalItemStats>      │   │
│  │      zones: HashMap<ZoneId, ZoneState>                     │   │
│  │  }                                                         │   │
│  └──────────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────────┘
```

---

## 3. 共享内存布局

```
 偏移      大小      字段                 说明
─────────────────────────────────────────────────────────
 0         8B       version             u64，单调递增版本号
 8         1B       health             u8，0=健康 1=panic 2=退化
 9         7B       _reserved1         保留对齐
 16        8B       result_offset      u64，结果数据在 buffer 中的偏移
 24        4B       result_len         u32，结果数据字节长度
 28        4B       result_format      u32，0=JSON 1=二进制 2=混合
 32        32B      _reserved2         保留
 64        8B       push_offset        u64，推送缓冲区偏移
 72        4B       push_len           u32，推送数据长度
 76        4B       push_ready         u32，0=空闲 1=就绪(Java写完) 2=已消费(Rust读完)
 80        48B      _reserved3         保留
─────────────────────────────────────────────────────────
 128       64MB     data_region        数据区（包含双缓冲结果 + 推送缓冲区）

数据区子布局:
  [128  .. 128+32MB)    result_buffer_A    结果缓冲区 A
  [128+32MB .. 128+64MB) result_buffer_B   结果缓冲区 B
  [128+64MB .. 128+80MB) push_buffer       推送缓冲区 (16MB)
```

**设计要点**：

- **首部 128 字节对齐到缓存行**（64B 边界），消除 false sharing
- **version 独立缓存行**：Java 每次 tick 只读这个字段，不与其他字段争用
- **push_ready 三态**：避免 Java 在 Rust 未消费时覆盖，也避免 Rust 读旧数据
- **结果使用偏移+长度而非指针**：共享内存在不同进程中虚拟地址不同，用相对偏移

---

## 4. 双向数据协议

### 4.1 Java → Rust：网络快照（推送方向）

频率：每 200 tick（可配置），或增量事件触发

```
NetworkSnapshot {
    header: SnapshotHeader {
        magic: [u8; 4] = b"AE2S",
        version: u16,          // 协议版本（当前=1）
        timestamp: u64,        // System.currentTimeMillis()
        snapshot_id: u64,      // 单调递增
        cell_count: u32,
        item_count: u32,
        flags: u32,            // bit0=增量 bit1=全量
    },
    cells: [CellSnapshot; cell_count],
    changes: [ItemDelta; item_count],  // 仅增量模式
}

CellSnapshot {
    drive_pos: [i32; 3],       // BlockPos x,y,z
    slot: u8,
    cell_kind: u8,             // 0=item 1=fluid 2=external_storage
    zone_id_len: u8,
    source_block_id_len: u16,
    stack_count: u16,          // 这个 cell 里有多少种物品
    zone_id: [u8; zone_id_len],
    source_block_id: [u8; source_block_id_len],
    stacks: [StackEntry; stack_count],
}

StackEntry {
    item_kind: u8,             // 0=AEItemKey 1=AEFluidKey
    namespace_len: u8,
    path_len: u16,
    nbt_len: u16,              // 序列化 NBT 长度
    amount: i64,
    namespace: [u8; namespace_len],
    path: [u8; path_len],
    nbt: [u8; nbt_len],        // 原始 NBT 二进制
}
```

**增量模式 vs 全量模式**：

- **全量**（默认）：每 200 tick 推送整个网络的完整快照。简单可靠，网络小于 200 cell 时足够。
- **增量**（优化路径）：Java hook AE2 的 `IGridNodeListener.onSaveChanges()` 收集变更，推送 delta。降低推送频率，但增加 Rust 侧合并复杂度。**启动时先做全量，之后只推增量。**

### 4.2 Rust → Java：分析结果（读取方向）

```
AnalysisResult {
    header: ResultHeader {
        magic: [u8; 4] = b"RS2J",
        version: u16,
        timestamp: u64,
        result_id: u64,         // 对应 snapshot_id
        section_count: u16,
    },
    sections: [ResultSection; section_count],
}

ResultSection {
    section_type: u16,          // 0=topology 1=fragmentation 2=health 3=plan 4=json
    offset: u32,                // 本 section 数据在结果 buffer 中的相对偏移
    len: u32,
}

// section_type=0: RuntimeTopology 的二进制表示
// section_type=1: FragmentationReport
// section_type=2: HealthReport
// section_type=3: MovePlan (排序/迁移计划)
// section_type=4: 预生成的 JSON 字符串（给前端，零拷贝发送）
```

**Java 读取路径**：

```java
// 主 tick - O(1)
long version = shm.getLong(VERSION_OFFSET);
if (version != lastVersion) {
    long offset = shm.getLong(RESULT_OFFSET_OFFSET);
    int len = shm.getInt(RESULT_LEN_OFFSET);
    // MemorySegment 零拷贝，不分配堆内存
    MemorySegment result = shm.segment().asSlice(HEADER_SIZE + offset, len);
    cachedResult = ResultDecoder.decode(result);
    lastVersion = version;
}
// 使用 cachedTopology 执行移动
executor.executeMoves(cachedResult.plan());
```

---

## 5. Rust 内部模型

```rust
// 网络拓扑模型
struct NetworkModel {
    cells: HashMap<CellId, CellState>,
    items: HashMap<ItemFingerprint, GlobalItemStats>,
    zones: HashMap<String, ZoneState>,
    last_full_snapshot_id: u64,
    degraded: bool,
}

struct CellState {
    drive_pos: [i32; 3],
    slot: u8,
    cell_kind: CellKind,
    zone_id: String,
    source_block_id: String,
    stacks: Vec<StackEntry>,
    total_bytes: u64,
    distinct_types: u32,
    last_updated_snapshot_id: u64,
}

struct GlobalItemStats {
    total_amount: i64,
    cell_count: u32,           // 出现在多少个 cell
    zone_distribution: HashMap<String, i64>,  // 各 zone 的数量
    fragmentation_score: f32,  // 碎片化评分
    nbt_pressure: u32,         // NBT 字节 * 出现次数
}

// 分析计算
fn analyze(model: &NetworkModel) -> AnalysisReport {
    // 1. 碎片分析：每个 item 分布在多少个 cell
    // 2. 健康检查：infinite cell 检测、外部存储泄漏、NBT 爆炸
    // 3. 容量趋势：bytes 使用率、type slot 使用率
    // 4. 异常检测：单一 item 占据 50%+ 空间、闲置 cell
}

// 排序规划
fn plan_moves(model: &NetworkModel, rules: &RuleProfile) -> MovePlan {
    // 1. 根据 routing profile 确定目标 zone
    // 2. 找到 misplaced items（在错误 zone 的 cell 里）
    // 3. 规划移动：source cell → zone target cell
    // 4. 排序：按优先级（碎片度 > 容量 > NBT 压力）
}
```

---

## 6. Rust 进程生命周期

```
Minecraft Server 启动
    │
    ├─→ NeoForge ServerAboutToStartEvent
    │     Java 启动 Rust 子进程
    │     ├─ Windows: .\appliedinsight_engine.exe
    │     ├─ Linux:   ./appliedinsight_engine
    │     └─ macOS:   ./appliedinsight_engine
    │     Rust 进程启动 → 创建/打开共享内存 → 写入 health=0 → 等待推送
    │
    ├─→ 运行期间
    │     Java 每 200 tick 推送快照
    │     Rust 消费 → 分析 → 写结果
    │     Java 每 tick 读结果
    │
    ├─→ Rust panic
    │     panic hook 写 health=1 → 进程退出
    │     Java 下个 tick 检测到 health!=0 → 降级：
    │       - 停止推送新快照
    │       - 继续使用上次有效 cachedResult
    │       - 在 GUI 显示 "分析引擎不可用"
    │       - 触发 Rust 进程重启（指数退避：1s → 2s → 4s → max 30s）
    │     Rust 重启成功 → health=0 → Java 自动恢复
    │
    └─→ Minecraft Server 停止
          NeoForge ServerStoppingEvent
          Java 发 SIGTERM → Rust 收到信号 → 保存状态 → 退出
          共享内存关闭
```

### 6.1 进程间通信通道

除了共享内存（数据通道），还需要一个**信号通道**：

```
方案: 共享内存中的原子标志 + eventfd (Linux) / Event (Windows)

push_ready 标志:
  0 (空闲)  → Rust 等待
  1 (就绪)  → Java 写完，Rust 开始读取
  2 (已消费) → Rust 读完，Java 可以写下一批

version 标志:
  Java 每 tick 读 version
  Rust 写完新结果后递增 version
  Java 检测到 version 变化 → 读新结果
```

---

## 7. 与现有代码的集成点

### 7.1 替换路径

```
现有: RuntimeZoneRegistryBuilder → RuntimeTopology
                    ↓
新路径: SharedMemoryReader → RustBackedTopology (impl RuntimeTopology 接口)
                    ↓
         SorterPlanService / SorterStorageAnalysisService
         (业务代码不变，仍调用 RuntimeTopology)
```

### 7.2 需要新增的类

```
com.knightcode.appliedstoragesorter.ffm/
    SharedMemoryLayout.java          // 共享内存布局常量 + 偏移定义
    SharedMemoryManager.java         // 共享内存创建/打开/关闭
    SharedMemoryWriter.java          // Java → Rust 推送（写 push buffer）
    SharedMemoryReader.java          // Rust → Java 读取（读 result buffer）
    RustProcessManager.java          // Rust 子进程启动/停止/重启
    NetworkSnapshotEncoder.java      // AE2 数据 → 二进制格式编码
    ResultDecoder.java               // 二进制结果 → RuntimeTopology 解码
    DegradedModeHandler.java         // 降级策略 + 健康监控

com.knightcode.appliedstoragesorter.ae2.zone/
    RustBackedTopology.java          // RuntimeTopology 的共享内存实现
```

### 7.3 需要修改的现有类

```
appliedinsight.java:
    + 在 commonSetup 中通过 RustProcessManager 启动 Rust 进程
    + 在 serverStopping 中停止 Rust 进程

Config.java:
    + scanEngineMode: "java" | "rust" | "auto"
    + rustScanIntervalTicks: 200 (默认)
    + rustBinaryPath: "" (留空自动检测)

RuntimeZoneRegistryBuilder.java:
    + 当 scanEngineMode=rust 时，返回 RustBackedTopology 而非构建拓扑
```

### 7.4 被 Rust 替代的计算逻辑

```
当前 Java 实现                      → Rust 替代
─────────────────────────────────────────────────────────
Ae2StorageAnalyzer.analyze()       → analyze() 碎片/健康/容量分析
LiveZoneAllocationPlanner.plan()   → plan_moves() 排序规划
ZoneMergePlanner                   → plan_merge() 合并规划
NewDavMigrationService.migrateOnce → plan_migration() DAV 迁移候选
EnergyCostCalculator               → estimate_cost() 能量预估
```

---

## 8. 崩溃恢复与降级

```
健康状态机:

  HEALTHY (0)
    │
    ├─ Rust 心跳超时 (5s 无 version 递增)
    │  → SUSPECT (Java 内部状态，health 字段仍为 0)
    │  → 等 3 个 tick 周期
    │     ├─ 恢复 → HEALTHY
    │     └─ 仍未递增 → 标记 health=1 (由 Java 写入)
    │
    ├─ Rust panic hook 写入 health=1
    │  → DEGRADED
    │  → Java 降级操作:
    │      1. cachedResult 保持不变
    │      2. GUI 显示警告
    │      3. 禁止自动排序（防止基于过期数据移动物品）
    │      4. 允许手动 dump（回退到纯 Java 路径）
    │
    └─ Rust 进程退出
       → 指数退避重启
       → 重启成功 + health=0
       → 下个推送周期触发全量扫描
       → 恢复正常
```

**降级模式下能做什么 / 不能做什么**：

| 操作 | 降级模式 | 说明 |
|------|---------|------|
| 查看上次分析结果 | ✅ | 使用缓存 |
| 手动触发 dump | ✅ | 回退 Java 路径 |
| 自动排序 | ❌ | 基于过期数据有风险 |
| 自动碎片整理 | ❌ | 同上 |
| 查看实时仪表盘 | ⚠️ | 显示缓存 + "数据可能过期"标志 |

---

## 9. 实现路线图

### Phase 1: 最小可行骨架（Week 1-2）

**目标**：Java 能启动 Rust 进程，共享内存能读写，能传递一个模拟网络快照并读回分析结果。

```
Java 侧:
  □ SharedMemoryLayout     - 常量定义
  □ SharedMemoryManager    - mmap 创建/打开（FFM API）
  □ SharedMemoryWriter     - 写入二进制快照
  □ SharedMemoryReader     - 读取分析结果
  □ RustProcessManager     - 子进程启动（ProcessBuilder）
  □ 单元测试：写入 → 手动读回验证布局

Rust 侧:
  □ Cargo.toml + 项目骨架
  □ shared_memory 模块（mmap, 布局常量）
  □ 接收 Java 写入的模拟快照
  □ 硬编码返回一个分析结果
  □ 集成测试：Java 写 → Rust 读 → Rust 写 → Java 读
```

### Phase 2: 真实数据管道（Week 3-4）

**目标**：Java 扫描真实 AE2 网络，编码为二进制，Rust 解码并构建模型。

```
Java 侧:
  □ NetworkSnapshotEncoder - AE2 DriveMachine → CellSnapshot 编码
  □ 每 200 tick 推送全量快照
  □ Integration: 在开发环境验证数据完整性

Rust 侧:
  □ NetworkModel 数据结构
  □ 全量快照解码 + 模型构建
  □ 简单分析（cell 数、item 数、总 bytes）
  □ 分析结果编码返回 Java
```

### Phase 3: 分析功能迁移（Week 5-6）

**目标**：把 Java 的分析逻辑逐步迁移到 Rust，功能对等。

```
Rust 侧:
  □ FragmentationAnalyzer  - 碎片分析（等价 Ae2StorageAnalyzer）
  □ HealthChecker          - 健康检查（无限 cell、外部泄漏）
  □ MovePlanner            - 排序规划（等价 LiveZoneAllocationPlanner）
  □ JSON 序列化            - 预生成前端 JSON（等价 Gson 输出）

Java 侧:
  □ ResultDecoder          - 解码 Rust 返回的分析结果
  □ RustBackedTopology     - 实现 RuntimeTopology 接口
  □ 并行验证：Java 和 Rust 同时跑，对比结果
```

### Phase 4: 增量更新 + 降级（Week 7-8）

**目标**：从全量推送升级到增量推送，完善崩溃恢复。

```
Java 侧:
  □ AE2 事件 hook → 收集变更 delta
  □ 增量推送 + 定期全量对账
  □ DegradedModeHandler 降级逻辑
  □ GUI 健康状态指示

Rust 侧:
  □ 增量 delta 合并到模型
  □ 定期全量对账（检测漂移）
  □ panic hook → health=1 写入
  □ 优雅退出处理
```

### Phase 5: DAV 存储引擎接入（Week 9+）

**目标**：DAV 的 `storedItems` 从 `LinkedHashMap<AEItemKey, Long>` 迁移到 Rust 管理的存储池。

```
Rust 侧:
  □ DAV 存储池数据结构（BTreeMap / LSM / 自定义索引）
  □ NBT 容量计算（raw bytes * multiplier）
  □ 垃圾压缩归档
  □ 持久化到独立文件（非 NBT）

Java 侧:
  □ NewDavStorage 改为通过共享内存读写 Rust 存储池
  □ IStorageProvider 接口不变（getAvailableStacks/extract/insert）
  □ NBT save/load 只保存元数据（absorbedBytes 等），实际数据在 Rust 侧
```

---

## 10. 风险与缓解

| 风险 | 概率 | 影响 | 缓解 |
|------|------|------|------|
| AE2 API 变动导致二进制协议不兼容 | 中 | 高 | 协议版本号机制；Rust 侧 feature flag 支持多版本 |
| Rust 进程在特定平台无法启动 | 低 | 高 | 启动失败自动降级到纯 Java；错误日志详细记录 |
| 共享内存在不同 JVM 实现下行为不一致 | 低 | 中 | 使用 FFM 标准 API（Java 21+ finalized）；CI 多 JDK 测试 |
| 增量模型漂移 | 中 | 中 | 每 N 次增量推送强制全量对账；漂移检测（item count mismatch） |
| 跨平台 native 二进制分发 | 中 | 低 | CI 矩阵构建 win/linux/mac；Gradle task 自动下载对应平台二进制 |
| Rust 内存泄漏导致 OOM | 低 | 高 | Rust 进程内存限制；定期重启（如每 24h）；监控 RSS |

---

## 11. 配置项

```java
// Config.java 新增
public static final ConfigValue<Boolean> RUST_ENGINE_ENABLED;
public static final ConfigValue<Integer> RUST_SCAN_INTERVAL_TICKS;  // 默认 200
public static final ConfigValue<String> RUST_ENGINE_MODE;           // "full" | "incremental"
public static final ConfigValue<Integer> RUST_MAX_MEMORY_MB;        // 默认 256
public static final ConfigValue<Boolean> RUST_AUTO_RESTART;         // 默认 true
public static final ConfigValue<Integer> RUST_HEARTBEAT_TIMEOUT_SEC; // 默认 5
```

---

## 附录 A: 与头脑风暴的关键差异

| 头脑风暴 | 本设计 | 原因 |
|----------|--------|------|
| 单向数据流 (Rust→Java) | 双向 (Java⇄Rust) | Rust 不能调 AE2 API，Java 必须推送网格数据 |
| JSON 为内部传输格式 | 二进制协议 (repr(C)) | 零拷贝、无序列化开销，JSON 只用于前端输出 |
| Rust "后台持续生成" | 事件驱动，Java 推送触发 | 避免无用计算，有变更才重新分析 |
| 结果用指针传递 | 结果用相对偏移+长度 | 不同进程虚拟地址不同，指针无意义 |
| 未提及增量更新 | 全量+增量双模式 | 大型网络必须增量，否则推送本身就是瓶颈 |

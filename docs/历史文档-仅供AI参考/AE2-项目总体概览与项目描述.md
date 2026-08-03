# AE2 项目总体概览与项目描述

## 1. 项目定位

Applied Energistics 2（AE2）是一个围绕 **物质存储、网络化物流、自动化制造、能量与空间技术** 展开的 Minecraft 模组。

从代码结构上看，它不是一个“单体功能模组”，而是一个以 **ME 网络内核** 为中心、向外扩展出存储、终端、自动化、合成、空间存储、P2P、渲染与交互系统的大型工程。

对于当前 1.21.1 / NeoForge 版本，这个项目的核心特点是：

- 以 **网格/节点/连接（Grid / Node / Connection）** 为基础建模网络
- 以 **服务化（Service-based）** 方式承载内核能力
- 通过 **BlockEntity / Part** 把世界中的方块与部件接入网络
- 通过统一 tick 调度驱动网络更新、通道计算、能量与存储行为

---

## 2. 项目一句话描述

AE2 是一个以 **ME 网络** 为核心的 Minecraft 模组，提供网络化存储、物品与流体管理、自动化合成、设备互联和空间技术；其实现上采用 **图结构内核 + 服务层 + 世界对象适配层** 的架构。

---

## 3. 当前版本技术背景

- **Minecraft**: 1.21.1
- **Mod Loader / API**: NeoForge
- **Java**: 21
- **构建工具**: Gradle

入口相关：

- `appeng.core.AppEngBase`
- `appeng.core.AppEngClient`
- `appeng.core.AppEngServer`

---

## 4. 整体代码结构

从顶层 package 看，项目大致可分为以下层次：

### 4.1 API 层
- `appeng.api`

职责：
- 对外暴露 AE2 的公共接口
- 定义网络、存储、部件、配置、集成等抽象
- 为 addon 或外部集成提供稳定边界

### 4.2 核心启动与注册层
- `appeng.core`
- `appeng.init`

职责：
- 模组入口
- 方块、物品、方块实体、菜单、网络包、粒子、创造标签等注册
- 配置加载
- client / server 初始化分流

### 4.3 内核层
- `appeng.me`

职责：
- ME 网络图结构
- 节点、连接、网格合并与拆分
- 通道/pathing
- 存储服务
- 能量服务
- crafting / p2p / spatial 等网络服务

这是当前 AE2 最重要的核心包。

### 4.4 世界对象接入层
- `appeng.blockentity`
- `appeng.parts`

职责：
- 让 BlockEntity 和 Part 拥有网格节点
- 管理 in-world 生命周期
- 维护世界中的连接关系
- 将内核状态反映到方块、部件、同步与外观

### 4.5 业务系统层
- `appeng.crafting`
- `appeng.spatial`
- `appeng.recipes`
- `appeng.items`
- `appeng.menu`

职责：
- 自动合成
- 空间存储
- 机器逻辑
- UI 与交互
- 物品与配方行为

### 4.6 客户端表现层
- `appeng.client`
- `appeng.core.network`
- `appeng.init.client`

职责：
- 界面
- 渲染
- 粒子与特效
- 客户端命令
- 客户端同步包处理

---

## 5. 内核架构概览

### 5.1 Grid：网络实例
关键类：`appeng.me.Grid`

职责：
- 表示一个独立的 ME 网络
- 保存网络中的全部节点
- 持有所有网格服务实例
- 在 tick 阶段驱动服务执行

可以理解为：

- **Grid = 一张运行中的网络图 + 一组附着其上的服务**

### 5.2 GridNode：网络节点
关键类：
- `appeng.me.GridNode`
- `appeng.me.InWorldGridNode`
- `appeng.me.ManagedGridNode`

职责：
- 表示图中的单个节点
- 保存连接、颜色、功耗、通道等状态
- 绑定一个 owner（通常是方块实体或 part）
- 在加入、销毁、改色、改变 exposed sides 时触发网络变化

其中：
- `GridNode` 是基础节点
- `InWorldGridNode` 会自动寻找世界相邻连接
- `ManagedGridNode` 是给宿主对象使用的生命周期包装器

### 5.3 GridConnection：节点连接
关键类：`appeng.me.GridConnection`

职责：
- 表示两个节点之间的一条边
- 建边时自动合并网络
- 断边时自动触发重新验证与 repath

### 5.4 Service：网络能力实现
关键注册文件：`appeng.init.internal.InitGridServices`

当前核心服务包括：
- `TickManagerService`
- `PathingService`
- `EnergyService`
- `StorageService`
- `P2PService`
- `SpatialPylonService`
- `CraftingService`
- `StatisticsService`

说明：
- Grid 本身不承担所有业务逻辑
- 业务能力分散在 service 中
- 这是一种比较清晰的服务化内核结构

---

## 6. 世界接入方式

### 6.1 普通网络方块
关键类：`appeng.blockentity.grid.AENetworkedBlockEntity`

模式：
- 持有一个 `IManagedGridNode`
- 读档时恢复节点数据
- `onReady()` 时正式创建节点并接入网络
- 区块卸载 / 方块移除时销毁节点

这是方块实体接入 AE2 网络的标准路径。

### 6.2 CableBus / Part 系统
关键类：
- `appeng.blockentity.networking.CableBusBlockEntity`
- `appeng.parts.CableBusContainer`

特点：
- 一个方块内可能存在多个部件
- 中心 cable 和六侧 part 都可能有自己的节点
- 节点之间还会有“内部连接”
- 同时还要处理 facade、形状、碰撞、渲染和同步

这是 AE2 最复杂、也最具有代表性的系统之一。

---

## 7. Tick 驱动模型

关键类：`appeng.hooks.ticking.TickHandler`

职责：
- 管理所有 Grid 的统一 tick 调度
- 管理 BlockEntity 的延迟 ready
- 管理跨 tick / 跨 level 的回调队列
- 在 server tick 和 level tick 的不同阶段驱动网络服务

大致时序：
- `ServerTick.Pre` → Grid `onServerStartTick`
- `LevelTick.Pre` → Grid `onLevelStartTick`
- `LevelTick.Post` → Grid `onLevelEndTick`
- `ServerTick.Post` → Grid `onServerEndTick`

这说明 AE2 内核是 **统一调度、分阶段执行** 的，而不是对象各自随意更新。

---

## 8. 三个最重要的核心服务

### 8.1 PathingService
关键类：`appeng.me.service.PathingService`

职责：
- 控制器状态判断
- 通道分配与重算
- ad-hoc 网络处理
- booting 状态维护

特点：
- 改图后通常只标记 `repath()`
- 真正计算延后到 tick 阶段执行
- 是网络拓扑与通道逻辑的核心

### 8.2 EnergyService
关键类：`appeng.me.service.EnergyService`

职责：
- 维护供电、耗电、储电
- 处理 energy injection / extraction
- 计算网络是否有电
- 维护 energy watcher 与 overlay energy grid

特点：
- 不只是简单的总电量统计
- 存在 overlay grid 概念，说明能量层和基础拓扑层并不完全等价

### 8.3 StorageService
关键类：`appeng.me.service.StorageService`

职责：
- 聚合网络中的存储 provider
- 暴露统一 `MEStorage`
- 维护库存缓存与 watcher
- 在 tick 末更新缓存和变更通知

特点：
- 现有 AE2 更偏向“聚合式存储暴露”
- 但缺乏更高层的全局存储治理逻辑

---

## 9. 当前项目的架构特点

### 优点
- 分层较清晰
- 内核抽象明确
- 节点/连接/服务模型比较稳定
- 客户端与服务端职责分离明确
- 适合大型模组长期演进

### 问题或限制
- 存储调度偏静态，过度依赖优先级、分区和人工规划
- CableBus / Part 体系复杂，维护成本高
- 某些能力是历史演进结果，整体一致性不总是理想
- “全局管理层”不足，尤其在存储治理与自动路由方面

---

## 10. 对重写内核的启发

如果团队计划重写内核，当前项目可粗分为四层：

1. **拓扑层**
   - Grid / Node / Connection / split / merge / visit
2. **服务层**
   - pathing / energy / storage / crafting / p2p
3. **宿主适配层**
   - ManagedGridNode / BlockEntity / Part / CableBus
4. **表现与同步层**
   - packet / menu / UI / render / block updates

重写时通常优先关注：
- 拓扑层
- 服务层

而适配层与表现层更适合先保持兼容边界。

---

## 11. 项目描述（适合作为团队内部介绍）

AE2 是一个大型、成熟的 Minecraft 技术模组，其核心是一个围绕 **网络化存储与自动化** 构建的 ME 网络系统。当前实现采用 **图结构内核 + 服务化逻辑 + 世界对象适配** 的架构：世界中的方块与部件通过节点接入网格，网格通过多个服务处理通道、能量、存储、合成与空间系统，再通过同步与客户端表现层将结果反馈给玩家。

从工程角度看，AE2 既是一个功能模组，也是一个具备明确内核边界的大型软件系统。其当前主要短板不在基础网络能力，而在更高层的全局治理能力，尤其是存储策略、自动分类与全局调度方面。

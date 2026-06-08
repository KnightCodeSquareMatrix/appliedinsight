# 新版 DAV 设计方案草案

> 日期：2026-06-01  
> 状态：产品与架构方向草案，尚未实现  
> 核心结论：DAV 不再只是 profile/zone 管理入口，而是 AE2 的最终智能存储后端与游戏内访问面板。

---

## 1. 核心定位

DAV（Digital Asset Vault）的长期目标是：

> **用一个智能存储后端替代传统 AE2 Drive 墙。**

它不是一个“超大潜影盒”，也不是一个“装着很多 Cell 的方块”。真正的资产数据应存储在世界级后端中，例如 `World SavedData`，未来也可以替换为数据库或独立持久化后端。

DAV 方块在世界中扮演的是：

- AE2 Storage Provider；
- 后端 vault 的访问点；
- 游戏内管理面板；
- 健康检查与治理入口；
- Cell 容量吸收入口。

一句话：

> **DAV block 是访问面板，不是资产本体。资产库存在 world backend 里。**

---

## 2. 目标玩家

本设计不主要服务已经会精细维护 AE2 的工程型玩家。

真正目标用户是：

- 不整理 AE2；
- 刷怪场直接接 ME；
- 装了 Apotheosis / 神话装备 / 随机词条类模组；
- 会把大量 NBT-heavy 装备、战利品和垃圾直接塞进系统；
- 不想理解 profile、zone、route rule；
- 只想继续探索、下矿、打怪、冒险。

因此 DAV 的核心原则是：

> **傻瓜、耐草、难用坏。**

它要保护玩家的 ME 系统不被 NBT 垃圾、类型爆炸、容量碎片和 Drive 墙维护成本拖垮。

---

## 3. 与旧思路的区别

### 3.1 旧思路

```text
玩家配置 profile
  ↓
绑定 profile
  ↓
按 zone / route rule 整理 Cell
```

这适合高级 UI、调试命令或管理员工具，但不适合普通游戏内体验。

### 3.2 新思路

```text
玩家放置 DAV
  ↓
登录 / 连接后端 vault
  ↓
插入 Cell 扩容
  ↓
DAV 自动统一管理 bytes 与 slots
  ↓
DAV 直接暴露给 AE2 网络
  ↓
系统自动治理 NBT、容量、类型槽和健康状态
```

普通玩家不需要感知：

- profile；
- zone；
- route rule；
- cell assignment；
- 物品具体放在哪张硬盘。

---

## 4. DAV 的存储模型

### 4.0 内置基础容量（已实现）

每个 DAV 放置后自带固定基础容量，相当于**两张空 1k 物品存储元件**：

```text
BUILTIN_BYTES         = 2048   // 2 × 1024
BUILTIN_TYPE_CAPACITY = 126    // 2 × 63 types
```

实现上通过 `getAbsorbedBytes()` / `getAbsorbedTypeCapacity()` 叠加，NBT 只持久化吸收获得的额外容量；旧存档加载后也会自动获得基础值。界面「已吸收 Cell」计数仅统计真实吸收的 Cell，不含内置部分。

### 4.1 Cell 被吸收为容量

玩家向 DAV 放入 AE2 Storage Cell 后，Cell 不再作为独立物理容器存在，而是被转化为 DAV 后端 vault 的容量。

吸收后增加：

```text
+ bytes 容量
+ slots / types 容量
```

Cell 的定位变为：

> **容量升级材料。**

普通玩家流程中不强调“取回 Cell”。取回 Cell 不是 v1 核心功能，可作为未来高级维护功能考虑。

---

### 4.2 全局大混池

DAV 使用一个全局容量池，而不是按物品、流体、气体、化学品、魔源等资源类型分池。

```text
DAV Global Pool
- totalBytes
- usedBytes
- totalSlots
- usedSlots
```

只要 AE2 或附属 mod 已经提供了能工作的 Cell，DAV 就尽量使用其容量信息，不为每种资源格式单独写支持。

也就是说，DAV 不主动关心：

- item；
- fluid；
- gas；
- chemical；
- mana；
- 其他 AE2 addon key。

它关心的是：

```text
这个 Cell 能提供多少 bytes？
这个 Cell 能提供多少 slots？
这个 key 如何由 AE2 表达和序列化？
```

---

### 4.3 不重新引入 Cell 边界

DAV 不应该内部维护：

```text
Cell A -> items
Cell B -> items
Cell C -> items
```

而应该维护：

```text
StoredKey -> amount
CapacityLedger -> bytes / slots
Indexes -> query / health / governance
```

这样才能真正解决传统 AE2 Cell 的局部浪费：

- 某张盘 bytes 空但 slots 满；
- 某张盘 slots 空但 bytes 满；
- 多张盘之间余量无法共享；
- 玩家需要手动倒盘和整理。

---

## 5. NBT 成本模型

NBT / Component 不作为第三种容量资源。

DAV 只保留两个核心约束：

```text
1. bytes
2. slots / types
```

NBT 成本按序列化长度折算进 bytes。

核心公式：

```text
nbtByteCost = serializedNbtBytes × nbtByteMultiplier
```

总字节成本可理解为：

```text
totalByteCost = normalStorageByteCost + nbtByteCost
```

要求：

- 不按 NBT 条数计算；
- 不做复杂评分；
- 不做指数、对数、阶梯或曲线；
- 不添加第三个 payload bar；
- 使用简单正比例函数；
- multiplier 可配置或用于平衡。

玩家侧表达：

> **复杂数据越大的物品，会按比例消耗更多 DAV 字节容量。**

---

## 6. NBT 垃圾治理与归档

DAV 必须避免 RS 式统一大仓库的 NBT 爆炸问题。

### 6.1 NBT 诊断

DAV 应能识别和解释：

- NBT-heavy key 数量；
- NBT 对 bytes 的额外占用；
- 不可堆叠装备数量；
- Apotheosis / 神话装备类随机词条污染；
- 短时间大量新增不同 NBT key；
- 可能来自刷怪场或战利品输入的 NBT 洪水。

但这些只是诊断指标，不是新的容量资源。

---

### 6.2 归档机制

对于极端 NBT 垃圾，DAV 可以提供归档动作：

```text
大量不同 NBT 的垃圾物品
  ↓
预览归档收益
  ↓
从活跃 DAV 存储中移除
  ↓
压缩为一个 archive item
  ↓
输出到旁边容器
```

归档包可以保存类似 dumpfile 的 manifest / payload。

玩家需要时可以手动解压：

```text
archive item -> DAV -> 解压 -> 恢复物品
```

归档不是删除，也不是免费活跃存储扩容。

它是：

> **把污染主存储体验的 NBT 垃圾转入冷存储。**

---

### 6.3 归档安全要求

归档必须：

- 明确预览；
- 显示将归档多少物品；
- 显示释放多少 slots；
- 显示释放多少 bytes；
- 输出到明确的旁边容器；
- 保留 manifest；
- 可恢复；
- 不静默删除；
- 不自动黑箱隐藏玩家资产。

对于超大 archive，不应把全部 payload 直接塞进 item NBT。可以使用 world data / 文件 / 数据库保存大 payload，archive item 只保存 archiveId、summary 和 checksum。

---

## 7. 非空 Cell 导入

玩家不应被要求手动清空 Cell。

当玩家把非空 Cell 放入 DAV 时，DAV 应自动：

```text
读取 Cell 内容
  ↓
读取 Cell 容量
  ↓
按 “当前 DAV 容量 + 该 Cell 容量” 模拟是否可容纳
  ↓
复用现有 merge / move 执行线导入内容
  ↓
导入成功后吸收 Cell 容量
  ↓
Cell 消失 / 转化为容量账本记录
```

关键点：

- 不要重写一套大迁移系统；
- 复用现有扫描、规划、extract → insert → rollback、日志和 report 基础；
- DAV 作为新的 insert target；
- 导入失败时，Cell 不应消失；
- DAV 已导入内容应回滚；
- 成功后才登记 absorbed cell ledger。

---

## 8. AE2 网络暴露

DAV 必须直接暴露为 AE2 Storage Provider。

不暴露给 ME 的 DAV 没意义，因为玩家的核心交互仍然是：

- ME Terminal；
- AE2 自动化；
- pattern provider；
- import/export bus；
- autocrafting。

DAV 不应是旁路仓库。

目标行为：

```text
ME 网络连接 DAV
  ↓
ME Terminal 直接看到 DAV 中的资产
  ↓
自动化可以 insert / extract
  ↓
合成系统可以使用 DAV 中的资源
```

---

## 9. ME Terminal 性能优化方向

DAV 作为巨大统一后端后，ME Terminal 打开性能会成为关键瓶颈。

长期计划可以使用 Mixin 优化 ME Terminal：

- 服务端分页；
- 服务端搜索；
- 延迟加载；
- 增量同步；
- NBT-heavy key 折叠；
- 大量同类 NBT 变体折叠显示；
- 默认不一次性同步所有 key；
- 终端 UI 虚拟列表；
- DAV-aware 查询接口。

目标：

> 即使 DAV 后端存储大量 key，也不因打开终端而卡死客户端或服务器。

---

## 10. 后端存储与持久化

### 10.1 数据不在 DAV 方块本体

DAV 方块不保存完整资产数据。

方块只保存轻量信息，例如：

```text
vaultId
connection status
UI cache summary
```

真实数据保存在 world-level backend：

```text
World SavedData
```

未来可替换为：

- 数据库；
- 独立文件后端；
- 索引化持久层。

---

### 10.2 后端接口

应定义后端接口，例如：

```text
DavStorageBackend
```

职责：

- insert；
- extract；
- simulate insert；
- simulate extract；
- query；
- capacity snapshot；
- health analysis；
- transaction / journal；
- persistence。

当前实现可以是：

```text
WorldSavedDataDavStorageBackend
```

未来可替换为：

```text
DatabaseDavStorageBackend
```

---

### 10.3 数据结构方向

DAV 不应模仿一张张 Cell 的内部结构，而应使用适合统一资产库的数据结构：

```text
VaultState
- vaultId
- capacityLedger
- storedKeyRecords
- indexes
- absorbedCellLedger
- archiveRecords
- transactionJournal
- schemaVersion
```

`StoredKeyRecord` 可包含：

```text
keyIdentity
serializedKeyBytes / hash
amount
byteCost
slotCost
lastModified
flags
```

---

## 11. 多 DAV、多网络、末影资产库

DAV 方块是后端 vault 的 access point。

多个 DAV 方块可以指向同一个 vault。

多个 DAV access point 可以接入多个 AE2 网络，共享同一后端资产库。

效果类似：

> **把 AE Drive 变成末影箱式共享资产库。**

但注意：

- 共享的是同一份后端数据；
- 不复制物品；
- 不复制容量；
- 不复制 Cell；
- 多入口只是访问同一 vault。

实现重点：

- insert / extract 事务安全；
- 并发访问正确；
- 多网络变更通知；
- 防止重复提取；
- 后端账本原子更新。

---

## 12. 账户与 vault 访问

DAV 不使用颜色频道、频率、链接卡作为主要 vault 访问方式。

DAV 使用账户/密码模型。

### 12.1 账户独立于 Minecraft 身份

DAV Account 与 Minecraft 玩家身份完全无关。

不绑定：

- Minecraft UUID；
- 玩家名；
- online/offline mode；
- 当前操作者；
- 自动 owner。

玩家使用 DAV 后端账户登录：

```text
username
password
```

只要账户对 vault 有权限，就能访问。

---

### 12.2 领域模型

推荐领域概念：

```text
Account
Credential
Vault
Membership
Role
Session
Permission
AuditLog
```

第一版可以简化权限模型，但后端概念应清晰。

示例：

```text
Account
- accountId
- username
- passwordHash
- salt
- createdAt

Vault
- vaultId
- name
- createdAt
- settings

Membership
- vaultId
- accountId
- role

AuditLog
- who
- action
- vaultId
- timestamp
- details
```

---

### 12.3 团队协作

多个玩家可以使用同一个 DAV account，或通过多个 account 加入同一个 vault。

这适合团队服务器：

```text
团队主资产库
团队成员账户
共同访问
共同扩容
共同治理
```

---

## 13. 供应型 / 单物品无限 Cell

类似 ATM 中的单物品无限供应 Cell，暂列为待实现兼容项。

当前判断：

- 即使被放进 DAV，主要问题也是 UI / 统计污染；
- 不会直接破坏 DAV 的核心容量池设计；
- 不作为 v1 设计阻塞项。

未来可实现：

- 供应型 Cell 识别；
- 特殊数量显示；
- 从最大数量统计中排除；
- 从健康评分中排除；
- 作为特殊 supply source 展示；
- 防止 `Long.MAX_VALUE` 之类数值污染 UI。

Backlog 名称：

```text
special supplier cell detection / UI decontamination
```

---

## 14. 与 RS 的区别

DAV 可以学习 RS 的统一存储体验，但不能复刻 RS 的失败模式。

DAV 必须坚持：

- bytes + slots 双账本；
- NBT 长度折算 bytes；
- NBT 垃圾可诊断；
- 极端 NBT 可归档；
- 存储后端可索引；
- insert / extract 事务安全；
- 后端数据可恢复；
- 终端性能优化；
- 健康检查与解释；
- 玩家可预览风险。

一句话：

> **像 RS 一样简单，像 AE2 一样有约束，比两者都更可解释。**

---

## 15. 游戏内 UI 原则

普通游戏内 UI 不展示：

- profile；
- zone；
- route rule；
- item filter；
- priority；
- JSON；
- 复杂配置表。

普通游戏内 UI 应展示：

```text
状态
容量
风险
建议
按钮
```

示例：

```text
DAV 智能资产库

Vault: 团队主资产库
状态: 警告

Bytes: 1.2M / 2.0M
Slots: 438 / 630

问题:
- 检测到大量 NBT-heavy 装备
- NBT 数据额外消耗约 310k bytes
- 184 个 slots 被唯一装备占用

建议:
[归档 NBT 垃圾]
[插入 Cell 扩容]
[查看详情]
```

---

## 16. 外部 UI / 高级功能

复杂设计应交给外部 UI 或高级界面。

外部 UI 可提供：

- vault 详情；
- 查询；
- 历史趋势；
- NBT 污染分析；
- 归档管理；
- account / membership 管理；
- audit log；
- profile / rule 高级设计；
- 搬家订单；
- backend 数据库查询；
- 终端优化配置。

普通游戏内只保留傻瓜入口。

---

## 17. 搬家模式

搬家模式不是普通整理的一部分，而是未来独立高光功能。

设计方向：

```text
选择货物
  ↓
估算需要容量 / cell
  ↓
生成搬家订单
  ↓
打包 / 归档 / 导出
  ↓
在目标 vault 导入
```

由于新版 DAV 是 backend vault，不是超大潜影盒，因此搬家更适合设计成：

```text
源 vault -> migration archive/package -> 目标 vault
```

而不是简单拆下 DAV 方块带走全部资产。

---

## 18. 初步实现阶段建议

### 阶段 1：DAV 后端账本原型

- vaultId；
- World SavedData；
- bytes / slots ledger；
- absorbed cell ledger；
- 简单 account / vault 创建；
- DAV 方块连接 vault。

### 阶段 2：Cell 吸收

- 读取 Cell capacity；
- 吸收空 Cell；
- 记录容量来源；
- UI 显示 bytes / slots。

### 阶段 3：DAV Storage Provider

- 暴露给 AE2 网络；
- insert / extract；
- simulate；
- stored key map；
- basic persistence。

### 阶段 4：非空 Cell 导入

- 复用 merge / move 线；
- Cell -> DAV 导入；
- 事务与回滚；
- 导入报告。

### 阶段 5：NBT 成本

- key 序列化长度计算；
- `nbtByteMultiplier`；
- bytes 成本折算；
- NBT-heavy 诊断。

### 阶段 6：多接入点共享

- 多 DAV 指向同一 vault；
- 多 AE2 网络访问同一 vault；
- change notification；
- 并发安全。

### 阶段 7：NBT 归档

- 归档候选检测；
- 预览；
- archive item；
- adjacent container output；
- 解压导入。

### 阶段 8：ME Terminal 优化

- Mixin；
- 服务端搜索；
- 分页；
- NBT 变体折叠；
- 增量同步。

---

## 19. 已定稿决策摘要

1. DAV 是最终存储方块，目标是替代 Drive 墙。
2. DAV block 是访问/管理面板，不是携带全部数据的超大容器。
3. 真实资产数据存在 world backend，优先 World SavedData，未来可数据库化。
4. Cell 被吸收为容量升级，增加 bytes + slots。
5. 不强调取回 Cell，取回不是 v1 核心功能。
6. DAV 使用全局大混池，不按资源类型分池。
7. DAV 不为 item/fluid/gas 等单独支持格式，跟随 AE2 Cell / AEKey 能力。
8. NBT 成本按序列化长度 × 固定倍率折算进 bytes。
9. 不引入第三个 payload 资源。
10. 极端 NBT 垃圾可归档成可恢复 archive item。
11. 非空 Cell 导入复用现有 merge / move 线。
12. DAV 必须暴露为 AE2 Storage Provider。
13. 多 DAV access point 可共享同一后端 vault。
14. 多 AE2 网络可接入同一 vault，形成末影资产库式体验。
15. DAV vault 访问使用账户/密码模型。
16. DAV Account 与 Minecraft 玩家 UUID / 名称完全无关。
17. 供应型 / 单物品无限 Cell 暂列待实现兼容项，主要处理 UI/统计污染。
18. 普通玩家不感知 profile / zone / route rule。
19. 目标用户是不会维护 AE2 的大冒险家，设计必须傻瓜、耐草、难用坏。
20. 长期需要 Mixin 优化 ME Terminal 性能。

---

## 20. 未来备忘

- Account 权限等级尚未最终细化，可从简单共享账号开始，也可增加 Owner/Member。
- 密码存储应使用 salted hash，避免明文。
- 数据库后端可在 World SavedData 原型稳定后再引入。
- 多网络访问同一 vault 需要重点验证事务、并发和通知。
- 归档包大 payload 不宜直接塞入 item NBT，应使用 archiveId + backend payload。
- 特殊供应 Cell 的识别不阻塞 v1，但未来要避免 UI 数值污染。
- 终端优化可能需要 DAV-aware 查询接口，而不仅是被动接 AE2 原接口。
- 外部 UI 可以承载复杂 profile/rule/vault 管理，游戏内保持傻瓜。

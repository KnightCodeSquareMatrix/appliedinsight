# 重构计划 Part 1

## 1. 本阶段目标

本阶段只解决三个问题：

1. 明确 `application` 的层级定位
2. 引入并启用 `infrastructure/**` 作为基础设施归宿
3. 明确配置真相来源与 runtime 投影关系

本阶段**不处理**：

- `Ae2ZoneMoveExecutor` 细拆
- topology 输出模型最终命名
- logger 收口
- merge 与 plan/move 的执行统一实现

一句话：

> 先把“谁是 glue，谁是配置源，谁是 runtime 投影”定死。

---

## 2. 已确认的架构结论

### 2.1 `application` 的定位

`application` 层被正式定义为：

> **glue service layer / use-case orchestration layer**

它负责：

- 编排完整用例
- 做前置校验
- 读取当前最新配置快照
- 调用 rule / ae2 / infrastructure / logging
- 返回统一的 use-case result

它不负责：

- 持久化实现本体
- JSON codec 实现
- 规则定义
- 底层 executor 细节

### 2.2 `infrastructure/**` 的定位

`infrastructure/**` 是本项目的必要抽象，用于承载：

- JSON 读取与写入
- profile 配置源
- binding 存储
- CLI / 导出工具
- 其他明确不属于 rule / application / ae2 的实现职责

### 2.3 JSON 是最终可靠来源

以下内容的最终可靠来源都是 JSON：

- zone 属性
- filter
- route
- profile

前端可随时修改 JSON，后端负责：

- 读取
- 解析
- 解释
- 执行

后端不是更高权威的配置真相来源。

### 2.4 runtime zone 只是运行时投影

`RuntimeZone` 不是配置真相。

它是：

> **声明配置 + 当前 `IGrid` 拓扑 的运行时投影**

因此：

- declared properties 来自 JSON
- runtime membership 来自 topology 扫描
- runtime placement / merge 行为属于执行态语义

---

## 3. 本阶段要迁出的类

以下类在 Part 1 中应优先迁移到 `infrastructure/**`：

### 3.1 从 `application/**` 迁出

- `application/NetworkProfileBindingStore`

建议目标：

- `infrastructure/binding/NetworkProfileBindingStore`

### 3.2 从 `rule/route/**` 迁出

- `rule/route/RoutingProfileRepository`
- `rule/route/RoutingProfileJsonCodec`
- `rule/route/RoutingProfileJsonCli`

建议目标：

- `infrastructure/profile/RoutingProfileRepository`
- `infrastructure/profile/RoutingProfileJsonCodec`
- `infrastructure/profile/RoutingProfileJsonCli`

### 3.3 从 `rule/filter/**` 迁出

- `rule/filter/FilterUiMetadataJsonCodec`
- `rule/filter/FilterUiMetadataCli`

建议目标：

- `infrastructure/filter/FilterUiMetadataJsonCodec`
- `infrastructure/filter/FilterUiMetadataCli`

### 3.4 暂不迁移

以下类本阶段先保留原位：

- `rule/filter/FilterUiMetadata`

理由：

- 它仍可能被视为规则对前端暴露的语义元数据模型
- 当前优先迁出的是 codec / cli 等实现类，而不是语义模型本体

---

## 4. 本阶段要保留的稳定核心

本阶段不重写以下核心对象，只允许修正引用与迁包：

- `rule/route/RoutingEngine`
- `rule/route/RoutingProfile`
- `rule/filter/ItemFilterMatcher`
- `plan/ZoneAllocationPlan`
- `plan/ItemZoneAssignment`
- `ae2/zone/RuntimeZone`
- `ae2/zone/RuntimeCell`
- `ae2/zone/RuntimeTopology`
- `ae2/sort/MergeMovePlanner`
- `ae2/sort/SorterMoveOperation`

---

## 5. 配置读取规则

本阶段正式引入以下执行规则：

### 5.1 每个 use-case 开始时读取一次最新配置

每个 application glue service 在用例开始时，应读取一次当前最新 JSON 配置快照。

目标：

- 保证本次执行内部一致
- 不在一次执行中途热切配置
- 下一次执行自然获得前端最新修改结果

### 5.2 rule 保持纯，但配置载入实时

`rule/**` 继续保持纯语义模型。

但配置载入必须支持：

- 按用例读取最新 JSON
- 不把旧配置偷偷当作长期内存真相

---

## 6. Part 1 执行步骤

## Step 1：创建 `infrastructure/**` 包结构

建议至少创建：

- `infrastructure/binding/`
- `infrastructure/profile/`
- `infrastructure/filter/`

目标：

- 给迁移类提供明确归宿
- 不再让 `rule/**` 和 `application/**` 继续承担实现类寄存地

---

## Step 2：迁移 binding store

迁移：

- `NetworkProfileBindingStore`

动作：

1. 迁移到 `infrastructure/binding`
2. 修正 `application` 中的引用
3. 保持行为不变

验收：

- `application/**` 中不再放持久化 store 实现类

---

## Step 3：迁移 profile 相关基础设施

迁移：

- `RoutingProfileRepository`
- `RoutingProfileJsonCodec`
- `RoutingProfileJsonCli`

动作：

1. 迁移到 `infrastructure/profile`
2. 修正所有引用
3. 确保 `rule/route/**` 中只保留纯 route 语义

验收：

- `rule/route/**` 不再包含文件系统 / JSON / CLI 实现职责

---

## Step 4：迁移 filter metadata codec / cli

迁移：

- `FilterUiMetadataJsonCodec`
- `FilterUiMetadataCli`

动作：

1. 迁移到 `infrastructure/filter`
2. 修正所有引用
3. 暂时保留 `FilterUiMetadata` 本体不动

验收：

- `rule/filter/**` 中不再包含 codec / cli 实现类

---

## Step 5：修正 `application` 对配置源的依赖语义

目标：

- 让 application 明确依赖基础设施提供的“当前配置读取能力”
- 不再假装这些实现是 application 本身的一部分

动作：

1. 修正 import 与 new 的位置语义
2. 确保 `SorterPlanService` / `SorterProfileBindingService` 等是“调用配置源”，不是“拥有配置源实现”

验收：

- `application` 与 `infrastructure` 的角色边界清楚

---

## Step 6：补充文档说明

至少更新：

- `docs/边界收敛重构计划.md`
- 如有必要，更新 `docs/整体逻辑.md`
- 如有必要，更新 `docs/类职责总览.md`

目标：

- 文档与新边界一致
- 后续 AI / 人工不会再被旧包结构误导

---

## 7. Part 1 验收标准

Part 1 完成后，应满足：

### 7.1 包边界验收

- `application/**` 中不再包含持久化 store 实现类
- `rule/route/**` 中不再包含 repository / json codec / cli
- `rule/filter/**` 中不再包含 metadata codec / cli
- `infrastructure/**` 成为这些实现职责的明确归宿

### 7.2 认知验收

阅读项目时应能快速回答：

- `application` 是什么？
  - glue service
- `infrastructure` 是什么？
  - JSON / 文件 / 配置源 / store / cli 实现层
- JSON 是什么？
  - zone/filter/route/profile 的最终可靠来源
- `RuntimeZone` 是什么？
  - 配置在当前拓扑上的运行时投影

### 7.3 行为验收

以下行为不应改变：

- profile 列表读取
- profile 绑定与展示
- 现有 plan / planAndMove / dump / storageDump / merge 命令能力

---

## 8. 给后续 Part 2 的接口前提

Part 1 完成后，Part 2 将在以下前提上继续：

1. application 已被正式视为 glue service
2. infrastructure 已成为实现层归宿
3. JSON 配置源是最终真相
4. runtime zone 是投影，不是配置真相

在这个基础上，Part 2 再继续处理：

- topology service
- `RuntimeZone` 抽象根
- `SourceOnlyRuntimeZone` / `TargetRuntimeZone`
- `Ae2ZoneMoveExecutor` 的收缩

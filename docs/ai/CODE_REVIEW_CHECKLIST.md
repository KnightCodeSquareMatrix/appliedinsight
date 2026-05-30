# AI 协作代码审查清单 (Code Review Checklist)

> 本文档供 AI 协作者在提交代码修改前逐项自检。
> 也适用于人工 PR 审查时的参考标准。

---

## 1. 架构边界检查

### 1.1 层依赖合规

- [ ] `rule/**` 不导入 `net.minecraft.*` 或 `appeng.*`
- [ ] `ae2/**` 不反向依赖 `application/` 或 `plan/`
- [ ] `application/` 不包含持久化实现（文件 I/O、JSON 序列化等）
- [ ] `plan/**` 不承担执行职责（不调用 `Ae2ZoneMoveExecutor` 等）
- [ ] `logging/**` 不参与业务决策（不修改执行逻辑）
- [ ] `analysis/` 不污染在线命令链（不注册事件、不修改游戏状态）
- [ ] `network/` 只负责序列化/传输，不包含业务逻辑
- [ ] `block/` + `blockentity/` 只负责方块声明和生命周期管理

### 1.2 能力线隔离

- [ ] merge 路径不引入 profile/zone/route 逻辑
- [ ] zone 治理主线不表述成"只是 merge"
- [ ] storageDump 的分析逻辑不塞进 merge 或 plan 路径
- [ ] 新增代码没有把 `route`、`zone`、`cell`、`execute` 混在一起

### 1.3 抽象合理性

- [ ] 没有为了未来猜想引入大框架
- [ ] 没有强行抽象一整套 node/endpoint/provider 体系
- [ ] 没有把简单 DTO 拆成多层空壳 service
- [ ] 没有随意重写 `DriveMachineAccessor`（除非上游变化）
- [ ] 没有做"未来全能 mod"级别的扩张设计

---

## 2. 命名与语义检查

- [ ] `merge` 只用于快速合并路径
- [ ] `plan` / `planAndMove` 只用于 profile/zone 主线
- [ ] 命令名称与现有语义一致（不重复、不冲突）
- [ ] 变量/方法名清晰表达意图，不使用模糊缩写
- [ ] 日志文案与文档语义保持一致

---

## 3. 兼容性检查

### 3.1 JSON 契约

- [ ] 新增字段是可选（nullable）的，向后兼容
- [ ] 修改现有字段类型时，确认前端消费方已同步更新
- [ ] JSON Schema（`src/main/resources/schema/`）已同步更新
- [ ] `docs/日志与JSON字段契约.md` 已同步更新
- [ ] 前端 prompt（`docs/ai/frontend-vibe-coding-prompt.md`）已同步更新

### 3.2 网络包

- [ ] 新增网络包已注册 `CustomPacketPayload` 和对应 handler
- [ ] 修改现有包字段时，序列化/反序列化保持兼容
- [ ] 分块传输的会话管理（`FileChunkPayloadHandler`）正确处理了边界情况

### 3.3 命令接口

- [ ] 新增命令已注册到 `SorterCommands.java`
- [ ] 命令参数和语义与现有命令不冲突
- [ ] `docs/COMMANDS_REFERENCE.md` 已同步更新
- [ ] `docs/API_REFERENCE.md` 已同步更新（若新增 API 接口）

---

## 4. 文档同步检查

- [ ] 新增类的职责文档已创建（`docs/类职责/`）
- [ ] `docs/类职责/索引.md` 已更新
- [ ] 架构边界变化已同步到 `docs/架构/FACTS.md`
- [ ] 类职责总览结论变化已同步到 `docs/类职责总览.md`
- [ ] JSON 契约变化已同步到 `docs/日志与JSON字段契约.md`
- [ ] 命令接口变化已同步到 `docs/COMMANDS_REFERENCE.md`
- [ ] API 接口变化已同步到 `docs/API_REFERENCE.md`
- [ ] 若涉及前端，前端 prompt 已同步更新

---

## 5. 测试与验证

- [ ] `./gradlew compileJava` 编译通过
- [ ] 现有单元测试不受影响（`./gradlew test`）
- [ ] 新增逻辑有对应的单元测试
- [ ] 若修改 JSON 输出，验证了输出格式正确
- [ ] 若修改网络包，验证了客户端/服务端通信正常
- [ ] 若修改 GUI 方块，验证了按钮功能和布局正常

---

## 6. 常见反模式清单

| 反模式 | 说明 | 正确做法 |
|--------|------|---------|
| 层穿透 | rule 层直接引用 AE2 类 | 通过接口/抽象隔离 |
| 职责膨胀 | 一个类同时做解析、执行、日志 | 拆分为独立类 |
| 过度抽象 | 为单一实现提取接口 | 等到有第二个实现再提取 |
| 幻想式重构 | 基于"未来可能"重写稳定代码 | 小步收口，只改当前需要 |
| 日志污染 | 核心模型因日志需求增加字段 | logger 自己派生所需信息 |
| 命名混淆 | merge 路径中出现 zone/route 术语 | 保持命名与能力线一致 |
| 契约破坏 | 修改 JSON 字段类型未通知前端 | 保持向后兼容，同步更新文档 |

---

## 7. 审查通过标准

所有检查项满足以下条件方可提交：

1. **架构边界**：无跨层依赖违规，能力线清晰隔离
2. **兼容性**：JSON 契约向后兼容，网络包序列化正确
3. **文档**：所有受影响的文档已同步更新
4. **测试**：编译通过，现有测试不失败
5. **命名**：与项目语义一致，不引入混淆

---

> **一句话原则**：优先维护现有清晰边界；小步收口，少做幻想式重构。

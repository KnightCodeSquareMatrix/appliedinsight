
AI Collaboration Guide
==========
For AI-assisted development in this repository, see:

- `docs/AI_DEVELOPMENT_GUIDE.md`
- `docs/整体逻辑.md`
- `docs/类职责总览.md`
- `docs/下一步计划.md`
- `docs/前端对接说明.md`
- `docs/日志与JSON字段契约.md`
- `docs/examples/merge-report.sample.log`
- `docs/类职责/索引.md`

Installation information
=======

# Applied Storage Sorter 当前设计说明

## 1. 项目当前定位

Applied Storage Sorter 当前已经不再收敛为“只做内部盘碎片整理”的单一小工具，
而是明确分成两条能力线：

- **自动整理**：把同一种物品尽量归并到最小足够容纳它的内部 cell / 盘
- **自动搬运**：由玩家手动配置存储区与过滤器，决定物品应该被送往哪里

当前项目仍然只面向 **AE2 内部 drive 类存储**，不处理：

- external storage
- storage bus
- drawer
- 其他外部仓储来源

当前已支持识别的内部 drive 方块包括：

- `ae2:drive`
- `extendedae:ex_drive`

---

## 2. 两个核心组件

### 2.1 存储区
存储区表示“物品最终要去哪里”。

它是逻辑目标，不等于单个盘，可以由多个 drive / slot / cell 组成。

例如：

- `bulk`
- `machines`
- `bees`
- `components`
- `misc`

### 2.2 过滤器
过滤器表示“什么物品会命中这个条件”。

未来过滤器会支持多种匹配方式，例如：

- item id
- mod id
- tag
- 名称匹配
- 是否带 components
- 其他可扩展条件

其中“按标签过滤”只是过滤器的一种匹配能力，
不是独立的标签中间层或图形化规则系统。

---

## 3. 两条能力线的边界

### 3.1 自动整理
自动整理的职责很保守：

- 只处理同类物品跨多个内部盘的分散问题
- 尽量把它们收拢到“最小足够容纳”的盘
- 不替玩家猜测分类意图
- 不擅自决定某类物品应该属于哪个仓储区

一句话：

- **自动整理 = 内部盘级归并与压实**

### 3.2 自动搬运
自动搬运的职责更高级，但必须由玩家手动配置：

- 玩家先定义存储区
- 再定义过滤器
- 再定义应用顺序
- 命中某个过滤器后，把物品送往对应存储区

一句话：

- **自动搬运 = 玩家显式指定去向的规则路由**

---

## 4. 当前已实现基础能力

当前代码已经具备：

- `/sorter merge`
- `/sorter me dump`
- 面向 `ME Controller` 的网络定位
- AE2 / ExtendedAE drive 扫描
- drive / cell / item key 摘要统计
- 第一版 `plan -> move` 闭环
- dump JSON 输出
- 离线 dump 分析器
- 纯 Java 规则模型（zone / filter / route / profile）
- 过滤器树形表达式模型（`root` / group / rule）
- 面向 React Query Builder 的 filter UI metadata 导出
- 纯 Java 路由引擎与 explain 输出
- 纯 Java 路由配置 JSON 编解码
- dump -> routing analysis -> suggestion analysis 离线链路
- analyzer 可调用的 profile generation 接口层
- 第一版启发式 draft profile 生成器
- `/sorter merge` 详细 merge report 输出（前后对比、收益排行、单物品变更明细）

其中 `/sorter me dump` 会把网络内容导出到：

- `dumps/appliedstoragesorter/`

并包含：

- command
- target
- summary
- cells
- items

而 `/sorter merge` 现在会输出两类日志：

- `logs/appliedstoragesorter.log`
  - 记录 merge preview / execute 摘要
  - 记录详细 merge report 的文件路径
- `logs/appliedstoragesorter/merge-*.log`
  - 记录 merge 效果复盘
  - 记录 top benefit items
  - 记录单物品 before / after 与 moved_out / merged_into 明细

当前这些日志与 JSON 的字段契约说明集中整理在：

- `docs/日志与JSON字段契约.md`
- `docs/examples/merge-report.sample.log`

当前还补充了两份 JSON Schema，作为未来 UI / Web 工具 / 外部脚本的稳定契约：

- `src/main/resources/schema/routing-profile.schema.json`
- `src/main/resources/schema/sorter-network-dump.schema.json`

其中：

- `routing-profile.schema.json` 面向策略配置本身，适合做编辑器校验
- `sorter-network-dump.schema.json` 主要用于描述 dump 结构边界，方便分析器和未来前端消费
- dump 来源默认视为本 mod 自己生成、结构可靠，因此 schema 更偏向**格式契约与文档用途**，而不是把它当成“不可信外部输入”去做重验证

---

## 5. 当前离线分析能力

项目当前已经新增多条**不依赖 Minecraft 运行时**的纯 Java 离线分析/规则链路：

- `com.knightcode.appliedstoragesorter.analysis.SorterDumpAnalyzer`
- `com.knightcode.appliedstoragesorter.rule.route.RoutingProfileJsonCli`
- `com.knightcode.appliedstoragesorter.analysis.SorterDumpRoutingAnalyzer`
- `com.knightcode.appliedstoragesorter.analysis.SorterDumpRoutingSuggestionAnalyzer`
- `com.knightcode.appliedstoragesorter.analysis.SorterDumpProfileGenerationAnalyzer`
- `com.knightcode.appliedstoragesorter.profilegen.RoutingProfileGenerator`

可通过 Gradle 任务运行：

```bash
./gradlew analyzeSorterDump -PdumpFile=src/main/resources/testfiles/your-dump.json -PanalysisOutput=tmp/analysis.txt
./gradlew routingProfileJsonCli
./gradlew filterUiMetadataCli -PcliArgs="write-default tmp/filter-ui-metadata.json"
./gradlew analyzeSorterDumpRouting -PprofileFile=src/main/resources/testfiles/routing-profile-example.json -PdumpFile=src/main/resources/testfiles/your-dump.json -PanalysisOutput=tmp/routing-analysis.txt
./gradlew analyzeSorterDumpRoutingSuggestions -PprofileFile=src/main/resources/testfiles/routing-profile-example.json -PdumpFile=src/main/resources/testfiles/your-dump.json -PanalysisOutput=tmp/routing-suggestions.txt
./gradlew generateRoutingProfileDraft -PprofileFile=src/main/resources/testfiles/routing-profile-example.json -PdumpFile=src/main/resources/testfiles/your-dump.json -PoutputProfile=tmp/generated-routing-profile.json -PanalysisOutput=tmp/generated-routing-profile.notes.txt
```

这些分析器采用 **Gson `JsonReader` 流式读取** 或纯 Java 模型计算，不要求 Minecraft client/server 环境。

当前已支持：

- dump summary
- item 总量统计
- top items
- top mods
- top tags
- 带 components 的物品列表
- bulk 候选计数
- routing profile JSON 读写
- dump item -> `ItemMatchContext` 转换
- Query Builder 所需 field/operator/combinator metadata 导出
- routing decision / explanation
- fallback item 聚合
- 基于 fallback 的规则建议输出
- 通过 `RoutingProfileGenerator` 接口生成 draft profile
- 第一版启发式 zone/filter/route 草案生成

---

## 6. 当前 UI/UX 方向

当前已经明确：

- **不做流程编辑器**
- **不做图形化编程式节点拼装**
- **流程图只作为只读解释视图**

UI/UX 将围绕以下页面组织：

- 总览页
- 存储区页
- 过滤器页
- 应用顺序页
- 流程图页（只读）
- 诊断 / 预览页

同时要求：

- 每个页面有清晰 tooltip
- 过滤相关控件有详细 tooltip
- 提供足够完整的 GuideME / 使用说明
- 让用户第一次接触也能理解系统结构

---

## 7. 当前设计原则

当前项目的设计原则已经比较明确：

1. **先做可解释，再做强功能**
2. **自动整理与自动搬运严格区分**
3. **列表负责编辑，流程图负责解释**
4. **过滤器负责匹配，存储区负责去向**
5. **默认避免图形化编程式复杂交互**
6. **先服务深度 AE2 用户，而不是轻度仓储用户**

---

## 8. 当前结论

Applied Storage Sorter 当前不再只是“整理碎盘”的小想法，
而是一个围绕 **AE2 内部存储治理** 逐步扩展的项目：

- 第一条线做自动整理
- 第二条线做玩家可控的自动搬运
- 中间通过 dump / 离线分析 / 过滤器 / 存储区逐步搭建基础设施
- 并且已经出现一条独立于 Minecraft 运行时的 **纯 Java 规则分析链路**
- 以及一层可替换的 **profile generation 接口**，供未来更智能的 analyzer / generator 演进

如果后续继续推进，项目的核心竞争力不会是“省几块盘”，
而是：

- **更可控**
- **更可解释**
- **更适合大型 AE2 网络长期维护**
- **更容易和未来的 React/Web 配置端对接**
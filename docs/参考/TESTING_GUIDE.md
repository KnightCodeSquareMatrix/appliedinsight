# 测试指南 (Testing Guide)

> 本文档描述 **Applied Energistics: Insight** 的测试策略、测试类型和编写指南。
> 面向开发者和 QA 人员。

---

## 📋 目录

- [测试策略概览](#1-测试策略概览)
- [单元测试](#2-单元测试)
- [集成测试](#3-集成测试)
- [离线分析测试](#4-离线分析测试)
- [GameTest（游戏内测试）](#5-gametest-游戏内测试)
- [编写测试指南](#6-编写测试指南)
- [运行测试](#7-运行测试)
- [测试文件与资源](#8-测试文件与资源)

---

## 1. 测试策略概览

### 1.1 测试金字塔

```
        ╱╲
       ╱  ╲          GameTest（游戏内集成测试）
      ╱    ╲         少量，验证 AE2 运行时交互
     ╱──────╲
    ╱        ╲       离线分析测试（JavaExec）
   ╱          ╲      中等数量，验证分析器正确性
  ╱────────────╲
 ╱              ╲    单元测试（JUnit 5）
╱                ╲   大量，覆盖规则层、规划层、模型层
```

### 1.2 测试类型分布

| 测试类型 | 框架 | 数量 | 运行环境 | 速度 |
|---------|------|------|---------|------|
| 单元测试 | JUnit 5 | 大量 | 纯 Java | 毫秒级 |
| 离线分析测试 | JUnit 5 + Gradle JavaExec | 中等 | 纯 Java | 秒级 |
| GameTest | NeoForge GameTest | 少量 | Minecraft 运行时 | 分钟级 |

### 1.3 测试依赖

```groovy
dependencies {
    testImplementation "org.junit.jupiter:junit-jupiter:5.10.2"
    testImplementation "com.google.code.gson:gson:2.11.0"
    testRuntimeOnly "org.junit.platform:junit-platform-launcher:1.10.2"
}
```

---

## 2. 单元测试

### 2.1 现有测试

| 测试类 | 包路径 | 测试内容 |
|--------|--------|---------|
| [`ItemFilterMatcherTest`](src/test/java/com/knightcode/appliedinsight/rule/filter/ItemFilterMatcherTest.java) | `rule.filter` | 过滤器匹配逻辑（ITEM_ID、MOD_ID、TAG、NBT_PATH、REGEX、AND/OR 组合） |
| [`RoutingProfileJsonCodecTest`](src/test/java/com/knightcode/appliedinsight/rule/route/RoutingProfileJsonCodecTest.java) | `rule.route` | Profile JSON 编解码（加载 web 生成的 profile） |
| [`ZoneAllocationPlannerTest`](src/test/java/com/knightcode/appliedinsight/plan/ZoneAllocationPlannerTest.java) | `plan` | 离线规划器（从 profile + dump 分类物品到 zone） |
| [`RuntimeCellTransferTest`](src/test/java/com/knightcode/appliedinsight/ae2/zone/RuntimeCellTransferTest.java) | `ae2.zone` | 搬运结果模型（ExecutionResult、DebugReport） |

### 2.2 测试模式

#### 纯模型测试（规则层）

规则层（`rule/**`）的测试是最简单、最快速的，因为不依赖 Minecraft 运行时：

```java
@Test
void itemIdEquals() {
    var filter = ItemFilter.allOf("t", "t", List.of(
            new FilterCondition(FilterField.ITEM_ID, FilterOperator.EQUALS, "minecraft:cobblestone")));
    assertTrue(ItemFilterMatcher.matches(filter, SIMPLE_CONTEXT));
}
```

#### 数据驱动测试（离线分析器）

使用测试资源目录中的 JSON 文件作为输入：

```java
@Test
void classifiesDumpItemsIntoZonesFromProfile() throws Exception {
    Path profileFile = Path.of("src/main/resources/testfiles/routing-profile-example.json");
    Path dumpFile = Path.of("src/main/resources/testfiles/me-dump-20260517-033650.json");
    ZoneAllocationPlan plan = ZoneAllocationPlanner.plan(profileFile, dumpFile);
    assertEquals(630, plan.assignmentCount());
}
```

#### 模型验证测试（结果对象）

验证 record 的构造、不变性、防御性拷贝：

```java
@Test
void detailedResult_rejectsNullExecutionResult() {
    var debug = new ZoneMoveExecutionDebugReport(...);
    assertThrows(NullPointerException.class,
            () -> new ZoneMoveExecutionDetailedResult(null, debug));
}
```

### 2.3 编写新的单元测试

**规则层测试**（推荐优先编写）：

```java
class MyNewFilterTest {
    @Test
    void myNewFilterCondition() {
        // 1. 准备测试数据
        var context = new ItemMatchContext(...);
        var filter = ItemFilter.allOf("t", "t", List.of(
                new FilterCondition(FilterField.MY_FIELD, FilterOperator.EQUALS, "expected")));
        
        // 2. 执行测试
        boolean result = ItemFilterMatcher.matches(filter, context);
        
        // 3. 验证结果
        assertTrue(result);
    }
}
```

**规划层测试**：

```java
class MyPlannerTest {
    @Test
    void plansWithCustomProfile() throws Exception {
        // 使用测试资源中的 profile 和 dump 文件
        var plan = ZoneAllocationPlanner.plan(
                Path.of("src/main/resources/testfiles/my-profile.json"),
                Path.of("src/main/resources/testfiles/my-dump.json"));
        assertEquals(expectedCount, plan.assignmentCount());
    }
}
```

---

## 3. 集成测试

### 3.1 离线分析器集成测试

离线分析器通过 Gradle `JavaExec` 任务运行，可作为集成测试使用：

```bash
# 运行所有离线分析任务，验证输出
./gradlew analyzeSorterDump -PdumpFile=src/main/resources/testfiles/me-dump-20260517-033650.json -PanalysisOutput=tmp/test-analysis.txt
./gradlew analyzeSorterDumpRouting -PprofileFile=src/main/resources/testfiles/routing-profile-example.json -PdumpFile=src/main/resources/testfiles/me-dump-20260517-033650.json -PanalysisOutput=tmp/test-routing.txt
./gradlew analyzeZoneAllocationPlan -PprofileFile=src/main/resources/testfiles/routing-profile-example.json -PdumpFile=src/main/resources/testfiles/me-dump-20260517-033650.json -PanalysisOutput=tmp/test-plan.txt
```

### 3.2 验证集成测试输出

```bash
# 检查分析输出文件是否存在且非空
test -s tmp/test-analysis.txt && echo "PASS" || echo "FAIL"
test -s tmp/test-routing.txt && echo "PASS" || echo "FAIL"
test -s tmp/test-plan.txt && echo "PASS" || echo "FAIL"
```

---

## 4. 离线分析测试

### 4.1 测试资源文件

测试使用的 JSON 文件位于 `src/main/resources/testfiles/`：

| 文件 | 说明 |
|------|------|
| `me-dump-20260517-033650.json` | 示例网络快照 dump |
| `routing-profile-example.json` | 示例路由配置 profile |
| `web_generated_filter.json` | Web 前端生成的 profile（用于编解码测试） |

### 4.2 添加新的测试资源

1. 通过 `/sorter me dump` 在游戏中生成 dump JSON
2. 将文件复制到 `src/main/resources/testfiles/`
3. 在测试中引用该文件

### 4.3 离线分析器测试要点

- 所有离线分析器都是纯 Java，可以在 IDE 中直接运行 `main()` 方法
- 使用 Gson `JsonReader` 流式读取，适合大文件
- 输出为文本文件，便于人工审查

---

## 5. GameTest（游戏内测试）

### 5.1 概述

GameTest 是 NeoForge 提供的游戏内测试框架，用于验证模组在 Minecraft 运行时环境中的行为。

### 5.2 运行 GameTest

```bash
# 启动 GameTest 服务端（自动运行所有注册的 GameTest）
./gradlew runGameTestServer
```

### 5.3 何时使用 GameTest

GameTest 适用于需要 AE2 运行时环境的测试场景：

- `RuntimeCell.TransferOutcome` 构造（需要 `BlockPos`、`MEStorage`）
- `Ae2ZoneMoveExecutor` 执行验证
- `RuntimeZoneRegistryBuilder` 构建验证
- DAV 方块交互测试

### 5.4 GameTest 编写示例

```java
@GameTest
public void testSorterMergeOnNetwork(GameTestHelper helper) {
    // 设置 AE2 网络
    // 执行 /sorter merge
    // 验证物品被正确归并
}
```

> 参考：[NeoForge GameTest 文档](https://docs.neoforged.net/docs/gametests/)

---

## 6. 编写测试指南

### 6.1 通用原则

1. **测试行为，而非实现** — 测试公开 API 的行为，不测试私有方法
2. **每个测试一个断言场景** — 一个 `@Test` 方法测试一个逻辑场景
3. **使用有意义的测试名** — `itemIdEquals()`、`nbtPathRegexNoMatch()`
4. **测试边界条件** — null 输入、空列表、最大值、最小值
5. **测试失败路径** — 不匹配、禁用过滤器、无效输入

### 6.2 规则层测试要点

- 规则层是纯 Java，测试最简单
- 使用 `ItemMatchContext` 构造测试上下文
- 测试所有 `FilterField` + `FilterOperator` 组合
- 测试 AND/OR 组合逻辑
- 测试禁用过滤器

### 6.3 规划层测试要点

- 使用测试资源中的 profile 和 dump 文件
- 验证分配数量、目标 zone、决策类型
- 测试边界情况（空 profile、空 dump）

### 6.4 模型测试要点

- 验证 record 构造（包括 null 检查）
- 验证防御性拷贝（`List.copyOf()`）
- 验证计算字段（如 `failedMoveCount()` 是三个字段之和）

### 6.5 命名约定

```
测试类: {TargetClass}Test
测试方法: {scenario}_{expectedBehavior}
示例:
- ItemFilterMatcherTest.itemIdEquals()
- ItemFilterMatcherTest.nbtPathEmptyNbtReturnsFalse()
- RuntimeCellTransferTest.executionResult_holdsPrimitiveCounts()
```

---

## 7. 运行测试

### 7.1 运行所有测试

```bash
./gradlew test
```

### 7.2 运行特定测试类

```bash
# 运行单个测试类
./gradlew test --tests "com.knightcode.appliedstoragesorter.rule.filter.ItemFilterMatcherTest"

# 运行特定测试方法
./gradlew test --tests "com.knightcode.appliedstoragesorter.rule.filter.ItemFilterMatcherTest.itemIdEquals"
```

### 7.3 跳过测试（快速构建）

```bash
./gradlew build -x test
```

### 7.4 在 IDE 中运行测试

IntelliJ IDEA：右键测试类或方法 → Run

### 7.5 查看测试报告

```bash
# HTML 测试报告
open build/reports/tests/test/index.html
```

---

## 8. 测试文件与资源

### 8.1 测试资源目录

```
src/main/resources/testfiles/
├── me-dump-20260517-033650.json          # 网络快照 dump
├── routing-profile-example.json          # 路由配置示例
└── web_generated_filter.json             # Web 前端生成的 profile
```

### 8.2 添加新测试资源

```bash
# 从游戏中导出 dump
# 在游戏中执行 /sorter me dump
# 复制到测试资源目录
cp dumps/appliedinsight/me-dump-*.json src/main/resources/testfiles/my-test-dump.json
```

### 8.3 测试输出目录

```bash
# 离线分析器输出
tmp/analysis.txt
tmp/routing-analysis.txt
tmp/routing-suggestions.txt
tmp/generated-routing-profile.json
tmp/zone-allocation-plan.txt
```

---

> **相关文档**：[`DEVELOPER_GUIDE.md`](DEVELOPER_GUIDE.md) | [`API_REFERENCE.md`](API_REFERENCE.md) | [`EXTENSION_GUIDE.md`](EXTENSION_GUIDE.md)

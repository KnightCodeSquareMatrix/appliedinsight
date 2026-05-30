# 扩展开发指南 (Extension Guide)

> 本文档描述如何扩展 **Applied Storage Sorter** 的功能。
> 面向希望新增命令、过滤器、分析器、profile generator 或其他扩展点的开发者。

---

## 📋 目录

- [扩展点总览](#1-扩展点总览)
- [新增命令](#2-新增命令)
- [新增过滤器字段](#3-新增过滤器字段)
- [新增过滤器操作符](#4-新增过滤器操作符)
- [新增分析器](#5-新增分析器)
- [新增 Profile Generator](#6-新增-profile-generator)
- [新增日志输出](#7-新增日志输出)
- [新增 Gradle 任务](#8-新增-gradle-任务)
- [新增 JSON Schema](#9-新增-json-schema)
- [最佳实践](#10-最佳实践)

---

## 1. 扩展点总览

| 扩展点 | 位置 | 难度 | 说明 |
|--------|------|------|------|
| 新增命令 | `command/` + `application/` | 中等 | 注册新 Brigadier 命令 |
| 新增过滤器字段 | `rule/filter/` | 简单 | 扩展 `FilterField` 枚举 |
| 新增过滤器操作符 | `rule/filter/` | 简单 | 扩展 `FilterOperator` 枚举 |
| 新增分析器 | `analysis/` | 中等 | 实现离线分析逻辑 |
| 新增 Profile Generator | `profilegen/` | 中等 | 实现 `RoutingProfileGenerator` 接口 |
| 新增日志输出 | `logging/` | 简单 | 扩展 Logger 类 |
| 新增 Gradle 任务 | `build.gradle` | 简单 | 注册 `JavaExec` 任务 |
| 新增 JSON Schema | `resources/schema/` | 简单 | 定义新的 JSON 契约 |

---

## 2. 新增命令

### 2.1 步骤概览

1. 在 [`SorterCommands`](src/main/java/com/knightcode/appliedstoragesorter/command/SorterCommands.java) 中注册新命令
2. 在 `application/` 包中创建对应的 Service 类
3. 在 `application/result/` 包中（可选）创建新的 Result record
4. 在 `logging/` 包中（可选）添加日志输出

### 2.2 示例：新增 `/sorter me health` 命令

**Step 1: 注册命令**

在 [`SorterCommands.register()`](src/main/java/com/knightcode/appliedstoragesorter/command/SorterCommands.java:24) 中添加：

```java
.then(Commands.literal("health")
        .executes(SorterCommands::runMeHealth))
```

添加处理方法：

```java
private static int runMeHealth(CommandContext<CommandSourceStack> context) {
    return sendFeedback(context.getSource(), SorterHealthService.execute(context.getSource()));
}
```

**Step 2: 创建 Service**

```java
package com.knightcode.appliedstoragesorter.application;

public final class SorterHealthService {
    private SorterHealthService() {}

    public static SorterFeedbackResult execute(CommandSourceStack source) {
        // 实现健康检查逻辑
        return SorterFeedbackResult.success(List.of(
                SorterComponentHelper.keyValue("status", "healthy")));
    }
}
```

**Step 3: 添加日志（可选）**

在 `logging/` 包中创建 `SorterHealthFileLogger`，或复用在 `SorterFileLogger` 中添加方法。

### 2.3 注意事项

- 命令方法必须返回 `int`（Brigadier 协议）
- 使用 `SorterFeedbackResult` 统一反馈格式
- 使用 `SorterComponentHelper` 构建控制台消息
- 检查 `Config.ENABLE_SORTER` 和 `Config.DEVELOPER_MODE`
- 检查 `source.getEntity()` 是否为 null

---

## 3. 新增过滤器字段

### 3.1 步骤

1. 在 [`FilterField`](src/main/java/com/knightcode/appliedstoragesorter/rule/filter/FilterField.java) 枚举中添加新字段
2. 在 [`FilterFieldDefinition`](src/main/java/com/knightcode/appliedstoragesorter/rule/filter/FilterFieldDefinition.java) 中添加字段定义
3. 在 [`ItemMatchContext`](src/main/java/com/knightcode/appliedstoragesorter/rule/filter/ItemMatchContext.java) 中添加对应的上下文数据
4. 在 [`ItemFilterMatcher`](src/main/java/com/knightcode/appliedstoragesorter/rule/filter/ItemFilterMatcher.java) 中添加匹配逻辑
5. 更新 [`FilterUiMetadata`](src/main/java/com/knightcode/appliedstoragesorter/rule/filter/FilterUiMetadata.java) 中的 metadata
6. 添加单元测试

### 3.2 示例：新增 `DAMAGE` 字段

**Step 1: 扩展枚举**

```java
public enum FilterField {
    ITEM_ID,
    MOD_ID,
    TAG,
    DISPLAY_NAME,
    HAS_COMPONENTS,
    TOTAL_AMOUNT,
    NBT_PATH,
    DAMAGE,  // 新增
}
```

**Step 2: 添加字段定义**

```java
// FilterFieldDefinition 中
new FilterFieldDefinition(
    FilterField.DAMAGE,
    "物品耐久",
    FilterValueType.NUMBER,
    List.of(FilterOperator.EQUALS, FilterOperator.GREATER_THAN, FilterOperator.LESS_THAN)
)
```

**Step 3: 扩展上下文**

```java
public record ItemMatchContext(
    String itemId,
    String modId,
    String displayName,
    Set<String> tags,
    boolean hasComponents,
    Long totalAmount,
    String nbt,
    Integer damage  // 新增
) {}
```

**Step 4: 添加匹配逻辑**

```java
// ItemFilterMatcher 中
case DAMAGE -> {
    if (context.damage() == null) return false;
    return switch (operator) {
        case EQUALS -> context.damage().equals(Integer.parseInt(value));
        case GREATER_THAN -> context.damage() > Integer.parseInt(value);
        case LESS_THAN -> context.damage() < Integer.parseInt(value);
        default -> false;
    };
}
```

### 3.3 注意事项

- 规则层必须保持纯 Java，不依赖 Minecraft 运行时
- 新增字段需要同步更新 `FilterUiMetadata` 以便前端识别
- 新增字段需要更新 JSON Schema（如果涉及）

---

## 4. 新增过滤器操作符

### 4.1 步骤

1. 在 [`FilterOperator`](src/main/java/com/knightcode/appliedstoragesorter/rule/filter/FilterOperator.java) 枚举中添加新操作符
2. 在 [`FilterOperatorDefinition`](src/main/java/com/knightcode/appliedstoragesorter/rule/filter/FilterOperatorDefinition.java) 中添加定义
3. 在 [`ItemFilterMatcher`](src/main/java/com/knightcode/appliedstoragesorter/rule/filter/ItemFilterMatcher.java) 中添加匹配逻辑
4. 更新 `FilterUiMetadata`
5. 添加单元测试

### 4.2 示例：新增 `STARTS_WITH` 操作符

```java
public enum FilterOperator {
    EQUALS,
    NOT_EQUALS,
    GREATER_THAN,
    LESS_THAN,
    GREATER_OR_EQUAL,
    LESS_OR_EQUAL,
    IS_TRUE,
    IS_FALSE,
    REGEX,
    STARTS_WITH,  // 新增
}
```

```java
// ItemFilterMatcher 中
case STARTS_WITH -> {
    if (value == null) return false;
    return switch (field) {
        case ITEM_ID -> context.itemId().startsWith(value);
        case MOD_ID -> context.modId().startsWith(value);
        case DISPLAY_NAME -> context.displayName().startsWith(value);
        default -> false;
    };
}
```

---

## 5. 新增分析器

### 5.1 步骤

1. 在 `analysis/` 包中创建新的分析器类（实现 `main()` 方法）
2. 在 `build.gradle` 中注册对应的 `JavaExec` 任务
3. 添加单元测试（使用测试资源文件）

### 5.2 示例：新增碎片化分析器

```java
package com.knightcode.appliedstoragesorter.analysis;

public final class FragmentationAnalyzer {
    private FragmentationAnalyzer() {}

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: FragmentationAnalyzer <dumpFile> [outputFile]");
            System.exit(1);
        }
        Path dumpFile = Path.of(args[0]);
        String output = args.length > 1 ? args[1] : "tmp/fragmentation.txt";

        // 使用 Gson JsonReader 流式读取
        try (var reader = new JsonReader(Files.newBufferedReader(dumpFile))) {
            // 分析逻辑...
        }

        System.out.println("Analysis written to " + output);
    }
}
```

### 5.3 注册 Gradle 任务

在 `build.gradle` 中添加：

```groovy
tasks.register('analyzeFragmentation', JavaExec) {
    group = 'application'
    description = 'Analyze fragmentation level from a sorter dump JSON.'
    classpath = sourceSets.main.runtimeClasspath
    mainClass = 'com.knightcode.appliedstoragesorter.analysis.FragmentationAnalyzer'

    if (project.hasProperty('dumpFile')) {
        args project.property('dumpFile')
    }
    if (project.hasProperty('analysisOutput')) {
        args project.property('analysisOutput')
    }
}
```

### 5.4 注意事项

- 分析器必须是纯 Java，不依赖 Minecraft 运行时
- 使用 Gson `JsonReader` 流式读取，支持大文件
- 输出为文本文件，便于人工审查
- 遵循已有的参数命名约定（`dumpFile`、`analysisOutput` 等）

---

## 6. 新增 Profile Generator

### 6.1 步骤

1. 实现 [`RoutingProfileGenerator`](src/main/java/com/knightcode/appliedstoragesorter/profilegen/RoutingProfileGenerator.java) 接口
2. 在 `profilegen/` 包中创建实现类
3. 在 `SorterDumpProfileGenerationAnalyzer` 中注册新的 generator（可选）

### 6.2 接口定义

```java
public interface RoutingProfileGenerator {
    ProfileGenerationResult generate(ProfileGenerationRequest request);
}
```

### 6.3 示例：实现基于规则的 Generator

```java
package com.knightcode.appliedstoragesorter.profilegen;

public final class RuleBasedRoutingProfileGenerator implements RoutingProfileGenerator {

    @Override
    public ProfileGenerationResult generate(ProfileGenerationRequest request) {
        // 1. 分析 dump 数据
        // 2. 根据规则生成 zone / filter / routeRule
        // 3. 返回 ProfileGenerationResult

        return new ProfileGenerationResult(
                generatedProfile,
                notes,  // 生成说明
                warnings);  // 警告列表
    }
}
```

### 6.4 注意事项

- Profile Generator 是纯 Java，不依赖 Minecraft 运行时
- 生成结果应包含说明（notes）和警告（warnings），便于用户理解
- 生成策略应该是启发式的，允许用户后续手动调整

---

## 7. 新增日志输出

### 7.1 步骤

1. 在 `logging/` 包中创建新的 Logger 类（或扩展现有 Logger）
2. 使用 SLF4J + Lombok `@Slf4j` 记录技术日志
3. 使用 `ReportFileSupport` 写入报告型日志文件

### 7.2 示例：新增健康检查日志

```java
package com.knightcode.appliedstoragesorter.logging;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class SorterHealthFileLogger {
    private SorterHealthFileLogger() {}

    public static String logHealthReport(
            CommandSourceStack source,
            HealthReport report) {
        String fileName = "health-" + System.currentTimeMillis() + ".log";
        Path filePath = ReportFileSupport.getReportPath("logs/appliedstoragesorter", fileName);

        try (var writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            writer.write("[health_report]\n");
            writer.write("timestamp=" + Instant.now() + "\n");
            writer.write("status=" + report.status() + "\n");
            // ...
        } catch (IOException e) {
            log.error("Failed to write health report", e);
        }

        log.info("Health report written to {}", filePath);
        return filePath.toString();
    }
}
```

### 7.3 注意事项

- Logger 是基础设施，不反向定义核心模型
- 技术日志使用 SLF4J，报告型日志写入独立文件
- 遵循已有的日志格式约定（section、key=value）

---

## 8. 新增 Gradle 任务

### 8.1 通用模板

```groovy
tasks.register('myNewTask', JavaExec) {
    group = 'application'
    description = 'Description of my new task.'
    classpath = sourceSets.main.runtimeClasspath
    mainClass = 'com.knightcode.appliedstoragesorter.analysis.MyNewAnalyzer'

    if (project.hasProperty('inputFile')) {
        args project.property('inputFile')
    }
    if (project.hasProperty('outputFile')) {
        args project.property('outputFile')
    }
}
```

### 8.2 任务类型

| 类型 | 用途 |
|------|------|
| `JavaExec` | 运行 Java 主类（离线分析器、CLI 工具） |
| `Copy` | 文件拷贝（mod JAR 部署、dump 同步） |
| `Delete` | 文件清理 |

---

## 9. 新增 JSON Schema

### 9.1 步骤

1. 在 `src/main/resources/schema/` 目录下创建新的 JSON Schema 文件
2. 定义字段类型、必填项、枚举值
3. 在相关文档中引用该 Schema

### 9.2 示例

```json
{
  "$schema": "https://json-schema.org/draft-07/schema#",
  "title": "Health Report",
  "type": "object",
  "properties": {
    "status": {
      "type": "string",
      "enum": ["healthy", "warning", "critical"]
    },
    "checks": {
      "type": "array",
      "items": {
        "type": "object",
        "properties": {
          "name": { "type": "string" },
          "passed": { "type": "boolean" },
          "message": { "type": "string" }
        },
        "required": ["name", "passed"]
      }
    }
  },
  "required": ["status", "checks"]
}
```

---

## 10. 最佳实践

### 10.1 层级依赖规则

```
command/    → 可依赖任何层
application/ → 可依赖 rule、ae2、plan、logging
rule/       → 纯 Java，不依赖任何 Minecraft/AE2 运行时
plan/       → 可依赖 rule
ae2/        → 可依赖 rule、plan
analysis/   → 纯 Java，不依赖 Minecraft 运行时
logging/    → 基础设施，不反向定义核心模型
```

### 10.2 代码审查清单

提交扩展代码前自查：

- [ ] 是否遵循了层级依赖规则？
- [ ] 规则层是否保持纯 Java？
- [ ] 是否添加了必要的日志？
- [ ] 是否更新了相关文档？
- [ ] 是否添加了单元测试？
- [ ] 是否遵循了命名约定？
- [ ] 是否处理了边界情况（null、空列表等）？
- [ ] 是否更新了 JSON Schema（如果涉及）？
- [ ] 是否更新了 FilterUiMetadata（如果涉及过滤器）？

### 10.3 文档同步

新增功能后，同步更新以下文档：

| 变更类型 | 需更新的文档 |
|---------|-------------|
| 新增命令 | `COMMANDS_REFERENCE.md`、`API_REFERENCE.md`、`README.md` |
| 新增过滤器字段 | `docs/架构/FACTS.md`、`API_REFERENCE.md`、`前端对接说明.md` |
| 新增分析器 | `API_REFERENCE.md`、`TESTING_GUIDE.md` |
| 新增 Profile Generator | `API_REFERENCE.md`、`EXTENSION_GUIDE.md` |
| 新增日志输出 | `日志与JSON字段契约.md`、`API_REFERENCE.md` |

---

> **相关文档**：[`API_REFERENCE.md`](API_REFERENCE.md) | [`docs/架构/FACTS.md`](docs/架构/FACTS.md) | [`DEVELOPER_GUIDE.md`](DEVELOPER_GUIDE.md) | [`TESTING_GUIDE.md`](TESTING_GUIDE.md)

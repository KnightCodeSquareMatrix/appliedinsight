# 开发者指南 (Developer Guide)

> 本文档帮助新开发者快速上手 Applied Storage Sorter 的本地开发和贡献流程。

---

## 1. 环境要求

| 工具 | 版本要求 | 说明 |
|------|---------|------|
| Java | 21 (Microsoft JVM vendor) | `java --version` 确认 |
| Git | 任意现代版本 | `git --version` 确认 |
| Gradle | 项目自带 Wrapper | 使用 `./gradlew` 而非系统 gradle |
| IDE | 推荐 VS Code 或 IntelliJ IDEA | VS Code 建议安装 Extension Pack for Java |
| Minecraft | 1.21.1 | 运行时自动下载 |
| NeoForge MDK | 21.1.224 | Gradle 自动处理 |

**注意**: 不要使用 Java 8、11、17。本项目强制要求 Java 21。

---

## 2. 项目结构

```
appliedstoragesorter-template-1.21.1/
├── src/
│   ├── main/
│   │   ├── java/com/knightcode/appliedstoragesorter/     # 主源码
│   │   │   ├── ae2/              # AE2 集成层 (扫描、执行、runtime)
│   │   │   ├── analysis/         # 离线分析
│   │   │   ├── application/      # 应用服务编排
│   │   │   ├── block/            # 方块
│   │   │   ├── blockentity/      # 方块实体
│   │   │   ├── client/           # 客户端
│   │   │   ├── command/          # 命令
│   │   │   ├── item/             # 物品
│   │   │   ├── logging/          # 日志
│   │   │   ├── menu/             # 容器
│   │   │   ├── plan/             # 规划模型
│   │   │   ├── profilegen/       # 规则草案生成
│   │   │   ├── recipe/           # 配方
│   │   │   ├── registry/         # 注册
│   │   │   └── rule/             # 纯规则模型 (31类)
│   │   └── resources/            # 资源文件
│   ├── generated/                # datagen 输出 (勿手动编辑)
│   └── test/                     # 测试代码
├── docs/                         # 项目文档
│   ├── 架构/FACTS.md              # 事实库（取代旧版 ARCHITECTURE_REFERENCE + GLOSSARY）
│   ├── ai/                       # AI协作者文档
│   ├── 类职责/                   # 类职责文档 (129个)
│   └── 历史文档-仅供AI参考/       # 历史档案
├── build.gradle                  # 构建配置
├── settings.gradle               # 项目设置
└── gradle.properties             # 版本信息
```

---

## 3. 构建与运行

### 3.1 首次构建

```bash
# 克隆后首次构建（下载依赖 + 编译）
./gradlew build

# 仅编译（不运行测试）
./gradlew compileJava
```

### 3.2 运行客户端

```bash
# 启动 Minecraft 客户端（带模组加载）
./gradlew runClient
```

### 3.3 数据生成 (Data Generation)

```bash
# 生成资源文件（配方、模型等）
./gradlew runDatagen
```

生成产物输出到 `src/generated/resources/`。这些文件不应手动编辑。

### 3.4 远程开发 — 自动同步 Mod JAR

> 适用于 SSH 连接服务器开发，Minecraft 客户端在本地 Windows 运行的情况。

服务器上每次 `./gradlew build` 后，jar 自动拷贝到 `tools/serve/` 目录。
本机运行 [`tools/sync_jar.py`](tools/sync_jar.py) 脚本，通过 SSH 监听文件变化，有更新时自动下载到多个指定目录（如 PrismLauncher mods 文件夹）。

#### 3.4.1 一次性设置：配置本机同步脚本

在**本机 Windows** 上，首次运行脚本生成配置文件：

```bash
python tools/sync_jar.py
```

脚本会在 `tools/` 目录下生成 `sync_jar_config.json`，编辑该文件：

```json
{
    "server": {
        "host": "172.16.201.63",
        "port": 22,
        "user": "root",
        "key": null,
        "remote_jar_path": "/data/appliedstoragesorter/tools/serve/appliedstoragesorter.jar"
    },
    "watch_interval": 5,
    "destinations": [
        "C:/Users/KnightCode/AppData/Roaming/PrismLauncher/instances/1.21.1/minecraft/mods",
        "C:/Users/KnightCode/AppData/Roaming/MultiMC/instances/1.21.1/.minecraft/mods"
    ],
    "rename_to": "appliedstoragesorter.jar"
}
```

| 配置项 | 说明 |
|--------|------|
| `server.host` | 服务器 IP |
| `server.port` | SSH 端口（默认 22） |
| `server.user` | SSH 用户名 |
| `server.key` | SSH 密钥路径（`null` 则用默认 `~/.ssh/id_rsa`） |
| `destinations` | 目标目录列表（支持多个） |
| `watch_interval` | 轮询间隔（秒） |

#### 3.4.2 日常使用

**服务器上**（构建）：
```bash
./gradlew build
```

**本机 Windows 上**（保持运行）：
```bash
python tools/sync_jar.py
```

脚本会自动：
1. 通过 SSH 连接服务器
2. 每 5 秒检查 jar 文件的 MD5
3. 检测到更新时自动下载
4. 拷贝到所有配置的目标目录

效果：**服务器上 `./gradlew build` → 本机自动同步到所有 mods 文件夹**，无需任何手动操作。

> **💡 提示**：本机需要安装 Python 3 和 SSH 客户端（Windows 10/11 自带 OpenSSH Client）。首次连接需要确认服务器指纹。

---

### 3.5 运行测试

```bash
# 运行所有测试
./gradlew test

# 运行特定测试类
./gradlew test --tests "*SorterPlanServiceTest*"
```

### 3.5 其他常见任务

```bash
# 清理构建产物
./gradlew clean

# 查看依赖树
./gradlew dependencies

# 打包模组 JAR
./gradlew build
# 产物在 build/libs/ 下
```

---

## 4. 开发工作流

### 4.1 新建一个类

1. 在对应包下创建 `.java` 源文件
2. 在 `docs/类职责/` 下创建同名 `.md` 职责文档
3. 更新 `docs/类职责/索引.md` 添加索引条目
4. 若新增涉及架构边界，同步更新：
   - `docs/架构/FACTS.md`
   - `docs/类职责总览.md`

### 4.2 修改现有类

1. **先读文档**：阅读对应 `docs/类职责/` 下的职责文档
2. **再读源码**：理解当前实现
3. **判断归属**：改动属于 merge 路径还是 me plan 主线？
4. **跨层判断**：若修改涉及跨层调用，确认是否违反现有边界
5. **更新文档**：若修改影响职责定位，更新对应职责文档

### 4.3 添加新命令

1. 在 `SorterCommands.java` 中注册 Brigadier 命令
2. 在 `application/` 下创建对应的 Service 编排类
3. 添加日志输出（使用 `SorterFileLogger` 或专用 Logger）
4. 更新命令语义文档

### 4.4 AI 协作者协作

> 参考 `docs/ai/AI_ENTRY.md` 了解完整的 AI 协作流程。

**最小原则**：
- 给 AI 协作者提供 `docs/ai/AI_ENTRY.md` 作为入口
- 提供相关 `docs/类职责/` 文档作为具体上下文
- 涉及架构判断时，提供 `docs/架构/FACTS.md` 的对应章节

---

## 5. 层级依赖规则

```
规则层 (rule/)     ← 无依赖，纯 Java 模型
规划模型层 (plan/)  ← 依赖规则层
AE2集成层 (ae2/)   ← 依赖规则层 + 规划层 + Minecraft/AE2 API
应用服务层 (app/)   ← 依赖以上所有
日志层 (logging/)   ← 依赖应用服务层返回的结果
注册层 (registry/)  ← 依赖方块/物品/菜单声明
```

**硬规则**：
- `rule/**` 不能导入任何 Minecraft 或 AE2 类
- `ae2/**` 不能反向依赖 `application/**`
- `logging/**` 不能参与业务决策
- `analysis/**` 不能反向污染在线命令链

---

## 6. 代码规范

### 6.1 命名约定

| 场景 | 约定 |
|------|------|
| merge 相关 | 类名前缀 `Merge` 或 `SorterMerge` |
| zone 主线 | `RuntimeXxx`、`ZoneXxx`、`PlanXxx` |
| 命令 | `/sorter merge`、`/sorter me xxx` |
| 执行结果 | `XxxResult`、`XxxDetailedResult` |

**不要**：
- 把 `/sorter merge` 写成通用 sorter 主入口
- 把 zone 主线错误表述成"只是 merge"

### 6.2 日志规范

**技术运行日志**（面向开发者/排错）：
```java
// 使用 SLF4J + Lombok @Slf4j
@Slf4j
public class MyClass {
    public void doSomething() {
        log.debug("Processing {} items for zone {}", count, zoneId);
    }
}
```

**报告型输出**（面向文件产物/审计）：
- 使用专用 Logger 类（如 `SorterPlanFileLogger`）
- 不通过技术日志通道输出结构化报告

### 6.3 包访问原则

| 包 | 可被访问 | 不可被访问 |
|----|---------|-----------|
| `rule/` | 任何层 | — |
| `ae2/` | 仅 application 层 | rule 层 |
| `plan/` | ae2、application | rule 层 |
| `application/` | command 层 | ae2、rule 层（反向） |
| `logging/` | application 层 | 核心业务层 |

---

## 7. 调试技巧

### 7.1 启用模组

```bash
# 确保 build.gradle 中配置正确
# 启动客户端后，在游戏中输入命令测试
/sorter me dump
/sorter me plan
```

### 7.2 日志查看

```
logs/appliedstoragesorter.log       # 主日志（命令摘要/错误）
logs/appliedstoragesorter/plan-*.log # plan 明细
dumps/appliedstoragesorter/me-dump-*.json  # 网络快照
```

### 7.3 常见问题

| 问题 | 可能原因 | 解决方案 |
|------|---------|---------|
| `ClassNotFoundException` | 编译未通过 | `./gradlew compileJava` |
| 命令无响应 | 未启用模组 | 检查 `Config.ENABLE_SORTER` |
| `DriveMachineAccessor` 失效 | AE2 版本升级 | 回归测试，更新 block id 白名单 |
| datagen 输出被覆盖 | 手动编辑了 generated 文件 | 改源码 datagen provider |

### 7.4 注意点

- `DriveMachineAccessor` 使用反射 + block id 白名单，AE2 或 ExtendedAE 升级后需重点回归
- 不要手动编辑 `src/generated/resources/` 下的文件

---

## 8. 贡献指南

### 8.1 PR 流程

1. Fork 并创建 feature branch
2. 确保 `./gradlew build` 通过
3. 确保新增类有对应的 `docs/类职责/` 文档
4. 若修改架构边界，同步更新架构文档
5. 提交 PR 并描述变更范围

### 8.2 文档要求

所有新增的 Java 类型**必须**有对应的类职责文档，放置于 `docs/类职责/` 下，并更新 `索引.md`。

文档内容至少包含：
- 类的定位与职责
- 关键方法说明
- 依赖关系
- 关键边界

### 8.3 代码审查检查清单

详细的代码审查清单请参考 [`docs/ai/CODE_REVIEW_CHECKLIST.md`](ai/CODE_REVIEW_CHECKLIST.md)，这里列出最关键的几项：

- [ ] `rule/**` 是否引入了 Minecraft/AE2 依赖？
- [ ] 新类或方法是否在正确的层？
- [ ] 是否更新了对应的职责文档？
- [ ] 是否保持了现有命令语义不变？
- [ ] 是否引入了不必要的抽象或框架？
- [ ] 网络通信（`network/**`）是否遵循了分块传输模式？
- [ ] GUI 方块修改是否同步更新了 Block / BlockEntity / Menu / Screen 四件套？

---

## 9. 快速参考

### 常用命令速查

```bash
./gradlew build          # 完整构建
./gradlew compileJava    # 仅编译
./gradlew runClient      # 启动游戏
./gradlew runDatagen     # 数据生成
./gradlew test           # 运行测试
./gradlew clean          # 清理
./gradlew serveModJar    # 启动 HTTP 下载服务器（远程开发用）
```

### 版本信息

| 项目 | 版本 |
|------|------|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.224 |
| AE2 | 19.2.17 |
| Java | 21 |
| Mod | 1.0.0 |

---

> **参考文档**: [`docs/架构/FACTS.md`](docs/架构/FACTS.md) | [`docs/类职责/索引.md`](类职责/索引.md) | [`docs/API_REFERENCE.md`](API_REFERENCE.md) | [`docs/COMMANDS_REFERENCE.md`](COMMANDS_REFERENCE.md) | [`docs/DEVELOPER_QUICKSTART.md`](DEVELOPER_QUICKSTART.md) | [`docs/TESTING_GUIDE.md`](TESTING_GUIDE.md) | [`docs/EXTENSION_GUIDE.md`](EXTENSION_GUIDE.md) | [`docs/ARCHITECTURE_DECISIONS.md`](ARCHITECTURE_DECISIONS.md)

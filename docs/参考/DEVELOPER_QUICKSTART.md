# 开发者快速入门 (Developer Quickstart)

> 本文档帮助新开发者从零搭建开发环境，完成第一次构建，并理解项目的基本开发流程。
> 面向首次接触此项目的 Java / Minecraft 模组开发者。

---

## 📋 目录

- [环境搭建](#1-环境搭建)
- [获取源码](#2-获取源码)
- [第一次构建](#3-第一次构建)
- [项目结构速览](#4-项目结构速览)
- [开发工作流](#5-开发工作流)
- [第一个贡献](#6-第一个贡献)
- [常见问题](#7-常见问题)

---

## 1. 环境搭建

### 1.1 必需工具

| 工具 | 版本要求 | 说明 |
|------|---------|------|
| Java | 21+ | 推荐使用 SDKMAN! 管理 |
| Git | 任意现代版本 | 版本控制 |
| IDE | IntelliJ IDEA 推荐 | 支持 Gradle 项目导入 |

### 1.2 Java 安装（使用 SDKMAN!）

```bash
# 安装 SDKMAN!
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# 安装 Java 21
sdk install java 21.0.5-tem

# 验证
java -version
# 输出应为：openjdk version "21.0.5" ...
```

### 1.3 IDE 配置

**IntelliJ IDEA** 推荐配置：

1. 安装插件：Minecraft Development（可选，提供 Minecraft 相关代码提示）
2. 确保 Gradle JVM 指向 Java 21
   - `File → Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JVM`
3. 启用注解处理器（Lombok）
   - `File → Settings → Build, Execution, Deployment → Compiler → Annotation Processors → Enable annotation processing`

---

## 2. 获取源码

```bash
git clone https://github.com/your-repo/appliedinsight.git
cd appliedinsight
```

---

## 3. 第一次构建

### 3.1 编译（不运行测试）

```bash
./gradlew build -x test
```

首次构建会下载 Gradle wrapper、NeoForge MDK、AE2 依赖等，耗时较长。

### 3.2 编译并运行测试

```bash
./gradlew build
```

### 3.3 启动 Minecraft 客户端

```bash
./gradlew runClient
```

启动后，在游戏中按 `T` 打开聊天框，输入 `/sorter me dump` 测试命令是否正常。

### 3.4 快速迭代

```bash
# 跳过测试，编译 + 打包 + 拷贝到 PrismLauncher
./gradlew quickBuild
```

---

## 4. 项目结构速览

```
appliedinsight/
├── build.gradle                    # Gradle 构建配置
├── settings.gradle                 # Gradle 项目设置
├── gradle.properties               # 版本号等属性
├── README.md                       # 项目总览
├── docs/                           # 文档目录
│   ├── 架构/FACTS.md               # 事实库（架构参考 + 整体逻辑 + 术语表）
│   ├── API_REFERENCE.md            # API 参考
│   ├── DEVELOPER_GUIDE.md          # 开发者指南（详细）
│   ├── DEVELOPER_QUICKSTART.md     # 开发者快速入门（本文档）
│   ├── 类职责总览.md               # 类职责总览
│   └── ...
├── src/
│   ├── main/
│   │   ├── java/com/knightcode/appliedinsight/
│   │   │   ├── appliedinsight.java    # 模组主入口
│   │   │   ├── Config.java                  # 配置
│   │   │   ├── command/                     # 命令层
│   │   │   ├── rule/                        # 规则层（纯 Java）
│   │   │   │   ├── filter/                  # 过滤器
│   │   │   │   ├── route/                   # 路由
│   │   │   │   └── zone/                    # 存储区
│   │   │   ├── plan/                        # 规划层
│   │   │   ├── ae2/                         # AE2 运行时层
│   │   │   │   ├── scan/                    # 扫描
│   │   │   │   ├── sort/                    # 整理
│   │   │   │   ├── zone/                    # Zone 运行时
│   │   │   │   ├── analysis/                # 分析
│   │   │   │   └── dump/                    # 导出
│   │   │   ├── analysis/                    # 离线分析
│   │   │   ├── application/                 # 应用层
│   │   │   ├── logging/                     # 日志
│   │   │   ├── network/                     # 网络包
│   │   │   ├── block/                       # 方块
│   │   │   ├── blockentity/                 # 方块实体
│   │   │   ├── item/                        # 物品
│   │   │   ├── menu/                        # 菜单
│   │   │   ├── client/                      # 客户端
│   │   │   ├── registry/                    # 注册
│   │   │   ├── profilegen/                  # Profile 生成
│   │   │   └── recipe/                      # 合成配方
│   │   └── resources/
│   │       ├── schema/                      # JSON Schema
│   │       └── testfiles/                   # 测试用文件
│   └── test/                                # 测试代码
└── tools/                                   # 工具脚本
```

### 4.1 六层架构速览

```
命令层 (command)     →  玩家输入
规则层 (rule)        →  纯 Java 规则模型（无 Minecraft 依赖）
规划层 (plan)        →  物品 → Zone 分配
运行时层 (ae2.zone)  →  RuntimeTopology / RuntimeZone
执行层 (ae2.sort)    →  实际搬运
分析层 (analysis)    →  离线分析 / 存储分析
```

> 详细架构说明见 [`docs/架构/FACTS.md`](docs/架构/FACTS.md)

---

## 5. 开发工作流

### 5.1 典型迭代流程

```bash
# 1. 修改代码
# 2. 编译验证
./gradlew build -x test

# 3. 运行测试
./gradlew test

# 4. 快速部署到开发实例
./gradlew quickBuild

# 5. 启动客户端测试
./gradlew runClient
```

### 5.2 代码规范

- **命名约定**：遵循 Java 命名规范
  - 类名：`PascalCase`
  - 方法/字段：`camelCase`
  - 常量：`UPPER_SNAKE_CASE`
  - 包名：全小写
- **日志**：使用 SLF4J + Lombok `@Slf4j`
- **包访问**：遵循层级依赖规则（上层可依赖下层，反之不可）

> 详细规范见 [`docs/DEVELOPER_GUIDE.md`](DEVELOPER_GUIDE.md)

### 5.3 调试技巧

- 命令输出通过 `SorterComponentHelper` 构建，支持 `keyValue`、`clickableFile` 等格式
- 日志文件位于 `logs/appliedinsight/`
- dump 文件位于 `dumps/appliedinsight/`
- 配置位于 `config/appliedinsight/server.toml`

---

## 6. 第一个贡献

### 6.1 选择一个起点

推荐从以下方向入手：

1. **修复已知问题** — 查看 [`docs/类职责总览.md`](类职责总览.md) 中的技术债列表
2. **增强离线分析器** — 在 `analysis/` 包中新增分析功能
3. **新增过滤器字段** — 在 `rule/filter/` 中扩展 `FilterField`
4. **改进日志输出** — 在 `logging/` 中增强报告型 Logger

### 6.2 贡献流程

1. Fork 仓库并创建特性分支
2. 编写代码并添加测试
3. 确保 `./gradlew build` 通过
4. 提交 PR，附上清晰的变更说明

### 6.3 代码审查检查清单

提交前自查：

- [ ] 是否遵循了层级依赖规则？（规则层不依赖 Minecraft 运行时）
- [ ] 是否添加了必要的日志？
- [ ] 是否更新了相关文档？
- [ ] 是否添加了单元测试？
- [ ] 是否遵循了命名约定？
- [ ] 是否处理了边界情况（null、空列表等）？

---

## 7. 常见问题

### Q: 构建时出现 `Could not find net.neoforged.moddev` 错误

确保 Gradle wrapper 版本正确，并检查网络连接。可以尝试：

```bash
./gradlew --refresh-dependencies
```

### Q: 运行 `runClient` 时崩溃

检查 `gradle.properties` 中的版本号是否与本地环境匹配。首次运行建议先执行 `build` 确认编译通过。

### Q: 如何查看详细日志？

```bash
tail -f logs/appliedinsight.log
```

### Q: 如何调试离线分析器？

离线分析器是纯 Java 应用，可以直接在 IDE 中运行 `main()` 方法，或通过 Gradle 任务执行：

```bash
./gradlew analyzeSorterDump -PdumpFile=path/to/dump.json
```

### Q: 如何添加新的 Gradle 任务？

在 `build.gradle` 中注册 `JavaExec` 类型任务，参考已有的 `analyzeSorterDump` 任务配置。

---

> **下一步**：阅读 [`docs/DEVELOPER_GUIDE.md`](DEVELOPER_GUIDE.md) 获取更详细的开发指南
> **架构理解**：阅读 [`docs/架构/FACTS.md`](docs/架构/FACTS.md)
> **术语学习**：阅读 [`docs/架构/FACTS.md` §6](docs/架构/FACTS.md#6-术语表精简版仅核心术语中英文对照--一句话定义)

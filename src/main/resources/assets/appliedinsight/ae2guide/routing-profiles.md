---
navigation:
  title: 路由配置（服主）
  icon: sorter_command_block
  parent: index.md
  position: 35
---

# 路由配置（服主）

「以绑定配置整理」功能面向**服务器管理员**或单人世界的服主。普通玩家日常整理只需使用 **分析当前网络** 与 **碎片物品合并**；本节说明如何启用规则搬运。

## 工作原理

1. 在服务器配置目录放置一份或多份**路由配置文件**（JSON）。
2. 管理员对目标 ME 网络执行 `/sorter me bindProfile <编号>`，把网络绑定到某份配置。
3. 玩家打开命令执行块，点击 **以绑定配置整理**，模组按配置中的 zones、filters、routeRules 计算搬运计划并执行。

绑定关系按 **维度 + ME 控制器位置** 存储，每个独立 ME 网络可绑定不同配置。

## 配置文件位置

```
config/appliedinsight/profiles/*.json
```

模组启动后若目录不存在会自动创建。每个 `.json` 文件描述一份完整的路由方案（Routing Profile）。

可参考模组内置样例结构（与测试资源同格式）：

- `zones` — 逻辑分区列表（如 bulk、misc）
- `filters` — 命名过滤器，每条含 `root` FilterExpression
- `routeRules` — 将过滤器匹配结果路由到目标 zone

最小概念示例：

```json
{
  "id": "default_profile",
  "name": "Default Profile",
  "enabled": true,
  "defaultZoneId": "misc",
  "version": 1,
  "zones": [ "..." ],
  "filters": [ "..." ],
  "routeRules": [ "..." ]
}
```

完整字段说明见仓库中 `src/main/resources/testfiles/routing-profile-example.json`。

## 管理员工作流

### 1. 列出可用配置

```
/sorter me listProfiles
```

按编号列出 `profiles/` 目录下所有已加载的配置文件。

### 2. 绑定到当前网络

站在目标 ME 网络附近（或已打开该网络上的命令执行块），执行：

```
/sorter me bindProfile <编号>
```

例如 `bindProfile 1` 绑定列表中的第 1 份配置。

### 3. 查看当前绑定

```
/sorter me showProfile
```

### 4. 让玩家执行整理

绑定成功后，命令执行块上的 **以绑定配置整理** 即可使用（等同 `/sorter me planAndMove`）。

仅生成计划、不搬运时，可使用 `/sorter me plan`（调试用途）。

## 常见错误

| 提示 | 原因 |
| :--- | :--- |
| 当前 ME 网络未绑定任何全局配置 | 尚未执行 `bindProfile` |
| 绑定的配置丢失 | 配置文件被删除或 `id` 不匹配 |
| 在 profiles 目录中未找到全局配置 | `profiles/` 为空或 JSON 无法解析 |
| 无法解析此 ME 网络的控制器身份 | 玩家附近没有可识别的 ME 控制器 |

## 命令权限说明

以下命令主要用于服主 / 调试，**默认不要求** `developerMode`：

- `/sorter merge`
- `/sorter me plan` / `planAndMove`
- `/sorter me listProfiles` / `bindProfile` / `showProfile`

以下命令需要 `config/appliedinsight-server.toml` 中 `developerMode = true`：

- `/sorter me dump`
- `/sorter me storageDump`
- `/sorter genTestItems`（测试物品生成）

命令执行块 GUI 上的 **分析当前网络** 与 **碎片物品合并** 对所有玩家可用（无需开发者模式）。详见[命令参考](commands.md)。

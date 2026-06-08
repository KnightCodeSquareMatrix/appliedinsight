---
navigation:
  title: 智能总线
  icon: smart_bus
  parent: index.md
  position: 30
item_ids:
  - appliedinsight:smart_bus
---

# 智能总线（Smart Bus）

智能总线是一个 AE2 线缆部件，在输入总线与输出总线的基础上增加了 **JSON 过滤器**。你可以用它对相邻方块库存做有条件的自动拉取或推送，而不必在总线上堆叠多张升级卡。

## 安装

1. 手持智能总线右键点击 AE2 线缆的某个面。
2. 确保该面已分配到 ME 频道且网络在线。

## 模式

智能总线有三种模式，外观纹理会随之变化：

| 模式 | 行为 |
| :--- | :--- |
| **未配置** | 不执行任何传输 |
| **输入** | 从线缆**背面**相邻库存拉取匹配过滤器的物品进入 ME 网络 |
| **输出** | 从 ME 网络向背面相邻库存推送匹配过滤器的物品 |

切换方式：

- **Shift + 右键** 总线（空手）循环切换模式
- 打开 GUI，点击 **切换** 按钮

模式顺序：未配置 → 输入 → 输出 → 输入 → …

## 搬运速度

Smart Bus 使用与 AE2 输入总线相同的动态 tick 节奏（有货时加速、空闲时休眠）。

每个 tick 最多搬运 **12 组**（12 次满 stack 传输），与 ExtendedAE 扩展输入/输出总线在 **4 张加速卡 + 默认 8 倍速** 下的吞吐相当（96 × 8 = 768 物品 ≈ 12 组 × 64）。

可在服务端配置 `smartBusStackTransfersPerTick` 调整（默认 12，范围 1–64）。

## 过滤器

智能总线 GUI 是一个**无物品栏的大面板**：用于展示 FilterExpression JSON、查看解析摘要，并支持剪贴板同步。推荐在外部网页编辑器中可视化建规则，再复制回游戏。

### 推荐工作流（网页编辑 · 离线）

**无需联网、无需 HTTP 服务器。**

1. 双击打开 `tools/filter-editor/index.html`（或在游戏 Smart Bus GUI 点 **网页编辑**，mod 会解压并打开本地副本）
2. 在网页中编辑过滤器，点击 **Copy for Game**
3. 回到游戏 GUI，点击 **粘贴**，再点 **保存**

若浏览器在 `file://` 下拦截剪贴板，可在网页底部 **Manual JSON paste** 框粘贴，或选中 JSON 预览后 Ctrl+C。

`filterEditorUrl` 配置项**留空**即可（默认）；仅在你自行托管编辑器时才需填写 URL。

### 游戏内按钮

| 按钮 | 作用 |
| :--- | :--- |
| **网页编辑** | 在浏览器打开过滤器编辑器 |
| **粘贴** | 从剪贴板导入 JSON（需再点保存才写入总线） |
| **复制** | 将当前 JSON 复制到剪贴板 |
| **保存** | 校验 JSON 并写入总线；无效时显示错误原因 |
| **删除** | 从总线上移除过滤器配置 |

### 游戏内快速预设

JSON 文本框上方有三个一键预设（点击即写入并保存）：

| 预设 | 含义 |
| :--- | :--- |
| **所有物品** | 匹配任意物品 |
| **有耐久** | 含耐久组件的物品 |
| **矿石** | 常见矿石标签与命名 |

悬停按钮可查看简要说明。需要更复杂的规则时再使用 **网页编辑**。

未配置过滤器时，总线不会传输任何物品。

### 最小示例

匹配所有 `ae2` 模组的物品：

```json
{
  "combinator": "AND",
  "rules": [
    {
      "field": "MOD_ID",
      "operator": "EQUALS",
      "value": "ae2"
    }
  ]
}
```

匹配物品 ID 包含 `ingot` 的条目：

```json
{
  "combinator": "OR",
  "rules": [
    {
      "field": "ITEM_ID",
      "operator": "CONTAINS",
      "value": "ingot"
    }
  ]
}
```

常用字段包括 `ITEM_ID`、`MOD_ID`、`TAG`、`TOTAL_AMOUNT`、`HAS_COMPONENTS` 等。`combinator` 可取 `AND` 或 `OR`，`rules` 中可嵌套子组，例如 `(A AND B) OR C`。

### 网页编辑器快速样板

离线编辑器提供三个一键样板（加载后仍需 **复制到游戏 → 粘贴 → 保存**）：

| 样板 | 含义 |
| :--- | :--- |
| **所有物品** | 匹配任意物品 |
| **有耐久的物品** | 含 `minecraft:damage` / `minecraft:max_damage` 组件 |
| **矿石** | `c:ores` 标签、`*:ores/*` 路径，或 ID 以 `_ore` 结尾 |

过滤器与路由配置（服主 profiles）使用同一套 FilterExpression 语法，但 Smart Bus 只需粘贴**表达式对象本身**，不需要完整的 routing profile 文件。

## 配方

<RecipeFor id="smart_bus" />

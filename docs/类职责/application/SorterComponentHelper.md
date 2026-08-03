# SorterComponentHelper
- **源码路径**: `src/main/java/com/knightcode/appliedinsight/application/SorterComponentHelper.java`
- **包**: `com.knightcode.appliedstoragesorter.application`
- **类型**: `class`
- **所属层**: 应用服务层

## 职责
生成 Minecraft 聊天栏可点击/带颜色的 `Component` 的工具类。统一管理 chat 输出的视觉格式。

## 边界检查
边界健康。纯工具方法，不参与业务决策。

## 抽象检查
没有过度抽象。将零散的 `Component.literal()` 调用收敛到一处。

## 方法

| 方法 | 返回 | 说明 |
|------|------|------|
| `clickableFile(label, path)` | `Component` | 绿色下划线文件路径，点击在系统默认程序中打开 |
| `keyValue(key, value)` | `Component` | 灰色 key + 白色 value |
| `line(text)` | `Component` | 纯白色文本行 |

## 主要协作者
- `net.minecraft.ChatFormatting`
- `net.minecraft.network.chat.ClickEvent`
- `net.minecraft.network.chat.Component`
- `net.minecraft.network.chat.HoverEvent`
- `net.minecraft.network.chat.Style`
- `net.neoforged.fml.loading.FMLPaths`

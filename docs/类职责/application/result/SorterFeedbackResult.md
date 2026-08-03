# SorterFeedbackResult
- **源码路径**: `src/main/java/com/knightcode/appliedinsight/application/result/SorterFeedbackResult.java`
- **包**: `com.knightcode.appliedstoragesorter.application.result`
- **类型**: `record`
- **所属层**: 应用服务层

## 职责
承载命令反馈结果。`lines` 为 `List<Component>`，支持带颜色/点击事件/悬浮提示的富文本输出。

## 边界检查
边界健康。该类型位于应用装配或游戏集成层，依赖 Minecraft `Component` 是合理的。

## 抽象检查
没有过度抽象；以值对象形式存在是合适的。

## 主要协作者
- `net.minecraft.network.chat.Component`

## 变更记录 (2026-05-24)
- **`lines` 类型变更**: `List<String>` → `List<Component>`
- 新增 `failure(String)` 便捷重载（自动转 `Component.literal`），减少调用方改动量
- 配合 `SorterComponentHelper` 支持可点击文件路径、带颜色 key=value 等富文本输出

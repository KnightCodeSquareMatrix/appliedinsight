# SorterProfileBindingService
- **源码路径**: `src/main/java/com/knightcode/appliedinsight/application/SorterProfileBindingService.java`
- **包**: `com.knightcode.appliedstoragesorter.application`
- **类型**: `class`
- **所属层**: 应用服务层

## 职责
负责列出全局 profile、绑定当前网络到 profile，以及展示当前绑定结果。

## 边界检查
边界健康。绑定逻辑没有泄漏到命令类或 planner。

## 抽象检查
没有过度抽象。

## 主要协作者
- `com.knightcode.appliedstoragesorter.Config`
- `com.knightcode.appliedstoragesorter.ae2.Ae2ControllerTargetResolver`
- `com.knightcode.appliedstoragesorter.application.result.SorterFeedbackResult`
- `com.knightcode.appliedstoragesorter.rule.route.RoutingProfileRepository`
- `net.minecraft.commands.CommandSourceStack`

## 维护备注
- 当前实现简洁，优先保持单一职责，不要为了未来猜想提前拆分。

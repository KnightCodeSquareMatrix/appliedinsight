# SorterPlanService
- **源码路径**: `src/main/java/com/knightcode/appliedinsight/application/SorterPlanService.java`
- **包**: `com.knightcode.appliedstoragesorter.application`
- **类型**: `class`
- **所属层**: 应用服务层

## 职责
主应用服务：负责读取网络绑定 profile、生成 live plan，并按需触发 zone 搬运和详细日志。

## 边界检查
边界总体健康。它承担的是用例编排，而不是底层细节。

## 抽象检查
没有过度抽象，但已经是当前最“宽”的应用服务。

## 主要协作者
- `com.knightcode.appliedstoragesorter.Config`
- `com.knightcode.appliedstoragesorter.ae2.Ae2ControllerTargetResolver`
- `com.knightcode.appliedstoragesorter.ae2.scan.Ae2DriveScanner`
- `com.knightcode.appliedstoragesorter.ae2.zone.Ae2ZoneMoveExecutor`
- `com.knightcode.appliedstoragesorter.ae2.zone.LiveZoneAllocationPlanner`
- `com.knightcode.appliedstoragesorter.ae2.zone.RuntimeTopology`
- `com.knightcode.appliedstoragesorter.ae2.zone.RuntimeZoneRegistryBuilder`
- `com.knightcode.appliedstoragesorter.application.SorterComponentHelper`
- `com.knightcode.appliedstoragesorter.application.result.SorterFeedbackResult`
- `com.knightcode.appliedstoragesorter.logging.SorterPlanFileLogger`
- `com.knightcode.appliedstoragesorter.plan.ItemZoneAssignment`
- `com.knightcode.appliedstoragesorter.plan.ZoneAllocationPlan`
- `com.knightcode.appliedstoragesorter.rule.route.RoutingProfileRepository`
- `net.minecraft.commands.CommandSourceStack`
- `net.minecraft.network.chat.Component`

## 维护备注
- 目前同时负责 binding/profile 读取、plan、move、摘要组织和日志落盘；如果命令继续扩展，可以考虑拆成更小的 use-case helper。

# SorterDumpService
- **源码路径**: `src/main/java/com/knightcode/appliedinsight/application/SorterDumpService.java`
- **包**: `com.knightcode.appliedstoragesorter.application`
- **类型**: `class`
- **所属层**: 应用服务层

## 职责
命令用例服务：校验命令上下文、解析 grid、扫描网络、导出 dump、记录日志并返回反馈。

## 边界检查
边界健康。它是应用服务层，依赖 MC/AE2 和日志是合理的。

## 抽象检查
没有过度抽象。

## 主要协作者
- `com.knightcode.appliedstoragesorter.Config`
- `com.knightcode.appliedstoragesorter.ae2.Ae2ControllerTargetResolver`
- `com.knightcode.appliedstoragesorter.ae2.dump.SorterNetworkDumpWriter`
- `com.knightcode.appliedstoragesorter.ae2.scan.Ae2DriveScanner`
- `com.knightcode.appliedstoragesorter.application.SorterComponentHelper`
- `com.knightcode.appliedstoragesorter.application.result.SorterFeedbackResult`
- `com.knightcode.appliedstoragesorter.logging.SorterFileLogger`
- `net.minecraft.commands.CommandSourceStack`

## 维护备注
- 当前实现简洁，优先保持单一职责，不要为了未来猜想提前拆分。

# SorterCommands
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/command/SorterCommands.java`
- **包**: `com.knightcode.appliedstoragesorter.command`
- **类型**: `class`
- **所属层**: 命令入口层

## 职责
注册 `/sorter` 命令树，并把 brigadier 请求转发到应用服务。

## 边界检查
边界健康。命令类没有掺入业务细节。

## 抽象检查
没有过度抽象；`merge` 与 `me` 两条命令线在这里被清楚拆开了。

## 主要协作者
- `com.knightcode.appliedstoragesorter.application.SorterDumpService`
- `com.knightcode.appliedstoragesorter.application.SorterMergeService`
- `com.knightcode.appliedstoragesorter.application.SorterPlanService`
- `com.knightcode.appliedstoragesorter.application.SorterProfileBindingService`
- `com.knightcode.appliedstoragesorter.application.result.SorterFeedbackResult`
- `com.mojang.brigadier.Command`
- `com.mojang.brigadier.arguments.IntegerArgumentType`
- `com.mojang.brigadier.context.CommandContext`
- `net.minecraft.commands.CommandSourceStack`
- `net.minecraft.commands.Commands`
- `net.minecraft.network.chat.Component`
- `net.neoforged.neoforge.event.RegisterCommandsEvent`

## 维护备注
- 当前命令树已经明确：`/sorter merge` 负责快速合并，`/sorter me ...` 负责 dump/profile/plan 主线。

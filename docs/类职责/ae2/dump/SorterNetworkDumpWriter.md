# SorterNetworkDumpWriter
- **源码路径**: `src/main/java/com/knightcode/appliedinsight/ae2/dump/SorterNetworkDumpWriter.java`
- **包**: `com.knightcode.appliedstoragesorter.ae2.dump`
- **类型**: `class`
- **所属层**: AE2 集成层

## 职责
负责 AE2 dump 导出过程中的结果对象或写出逻辑。

## 边界检查
边界健康。该类型明确属于 AE2 集成层，依赖 Minecraft/AE2 API 是合理的，并未反向污染规则层。

## 抽象检查
整体没有过度抽象，职责保持在可理解范围内。

## 主要协作者
- `com.google.gson.Gson`
- `com.google.gson.GsonBuilder`
- `com.knightcode.appliedstoragesorter.ae2.Ae2ControllerTargetResolver`
- `com.knightcode.appliedstoragesorter.ae2.Ae2GridTargetResult`
- `com.knightcode.appliedstoragesorter.ae2.scan.Ae2DriveScanSummary`
- `com.knightcode.appliedstoragesorter.ae2.scan.DriveMachineAccessor`
- `appeng.api.stacks.AEItemKey`
- `net.minecraft.commands.CommandSourceStack`
- `net.minecraft.core.registries.BuiltInRegistries`
- `net.minecraft.nbt.Tag`
- `net.minecraft.tags.TagKey`
- `net.minecraft.world.item.ItemStack`
- 其余依赖省略 2 项，以源码为准。

## 维护备注
- 当前实现简洁，优先保持单一职责，不要为了未来猜想提前拆分。

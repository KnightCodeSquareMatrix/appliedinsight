# Ae2ControllerTargetResolver
- **源码路径**: `src/main/java/com/knightcode/appliedinsight/ae2/Ae2ControllerTargetResolver.java`
- **包**: `com.knightcode.appliedstoragesorter.ae2`
- **类型**: `class`
- **所属层**: AE2 集成层

## 职责
把命令源转换成 AE2 网络目标：判定玩家是否正看向 controller，并解析出 grid、controller 位置和维度信息。

## 边界检查
边界健康。它是典型的 AE2/Minecraft 集成适配器，没有侵入规则层。

## 抽象检查
没有过度抽象；resolveGridTarget/resolve 两个入口分别服务不同调用方。

## 主要协作者
- `com.knightcode.appliedstoragesorter.ae2.scan.Ae2DriveScanner`
- `appeng.blockentity.networking.ControllerBlockEntity`
- `net.minecraft.commands.CommandSourceStack`
- `net.minecraft.core.BlockPos`
- `net.minecraft.core.registries.BuiltInRegistries`
- `net.minecraft.world.level.block.entity.BlockEntity`
- `net.minecraft.world.phys.BlockHitResult`
- `net.minecraft.world.phys.HitResult`

## 维护备注
- resolve 与 resolveGridTarget 有部分重复结果组装；如果调用面继续扩大，可考虑统一到一个结果模型。

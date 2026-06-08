# SorterMoveOperation
- **源码路径**: `src/main/java/com/knightcode/appliedinsight/ae2/sort/SorterMoveOperation.java`
- **包**: `com.knightcode.appliedstoragesorter.ae2.sort`
- **类型**: `class`
- **所属层**: AE2 集成层

## 职责
封装一组已规划好的底层搬运，并负责真正执行 extract -> insert -> rollback，同时产出逐条 move 的执行结果明细。
执行完成后根据 `Config.ENERGY_COST_ENABLED` 决定是否计算并附加电量消耗估算。

## 边界检查
边界健康。它不参与 route/zone 决策，只做执行原语。

## 抽象检查
抽象适中。内部 ExecutableMove 让外部不必暴露太多执行细节。

## 主要协作者
- `appeng.api.config.Actionable`
- `appeng.api.networking.security.IActionHost`
- `appeng.api.networking.security.IActionSource`
- `appeng.api.storage.MEStorage`
- `com.knightcode.appliedstoragesorter.Config` (读取 `ENERGY_COST_ENABLED`)
- `com.knightcode.appliedstoragesorter.ae2.sort.EnergyCostCalculator` (执行后计量)

## `execute()` 执行流程

```
1. 循环执行 extract → insert → rollback（原有逻辑）
2. 构建 SorterMoveExecutionResult（energyCost = null）
3. if (Config.ENERGY_COST_ENABLED.get())
     energyCost = EnergyCostCalculator.estimate(result)
     result = result.withEnergyCost(energyCost)
4. return result
```

## 维护备注
- 当前实现简洁，优先保持单一职责，不要为了未来猜想提前拆分。
- 电量消耗为**执行后计量**（post-execution accounting），非规划时预估。
- 两条能力线（merge / planAndMove）共享同一执行引擎，自动受益于电量消耗功能。
- 电量消耗默认关闭（`energyCostEnabled = false`），向后兼容。

## 近期变更 (2026-05-25)
- **`execute()` 末尾新增电量计算** — 构建完基础 `SorterMoveExecutionResult` 后，根据 Config 开关调用 `EnergyCostCalculator.estimate()` 并附加上 `energyCost` 字段

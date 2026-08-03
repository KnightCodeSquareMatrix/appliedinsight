# EnergyCostCalculator

| 维度 | 值 |
|------|-----|
| **包** | `com.knightcode.appliedstoragesorter.ae2.sort` |
| **类型** | `public final class`（工具类，私有构造） |
| **职责** | 从 `SorterMoveExecutionResult` 计算电量消耗 |
| **定位** | 纯静态方法，无实例状态 |
| **能力线** | 公用（merge 和 zone 两条线共享） |

## 核心方法

### `estimate(SorterMoveExecutionResult result)`

- **签名**: `public static EnergyCostEstimate estimate(SorterMoveExecutionResult result)`
- **输入**: 搬运执行结果（非 null）
- **输出**: 电量消耗估算值
- **边缘情况**: 如果 `result.moveResults()` 为空，返回全零估算

### `log2PlusOne(long n)`

- **签名**: `static long log2PlusOne(long n)`（包级可见，便于测试）
- **计算**: `floor(log₂(1 + n))`
- **实现**: 使用 `Long.SIZE - 1 - Long.numberOfLeadingZeros(x)` 位运算，无浮点
- **边缘情况**: `n <= 0` 返回 0；`n == Long.MAX_VALUE` 时防止溢出

### `manhattanDistance(BlockPos a, BlockPos b)`

- **签名**: `static int manhattanDistance(BlockPos a, BlockPos b)`（包级可见，便于测试）
- **公式**: `|x₁-x₂| + |y₁-y₂| + |z₁-z₂|`

## 计算公式（方案 B：对数饱和）

```
totalCost = floor(α × N + β × log₂(1 + A) + γ × log₂(1 + T) + δ × log₂(1 + avgDist))
```

| 符号 | Config 键 | 默认值 | 说明 |
|------|-----------|--------|------|
| α | `energyCostBaseFee` | 2.0 | 基础搬运费 |
| β | `energyCostAmountCoeff` | 20.0 | 数量饱和系数 |
| γ | `energyCostTypeCoeff` | 30.0 | 种类饱和系数 |
| δ | `energyCostDistanceCoeff` | 15.0 | 距离饱和系数 |

## 设计约束

1. **纯静态方法** — 无实例状态，线程安全
2. **无浮点运算** — `log₂` 全部用位运算实现
3. **不依赖 `rule/` 层** — 属 AE2 集成层
4. **执行后计量** — 非规划时预估

## 依赖关系

- `SorterMoveExecutionResult` — 输入
- `PlannedMove` — 通过 `MoveResult` 访问
- `DriveCellReference` — 获取 `drivePos` 计算曼哈顿距离
- `BlockPos` — 坐标距离计算（Minecraft 类，AE2 集成层合法依赖）
- `Config` — 读取系数

## 相关文档

- [EnergyCostEstimate](./EnergyCostEstimate.md)
- [SorterMoveExecutionResult](./SorterMoveExecutionResult.md)
- [SorterMoveOperation](./SorterMoveOperation.md)
- 分析报告: [`docs/analysis/自动路由和merge电量消耗模型分析报告.md`](../../analysis/自动路由和merge电量消耗模型分析报告.md)

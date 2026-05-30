# Analyzer 阶段定位草案

## 1. 定位
系统中应正式存在 `Analyzer`。

它的长期定位是：
- 解释运行时能力
- 承接诊断
- 承接可用性判断
- 承接 merge / placement 等语义解释
- 成为前端主要消费的分析出口之一

也就是说，`Analyzer` 未来潜力很大。

---

## 2. 现阶段范围
**现阶段不要把 `Analyzer` 做复杂。**

当前只做一个最小问题：

> **能不能把这批物品搬进去。**

也就是只回答搬运可行性，不展开完整网络分析体系。

---

## 3. 现阶段最小职责
现阶段 `Analyzer` 只需要解释：
- target zone 是否可用
- target zone 是否可写
- target zone 是否允许 merge / placement
- 这批 source items 是否都有可去处
- 是否存在明显无法搬入的物品

---

## 4. 与 Executor 的边界
### Analyzer 负责
- 判断能不能搬
- 解释为什么不能搬
- 给出最小阻塞原因

### Executor 负责
- 真正执行搬运
- 不承担复杂解释权
- 现阶段以“能搬就搬”为原则

---

## 5. 与 Topology / Zone 的边界
### RuntimeTopology 提供
- 网络级运行时事实
- zone / machine / cell 的可用信息

### RuntimeZone 提供
- zone 内部可用性
- merge / placement / writable 等能力事实
- zone 内部诊断

### Analyzer 消费这些事实并做解释
但现阶段只做“能不能搬进去”的解释。

---

## 6. 下一阶段再扩展的内容
以下能力留到后续阶段：
- 整个网络分析
- zone 级完整分析
- bus / machine 级分析
- 前端深度消费模型
- 更丰富的诊断树与解释树

---

## 7. 一句话结论
> **Analyzer 应被正式承认为未来的大能力层，但现阶段只落地为“搬运可行性分析器”。**

# 执行链重构 Cline 提示词

请对当前项目执行一轮**非常克制的执行链重构**，目标聚焦在：

> **让 `Ae2ZoneMoveExecutor` 从混合执行大类，收敛成可读的执行编排器。**

这次只做执行链，不扩 analyzer，不扩 topology，不扩 logger 框架，不做 `RuntimeZone` 大升级。

---

## 一、当前已确定的设计前提

请严格以以下前提为准，不要自行改题：

### 1. 运行时主干已经收口

- `RuntimeTopology` 已经是运行时统一主入口
- `RuntimeZoneRegistry` 已被移除
- 不要再回头处理 topology 主干问题

### 2. logger 已经定性

- logger 一律视为基础设施
- 技术运行日志可用 `SLF4J + Lombok @Slf4j` 或现有风格，但**这不是本轮主任务**
- plan / merge / dump / analysis 报告型输出也不是本轮重点
- 不要为了 logger 顺手改执行核心结构

### 3. 执行语义已经确定

本轮执行链必须遵守以下语义：

- 总目标是：**尽量全搬**
- 实际执行是：**best-effort**
- 能搬多少搬多少
- 如果搬不完，不是立刻失败退出，而是尽量继续
- “满了 / 容量不足 / 无法全部接纳”属于 **error** 级事实
- “部分搬运成功”属于 **warning** 级事实
- 不要求为了“全局最优”做复杂调度
- 采用**更直接、更笨但可解释的执行方式**

### 4. planner / zone / executor 的边界已经确定

- 入口层负责确定来源语义，例如：
  - 外部进入
  - 内部整理
- planner 负责：
  - 基于当前 ME 网络绑定的 `zone / filter / router json`
  - 为某个 item 决定目标 zone / 优先级 / 分配意图
- `RuntimeZone` 负责：
  - 当前是否可接纳
  - 在当前约束下最多能接多少
  - 当前阻塞原因
- executor 负责：
  - 消费 planner 决策与 zone 裁决
  - 尽量执行搬运
  - 能搬多少搬多少
  - 汇总结果

**不要让 executor 重新实现 planner 规则判断。**  
**不要让 executor 自己替 zone 思考太多局部接纳规则。**

### 5. 关于“聚合”的明确约束

这里请不要把任务理解成“按物理 slot 细抠搬运”。

我们读的是 AE2 存储视图，天然就是聚合的。  
所以这次可以按 **item 聚合量** 的现实语义执行。

但要注意：

- 可以按 `item -> amount` 的聚合视角工作
- 不要为了“理论最优”再引入更高一层复杂抽象账本
- 不要构造一个难以回退、难以解释的全局最优搬运计划
- 保持执行方式直接、保守、易解释

一句话：

> **可以按 item 聚合量执行，但不要做额外复杂的执行层超级聚合计划。**

---

## 二、本轮重构目标

请只围绕 `Ae2ZoneMoveExecutor` 做**小范围、局部完整**的执行链收敛。

目标是让代码清楚分出这些职责：

1. 执行入口 / orchestration
2. assignment / target lookup
3. 单个 item 搬运尝试
4. 统计 / sample / debug 收集
5. 最终结果组装

请优先让主流程“显形”，让阅读者一眼能看出执行步骤。

---

## 三、推荐重构方式

### 第一优先级：让主流程显形

先把 `Ae2ZoneMoveExecutor` 主体重构成清楚的阶段流，例如接近这种语义：

1. 构建执行上下文
2. 准备 assignment / target lookup
3. 遍历待处理 item
4. 对单个 item 执行 move attempt
5. 收集统计 / sample / debug
6. 组装 detailed result / debug report / final result

可以用：

- 私有方法
- 小型辅助类
- 小型 collector / context 对象

但不要上复杂框架。

### 第二优先级：抽出“单次搬运尝试”单元

请把“对单个 item 进行一次实际搬运尝试”的逻辑收成一个清晰单元。

这个单元应该能表达至少这些内容：

- 请求搬运多少
- 实际搬运多少
- 是否完全成功
- 是否部分成功
- 为什么没搬满
- 是否因为 zone 满 / 容量不足 / placement 失败 / extract / insert 问题而受阻
- 是否需要 rollback（如果现有语义中有）

它可以是：

- 一个小型 helper 类
- 或一个小型 result object + method 组合

但不要引入大规模抽象体系。

### 第三优先级：把统计逻辑从行为逻辑里拔薄

重构后请尽量做到：

- attempt 先返回“这次发生了什么”
- stats / debug collector 再根据结果累计
- 不要在执行细节里到处散落：
  - `skipped++`
  - `sampleMessages.add(...)`
  - `warningCount++`
  - `errorCount++`

可以保留必要的局部收集，但整体结构应明显更清楚。

---

## 四、本轮不要做的事

请严格不要越界到下面这些方向：

1. 不扩展 `RuntimeTopology`
2. 不恢复任何 `RuntimeZoneRegistry` 相关模型
3. 不正式扩展 analyzer 体系
4. 不把 `RuntimeZone` 直接升级成主动合成 / 主动扩容调度器
5. 不做 plan / route / zone 语义大改
6. 不把 logger 改造成大平台
7. 不顺手大规模重构别的 service
8. 不为了形式漂亮而引入一堆接口、strategy、processor chain、pipeline 框架

---

## 五、你在实现时应保持的设计判断

### 1. executor 是编排器，不是第二个 planner

executor 只负责执行，不要把 rule / route 再实现一遍。

### 2. executor 也不是第二个 zone

zone-local 的“当前最多能接多少”“为什么接不了”这类判断，如果已有合适能力，优先消费现有能力；如果没有，也不要在本轮借题发挥做大规模 zone 设计升级。

### 3. 结果结构要服务于执行链清晰

如果需要增加一个很轻量的小结果对象来承载单次 attempt 结果，是允许的。  
但前提是：

- 轻量
- 只服务本次执行链收敛
- 不扩成通用框架

### 4. 局部完整优先

如果只能做一半，就优先：

- 让主流程显形
- 抽出单次 attempt
- 让统计从主行为中降噪

不要试图一次做完“执行层的终极结构”。

---

## 六、建议审计范围

请优先审计并修改：

- `src/main/java/com/knightcode/appliedinsight/ae2/zone/Ae2ZoneMoveExecutor.java`

必要时可轻量波及：

- 当前与它直接耦合的 debug / result 小对象
- 仅当确有必要的小型 helper / collector

但请保持波及面尽可能小。

---

## 七、预期产出

请最终汇报：

1. `Ae2ZoneMoveExecutor` 原先承担了哪些职责
2. 你如何把主流程显形
3. 是否抽出了单次 move / item attempt 单元
4. 统计 / debug 逻辑是如何降噪的
5. 实际修改了哪些文件
6. 哪些本来可以继续做，但你刻意没有做，以保持本轮克制
7. 编译验证结果

---

## 八、验证要求

修改完成后请务必运行：

```bash
./gradlew clean compileJava --no-daemon --no-configuration-cache
```

如果有旧缓存干扰，以上命令应作为最终验证口径。

---

## 九、完成标准

本轮完成后，应至少满足：

- `Ae2ZoneMoveExecutor` 主体比之前明显更像执行编排器
- 主流程结构一眼可读
- 单次 item 搬运尝试有清楚边界
- 统计 / debug 不再严重污染主行为逻辑
- 不引入新的大抽象负担
- `compileJava` 通过

请严格按这个范围执行，不要扩题。

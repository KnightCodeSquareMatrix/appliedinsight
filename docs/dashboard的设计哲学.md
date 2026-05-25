# Dashboard 的设计哲学

## 1. 定位

本项目的 dashboard 不是一个附属可视化页面，也不是单纯把 dump JSON 画成表格。

它的目标是：

> **成为 AE2 网络的宏观观测、解释、规划与调控主界面。**

因此，dashboard 的定位不是“前端皮肤”，而是本项目产品体验的主场之一。

---

## 2. 核心哲学

### 2.1 先给用户结果，再让用户修正

dashboard 应尽量做到 **开箱即用**。

用户打开 dashboard 时，系统应先尽力提供：

- 可读的 overview
- 尽量合理的 storage ranking
- 尽量不被特殊节点污染的统计结果
- 明确的 health / warnings

而不是先要求用户完成一系列配置，才能看到可用结果。

一句话：

> **先给价值，再让用户按需介入。**

---

### 2.2 宏观调控优先，不做纯明细堆砌

dashboard 的第一职责不是“把所有数据列出来”，而是帮助用户：

- 一眼看全局
- 快速定位异常
- 理解系统结构
- 决定下一步治理动作

因此，dashboard 应优先强调：

- overall summary
- storage topology
- dominant storage
- fragmentation
- health flags
- semantic review candidates

而不是一开始就把所有 item 明细堆给用户。

---

### 2.3 解释性比黑盒自动化更重要

本项目不是要做一个完全黑盒的自动系统，而是要做一个：

> **会解释自己为什么这么判断的治理系统。**

因此，dashboard 中所有高价值判断都应尽量可解释，例如：

- 为什么某节点被认为疑似无限容器
- 为什么某 health flag 被触发
- 为什么某 storage 被视为 dominant
- 为什么某 item 被视为 fragmented

系统可以聪明，但不能神秘。

---

### 2.4 角色视角高于坐标视角

宏观调控关心的不是单纯的：

- `x,y,z slot=0`

而是：

- 这个节点是什么角色
- 它是否在承担符合预期的职责
- 它是否污染了网络统计

所以 dashboard 长期应从“位置列表”逐步过渡到“角色化存储节点视图”。

这也是为什么需要推进：

- `locationId`
- 节点语义层
- semantic review
- world save override

---

## 3. 开箱即用原则

### 3.1 高度可疑节点默认按无限容器假定处理

对于高置信度疑似无限节点，dashboard 应遵循：

> **默认按 infinite-like 节点处理，以避免首屏统计被严重污染。**

原因：

- 在大型整合包中，极高数量级节点往往确实是 creative / unlimited 类容器
- 如果先按普通节点处理，overview 很容易失真
- 用户打开 dashboard 时，应先看到“基本正确”的宏观结果

这是一种 **smart default**，而不是强制事实。

---

### 3.2 默认假定不等于最终事实

系统默认按无限处理，并不意味着它已经被正式确认。

需要区分：

- `suspected`
- `assumed`
- `confirmed`
- `rejected`

即：

- 系统可以默认采用某个高置信度假设
- 但仍要明确告诉用户这是“当前假定”
- 用户必须能够轻松把它标记为“不是无限”

一句话：

> **默认生效是为了好用，不是为了越权。**

---

### 3.3 用户的主要操作应是“纠偏”，而不是“先配置后可用”

对于高置信度节点，dashboard 的理想体验不是：

1. 先进入专门配置页面
2. 手动设置角色
3. 回来重新看结果

而应该是：

1. 直接看到已默认处理后的 dashboard
2. 在原地看到系统提示
3. 若系统判断不对，再一键改成“非无限”

也就是说：

> **dashboard 优先让用户做低成本纠偏，而不是高成本建模。**

---

## 4. 节点语义是基础设施，不是附属标签

本项目长期目标是宏观调控 AE2 网络，因此必须逐步建立：

> **Storage Node Semantics（存储节点语义层）**

节点不应只被理解为：

- 一个坐标
- 一个槽位
- 一个数量

还应被理解为：

- 一个角色
- 一个职责
- 一个在治理系统中的行为单元

第一阶段从“疑似无限容器”起步，后续可扩展到：

- external buffer
- temporary staging
- overflow sink
- restricted storage
- main storage
- archive storage

详见：`docs/存储节点语义层.md`

---

## 5. Dashboard 与后端的分工

### 5.1 后端负责

- 扫描 live network
- 生成 analyzer snapshot
- 产出 suspected / assumed candidates
- 提供 health / ranking / summary 所需数据
- 保存 world save override

### 5.2 Dashboard 负责

- 展示 overview / storage / item / health
- 呈现系统判断的理由
- 让用户原地审核与纠偏
- 把用户决策写回游戏端

### 5.3 原则

- analyze 可以大胆提出建议
- dashboard 可以默认采用高置信度假定
- 最终可持久化事实必须允许用户覆盖

---

## 6. 当前最重要的交互原则

### 6.1 不要求用户先学配置才能获得价值

### 6.2 不让系统偷偷替用户做不可撤销决定

### 6.3 高置信度判断应原地可撤销

### 6.4 复杂配置页是补充入口，不是主入口

这意味着：

- 语义确认首先应该出现在分析视图中
- 专门语义配置界面未来可以有，但不能成为唯一入口

---

## 7. 一句话结论

本项目 dashboard 的设计哲学可以概括为：

> **先用智能默认给出一个可用、可读、尽量正确的 AE2 宏观视图；再让用户以最低摩擦完成解释、纠偏与治理。**

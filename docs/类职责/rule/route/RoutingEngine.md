# RoutingEngine
- **源码路径**: `src/main/java/com/knightcode/appliedinsight/rule/route/RoutingEngine.java`
- **包**: `com.knightcode.appliedstoragesorter.rule.route`
- **类型**: `class`
- **所属层**: 纯规则模型层

## 职责
规则层核心执行器：按优先级遍历 routeRules，结合 filter 产出 RoutingDecision 和可选解释。

## 边界检查
边界非常健康。它只做 item -> zone 决策，不触碰 runtime cell/AE2。

## 抽象检查
抽象恰当，是当前规则系统的稳定核心。

## 主要协作者
- `com.knightcode.appliedstoragesorter.rule.filter.ItemFilterMatcher`
- `com.knightcode.appliedstoragesorter.rule.filter.ItemMatchContext`

## 维护备注
- 当前实现简洁，优先保持单一职责，不要为了未来猜想提前拆分。

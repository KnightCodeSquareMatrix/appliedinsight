# RoutingProfileJsonCodec
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/rule/route/RoutingProfileJsonCodec.java`
- **包**: `com.knightcode.appliedstoragesorter.rule.route`
- **类型**: `class`
- **所属层**: 纯规则模型层

## 职责
RoutingProfile 的 JSON 编解码器，同时兼容旧版 filter matchMode/conditions 结构。

## 边界检查
边界健康，属于规则层序列化组件。

## 抽象检查
没有过度抽象，但文件承担了 schema 兼容、读写和示例入口三部分细节。

## 主要协作者
- `com.google.gson.Gson`
- `com.google.gson.GsonBuilder`
- `com.google.gson.JsonArray`
- `com.google.gson.JsonElement`
- `com.google.gson.JsonObject`
- `com.google.gson.JsonParser`
- `com.knightcode.appliedstoragesorter.rule.filter.FilterCombinator`
- `com.knightcode.appliedstoragesorter.rule.filter.FilterCondition`
- `com.knightcode.appliedstoragesorter.rule.filter.FilterExpression`
- `com.knightcode.appliedstoragesorter.rule.filter.FilterField`
- `com.knightcode.appliedstoragesorter.rule.filter.FilterGroup`
- `com.knightcode.appliedstoragesorter.rule.filter.FilterMatchMode`
- 其余依赖省略 4 项，以源码为准。

## 维护备注
- 如果 JSON schema 继续复杂化，可以考虑把 filter/route/zone 的读写拆成私有 helper 类。

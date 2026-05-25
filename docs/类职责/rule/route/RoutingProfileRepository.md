# RoutingProfileRepository
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/rule/route/RoutingProfileRepository.java`
- **包**: `com.knightcode.appliedstoragesorter.rule.route`
- **类型**: `class`
- **所属层**: 纯规则模型层

## 职责
从 config/appliedstoragesorter/profiles 目录加载、列出和按编号/ID 查找 profile。

## 边界检查
边界健康。文件系统访问集中在仓储层。

## 抽象检查
没有过度抽象。

## 主要协作者
- `net.neoforged.fml.loading.FMLPaths`

## 维护备注
- 当前遇到损坏 profile 会静默跳过，适合工具场景，但也意味着排错信息较弱。

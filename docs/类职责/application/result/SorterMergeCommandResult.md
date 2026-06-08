# SorterMergeCommandResult
- **源码路径**: `src/main/java/com/knightcode/appliedinsight/application/result/SorterMergeCommandResult.java`
- **包**: `com.knightcode.appliedstoragesorter.application.result`
- **类型**: `record`
- **所属层**: 应用服务层

## 职责
作为轻量结果对象承载 `/sorter merge` 的执行结果快照。

## 边界检查
边界健康。该类型位于应用服务返回层，不承担额外业务。

## 抽象检查
没有过度抽象；以 record 表达结果数据非常合适。

## 主要协作者
- 主要依赖为 JDK 或同文件内部成员。

## 维护备注
- 字段已经足够表达 merge 结果，保持轻量即可。

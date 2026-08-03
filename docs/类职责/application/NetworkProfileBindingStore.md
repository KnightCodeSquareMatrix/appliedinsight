# NetworkProfileBindingStore
- **源码路径**: `src/main/java/com/knightcode/appliedinsight/application/NetworkProfileBindingStore.java`
- **包**: `com.knightcode.appliedstoragesorter.application`
- **类型**: `class`
- **所属层**: 应用服务层

## 职责
把“ME 网络标识 -> profileId”持久化到世界存档 data 目录。

## 边界检查
边界健康。它是持久化基础设施，不参与 route/scan/execute。

## 抽象检查
不过度抽象，使用一个文件存储已足够。

## 主要协作者
- `com.google.gson.Gson`
- `com.google.gson.GsonBuilder`
- `com.google.gson.JsonElement`
- `com.google.gson.JsonObject`
- `com.google.gson.JsonParser`
- `net.minecraft.server.MinecraftServer`
- `net.minecraft.world.level.storage.LevelResource`

## 维护备注
- 如果未来需要删除/列出绑定，可在这里继续扩充 API，而不是把 JSON 细节散落到 service 层。

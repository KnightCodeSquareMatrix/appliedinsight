# ClientStorageAnalysisCache

| 维度 | 说明 |
|------|------|
| **包路径** | `client/ClientStorageAnalysisCache.java` |
| **定位** | 客户端存储分析缓存便捷入口 |
| **职责** | 委托给 `SorterAnalysisPayloadHandler` 的缓存，提供 `getRawJson()`、`hasData()`、`clear()` 静态方法供游戏内 Screen 使用 |
| **关键方法** | `getRawJson()` → `String`；`hasData()` → `boolean`；`clear()` |
| **依赖** | `SorterAnalysisPayloadHandler` |
| **被谁使用** | 客户端 Screen（如 `DigitalAssetVaultScreen`） |

# （已移除）DigitalAssetManagementCardItem

> **v0.9.0-beta 起已从游戏中注销。** 管理卡（Zone Card）机制已由 **路由 Profile 文件绑定**（`config/appliedinsight/profiles/` + `/sorter me bindProfile`）替代。
>
> 仓库内可能仍保留历史源码/纹理/Recipe 文档供参考；**不要**在新功能中重新引入该物品，除非 ADR 明确恢复。

## 历史职责（归档）
携带 zone stamp 数据的可持久化物品，用于旧版 DAV 输入层。

## 替代方案
- 服主/开发者：编辑 `config/appliedinsight/profiles/*.json`
- 绑定网络：`/sorter me bindProfile <n>`
- 玩家 GUI：SorterCommandBlock「以绑定配置整理」

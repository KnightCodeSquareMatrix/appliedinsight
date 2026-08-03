# SorterCommandBlock

> **最后更新**: 2026-05-30

| 维度 | 说明 |
|------|------|
| **包路径** | `block/SorterCommandBlock.java` |
| **定位** | 命令执行方块声明 |
| **职责** | 声明 AE2 网络命令执行方块：可放置、可朝向、右键打开 GUI、移除时销毁 AE2 网格节点 |
| **关键特性** | 继承 `HorizontalDirectionalBlock` + 实现 `EntityBlock`；使用 `MapCodec` 序列化 |
| **依赖** | `SorterCommandBlockEntity` |
| **被谁使用** | `SorterBlocks`（注册） |
| **资源资产** | blockstate: [`sorter_command_block.json`](src/main/resources/assets/appliedinsight/blockstates/sorter_command_block.json)（复用 `ae2:block/drive/drive_front`）<br>block model: [`sorter_command_block.json`](src/main/resources/assets/appliedinsight/models/block/sorter_command_block.json)<br>item model: [`sorter_command_block.json`](src/main/resources/assets/appliedinsight/models/item/sorter_command_block.json)<br>旧版 GUI 纹理: [`sorter_command_block.png`](src/main/resources/assets/appliedinsight/textures/gui/sorter_command_block.png)（176×145，当前宽屏终端实现未使用） |

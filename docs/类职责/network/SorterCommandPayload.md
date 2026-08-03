# SorterCommandPayload

| 维度 | 说明 |
|------|------|
| **包路径** | `network/SorterCommandPayload.java` |
| **定位** | 方块 GUI 命令请求网络包（record） |
| **职责** | 从客户端向服务端发送命令请求，携带 `buttonId` 标识要执行的命令 |
| **常量** | `CMD_ME_DUMP=1`、`CMD_ME_STORAGE_DUMP=2`、`CMD_ME_PLAN=3`、`CMD_ME_PLAN_AND_MOVE=4`、`CMD_MERGE=5` |
| **字段** | `buttonId` (`int`) |
| **编解码** | 使用 `ByteBufCodecs.VAR_INT` |
| **通道** | `appliedinsight:sorter_command` |
| **依赖** | NeoForge `CustomPacketPayload`、`StreamCodec` |
| **被谁使用** | `SorterCommandBlockScreen`（发送）、`SorterCommandPayloadHandler`（接收） |

# DavCellLedger
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/ae2/dav/cell/DavCellLedger.java`
- **包**: `com.knightcode.appliedstoragesorter.ae2.dav.cell`
- **类型**: `record`
- **所属层**: AE2 集成层

## 职责
DAV Cell 喂入账本：`absorbedCellCount`、`absorbedBytes`、`absorbedTypeCapacity`。`totalBytes()` / `totalTypeCapacity()` 含 `DavCellConstants` 内置基础容量。

## 主要协作者
- `com.knightcode.appliedstoragesorter.ae2.dav.cell.DavCellConstants`

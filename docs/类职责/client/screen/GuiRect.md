# GuiRect
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/client/screen/GuiRect.java`
- **包**: `com.knightcode.appliedstoragesorter.client.screen`
- **类型**: `record`（package-private）
- **所属层**: 客户端展示层

## 职责
屏幕布局用的 immutable 矩形工具：`x/y/width/height`，提供几何运算避免 SorterCommandBlock 布局魔法数散落。

## 主要方法
- `right()` / `bottom()` — 边界
- `inset(h)` / `inset(h, v)` — 内缩
- `move(dx, dy)` — 平移
- `topBand(h)` / `bottomBand(h)` — 取上/下条带
- `contains(px, py)` —  hit test

## 主要协作者
- `com.knightcode.appliedstoragesorter.client.screen.SorterTerminalLayout`

## 维护备注
- 仅 SCB 终端布局使用；其他 Screen 若需类似工具可复用或扩展为 public。

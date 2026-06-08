# Smart Bus UV 调试（阶段 0）

## 面编号与游戏内视角（已验证）

模型 JSON 的 north/south 与「你站哪里看」不同。**线缆贴在 south（数字 2）** 时：

| 你看到的方向 | 模型面 | 调试数字 |
|-------------|--------|----------|
| 正对面（朝线缆） | south | **2** |
| 背后（网外） | north | **1** |
| 右侧 | east | **3** |
| 左侧 | west | **4** |
| 头顶 | up | **5**（不旋转） |
| 脚下抬头 | down | **6** |

这与 Block Model 规范一致，**不是 NeoForge 渲染错误**。之前文档写「north=网外」容易误解为「你放下来第一眼看到 1」——实际上第一眼通常是 **2**（线缆侧）。

## 启用

编辑 `config/appliedinsight-common.toml`：

```toml
# OFF | LETTERBOX | SQUARE
debugSmartBusUv = "LETTERBOX"
```

重启客户端或重进世界。Smart Bus **所有模式**均显示调试图（与 mode 无关）。

生成贴图：

```bash
python tools/gen_smart_bus_textures.py --debug
```

## 贴图规则（每面一张）

| 文件 | 模型面 | 中心数字 | 四角（纹理像素） |
|------|--------|----------|------------------|
| `smart_bus_uv_debug_1.png` | **north**（朝外/网外） | **1** | TL红 TR绿 BL蓝 BR黄 |
| `smart_bus_uv_debug_2.png` | **south**（线缆侧） | **2** | 同上 |
| `smart_bus_uv_debug_3.png` | **east** | **3** | 同上 |
| `smart_bus_uv_debug_4.png` | **west** | **4** | 同上 |
| `smart_bus_uv_debug_5.png` | **up** | **5** | 同上 |
| `smart_bus_uv_debug_6.png` | **down** | **6** | 同上 |

其余区域：灰 `#808080`。每面中心仅 **一个** 数字，便于直接辨认「这是哪一面」。

## 两套 UV 对照

| 配置 | 模型 | UV |
|------|------|-----|
| `LETTERBOX` | `smart_bus_debug.json` | **全幅拉伸** `[0,0,16,16]`，无 rotation |
| `SQUARE` | `smart_bus_debug_square.json` | 居中 `[4,4,12,12]`，无 rotation |

## 如何读图

放置于 AE2 线缆（canonical：south 贴线）后：

1. 看 **数字**：你正在看的是模型里的哪一面（1=north … 6=down）。
2. 看 **四角颜色**：红/绿/蓝/黄 应对应纹理 TL/TR/BL/BR；若颜色错位 → UV 轴或 AE2 `QuadRotator` 问题。
3. **LETTERBOX（拉伸）**：非正方形侧面（6×12）会把数字拉变形——预期行为，用于确认采样范围。
4. **SQUARE**：若数字清晰、角点正确，而 LETTERBOX 乱 → 正式贴图应改 UV，不是 NeoForge bug。

## 结束调试

1. `debugSmartBusUv = "OFF"`
2. 阶段 1 修正式 UV 后删除 debug 资源

# AE2 (Applied Energistics 2) 仓库结构索引

> 仓库地址: https://github.com/AppliedEnergistics/Applied-Energistics-2
> 克隆位置: `/data/ae2-repo`
> 当前版本: `19.2.17` (对应 Minecraft 1.21.1 / NeoForge)
> 克隆时间: 2026-05-28

---

## 一、仓库根目录文档

| 文件 | 大小 | 说明 |
|------|------|------|
| [`README.md`](/data/ae2-repo/README.md) | 9.3 KB | 项目简介 |
| [`API.md`](/data/ae2-repo/API.md) | 20.1 KB | **Addon 开发 API 文档** — 核心参考 |
| [`guidebook.md`](/data/ae2-repo/guidebook.md) | 12.9 KB | 游戏内指南书概述 |
| [`ponderjs.md`](/data/ae2-repo/ponderjs.md) | 9.2 KB | PonderJS 场景定义文档 |
| [`LICENSE`](/data/ae2-repo/LICENSE) | 42.8 KB | 许可证 |
| [`build.gradle`](/data/ae2-repo/build.gradle) | 13.2 KB | Gradle 构建配置 |
| [`settings.gradle`](/data/ae2-repo/settings.gradle) | 2.4 KB | Gradle 项目设置 |
| [`gradle.properties`](/data/ae2-repo/gradle.properties) | 2.2 KB | Gradle 属性配置 |

---

## 二、Guidebook 指南文档 (`guidebook/`)

### 2.1 入门与索引

| 文件 | 说明 |
|------|------|
| [`index.md`](/data/ae2-repo/guidebook/index.md) | 指南书首页/目录 |
| [`getting-started.md`](/data/ae2-repo/guidebook/getting-started.md) | **新手入门指南** |
| [`tips-and-tricks.md`](/data/ae2-repo/guidebook/tips-and-tricks.md) | 技巧与窍门 |

### 2.2 AE2 核心机制 (`ae2-mechanics/`)

| 文件 | 说明 |
|------|------|
| [`ae2-mechanics-index.md`](/data/ae2-repo/guidebook/ae2-mechanics/ae2-mechanics-index.md) | 机制索引页 |
| [`autocrafting.md`](/data/ae2-repo/guidebook/ae2-mechanics/autocrafting.md) | **自动合成** |
| [`bytes-and-types.md`](/data/ae2-repo/guidebook/ae2-mechanics/bytes-and-types.md) | **存储字节与类型** |
| [`cable-subparts.md`](/data/ae2-repo/guidebook/ae2-mechanics/cable-subparts.md) | 线缆子部件 |
| [`certus-growth.md`](/data/ae2-repo/guidebook/ae2-mechanics/certus-growth.md) | Certus 石英生长 |
| [`channels.md`](/data/ae2-repo/guidebook/ae2-mechanics/channels.md) | **频道机制** |
| [`devices.md`](/data/ae2-repo/guidebook/ae2-mechanics/devices.md) | 设备 |
| [`energy.md`](/data/ae2-repo/guidebook/ae2-mechanics/energy.md) | **能量系统** |
| [`import-export-storage.md`](/data/ae2-repo/guidebook/ae2-mechanics/import-export-storage.md) | **导入/导出/存储** |
| [`me-network-connections.md`](/data/ae2-repo/guidebook/ae2-mechanics/me-network-connections.md) | **ME 网络连接** |
| [`meteorites.md`](/data/ae2-repo/guidebook/ae2-mechanics/meteorites.md) | 陨石 |
| [`p2p-tunnels.md`](/data/ae2-repo/guidebook/ae2-mechanics/p2p-tunnels.md) | **P2P 隧道** |
| [`quantum-bridge.md`](/data/ae2-repo/guidebook/ae2-mechanics/quantum-bridge.md) | **量子桥** |
| [`spatial-io.md`](/data/ae2-repo/guidebook/ae2-mechanics/spatial-io.md) | **空间 IO** |
| [`subnetworks.md`](/data/ae2-repo/guidebook/ae2-mechanics/subnetworks.md) | **子网络** |

### 2.3 示例搭建 (`example-setups/`)

| 文件 | 说明 |
|------|------|
| [`example-setups-index.md`](/data/ae2-repo/guidebook/example-setups/example-setups-index.md) | 示例索引 |
| [`main-network.md`](/data/ae2-repo/guidebook/example-setups/main-network.md) | 主网络搭建 |
| [`interface-autostocking.md`](/data/ae2-repo/guidebook/example-setups/interface-autostocking.md) | 接口自动补货 |
| [`level-emitter-autostocking.md`](/data/ae2-repo/guidebook/example-setups/level-emitter-autostocking.md) | 电平发射器自动补货 |
| [`pipe-subnet.md`](/data/ae2-repo/guidebook/example-setups/pipe-subnet.md) | 管道子网 |
| [`specialized-local-storage.md`](/data/ae2-repo/guidebook/example-setups/specialized-local-storage.md) | 专用本地存储 |
| [`storage-types.md`](/data/ae2-repo/guidebook/example-setups/storage-types.md) | 存储类型 |
| [`processor-automation.md`](/data/ae2-repo/guidebook/example-setups/processor-automation.md) | 处理器自动化 |
| [`recursive-crafting-setup.md`](/data/ae2-repo/guidebook/example-setups/recursive-crafting-setup.md) | 递归合成 |
| [`furnace-automation.md`](/data/ae2-repo/guidebook/example-setups/furnace-automation.md) | 熔炉自动化 |
| [`charger-automation.md`](/data/ae2-repo/guidebook/example-setups/charger-automation.md) | 充电器自动化 |
| [`cell-dumper-filler.md`](/data/ae2-repo/guidebook/example-setups/cell-dumper-filler.md) | 存储元件倾倒/填充 |
| [`bucket-emptier.md`](/data/ae2-repo/guidebook/example-setups/bucket-emptier.md) | 桶清空器 |
| [`bucket-filler.md`](/data/ae2-repo/guidebook/example-setups/bucket-filler.md) | 桶填充器 |
| [`ore-fortuner.md`](/data/ae2-repo/guidebook/example-setups/ore-fortuner.md) | 矿石时运 |
| [`regulated-cobble-gen.md`](/data/ae2-repo/guidebook/example-setups/regulated-cobble-gen.md) | 调节圆石生成 |
| [`simple-certus-farm.md`](/data/ae2-repo/guidebook/example-setups/simple-certus-farm.md) | 简单 Certus 农场 |
| [`semiauto-certus-farm.md`](/data/ae2-repo/guidebook/example-setups/semiauto-certus-farm.md) | 半自动 Certus 农场 |
| [`advanced-certus-farm.md`](/data/ae2-repo/guidebook/example-setups/advanced-certus-farm.md) | 高级 Certus 农场 |
| [`amethyst-farm.md`](/data/ae2-repo/guidebook/example-setups/amethyst-farm.md) | 紫水晶农场 |
| [`throw-in-water-automation.md`](/data/ae2-repo/guidebook/example-setups/throw-in-water-automation.md) | 扔水里自动化 |

### 2.4 物品/方块/机器 (`items-blocks-machines/`)

| 文件 | 说明 |
|------|------|
| [`items-blocks-machines-index.md`](/data/ae2-repo/guidebook/items-blocks-machines/items-blocks-machines-index.md) | 索引页 |
| [`controller.md`](/data/ae2-repo/guidebook/items-blocks-machines/controller.md) | **ME 控制器** |
| [`drive.md`](/data/ae2-repo/guidebook/items-blocks-machines/drive.md) | **ME 驱动器** |
| [`chest.md`](/data/ae2-repo/guidebook/items-blocks-machines/chest.md) | **ME 箱子** |
| [`interface.md`](/data/ae2-repo/guidebook/items-blocks-machines/interface.md) | **ME 接口** |
| [`storage_cells.md`](/data/ae2-repo/guidebook/items-blocks-machines/storage_cells.md) | **存储元件** |
| [`terminals.md`](/data/ae2-repo/guidebook/items-blocks-machines/terminals.md) | **终端** |
| [`cables.md`](/data/ae2-repo/guidebook/items-blocks-machines/cables.md) | **线缆** |
| [`import_bus.md`](/data/ae2-repo/guidebook/items-blocks-machines/import_bus.md) | **输入总线** |
| [`export_bus.md`](/data/ae2-repo/guidebook/items-blocks-machines/export_bus.md) | **输出总线** |
| [`storage_bus.md`](/data/ae2-repo/guidebook/items-blocks-machines/storage_bus.md) | **存储总线** |
| [`formation_plane.md`](/data/ae2-repo/guidebook/items-blocks-machines/formation_plane.md) | 成型面板 |
| [`annihilation_plane.md`](/data/ae2-repo/guidebook/items-blocks-machines/annihilation_plane.md) | 湮灭面板 |
| [`level_emitter.md`](/data/ae2-repo/guidebook/items-blocks-machines/level_emitter.md) | 电平发射器 |
| [`energy_acceptor.md`](/data/ae2-repo/guidebook/items-blocks-machines/energy_acceptor.md) | 能量接收器 |
| [`energy_cells.md`](/data/ae2-repo/guidebook/items-blocks-machines/energy_cells.md) | 能量元件 |
| [`molecular_assembler.md`](/data/ae2-repo/guidebook/items-blocks-machines/molecular_assembler.md) | 分子装配室 |
| [`pattern_provider.md`](/data/ae2-repo/guidebook/items-blocks-machines/pattern_provider.md) | 样板供应器 |
| [`patterns.md`](/data/ae2-repo/guidebook/items-blocks-machines/patterns.md) | 样板 |
| [`crafting_cpu_multiblock.md`](/data/ae2-repo/guidebook/items-blocks-machines/crafting_cpu_multiblock.md) | 合成 CPU 多方块 |
| [`inscriber.md`](/data/ae2-repo/guidebook/items-blocks-machines/inscriber.md) | 压印器 |
| [`charger.md`](/data/ae2-repo/guidebook/items-blocks-machines/charger.md) | 充电器 |
| [`io_port.md`](/data/ae2-repo/guidebook/items-blocks-machines/io_port.md) | IO 端口 |
| [`cell_workbench.md`](/data/ae2-repo/guidebook/items-blocks-machines/cell_workbench.md) | 存储元件工作台 |
| [`condenser.md`](/data/ae2-repo/guidebook/items-blocks-machines/condenser.md) | 物质冷凝器 |
| [`vibration_chamber.md`](/data/ae2-repo/guidebook/items-blocks-machines/vibration_chamber.md) | 振动室 |
| [`quantum_bridge.md`](/data/ae2-repo/guidebook/items-blocks-machines/quantum_bridge.md) | 量子桥 |
| [`p2p_tunnels.md`](/data/ae2-repo/guidebook/items-blocks-machines/p2p_tunnels.md) | P2P 隧道 |
| [`wireless_access_point.md`](/data/ae2-repo/guidebook/items-blocks-machines/wireless_access_point.md) | 无线接入点 |
| [`wireless_terminals.md`](/data/ae2-repo/guidebook/items-blocks-machines/wireless_terminals.md) | 无线终端 |
| [`spatial_anchor.md`](/data/ae2-repo/guidebook/items-blocks-machines/spatial_anchor.md) | 空间锚 |
| [`spatial_cells.md`](/data/ae2-repo/guidebook/items-blocks-machines/spatial_cells.md) | 空间元件 |
| [`spatial_io_port.md`](/data/ae2-repo/guidebook/items-blocks-machines/spatial_io_port.md) | 空间 IO 端口 |
| [`spatial_pylon.md`](/data/ae2-repo/guidebook/items-blocks-machines/spatial_pylon.md) | 空间塔 |
| [`facades.md`](/data/ae2-repo/guidebook/items-blocks-machines/facades.md) | 伪装板 |
| [`upgrade_cards.md`](/data/ae2-repo/guidebook/items-blocks-machines/upgrade_cards.md) | 升级卡 |
| [`memory_card.md`](/data/ae2-repo/guidebook/items-blocks-machines/memory_card.md) | 内存卡 |
| [`network_tool.md`](/data/ae2-repo/guidebook/items-blocks-machines/network_tool.md) | 网络工具 |
| [`entropy_manipulator.md`](/data/ae2-repo/guidebook/items-blocks-machines/entropy_manipulator.md) | 熵操作器 |
| [`matter_cannon.md`](/data/ae2-repo/guidebook/items-blocks-machines/matter_cannon.md) | 物质炮 |
| [`color_applicator.md`](/data/ae2-repo/guidebook/items-blocks-machines/color_applicator.md) | 颜色涂抹器 |
| [`charged_staff.md`](/data/ae2-repo/guidebook/items-blocks-machines/charged_staff.md) | 充能法杖 |
| [`crank.md`](/data/ae2-repo/guidebook/items-blocks-machines/crank.md) | 曲柄 |
| [`cutting_knives.md`](/data/ae2-repo/guidebook/items-blocks-machines/cutting_knives.md) | 切割刀 |
| [`certus_quartz_crystal.md`](/data/ae2-repo/guidebook/items-blocks-machines/certus_quartz_crystal.md) | Certus 石英水晶 |
| [`certus_quartz_crystal_charged.md`](/data/ae2-repo/guidebook/items-blocks-machines/certus_quartz_crystal_charged.md) | 充能 Certus 石英水晶 |
| [`certus_quartz_dust.md`](/data/ae2-repo/guidebook/items-blocks-machines/certus_quartz_dust.md) | Certus 石英粉 |
| [`fluix_crystal.md`](/data/ae2-repo/guidebook/items-blocks-machines/fluix_crystal.md) | Fluix 水晶 |
| [`fluix_dust.md`](/data/ae2-repo/guidebook/items-blocks-machines/fluix_dust.md) | Fluix 粉 |
| [`fluix_pearl.md`](/data/ae2-repo/guidebook/items-blocks-machines/fluix_pearl.md) | Fluix 珍珠 |
| [`fluix_block.md`](/data/ae2-repo/guidebook/items-blocks-machines/fluix_block.md) | Fluix 块 |
| [`fluix_tools.md`](/data/ae2-repo/guidebook/items-blocks-machines/fluix_tools.md) | Fluix 工具 |
| [`fluix_upgrade_smithing_template.md`](/data/ae2-repo/guidebook/items-blocks-machines/fluix_upgrade_smithing_template.md) | Fluix 升级锻造模板 |
| [`processors.md`](/data/ae2-repo/guidebook/items-blocks-machines/processors.md) | 处理器 |
| [`presses.md`](/data/ae2-repo/guidebook/items-blocks-machines/presses.md) | 压印模板 |
| [`singularities.md`](/data/ae2-repo/guidebook/items-blocks-machines/singularities.md) | 奇点 |
| [`matter_ball.md`](/data/ae2-repo/guidebook/items-blocks-machines/matter_ball.md) | 物质球 |
| [`sky_stone.md`](/data/ae2-repo/guidebook/items-blocks-machines/sky_stone.md) | 天空石 |
| [`sky_dust.md`](/data/ae2-repo/guidebook/items-blocks-machines/sky_dust.md) | 天空粉 |
| [`sky_stone_tank.md`](/data/ae2-repo/guidebook/items-blocks-machines/sky_stone_tank.md) | 天空石储罐 |
| [`ender_dust.md`](/data/ae2-repo/guidebook/items-blocks-machines/ender_dust.md) | 末影粉 |
| [`quartz_glass.md`](/data/ae2-repo/guidebook/items-blocks-machines/quartz_glass.md) | 石英玻璃 |
| [`quartz_fiber.md`](/data/ae2-repo/guidebook/items-blocks-machines/quartz_fiber.md) | 石英纤维 |
| [`quartz_fixture.md`](/data/ae2-repo/guidebook/items-blocks-machines/quartz_fixture.md) | 石英固定器 |
| [`quartz_block.md`](/data/ae2-repo/guidebook/items-blocks-machines/quartz_block.md) | 石英块 |
| [`quartz_tools.md`](/data/ae2-repo/guidebook/items-blocks-machines/quartz_tools.md) | 石英工具 |
| [`decorative_certus.md`](/data/ae2-repo/guidebook/items-blocks-machines/decorative_certus.md) | Certus 装饰块 |
| [`decorative_fluix.md`](/data/ae2-repo/guidebook/items-blocks-machines/decorative_fluix.md) | Fluix 装饰块 |
| [`decorative_sky_stone.md`](/data/ae2-repo/guidebook/items-blocks-machines/decorative_sky_stone.md) | 天空石装饰块 |
| [`illuminated_panels.md`](/data/ae2-repo/guidebook/items-blocks-machines/illuminated_panels.md) | 照明面板 |
| [`monitors.md`](/data/ae2-repo/guidebook/items-blocks-machines/monitors.md) | 显示器 |
| [`growth_accelerator.md`](/data/ae2-repo/guidebook/items-blocks-machines/growth_accelerator.md) | 生长加速器 |
| [`budding_certus.md`](/data/ae2-repo/guidebook/items-blocks-machines/budding_certus.md) | 含晶 Certus 块 |
| [`crystal_resonance_generator.md`](/data/ae2-repo/guidebook/items-blocks-machines/crystal_resonance_generator.md) | 水晶共振发生器 |
| [`cable_anchor.md`](/data/ae2-repo/guidebook/items-blocks-machines/cable_anchor.md) | 线缆锚 |
| [`toggle_bus.md`](/data/ae2-repo/guidebook/items-blocks-machines/toggle_bus.md) | 开关总线 |
| [`formation_annihilation_core.md`](/data/ae2-repo/guidebook/items-blocks-machines/formation_annihilation_core.md) | 成型/湮灭核心 |
| [`view_cell.md`](/data/ae2-repo/guidebook/items-blocks-machines/view_cell.md) | 查看单元 |
| [`paintballs.md`](/data/ae2-repo/guidebook/items-blocks-machines/paintballs.md) | 涂料球 |
| [`tiny_tnt.md`](/data/ae2-repo/guidebook/items-blocks-machines/tiny_tnt.md) | 微型 TNT |
| [`mysterious_cube.md`](/data/ae2-repo/guidebook/items-blocks-machines/mysterious_cube.md) | 神秘方块 |
| [`fluix_researcher.md`](/data/ae2-repo/guidebook/items-blocks-machines/fluix_researcher.md) | Fluix 研究者 |
| [`guide.md`](/data/ae2-repo/guidebook/items-blocks-machines/guide.md) | 指南书物品 |
| [`wireless_receiver.md`](/data/ae2-repo/guidebook/items-blocks-machines/wireless_receiver.md) | 无线接收器 |
| [`meteorite_compass.md`](/data/ae2-repo/guidebook/items-blocks-machines/meteorite_compass.md) | 陨石指南针 |
| [`wrench.md`](/data/ae2-repo/guidebook/items-blocks-machines/wrench.md) | 扳手 |

---

## 三、源代码结构 (`src/main/java/appeng/`)

### 3.1 API 层 (`api/`) — **Addon 开发核心**

```
api/
├── IAEAddonEntrypoint.java          # Addon 入口点接口
├── AECapabilities.java              # 能力注册
├── behaviors/                       # 容器行为策略
│   ├── ContainerItemStrategy.java   # 容器物品策略
│   ├── ExternalStorageStrategy.java # 外部存储策略
│   ├── StackExportStrategy.java     # 堆栈导出策略
│   ├── StackImportStrategy.java     # 堆栈导入策略
│   ├── PlacementStrategy.java       # 放置策略
│   └── PickupStrategy.java          # 拾取策略
├── client/
│   └── StorageCellModels.java       # 存储元件模型
├── components/
│   └── ExportedUpgrades.java        # 导出升级
├── config/                          # 配置枚举
│   ├── Settings.java                # 所有设置定义
│   ├── FuzzyMode.java               # 模糊匹配模式
│   ├── SortOrder.java               # 排序顺序
│   ├── SortDir.java                 # 排序方向
│   └── ...
├── crafting/                        # 合成 API
│   ├── IPatternDetails.java         # 样板详情接口
│   └── PatternDetailsHelper.java    # 样板详情工具
├── features/                        # 特性注册
│   ├── GridLinkables.java           # 网格可链接项
│   ├── Locatables.java              # 可定位对象
│   └── P2PTunnelAttunement.java     # P2P 隧道调谐
├── ids/                             # ID 常量
│   ├── AEBlockIds.java              # 方块 ID
│   ├── AEItemIds.java               # 物品 ID
│   ├── AEPartIds.java               # 部件 ID
│   ├── AEComponents.java            # 数据组件
│   └── AETags.java                  # 标签
├── implementations/
│   ├── blockentities/
│   │   └── IChestOrDrive.java       # 箱子/驱动器接口
│   └── items/
│       ├── IAECell.java             # 存储单元接口
│       └── IAEStorageCell.java      # 存储元件接口
├── networking/                       # 网络 API
│   ├── IGridConnection.java         # 网格连接
│   ├── IGridNode.java               # 网格节点
│   ├── IGridNodeListener.java       # 网格节点监听器
│   ├── IGridHelper.java             # 网格辅助
│   ├── GridServices.java            # 网格服务注册
│   └── IGridService.java            # 网格服务接口
├── orientation/                     # 方向系统
├── parts/                           # 部件 API
│   ├── IPart.java                   # 部件接口
│   ├── IPartHost.java               # 部件宿主
│   └── IPartItem.java               # 部件物品
├── stacks/                          # **存储键系统 — 核心**
│   ├── AEKey.java                   # 存储键基类
│   ├── AEItemKey.java               # 物品键
│   ├── AEFluidKey.java              # 流体键
│   ├── AEKeyType.java               # 键类型
│   ├── GenericStack.java            # 通用堆栈 (键+数量)
│   └── KeyCounter.java              # 键计数器
├── storage/                         # **存储 API — 核心**
│   ├── MEStorage.java               # ME 存储基类
│   ├── IStorageProvider.java        # 存储提供者
│   ├── IStorageMounts.java          # 存储挂载
│   ├── StorageCells.java            # 存储单元注册
│   ├── StorageHelper.java           # 存储工具
│   ├── AEKeyFilter.java             # 键过滤器
│   ├── ITerminalHost.java           # 终端宿主
│   └── cells/                       # 存储单元
│       ├── ICellHandler.java        # 单元处理器
│       ├── IBasicCellItem.java      # 基本单元物品
│       ├── StorageCell.java         # 存储单元
│       └── CellState.java           # 单元状态
└── upgrades/                        # 升级 API
    ├── IUpgradeInventory.java       # 升级库存
    ├── Upgrades.java                # 升级注册
    └── ...
```

### 3.2 ME 核心实现 (`me/`) — **网格与存储实现**

```
me/
├── Grid.java                        # 网格实现
├── GridNode.java                    # 网格节点实现
├── GridConnection.java              # 网格连接
├── ManagedGridNode.java             # 托管网格节点
├── InWorldGridNode.java             # 世界内网格节点
├── GridEventBus.java                # 网格事件总线
├── GridPropagator.java              # 网格传播器
├── GridSplitDetector.java           # 网格分裂检测器
├── cells/                           # 存储单元实现
│   ├── BasicCellHandler.java        # 基本单元处理器
│   ├── BasicCellInventory.java      # 基本单元库存
│   ├── CreativeCellHandler.java     # 创造单元处理器
│   └── CreativeCellInventory.java   # 创造单元库存
├── cluster/                         # 多方块集群
│   ├── IAECluster.java              # 集群接口
│   ├── IAEMultiBlock.java           # 多方块接口
│   └── implementations/
│       ├── CraftingCPUCluster.java  # 合成 CPU 集群
│       ├── QuantumCluster.java      # 量子集群
│       └── SpatialPylonCluster.java # 空间塔集群
├── energy/                          # 能量系统
│   ├── GridEnergyStorage.java       # 网格能量存储
│   ├── EnergyWatcher.java           # 能量监视器
│   └── EnergyThreshold.java         # 能量阈值
├── helpers/                         # 辅助类
│   ├── BaseActionSource.java        # 动作源基类
│   ├── MachineSource.java           # 机器源
│   ├── PlayerSource.java            # 玩家源
│   └── StackWatcher.java            # 堆栈监视器
├── pathfinding/                     # 路径寻找
│   ├── PathingCalculation.java      # 路径计算
│   ├── ControllerValidator.java     # 控制器验证
│   ├── ChannelFinalizer.java        # 频道终结
│   └── IPathItem.java               # 路径项接口
├── service/                         # 网格服务
│   ├── StorageService.java          # **存储服务**
│   ├── CraftingService.java         # 合成服务
│   ├── PathingService.java          # 路径服务
│   ├── EnergyService.java           # 能量服务
│   ├── P2PService.java              # P2P 服务
│   ├── StatisticsService.java       # 统计服务
│   └── SpatialPylonService.java     # 空间塔服务
└── storage/                         # 存储实现
    ├── NetworkStorage.java          # **网络存储**
    ├── MEInventoryHandler.java      # **ME 库存处理器**
    ├── CompositeStorage.java        # 复合存储
    ├── DriveWatcher.java            # 驱动器监视器
    ├── ExternalStorageFacade.java   # 外部存储外观
    └── ExternalInventoryCache.java  # 外部库存缓存
```

### 3.3 方块 (`block/`)

```
block/
├── BlockBase.java                   # 方块基类
├── crafting/                        # 合成方块
│   ├── CraftingBlockItem.java
│   ├── CraftingMonitorBlock.java
│   ├── CraftingStorageBlock.java
│   └── CraftingUnitBlock.java
├── misc/                            # 杂项方块
│   ├── ChargerBlock.java
│   ├── CondenserBlock.java
│   ├── InscriberBlock.java
│   ├── VibrationChamberBlock.java
│   └── LightDetectingFixtureBlock.java
├── networking/                      # 网络方块
│   ├── ControllerBlock.java         # **控制器方块**
│   ├── CableBusBlock.java           # 线缆总线方块
│   ├── EnergyAcceptorBlock.java
│   ├── EnergyCellBlock.java
│   ├── DenseEnergyCellBlock.java
│   └── WirelessAccessPointBlock.java
├── qnb/                             # 量子网络桥
│   ├── QuantumBridgeBlock.java
│   └── QuantumRingBlock.java
├── spatial/                         # 空间方块
│   ├── SpatialAnchorBlock.java
│   ├── SpatialPylonBlock.java
│   └── SpatialIOPortBlock.java
└── storage/                         # **存储方块**
    ├── DriveBlock.java              # **驱动器方块**
    ├── ChestBlock.java              # **ME 箱子方块**
    ├── SkyStoneTankBlock.java
    ├── IOPortBlock.java
    └── CellWorkbenchBlock.java
```

### 3.4 方块实体 (`blockentity/`)

```
blockentity/
├── crafting/
│   ├── CraftingBlockEntity.java
│   ├── CraftingMonitorBlockEntity.java
│   ├── CraftingStorageBlockEntity.java
│   └── CraftingUnitBlockEntity.java
├── grid/
│   ├── GridNodeBlockEntity.java     # 网格节点 BE 基类
│   └── InWorldGridNodeBlockEntity.java
├── misc/
│   ├── ChargerBlockEntity.java
│   ├── CondenserBlockEntity.java
│   ├── InscriberBlockEntity.java
│   └── VibrationChamberBlockEntity.java
├── networking/
│   ├── ControllerBlockEntity.java   # **控制器 BE**
│   ├── CableBusBlockEntity.java     # 线缆总线 BE
│   ├── EnergyAcceptorBlockEntity.java
│   ├── EnergyCellBlockEntity.java
│   └── WirelessAccessPointBlockEntity.java
├── qnb/
│   ├── QuantumBridgeBlockEntity.java
│   └── QuantumRingBlockEntity.java
├── spatial/
│   ├── SpatialAnchorBlockEntity.java
│   ├── SpatialPylonBlockEntity.java
│   └── SpatialIOPortBlockEntity.java
└── storage/                         # **存储 BE**
    ├── DriveBlockEntity.java        # **驱动器 BE**
    ├── ChestBlockEntity.java        # **ME 箱子 BE**
    ├── SkyStoneTankBlockEntity.java
    ├── IOPortBlockEntity.java
    └── CellWorkbenchBlockEntity.java
```

### 3.5 其他模块

| 包 | 说明 |
|----|------|
| `items/` | 物品实现（存储元件、工具、终端等） |
| `parts/` | 部件实现（总线、面板、隧道等） |
| `menu/` | GUI 菜单 |
| `client/` | 客户端渲染、模型、GUI 屏幕 |
| `crafting/` | 合成系统实现 |
| `helpers/` | 辅助工具 |
| `integration/` | 与其他模组的集成 |
| `recipes/` | 配方 |
| `worldgen/` | 世界生成（陨石等） |
| `datagen/` | 数据生成 |
| `facade/` | 伪装板 |
| `spatial/` | 空间 IO |
| `sounds/` | 声音 |
| `entity/` | 实体 |
| `debug/` | 调试工具 |
| `decorative/` | 装饰性方块 |
| `mixins/` | Mixin |
| `thirdparty/` | 第三方代码 |
| `util/` | 工具类 |
| `init/` | 初始化 |
| `hotkeys/` | 快捷键 |
| `hooks/` | 钩子 |
| `server/` | 服务器端 |

---

## 四、关键 API 速查

### 4.1 存储系统核心类

| 类 | 路径 | 说明 |
|----|------|------|
| `AEKey` | `appeng.api.stacks.AEKey` | 存储键基类，表示物品/流体的"类型" |
| `AEItemKey` | `appeng.api.stacks.AEItemKey` | 物品键 |
| `AEFluidKey` | `appeng.api.stacks.AEFluidKey` | 流体键 |
| `GenericStack` | `appeng.api.stacks.GenericStack` | 通用堆栈（键+数量） |
| `KeyCounter` | `appeng.api.stacks.KeyCounter` | 键计数器 |
| `MEStorage` | `appeng.api.storage.MEStorage` | ME 存储基类 |
| `IStorageProvider` | `appeng.api.storage.IStorageProvider` | 存储提供者接口 |
| `StorageCells` | `appeng.api.storage.StorageCells` | 存储单元注册 |
| `StorageHelper` | `appeng.api.storage.StorageHelper` | 存储工具方法 |
| `ICellHandler` | `appeng.api.storage.cells.ICellHandler` | 存储单元处理器 |
| `IBasicCellItem` | `appeng.api.storage.cells.IBasicCellItem` | 基本存储单元物品 |
| `StorageCell` |
| `StorageCell` | `appeng.api.storage.cells.StorageCell` | 存储单元接口 |
| `IChestOrDrive` | `appeng.api.implementations.blockentities.IChestOrDrive` | 箱子/驱动器通用接口 |
| `ITerminalHost` | `appeng.api.storage.ITerminalHost` | 终端宿主 |
| `IStorageMounts` | `appeng.api.storage.IStorageMounts` | 存储挂载接口 |
| `AEKeyFilter` | `appeng.api.storage.AEKeyFilter` | 键过滤器 |
| `AEKeyType` | `appeng.api.stacks.AEKeyType` | 键类型注册 |
| `AEKeyTypes` | `appeng.api.stacks.AEKeyTypes` | 键类型注册表 |

### 4.2 网格/网络核心类

| 类 | 路径 | 说明 |
|----|------|------|
| `IGrid` | `appeng.api.networking.IGrid` | 网格接口 |
| `IGridNode` | `appeng.api.networking.IGridNode` | 网格节点接口 |
| `IGridConnection` | `appeng.api.networking.IGridConnection` | 网格连接接口 |
| `IGridHelper` | `appeng.api.networking.IGridHelper` | 网格辅助工具 |
| `IGridService` | `appeng.api.networking.IGridService` | 网格服务接口 |
| `GridServices` | `appeng.api.networking.GridServices` | 网格服务注册表 |
| `ManagedGridNode` | `appeng.me.ManagedGridNode` | 托管网格节点（推荐使用） |
| `Grid` | `appeng.me.Grid` | 网格实现 |
| `GridNode` | `appeng.me.GridNode` | 网格节点实现 |

### 4.3 网络服务

| 服务类 | 路径 | 说明 |
|--------|------|------|
| `StorageService` | `appeng.me.service.StorageService` | **存储服务** — 管理所有挂载的存储 |
| `CraftingService` | `appeng.me.service.CraftingService` | 合成服务 |
| `PathingService` | `appeng.me.service.PathingService` | 路径服务（频道分配） |
| `EnergyService` | `appeng.me.service.EnergyService` | 能量服务 |
| `P2PService` | `appeng.me.service.P2PService` | P2P 隧道服务 |
| `StatisticsService` | `appeng.me.service.StatisticsService` | 统计服务 |
| `SpatialPylonService` | `appeng.me.service.SpatialPylonService` | 空间塔服务 |
| `TickManagerService` | `appeng.me.service.TickManagerService` | Tick 管理服务 |

### 4.4 配置/设置

| 类 | 路径 | 说明 |
|----|------|------|
| `Settings` | `appeng.api.config.Settings` | 所有可配置设置的注册表 |
| `Setting` | `appeng.api.config.Setting` | 单个设置定义 |
| `FuzzyMode` | `appeng.api.config.FuzzyMode` | 模糊匹配模式（PERCENT_25/50/75/99, IGNORE） |
| `SortOrder` | `appeng.api.config.SortOrder` | 排序顺序（NAME/COUNT/STATUS） |
| `SortDir` | `appeng.api.config.SortDir` | 排序方向（ASCENDING/DESCENDING） |
| `ViewItems` | `appeng.api.config.ViewItems` | 视图过滤（ALL/STORED/EXTRAS） |
| `AccessRestriction` | `appeng.api.config.AccessRestriction` | 访问限制（READ/WRITE/READ_WRITE） |
| `Actionable` | `appeng.api.config.Actionable` | 操作模式（SIMULATE/MODULATE） |
| `PowerMultiplier` | `appeng.api.config.PowerMultiplier` | 能量倍率 |
| `PowerUnit` | `appeng.api.config.PowerUnit` | 能量单位 |

### 4.5 ID 常量

| 类 | 路径 | 说明 |
|----|------|------|
| `AEBlockIds` | `appeng.api.ids.AEBlockIds` | 所有 AE2 方块的 ID |
| `AEItemIds` | `appeng.api.ids.AEItemIds` | 所有 AE2 物品的 ID |
| `AEPartIds` | `appeng.api.ids.AEPartIds` | 所有 AE2 部件的 ID |
| `AEComponents` | `appeng.api.ids.AEComponents` | 数据组件类型 |
| `AETags` | `appeng.api.ids.AETags` | 标签常量 |
| `AEConstants` | `appeng.api.ids.AEConstants` | 通用常量 |

---

## 五、与本项目 (Applied Energistics: Insight) 的关联

本项目 (`appliedinsight`) 作为 AE2 的 Addon，主要使用以下 AE2 API：

| 本项目类 | 使用的 AE2 API | 用途 |
|----------|---------------|------|
| `Ae2DriveScanner` | `IChestOrDrive`, `IStorageProvider`, `DriveBlockEntity` | 扫描驱动器中的存储单元 |
| `GridNetworkLocator` | `IGrid`, `IGridNode`, `ManagedGridNode` | 定位网格网络 |
| `Ae2ControllerTargetResolver` | `ControllerBlockEntity` | 解析控制器目标 |
| `Ae2StorageAnalyzer` | `MEStorage`, `StorageService`, `KeyCounter` | 分析存储内容 |
| `Ae2MoveAnalyzer` | `MEStorage`, `GenericStack` | 分析物品移动 |
| `CellCapacityInspector` | `ICellHandler`, `IBasicCellItem`, `StorageCell` | 检查存储单元容量 |
| `EnergyCostCalculator` | `GridEnergyStorage` | 计算能量消耗 |
| `SorterMeScanResult` | `AEKey`, `AEItemKey`, `GenericStack` | 扫描结果封装 |
| `StorageAnalyzerReport` | `AEKey`, `KeyCounter` | 存储分析报告 |

---

## 六、相关资源

- **GitHub 仓库**: https://github.com/AppliedEnergistics/Applied-Energistics-2
- **Javadoc (最新版)**: https://appliedenergistics.github.io/javadoc/
- **API 文档**: https://appliedenergistics.github.io/api.html
- **Discord**: https://discord.gg/GygKjjm
- **本地克隆**: `/data/ae2-repo`

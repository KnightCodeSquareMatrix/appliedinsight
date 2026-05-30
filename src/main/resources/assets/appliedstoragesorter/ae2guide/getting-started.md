---
navigation:
  title: 快速上手
  icon: sorter_command_block
  parent: index.md
  position: 10
---

# 快速上手

## 第一步：制作一个命令执行块

先做一个[Sorter Command Block](sorter-command-block.md)。把它放到你的 ME 网络旁边，确保它能连上网络（它会自动和相邻的线缆或方块连接）。

## 第二步：试试整理合并

右键打开命令执行块，点 **整理合并**。它会把网络里同种物品合并到一起。操作结束后，可以在游戏根目录下的 `dumps/appliedstoragesorter/` 里查看合并报告。

## 第三步（可选）：设置规则搬运

如果你希望把不同物品自动分到不同区域：

1. 做一个[数字资产库](digital-asset-vault.md)和一张**镀印区域管理卡**。
2. 把你想要分类的存储元件放入资产库。
3. 把镀印好的管理卡插入资产库。
4. 在资产库里给每个存储元件设定一个区域标签。
5. 回到命令执行块，先生成计划，再执行搬运。

每次搬运结束后，命令执行块会在聊天栏里显示摘要：搬了多少物品、花了多少 AE 能量。

## 查看日志

所有操作的详细日志可以在 `dumps/appliedstoragesorter/` 目录下找到。每个操作都有独立的 JSON 和文本报告。

# AI 模型编排约定：GPT Orchestrator + DeepSeek Worker

> 本文记录本仓库使用 Claude Code 时的本地协作约定。目标是让主对话保持强模型判断力，同时把便宜、局部、可验证的子任务交给 DeepSeek V4 Flash。
>
> 核心偏好：用户不喜欢 worker 聊着聊着开始替项目做实现。worker 应保持“只交付局部材料”的心智，不承担项目 owner / 架构师 / 实施者角色。

## 默认角色

- **Orchestrator**：主 Claude Code 会话，默认使用更强的 GPT-5.5 high / xhigh 中转模型。
  - 负责和用户交流、澄清需求、制定计划、风险判断、最终代码修改与验收。
  - 负责理解项目架构、维护边界，并审核 worker 输出；worker 输出不能直接等同于事实或最终方案。
- **Worker**：[`tools/ask_deepseek.py`](../../tools/ask_deepseek.py)，默认模型 `deepseek-v4-flash`。
  - 负责局部、便宜、低风险、可复查的子任务。
  - 不主动延展需求，不主动提出落地改项目，不扮演项目架构负责人。
  - 通过 `DEEPSEEK_API_KEY` 环境变量认证，不在仓库中保存密钥。

## 什么时候调用 DeepSeek worker

适合：

- 文档、日志、报错信息的摘要和分类。
- 单文件或小片段代码解释。
- 生成注释、说明文档、翻译、命名建议等草稿。
- 对局部 diff / 小函数给第二意见。
- 批量但浅层的提取任务，例如从多个文档片段中抽取命令列表。

不适合：

- 和用户进行主对话、需求裁剪、最终决策。
- 多文件架构设计或跨层重构。
- 主动把讨论推进成实现、改文件、重构或“我来帮你落地”。
- 对项目长期方向、职责边界、抽象层级做判断。
- 删除、覆盖、提交、推送等高风险动作的判断。
- 安全敏感内容、密钥、token、私有凭据、完整环境配置。
- 需要直接执行或验证的结论；worker 只能给建议，不能声称已验证。

## Worker 行为边界

DeepSeek worker 的输出应像“材料包”，而不是“接管项目的程序员”。

应当：

- 回答被问到的局部问题。
- 标注不确定性和缺失上下文。
- 提供可供 orchestrator 审核的摘要、候选项、风险点或小草稿。
- 在涉及实现时使用“可能方案 / 草案 / 需要主控复核”的语气。

不应当：

- 主动说“我将修改项目”“下一步我会实现”“已完成修改”。
- 因为看到代码片段就扩展到全项目重构建议。
- 绕过 orchestrator 直接定义需求、优先级或架构边界。
- 用模板化的执行清单替代对项目语义的理解。

## 隐私与上下文最小化

调用 worker 时遵循最小上下文原则：



1. 默认只发送完成子任务所需的片段。
2. 不发送 `.env`、API key、token、密码、私有 SSH 信息。
3. 不默认发送完整仓库、完整 diff 或大量无关文件。
4. 若需要发送完整单文件或较大 diff，orchestrator 应先判断是否必要；敏感文件需先询问用户。
5. worker 输出必须由 orchestrator 复核后才能用于代码修改或结论。

## 使用方法

先在本机环境中设置 DeepSeek API Key：

```bash
export DEEPSEEK_API_KEY="你的 DeepSeek API Key"
```

基本调用：

```bash
python3 tools/ask_deepseek.py --prompt "总结这段日志的主要错误"
```

通过 stdin 输入：

```bash
python3 tools/ask_deepseek.py --prompt "提取关键失败原因" < test_output/build.log
```

附加一个 UTF-8 文本文件：

```bash
python3 tools/ask_deepseek.py --prompt "解释这个文件的职责" --file src/main/java/example/Foo.java
```

只打印请求体、不调用 API，用于检查将发送什么：

```bash
python3 tools/ask_deepseek.py --dry-run --prompt "测试"
```

可选环境变量：

- `DEEPSEEK_API_KEY`：DeepSeek API Key，必需。
- `DEEPSEEK_MODEL`：覆盖默认模型，默认 `deepseek-v4-flash`。
- `DEEPSEEK_BASE_URL`：覆盖默认 OpenAI-compatible base URL，默认 `https://api.deepseek.com`。

## 用户可见进度约定

调用 worker 可能造成明显等待时，orchestrator 应避免长时间静默，但不承诺固定 ETA 或固定频率更新。

1. **调用前说明用途与边界**：简短说明让 worker 做什么、覆盖哪些文件或问题，以及其输出只作为候选材料，不是最终结论。
2. **给出保守等待说明**：若可能耗时较久，只说明正在等待外部 worker；耗时不确定时，说明会在有结果、异常或需要用户决策时更新。
3. **不制造进度**：等待明显变长时，可简短说明仍在等待；没有新材料时，不编造阶段性进展或准确完成时间。
4. **阶段性材料需标注状态**：若 worker 先返回了可用片段，可作为“未复核的初步材料”转述；不得写成已确认事实。
5. **如实披露失败与质量问题**：worker 报错、超时、输出质量不足或上下文不够时，应直接说明，并由 orchestrator 自行推进或询问用户是否继续等待。
6. **区分材料与结论**：对外表述必须区分“worker 提供的候选材料/线索”和“orchestrator 已复核的事实/方案”。
7. **编辑前复核主工作区**：任何基于 worker 材料的修改前，orchestrator 必须重新读取主工作区相关文件，确认当前内容、用户改动和架构边界后再编辑。

## Orchestrator 采用规则

1. 先判断任务是否真的需要 worker；简单任务直接处理。
2. 调用前过滤上下文，只传最小必要内容。
3. Prompt 应明确要求 worker 只输出材料，不要主动要求/承诺修改项目。
4. 把 worker 输出当作“草稿/第二意见/摘要”，不是事实源。
5. 对涉及代码的建议，orchestrator 需要重新读相关文件并自行决定是否修改。
6. 修改后仍由 orchestrator 运行合适的验证命令并如实报告结果。

一句话：**强模型负责方向和责任，便宜模型负责局部体力活；worker 不接管项目。**

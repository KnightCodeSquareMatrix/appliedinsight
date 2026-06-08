#!/usr/bin/env python3
from __future__ import annotations

import argparse
import json
import os
import sys
import urllib.error
import urllib.request

DEFAULT_BASE_URL = "https://api.deepseek.com"
DEFAULT_MODEL = "deepseek-v4-flash"

SYSTEM_PROMPT = """你是 DeepSeek V4 Flash worker。你的职责是处理局部、低风险、可验证的子任务。
请遵守：
- 只回答被交给你的局部问题，不主动把讨论推进成项目实现、改文件或重构。
- 不要扮演项目 owner、架构师或实施者；项目方向、架构边界和最终修改由 orchestrator 决定。
- 不要声称已经修改、运行或验证了任何文件/命令。
- 不要请求或复述密钥、token、密码等敏感信息。
- 如果输入不足，明确说明缺口，并给出可供 orchestrator 判断的下一步，而不是直接接管执行。
- 输出要结构化、简洁，便于 orchestrator 审核和整合。
"""


def read_prompt(args: argparse.Namespace) -> str:
    parts: list[str] = []
    if args.prompt:
        parts.append(args.prompt)
    if args.file:
        for path in args.file:
            with open(path, "r", encoding="utf-8") as f:
                parts.append(f"\n--- FILE: {path} ---\n{f.read()}")
    if not sys.stdin.isatty():
        stdin = sys.stdin.read()
        if stdin.strip():
            parts.append(stdin)
    prompt = "\n\n".join(parts).strip()
    if not prompt:
        raise SystemExit("未提供 prompt。用 --prompt、--file 或 stdin 输入内容。")
    return prompt


def build_payload(args: argparse.Namespace, prompt: str) -> dict:
    messages = []
    if args.system:
        messages.append({"role": "system", "content": args.system})
    elif not args.no_default_system:
        messages.append({"role": "system", "content": SYSTEM_PROMPT})
    messages.append({"role": "user", "content": prompt})
    return {
        "model": args.model,
        "messages": messages,
        "temperature": args.temperature,
        "max_tokens": args.max_tokens,
        "stream": False,
    }


def call_deepseek(base_url: str, api_key: str, payload: dict, timeout: int) -> dict:
    url = base_url.rstrip("/") + "/chat/completions"
    data = json.dumps(payload, ensure_ascii=False).encode("utf-8")
    request = urllib.request.Request(
        url,
        data=data,
        headers={
            "Authorization": f"Bearer {api_key}",
            "Content-Type": "application/json",
            "Accept": "application/json",
        },
        method="POST",
    )
    try:
        with urllib.request.urlopen(request, timeout=timeout) as response:
            return json.loads(response.read().decode("utf-8"))
    except urllib.error.HTTPError as exc:
        body = exc.read().decode("utf-8", errors="replace")
        raise SystemExit(f"DeepSeek API HTTP {exc.code}: {body}") from exc
    except urllib.error.URLError as exc:
        raise SystemExit(f"DeepSeek API 请求失败: {exc}") from exc


def extract_text(response: dict) -> str:
    try:
        return response["choices"][0]["message"]["content"]
    except (KeyError, IndexError, TypeError) as exc:
        raise SystemExit("DeepSeek API 返回格式异常:\n" + json.dumps(response, ensure_ascii=False, indent=2)) from exc


def main() -> None:
    parser = argparse.ArgumentParser(description="Call DeepSeek V4 Flash as a cheap local worker for Claude Code orchestration.")
    parser.add_argument("--prompt", "-p", help="Prompt text. If omitted, reads stdin and/or --file.")
    parser.add_argument("--file", "-f", action="append", default=[], help="UTF-8 text file to append to the prompt. Can be repeated.")
    parser.add_argument("--model", default=os.environ.get("DEEPSEEK_MODEL", DEFAULT_MODEL), help=f"Model name. Default: {DEFAULT_MODEL}")
    parser.add_argument("--base-url", default=os.environ.get("DEEPSEEK_BASE_URL", DEFAULT_BASE_URL), help=f"DeepSeek OpenAI-compatible base URL. Default: {DEFAULT_BASE_URL}")
    parser.add_argument("--api-key-env", default="DEEPSEEK_API_KEY", help="Environment variable holding the API key. Default: DEEPSEEK_API_KEY")
    parser.add_argument("--system", help="Override system prompt for this worker call.")
    parser.add_argument("--no-default-system", action="store_true", help="Do not send the built-in worker safety/system prompt.")
    parser.add_argument("--temperature", type=float, default=0.2, help="Sampling temperature. Default: 0.2")
    parser.add_argument("--max-tokens", type=int, default=2000, help="Maximum output tokens. Default: 2000")
    parser.add_argument("--timeout", type=int, default=60, help="Request timeout seconds. Default: 60")
    parser.add_argument("--json", action="store_true", help="Print full JSON response instead of assistant text.")
    parser.add_argument("--dry-run", action="store_true", help="Print request payload without calling the API.")
    args = parser.parse_args()

    prompt = read_prompt(args)
    payload = build_payload(args, prompt)

    if args.dry_run:
        print(json.dumps({"base_url": args.base_url, "payload": payload}, ensure_ascii=False, indent=2))
        return

    api_key = os.environ.get(args.api_key_env)
    if not api_key:
        raise SystemExit(f"未找到环境变量 {args.api_key_env}。请先设置 DeepSeek API Key。")

    response = call_deepseek(args.base_url, api_key, payload, args.timeout)
    if args.json:
        print(json.dumps(response, ensure_ascii=False, indent=2))
    else:
        print(extract_text(response).strip())


if __name__ == "__main__":
    main()

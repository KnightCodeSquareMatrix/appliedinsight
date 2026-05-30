#!/usr/bin/env python3
"""
Applied Storage Sorter — 自动同步 Mod JAR

在本机 Windows 上运行，通过 SSH 监听服务器上的 jar 文件变化，
有更新时自动下载到多个指定目录（如 PrismLauncher mods 文件夹）。

用法：
    # 首次使用：编辑 sync_jar_config.json 配置服务器信息和目标目录
    python3 tools/sync_jar.py

    # 指定配置文件
    python3 tools/sync_jar.py --config my_config.json

    # 指定 SSH 密钥
    python3 tools/sync_jar.py --key ~/.ssh/id_rsa

配置说明见 sync_jar_config.json。
"""

import argparse
import hashlib
import json
import os
import stat
import subprocess
import sys
import tempfile
import time
from datetime import datetime, timezone
from pathlib import Path


# ── 默认配置 ──────────────────────────────────────────────────────────────────
DEFAULT_CONFIG = {
    "server": {
        "host": "172.16.201.63",
        "port": 22,
        "user": "root",
        "key": None,  # 默认使用 ~/.ssh/id_rsa
        "remote_jar_path": "/data/appliedstoragesorter/tools/serve/appliedstoragesorter.jar"
    },
    "watch_interval": 5,  # 轮询间隔（秒）
    "destinations": [
        # 示例：PrismLauncher mods 目录
        # "C:/Users/YourName/AppData/Roaming/PrismLauncher/instances/1.21.1/minecraft/mods",
        # 示例：MultiMC mods 目录
        # "C:/Users/YourName/AppData/Roaming/MultiMC/instances/1.21.1/.minecraft/mods",
    ],
    "rename_to": "appliedstoragesorter.jar"  # 下载后重命名（None 则保持原名）
}


def get_default_config_path() -> Path:
    """获取默认配置文件路径（与脚本同目录）"""
    return Path(__file__).parent / "sync_jar_config.json"


def load_config(config_path: Path) -> dict:
    """加载配置文件，缺失字段用默认值填充"""
    if config_path.exists():
        with open(config_path, "r", encoding="utf-8") as f:
            user_config = json.load(f)
        # 合并默认值
        config = DEFAULT_CONFIG.copy()
        for key in config:
            if key in user_config:
                if isinstance(config[key], dict) and isinstance(user_config[key], dict):
                    config[key].update(user_config[key])
                else:
                    config[key] = user_config[key]
        return config
    else:
        # 创建默认配置文件
        with open(config_path, "w", encoding="utf-8") as f:
            json.dump(DEFAULT_CONFIG, f, indent=2, ensure_ascii=False)
        print(f"  ✗ 配置文件不存在，已创建默认配置: {config_path}")
        print(f"  请编辑该文件，填入服务器信息和目标目录后重新运行。")
        sys.exit(1)


def build_scp_command(config: dict, remote_path: str, local_path: str) -> list:
    """构建 scp 命令"""
    server = config["server"]
    cmd = ["scp"]
    if server["port"] != 22:
        cmd.extend(["-P", str(server["port"])])
    if server["key"]:
        cmd.extend(["-i", os.path.expanduser(server["key"])])
    cmd.append(f"{server['user']}@{server['host']}:{remote_path}")
    cmd.append(local_path)
    return cmd


def get_remote_file_md5(config: dict) -> str | None:
    """通过 SSH 获取远程文件的 MD5"""
    server = config["server"]
    remote_path = config["server"]["remote_jar_path"]
    
    ssh_cmd = ["ssh"]
    if server["port"] != 22:
        ssh_cmd.extend(["-p", str(server["port"])])
    if server["key"]:
        ssh_cmd.extend(["-i", os.path.expanduser(server["key"])])
    ssh_cmd.append(f"{server['user']}@{server['host']}")
    ssh_cmd.append(f"md5sum {remote_path} 2>/dev/null || echo 'NOT_FOUND'")
    
    try:
        result = subprocess.run(
            ssh_cmd,
            capture_output=True,
            text=True,
            timeout=10
        )
        output = result.stdout.strip()
        if output and output != "NOT_FOUND":
            return output.split()[0]  # md5sum 输出格式: "hash  filename"
        return None
    except (subprocess.TimeoutExpired, subprocess.CalledProcessError, FileNotFoundError):
        return None


def download_file(config: dict, local_path: str) -> bool:
    """通过 SCP 下载远程文件"""
    server = config["server"]
    remote_path = config["server"]["remote_jar_path"]
    
    scp_cmd = build_scp_command(config, remote_path, local_path)
    
    try:
        result = subprocess.run(scp_cmd, capture_output=True, text=True, timeout=30)
        if result.returncode == 0:
            return True
        else:
            print(f"  ✗ SCP 失败: {result.stderr.strip()}")
            return False
    except (subprocess.TimeoutExpired, subprocess.CalledProcessError, FileNotFoundError) as e:
        print(f"  ✗ SCP 异常: {e}")
        return False


def copy_to_destinations(temp_file: str, config: dict):
    """将下载的文件拷贝到所有目标目录"""
    rename = config.get("rename_to")
    dest_dir = Path(temp_file).parent
    
    for dest in config["destinations"]:
        dest_path = Path(dest)
        if not dest_path.exists():
            print(f"  ⚠ 目标目录不存在，跳过: {dest}")
            continue
        
        if rename:
            target = dest_path / rename
        else:
            target = dest_path / Path(temp_file).name
        
        try:
            import shutil
            shutil.copy2(temp_file, target)
            print(f"  ✓ 已同步到: {target}")
        except Exception as e:
            print(f"  ✗ 同步失败 {target}: {e}")


def main():
    parser = argparse.ArgumentParser(
        description="Applied Storage Sorter — 自动同步 Mod JAR"
    )
    parser.add_argument(
        "--config", "-c",
        type=str,
        default=None,
        help="配置文件路径（默认: tools/sync_jar_config.json）"
    )
    parser.add_argument(
        "--key", "-k",
        type=str,
        default=None,
        help="SSH 密钥路径（覆盖配置文件中的 key）"
    )
    parser.add_argument(
        "--once", "-o",
        action="store_true",
        help="只同步一次，不持续监听"
    )
    args = parser.parse_args()
    
    # 加载配置
    config_path = Path(args.config) if args.config else get_default_config_path()
    config = load_config(config_path)
    
    if args.key:
        config["server"]["key"] = args.key
    
    # 验证配置
    if not config["destinations"]:
        print("  ✗ 未配置目标目录 (destinations)")
        print(f"  请编辑 {config_path} 添加目标目录后重新运行。")
        sys.exit(1)
    
    server = config["server"]
    remote_path = server["remote_jar_path"]
    
    print(f"  Applied Storage Sorter — JAR 同步器")
    print(f"  ─────────────────────────────────────")
    print(f"  服务器: {server['user']}@{server['host']}:{server['port']}")
    print(f"  远程文件: {remote_path}")
    print(f"  目标目录 ({len(config['destinations'])} 个):")
    for d in config["destinations"]:
        print(f"    - {d}")
    print(f"  轮询间隔: {config['watch_interval']} 秒")
    print()
    
    last_md5 = None
    first_run = True
    
    try:
        while True:
            # 获取远程文件 MD5
            current_md5 = get_remote_file_md5(config)
            
            if current_md5 is None:
                if first_run:
                    print(f"  ⚠ 无法连接到服务器或文件不存在")
                    print(f"  请确认:")
                    print(f"    1. 服务器 {server['host']} 是否可达")
                    print(f"    2. SSH 连接是否正常")
                    print(f"    3. 远程文件 {remote_path} 是否存在")
                    print(f"    4. 服务器上已执行过 ./gradlew build")
                    sys.exit(1)
                else:
                    time.sleep(config["watch_interval"])
                    continue
            
            if current_md5 != last_md5:
                if last_md5 is not None:
                    print(f"  [{datetime.now().strftime('%H:%M:%S')}] 检测到更新 (MD5: {current_md5[:12]}...)")
                else:
                    print(f"  [{datetime.now().strftime('%H:%M:%S')}] 首次检测到文件 (MD5: {current_md5[:12]}...)")
                
                # 下载到临时文件
                with tempfile.NamedTemporaryFile(delete=False, suffix=".jar") as tmp:
                    tmp_path = tmp.name
                
                if download_file(config, tmp_path):
                    file_size = os.path.getsize(tmp_path)
                    print(f"  ✓ 下载完成 ({file_size / 1024:.1f} KB)")
                    
                    # 拷贝到所有目标目录
                    copy_to_destinations(tmp_path, config)
                    print()
                
                # 清理临时文件
                try:
                    os.unlink(tmp_path)
                except:
                    pass
                
                last_md5 = current_md5
                
                if args.once:
                    print("  单次同步完成")
                    break
            
            if first_run:
                first_run = False
                if not args.once:
                    print(f"  开始监听文件变化...")
                    print()
            
            if not args.once:
                time.sleep(config["watch_interval"])
                
    except KeyboardInterrupt:
        print()
        print("  监听已停止")
        sys.exit(0)


if __name__ == "__main__":
    main()

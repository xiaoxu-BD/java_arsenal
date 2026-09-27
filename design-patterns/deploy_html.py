# -*- coding: utf-8 -*-
"""
把 design-patterns/html 部署到云服务器 nginx 挂载目录 /dev-files/nginx/ 下的 design-patterns/ 子目录。

用法（三种方式提供连接信息，优先级：参数 > 环境变量 > 交互输入）：
    python deploy_html.py --host 1.2.3.4 --user root --password xxxx
    DP_HOST=1.2.3.4 DP_USER=root DP_PASSWORD=xxxx python deploy_html.py
    python deploy_html.py                # 交互式提示输入

部署动作：
    1. SFTP 上传 html/ 全部文件 -> /dev-files/nginx/design-patterns/
    2. 服务端自检：ls 统计文件数 + curl 本机访问入口 URL
    3. 若 nginx 尚未配置该目录，按脚本尾部注释的 location 片段补充后 reload（见 --reload 选项）

默认只传文件不动 nginx 配置；确认需要 reload 时加 --reload。
"""
import argparse
import os
import stat
import sys

import paramiko

DEFAULT_REMOTE_ROOT = "/dev-files/nginx/html/design-patterns"   # 已挂载为容器 /usr/share/nginx/html，无需改 nginx 配置
REMOTE_ROOT = DEFAULT_REMOTE_ROOT
LOCAL_HTML = os.path.join(os.path.dirname(os.path.abspath(__file__)), "html")


def connect(host, port, user, password=None, key_file=None):
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    kwargs = dict(hostname=host, port=port, username=user, timeout=10)
    if key_file:
        kwargs.update(key_filename=key_file)
    if password:
        kwargs.update(password=password, allow_agent=False, look_for_keys=False)
    ssh.connect(**kwargs)
    return ssh


def sftp_put_dir(sftp, local_dir, remote_dir, uploaded):
    for name in sorted(os.listdir(local_dir)):
        local_path = os.path.join(local_dir, name)
        remote_path = remote_dir.rstrip("/") + "/" + name
        if os.path.isdir(local_path):
            try:
                sftp.stat(remote_path)
            except FileNotFoundError:
                sftp.mkdir(remote_path)
            sftp_put_dir(sftp, local_path, remote_path, uploaded)
        else:
            sftp.put(local_path, remote_path)
            uploaded.append(remote_path)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--host", default=os.environ.get("DP_HOST", ""))
    ap.add_argument("--port", type=int, default=int(os.environ.get("DP_PORT", "22")))
    ap.add_argument("--user", default=os.environ.get("DP_USER", "root"))
    ap.add_argument("--password", default=os.environ.get("DP_PASSWORD", ""))
    ap.add_argument("--key", default=os.environ.get("DP_KEY", ""), help="私钥路径（与密码二选一）")
    ap.add_argument("--remote-root", default=DEFAULT_REMOTE_ROOT, help=f"服务器目标目录（默认 {DEFAULT_REMOTE_ROOT}）")
    ap.add_argument("--reload", action="store_true", help="上传后尝试 reload nginx")
    args = ap.parse_args()
    global REMOTE_ROOT
    REMOTE_ROOT = args.remote_root

    if not args.host:
        args.host = input("服务器地址: ").strip()
    if not args.key and not args.password:
        args.password = getpass.getpass(f"{args.user}@{args.host} 密码: ")

    if not os.path.isdir(LOCAL_HTML):
        sys.exit(f"本地目录不存在: {LOCAL_HTML}")

    ssh = connect(args.host, args.port, args.user, args.password or None, args.key or None)
    print(f"[1/3] 已连接 {args.user}@{args.host}")

    # 确保远端目录存在（不接管已存在内容，仅补齐路径）
    ssh.exec_command(f"mkdir -p {REMOTE_ROOT}")
    sftp = ssh.open_sftp()
    uploaded = []
    sftp_put_dir(sftp, LOCAL_HTML, REMOTE_ROOT, uploaded)
    sftp.close()
    print(f"[2/3] 上传完成: {len(uploaded)} 个文件 -> {REMOTE_ROOT}")

    # 服务端自检
    _, out, _ = ssh.exec_command(
        f"find {REMOTE_ROOT} -type f | wc -l; find {REMOTE_ROOT} -name '*.html' | wc -l")
    files, htmls = out.read().decode().split()
    print(f"[3/3] 服务端自检: 共 {files} 个文件，其中 html {htmls} 个")

    # 看看 nginx 是宿主机直装还是 docker，顺便找配置线索
    _, out, _ = ssh.exec_command(
        "(command -v nginx >/dev/null && echo host-nginx) || "
        "(docker ps --format '{{.Names}}' 2>/dev/null | grep -i nginx) || echo no-nginx-found")
    how = out.read().decode().strip()
    print("nginx 运行方式:", how)

    if args.reload:
        cmd = "nginx -s reload" if how == "host-nginx" else f"docker exec {how} nginx -s reload"
        _, out, err = ssh.exec_command(cmd)
        e = err.read().decode().strip()
        print("reload:", e if e else "ok")

    ssh.close()
    print(f"\n访问入口（以实际 server 配置为准）: http://{args.host}/design-patterns/index.html")
    print("若 404：nginx 还没把 /dev-files/nginx 暴露成站点，参考 deploy_html.py 文件尾部的配置片段。")


"""
nginx 配置片段（若 /dev-files/nginx 尚未被任何 server 覆盖）：

# /etc/nginx/conf.d/design-patterns.conf   （宿主机直装）
# 或 docker 部署时挂载进容器的 conf.d 目录
server {
    listen 80;
    server_name _;                       # 有域名就填域名

    location /design-patterns/ {
        alias /dev-files/nginx/design-patterns/;
        index index.html;
        # html 是静态文件，无需额外配置；charset 保证中文不乱码
        charset utf-8;
    }
}

配置后执行: nginx -t && nginx -s reload
（docker 方式: docker exec <容器名> nginx -t && docker exec <容器名> nginx -s reload）
"""

if __name__ == "__main__":
    import getpass  # noqa: E402  (延迟导入，argparse 场景用不到)
    main()

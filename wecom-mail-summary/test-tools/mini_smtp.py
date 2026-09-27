# -*- coding: utf-8 -*-
"""
迷你 SMTP 收件服务器(联调用):接受任意 MAIL/RCPT/DATA,把收到的邮件原文追加到 received_mails.log。
用法: python mini_smtp.py [port]   # 默认 3025
配合应用启动参数: --spring.mail.host=localhost --spring.mail.port=3025
"""
import socket
import sys
import threading

HOST = '127.0.0.1'
PORT = int(sys.argv[1]) if len(sys.argv) > 1 else 3025
LOG = open('received_mails.log', 'ab')


def handle(conn):
    f = conn.makefile('rwb')

    def send(s):
        f.write(s.encode() + b'\r\n')
        f.flush()

    send('220 mini-smtp ready')
    in_data = False
    msg = []
    while True:
        line = f.readline()
        if not line:
            break
        line = line.rstrip(b'\r\n')
        if in_data:
            if line == b'.':
                in_data = False
                LOG.write(b'---- MAIL ----\n' + b'\n'.join(msg) + b'\n--------------\n')
                LOG.flush()
                send('250 OK message accepted')
            else:
                msg.append(line)
            continue
        cmd = line.upper()
        if cmd.startswith(b'EHLO') or cmd.startswith(b'HELO'):
            send('250-mini')
            send('250 OK')
        elif cmd.startswith(b'MAIL') or cmd.startswith(b'RCPT'):
            send('250 OK')
        elif cmd.startswith(b'DATA'):
            in_data = True
            msg = []
            send('354 End data with <CR><LF>.<CR><LF>')
        elif cmd.startswith(b'QUIT'):
            send('221 bye')
            break
        else:
            send('250 OK')
    conn.close()


def main():
    srv = socket.socket()
    srv.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    srv.bind((HOST, PORT))
    srv.listen(10)
    print('mini-smtp listening on %s:%d, mails -> received_mails.log' % (HOST, PORT))
    while True:
        conn, addr = srv.accept()
        threading.Thread(target=handle, args=(conn,), daemon=True).start()


if __name__ == '__main__':
    main()
